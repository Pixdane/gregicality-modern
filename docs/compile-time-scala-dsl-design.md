# Compile-Time Scala DSL Codegen Design

Status: design note. This document records the current architecture decisions
for Gregicality registration code generation. It is not yet an implementation
contract; update it when implementation proves a different shape is better.

Current Gradle ownership: `scala-project` provides built-in Scala setup and
generic resource ordering; `scalafmt` owns formatting tasks; and `codegen` owns
the symbolgen/codegen source sets, generated resources, and generation tasks.

The design goal is to let migration authors write typed Scala configuration
files that feel closer to TOML or JSON than handwritten GTCEu builder code,
while still getting IDE completion, type checking, navigation, generated source
visibility, and Gradle incremental builds.

Initial target: material registration.

Expected extension targets: recipe registration, machine definitions, tag prefix
registration, worldgen definitions, and other generated registration surfaces.

## Current Decision

Use Scala DSL files as typed config input, compile them in a dedicated source
set, load the resulting DSL values from a Gradle generator task, transform them
through pure ADTs and pure code AST rendering, then write ordinary `.scala`
registration sources under `build/generated/...`.

```text
src/gcyDsl/scala/*.scala
  -> compileGcyDsl
  -> generateGcyDslSources
  -> build/generated/sources/gcyDsl/scala/main/*.scala
  -> compileScala
```

The DSL input files are compiled for validation and completion, but they are not
part of the main mod runtime source set. The generated `.scala` files are added
to `sourceSets.main.scala` and compiled as normal mod classes.

## Directory Layout

Recommended initial single-project layout:

```text
src/main/scala/
  com/pixdane/gregicality/
    Gregicality.scala
    common/data/GCYMaterials.scala

src/codegen/scala/
  com/pixdane/gregicality/codegen/
    dsl/        # human-facing DSL syntax and keys
    core/       # ADTs, validators, planners, code AST, renderers
    gradle/     # generator entrypoint and Gradle-facing helpers

src/gcyDsl/scala/
  com/pixdane/gregicality/gcydsl/materials/chemistry/Polymers.scala
  com/pixdane/gregicality/gcydsl/materials/metallurgy/Alloys.scala

build/generated/sources/gcyDsl/scala/main/
  com/pixdane/gregicality/common/data/materials/GCYMaterialsChemistryPolymers.scala
  com/pixdane/gregicality/common/data/materials/GCYMaterialsGeneratedIndex.scala
```

Important package distinction:

- `com.pixdane.gregicality.gcydsl.*` is input configuration for the generator.
- `com.pixdane.gregicality.codegen.*` is the codegen system.
- `com.pixdane.gregicality.common.data.*` is runtime-facing registration code.

Generated files physically live under `build/generated/...`, but their logical
package should match the runtime-facing API when other code needs to reference
generated materials.

## Generated Naming

Use `GCYMaterials*` class names for generated material registries because
recipes, machines, tags, and hand-written code may need to refer to generated
material fields.

Examples:

```text
DSL package id: chemistry/polymers
Generated object: GCYMaterialsChemistryPolymers

DSL package id: metallurgy/alloys
Generated object: GCYMaterialsMetallurgyAlloys

Generated index object: GCYMaterialsGeneratedIndex
```

Generated material package:

```scala
package com.pixdane.gregicality.common.data.materials
```

Runtime entrypoint usage:

```scala
package com.pixdane.gregicality.common.data

import com.pixdane.gregicality.common.data.materials.GCYMaterialsGeneratedIndex

object GCYMaterials:
  private def registerMaterials(event: MaterialEvent): Unit =
    GCYMaterialsGeneratedIndex.registerAll()

  private def modifyMaterials(event: PostMaterialEvent): Unit =
    GCYMaterialsGeneratedIndex.patchAll()
```

Generated package objects may expose stable material fields:

```scala
import com.pixdane.gregicality.common.data.materials.GCYMaterialsChemistryPolymers.Polyimide
```

This is the reason generated material code should not use implementation-only
names such as `GeneratedChemistryPolymers`.

## DSL Style

The authored DSL is Scala, but should read like typed config rather than direct
GTCEu registration code.

Example:

```scala
package com.pixdane.gregicality.gcydsl.materials.chemistry

import com.pixdane.gregicality.codegen.dsl.materials.*

object Polymers extends MaterialPackage("chemistry/polymers"):

  material("polyimide"):
    field := Polyimide
    lifecycle := registerNew
    kind := ingot(level = 4)
    fluid := liquid()
    color := rgb"2d2d2d"
    iconSet := METALLIC
    flags := flags(STD_METAL, GENERATE_PLATE, GENERATE_ROD)
    components := components(
      Carbon -> 22,
      Hydrogen -> 10,
      Nitrogen -> 2,
      Oxygen -> 5
    )
```

This keeps completion for:

- DSL keys such as `kind`, `fluid`, `color`, and `components`;
- material kinds such as `ingot`, `dust`, `gem`, and `polymer`;
- icon sets, material flags, fluid kinds, voltage tiers, and material refs;
- package-specific material fields used by later recipe or machine code.

Avoid writing the DSL as a thin wrapper over `Material.Builder`, because that
would couple human-authored config to the target API and make other output
targets harder to support later.

## Pipeline Boundaries

The system has four conceptual stages:

```text
Scala DSL files
  -> Raw ADT
  -> Verified ADT
  -> Code AST / registration plan
  -> Rendered Scala source
  -> Gradle writes generated .scala files
```

Only the Gradle scan/write layer is impure. DSL-to-ADT validation and
ADT-to-code rendering should be ordinary pure Scala logic.

### Stage 1: DSL Files And Routing

Gradle scans `src/gcyDsl/scala` and routes files by directory and file name.

| DSL path | Domain | Registration owner |
| --- | --- | --- |
| `src/gcyDsl/scala/**/materials/**` | material | `GCYMaterials` |
| `src/gcyDsl/scala/**/recipes/**` | recipe | future `GCYRecipes` |
| `src/gcyDsl/scala/**/tag-prefixes/**` | tag prefix | future tag prefix owner |

The path and object name determine route metadata:

```text
src/gcyDsl/scala/.../materials/chemistry/Polymers.scala
  -> domain: materials
  -> package id: chemistry/polymers
  -> generated package: com.pixdane.gregicality.common.data.materials
  -> generated object: GCYMaterialsChemistryPolymers
```

### Stage 2: DSL To ADT

This stage is pure after Gradle has loaded a compiled DSL value.

Use a generic domain boundary:

```scala
trait RegisterDomain:
  type Raw
  type Verified
  type Plan

  def verify(raw: Raw): ValidatedNec[DslError, Verified]
  def plan(verified: Verified): ValidatedNec[DslError, Plan]
  def render(plan: Plan): ValidatedNec[DslError, RenderedScalaSource]
```

Materials are one domain instance:

```scala
object MaterialDomain extends RegisterDomain:
  type Raw = RawMaterialPackageSpec
  type Verified = MaterialPackageSpec
  type Plan = MaterialRegistrationPlan
```

Recipes can later use the same framework with different `Raw`, `Verified`, and
`Plan` types.

### Stage 3: ADT To Code AST

Do not jump directly from `MaterialSpec` to string concatenation. Use an
intermediate code AST or registration plan.

```text
Verified ADT
  -> MaterialRegistrationPlan
  -> ScalaObjectDef / BuilderStep AST
  -> RenderedScalaSource
```

The final render still produces text because Gradle and scalac consume ordinary
source files. The AST exists to keep generation structured, testable, and
deterministic.

### Stage 4: Gradle Scan And Write

Gradle owns all I/O:

- scanning DSL source directories;
- compiling DSL source sets;
- loading DSL package objects;
- invoking the pure pipeline;
- writing generated `.scala` files;
- deleting stale generated files;
- wiring generated sources into `compileScala`;
- declaring inputs and outputs for up-to-date checks and build cache.

The generator should write files only when content changed.

## Raw And Verified ADTs

Use two ADT layers.

Raw ADTs are tolerant. They preserve enough bad input to report multiple errors:
missing fields, repeated keys, invalid combinations, and invalid literals.

Verified ADTs are strict. Codegen should accept only verified ADTs.

### Common Types

Avoid bare `String` and unconstrained `Int` for important values.

```scala
opaque type RegistryPath = String
opaque type PackageId = String
opaque type ScalaIdent = String
opaque type HexRgb = Int
opaque type PositiveInt = Int
opaque type NonNegativeInt = Int
opaque type Kelvin = Int
opaque type DurationTicks = Int

final case class SourceTrace(
  domain: DomainId,
  packageId: PackageId,
  sourceFile: String,
  sourcePos: Option[SourcePos]
)

enum DomainId:
  case Materials
  case Recipes
  case TagPrefixes
```

Constructors for opaque types should return validation results:

```scala
type Result[A] = ValidatedNec[DslError, A]

object RegistryPath:
  def from(value: String): Result[RegistryPath] = ???

object ScalaIdent:
  def from(value: String): Result[ScalaIdent] = ???
```

### Raw Material ADT

Raw material specs allow optional fields and repeated singleton keys so the
validator can emit useful diagnostics.

```scala
final case class RawMaterialPackageSpec(
  id: PackageId,
  route: RouteInfo,
  materials: List[RawMaterialSpec],
  source: SourceTrace
)

final case class RawMaterialSpec(
  id: RegistryPath,
  field: Option[ScalaIdent],
  lifecycle: Option[RawMaterialLifecycle],
  kind: List[RawMaterialKind],
  displayName: Option[String],
  fluids: List[RawFluidSpec],
  color: Option[HexRgb],
  secondaryColor: Option[HexRgb],
  iconSet: Option[MaterialIconRef],
  flags: List[MaterialFlagRef],
  components: List[RawComponentSpec],
  blast: Option[RawBlastSpec],
  properties: List[RawMaterialProperty],
  source: SourceTrace
)
```

`kind` is a list in the raw layer so duplicate `kind := ...` assignments can be
reported instead of silently overwritten.

### Verified Material ADT

Verified material declarations should be a top-level sum type. This prevents
meaningless combinations such as a deferred material with full registration
properties.

```scala
enum MaterialDeclaration:
  case NewMaterial(spec: NewMaterialSpec)
  case ExistingPatch(spec: MaterialPatchSpec)
  case Deferred(spec: DeferredMaterialSpec)

final case class MaterialPackageSpec(
  id: PackageId,
  route: RouteInfo,
  declarations: NonEmptyList[MaterialDeclaration],
  source: SourceTrace
)

final case class NewMaterialSpec(
  id: RegistryPath,
  field: ScalaIdent,
  kind: MaterialKind,
  visuals: VisualSpec,
  composition: CompositionSpec,
  processing: ProcessingSpec,
  extraProperties: List[MaterialProperty],
  source: SourceTrace
)

final case class MaterialPatchSpec(
  target: MaterialRef,
  operations: NonEmptyList[PatchOperation],
  source: SourceTrace
)

final case class DeferredMaterialSpec(
  id: RegistryPath,
  field: Option[ScalaIdent],
  reason: String,
  source: SourceTrace
)
```

Product types then model grouped fields:

```scala
final case class VisualSpec(
  displayName: Option[String],
  color: Option[HexRgb],
  secondaryColor: Option[HexRgb],
  iconSet: Option[MaterialIconRef]
)

final case class CompositionSpec(
  components: List[ComponentSpec],
  formula: Option[String]
)

final case class ProcessingSpec(
  fluids: List[FluidSpec],
  flags: List[MaterialFlagRef],
  blast: Option[BlastSpec]
)
```

Material kind is a sum type:

```scala
enum MaterialKind:
  case Dust(harvestLevel: Option[NonNegativeInt], burnTime: Option[NonNegativeInt])
  case Ingot(harvestLevel: Option[NonNegativeInt], burnTime: Option[NonNegativeInt])
  case Gem(harvestLevel: Option[NonNegativeInt], burnTime: Option[NonNegativeInt])
  case Polymer(harvestLevel: Option[NonNegativeInt], burnTime: Option[NonNegativeInt])
  case Wood(harvestLevel: Option[NonNegativeInt], burnTime: Option[NonNegativeInt])
  case Marker
```

`case class` is used for product data. `enum` is used for closed alternatives.
Complex ADTs should freely combine both.

### Symbolic References

The ADT should not contain runtime GTCEu `Material`, `Element`,
`MaterialFlag`, `MaterialIconSet`, `FluidAttribute`, or similar objects. Use
typed symbolic references backed by pure data. The generated ref values may
store the final Scala path because they are generated from the same symbol scan
that discovers the path. They still must not store runtime GTCEu objects.

```scala
final case class ScalaSymbolPath(parts: Vector[ScalaIdent])
final case class ResourceId(namespace: String, path: RegistryPath)

final case class MaterialRef(
  id: ResourceId,
  path: ScalaSymbolPath
)

final case class ElementRef(path: ScalaSymbolPath)
final case class MaterialFlagRef(path: ScalaSymbolPath)
final case class MaterialFlagPresetRef(path: ScalaSymbolPath)
final case class MaterialIconRef(path: ScalaSymbolPath)
final case class FluidAttributeRef(path: ScalaSymbolPath)
final case class TagPrefixRef(path: ScalaSymbolPath)

final case class ComponentSpec(
  material: MaterialRef,
  amount: PositiveInt
)
```

Only material references need a registry id in the first implementation. They
are used for component references, duplicate/collision checks, future material
patches, and generated material field planning. Other refs only need a path
because the DSL does not yet expose string literal input such as
`icon"metallic"` or `flag"generate_plate"`.

For materials, the generated ref should include both identity and render path:

```scala
GTMaterialsRef.Carbon
// MaterialRef(
//   id = ResourceId("gtceu", "carbon"),
//   path = ScalaSymbolPath(..., "GTMaterials", "Carbon")
// )
```

For icon sets, flags, fluid attributes, elements, and tag prefixes, the first
implementation can be path-only:

```scala
MaterialIconSetsRef.METALLIC
// MaterialIconRef(path = ScalaSymbolPath(..., "MaterialIconSet", "METALLIC"))
```

For IDE completion, generate Scala ref objects whose values still produce these
same typed refs:

```scala
import com.pixdane.gregicality.codegen.dsl.refs.gtceu.GTMaterialsRef.*
import com.pixdane.gregicality.codegen.dsl.refs.gtceu.MaterialIconSetsRef.*

components := components(Carbon -> 22, Hydrogen -> 10)
iconSet := METALLIC
```

For convenience, generated Scala 3 `export` statements expose the named GTCEu
refs through `GTRefs.*`. Domain-specific imports remain useful when names from
different catalogs collide. Catalogs do not expose an `all` collection: the DSL
needs member completion, and only material ids need a lookup index.

The generated `Carbon` above is a `MaterialRef`, not a runtime GTCEu
`Material`. This gives normal Scala completion without importing
`GTMaterials.*` into authored DSL files or evaluating GTCEu static fields while
the DSL is loaded.

Because refs already contain paths, the first codegen slice does not need a
separate resolver for GTCEu refs. Validation can still sanity-check refs against
the generated symbol scan if useful, but rendering may directly consume the path
stored in the ref.

Local material refs are future work. Once authored DSL declarations exist, a
light declaration scan can derive:

```text
DSL route + material id/field
  -> ResourceId("gregicality", "polyimide")
  -> GCYMaterialsChemistryPolymers.Polyimide
```

and generate local `MaterialRef` values with the same `id + path` shape. This
document intentionally avoids introducing that DSL declaration scan in the first
implementation slice.

### Fluids And Blast

```scala
enum FluidSpec:
  case DefaultFluid
  case Liquid(temperature: Option[Kelvin])
  case Gas(temperature: Option[Kelvin])
  case Plasma(temperature: Option[Kelvin])
  case Molten(temperature: Option[Kelvin])

final case class BlastSpec(
  temperature: Kelvin,
  gasTier: Option[GasTierRef],
  eut: Option[VoltageExpr],
  duration: Option[DurationTicks],
  vacuumEut: Option[VoltageExpr],
  vacuumDuration: Option[DurationTicks]
)

final case class GasTierRef(name: ScalaIdent)

enum VoltageExpr:
  case Tier(name: ScalaIdent)
  case VA(name: ScalaIdent)
  case Literal(value: Long)
```

## Validation Rules

Validation should be layered:

```text
RawMaterialSpec
  -> lexical validation
  -> package validation
  -> semantic validation
  -> MaterialDeclaration
```

Lexical validation:

- registry paths are valid;
- Scala fields are valid identifiers;
- RGB colors are within `0x000000..0xffffff`;
- amounts, temperatures, durations, and levels satisfy their numeric wrappers.

Package validation:

- material ids are unique within a package;
- material fields are unique within a package;
- route domain matches the DSL directory;
- generated object name is a valid Scala identifier;
- new material registration does not collide with known canonical GTCEu material
  ids unless an explicit policy allows it.

Semantic validation:

- a new material has exactly one `MaterialKind`;
- fluid storage keys do not duplicate;
- component amounts are positive;
- `Deferred` does not enter normal register plans;
- `ExistingPatch` never emits `new Material.Builder`;
- `Marker` does not carry ordinary ingot/gem/fluid generation unless a later
  explicit rule allows it;
- blast data that looks suspicious for non-metal-like materials should begin as
  a warning, not necessarily a hard error.

Use Cats `ValidatedNec[DslError, A]` so a package can report many errors in one
run.

## Code AST And Rendering

Suggested code AST shape:

```scala
final case class ScalaObjectDef(
  packageName: String,
  objectName: ScalaIdent,
  imports: List[ScalaImport],
  members: List[ScalaMember]
)

sealed trait ScalaMember
final case class ScalaVar(name: ScalaIdent, tpe: ScalaType, rhs: ScalaExpr) extends ScalaMember
final case class ScalaDef(name: ScalaIdent, body: List[ScalaStmt]) extends ScalaMember

sealed trait MaterialBuilderStep
object MaterialBuilderStep:
  final case class Dust(level: Option[Int], burnTime: Option[Int]) extends MaterialBuilderStep
  final case class Ingot(level: Option[Int], burnTime: Option[Int]) extends MaterialBuilderStep
  final case class Gem(level: Option[Int], burnTime: Option[Int]) extends MaterialBuilderStep
  final case class Fluid(kind: FluidSpec) extends MaterialBuilderStep
  final case class Color(rgb: HexRgb) extends MaterialBuilderStep
  final case class IconSet(iconSet: MaterialIconRef) extends MaterialBuilderStep
  final case class AppendFlags(flags: List[MaterialFlagRef]) extends MaterialBuilderStep
  final case class Components(components: List[ComponentSpec]) extends MaterialBuilderStep
  final case class Blast(blast: BlastSpec) extends MaterialBuilderStep
  case object BuildAndRegister extends MaterialBuilderStep
```

Example generated material object:

```scala
package com.pixdane.gregicality.common.data.materials

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*
import com.gregtechceu.gtceu.common.data.GTMaterials.*
import com.pixdane.gregicality.Gregicality

object GCYMaterialsChemistryPolymers:
  var Polyimide: Material = _

  def register(): Unit =
    Polyimide = new Material.Builder(Gregicality.id("polyimide"))
      .ingot(4)
      .liquid()
      .color(0x2d2d2d)
      .iconSet(METALLIC)
      .appendFlags(STD_METAL, GENERATE_PLATE, GENERATE_ROD)
      .components(Carbon, 22, Hydrogen, 10, Nitrogen, 2, Oxygen, 5)
      .buildAndRegister()

  def patch(): Unit =
    ()
```

Generated material code follows the current GTCEu lifecycle:

- `MaterialRegistryEvent`: create the Gregicality material registry.
- `MaterialEvent`: call generated `register()` methods.
- `PostMaterialEvent`: call generated `patch()` methods.

## Gradle Integration

Use dedicated source sets. Do not put authored DSL files under `src/main/scala`,
because main compilation should consume generated registration code, not the
raw DSL input.

### GTCEu Symbol Table And Generated Refs

Before compiling authored DSL files, generate stable Scala ref objects from
GTCEu artifacts. This should be an offline artifact scan, not a runtime import
of GTCEu classes. A separate symbol table file is optional and only needed for
diagnostics or sanity checks.

The important rule is:

```text
read GTCEu sources jar
  -> discover public static fields and material ids
  -> normalize them as ScannedRef values
  -> render typed Scala ref objects for DSL completion
```

Do not use `Class.forName`, `GTMaterials.*`, `MaterialIconSet.ICON_SETS`, or
other runtime/static-object access in the generator. Even when an ordinary
`import GTMaterials.*` would give IDE completion, it makes the DSL compile and
execution phases depend on GTCEu runtime objects. The generated ref objects
should provide the same completion surface while returning pure refs.

Use one Gradle task and one `symbolgen` entrypoint:

- source set: `symbolgen`;
- source path: `src/symbolgen/scala`;
- implementation packages: `cli`, `gtceu`, `io`, `model`, and `render`;
- entrypoint: `com.pixdane.gregicality.symbolgen.cli.GenerateGtRefs`;
- Gradle task: `generateGtRefs`;
- generated source directory: `build/generated/sources/gcyDslRefs/scala/main`;
- generated refs package: `com.pixdane.gregicality.codegen.dsl.refs.gtceu`;
- stable ref value package: `com.pixdane.gregicality.codegen.dsl.model`.

The generated package contains completion surfaces and one aggregate export:

```scala
object GTMaterialsRef
object GTElementsRef
object MaterialIconSetsRef
object FluidAttributesRef
object MaterialFlagsRef

object GTRefs:
  export GTMaterialsRef.*
  export GTElementsRef.*
  export MaterialIconSetsRef.*
  export FluidAttributesRef.*
  export MaterialFlagsRef.*
```

The stable value types are hand-written codegen models, not files emitted by
the scanner. This keeps downstream DSL and codegen source compatibility
independent of generator implementation changes:

```scala
final case class ScalaSymbolPath(parts: Vector[String])
final case class ResourceId(namespace: String, path: String)

final case class MaterialRef(id: ResourceId, path: ScalaSymbolPath)
final case class ElementRef(path: ScalaSymbolPath)
final case class MaterialIconRef(path: ScalaSymbolPath)
final case class MaterialFlagRef(path: ScalaSymbolPath)
final case class FluidAttributeRef(path: ScalaSymbolPath)
final case class TagPrefixRef(path: ScalaSymbolPath)
```

The internal scan model encodes whether an id exists in the type hierarchy,
instead of representing invalid combinations with `Option`:

```scala
sealed trait ScannedRef:
  def name: String
  def path: ScalaSymbolPath

sealed trait ScannedMaterialRef extends ScannedRef:
  def id: ResourceId

final case class ScannedRegisteredMaterialRef(
  name: String, id: ResourceId, path: ScalaSymbolPath
) extends ScannedMaterialRef

final case class ScannedMaterialAliasRef(
  name: String, id: ResourceId, path: ScalaSymbolPath
) extends ScannedMaterialRef

final case class ScannedPathRef(
  name: String, path: ScalaSymbolPath
) extends ScannedRef
```

Registered materials enter the id index. Aliases remain generated members for
completion, but do not replace the canonical result returned by id lookup.

The source reader loads the GTCEu sources jar into a `SourceArchive`. Scanners
parse only the configured Java sources with JavaParser. Jobs are typed by the
data they can produce:

```scala
enum RefJob:
  case Materials(
    id: String,
    scan: SourceArchive => Vector[ScannedMaterialRef],
    objectTarget: RefObjectTarget
  )
  case Paths(
    id: String,
    scan: SourceArchive => Vector[ScannedPathRef],
    objectTarget: RefObjectTarget
  )
```

The first jobs are:

| Job id | Output object | Value type | Job kind | Source shape |
| --- | --- | --- | --- | --- |
| `gt-materials` | `GTMaterialsRef` | `MaterialRef` | `Materials` | `GTMaterials.java` declarations plus material assignments and aliases |
| `gt-elements` | `GTElementsRef` | `ElementRef` | `Paths` | static `Element` fields in `GTElements.java` |
| `material-icon-sets` | `MaterialIconSetsRef` | `MaterialIconRef` | `Paths` | static `MaterialIconSet` fields in `MaterialIconSet.java` |
| `fluid-attributes` | `FluidAttributesRef` | `FluidAttributeRef` | `Paths` | static `FluidAttribute` fields in `FluidAttributes.java` |
| `material-flags` | `MaterialFlagsRef` | `MaterialFlagRef` | `Paths` | static `MaterialFlag` fields in `MaterialFlags.java` |

For material refs, the scanner joins declarations to assignments rooted at the
exact `new Material.Builder(GTCEu.id("..."))` shape. It also recognizes direct
`GTMaterials` aliases. Missing assignments, duplicate assignments, duplicate
registered ids, unsupported builder shapes, and assignments through another
owner are diagnostics rather than silently dropped symbols. This preserves the
true registry id without lossy field-name conversion.

Fields annotated with `@Deprecated` are intentionally excluded from every
generated ref surface, including static path refs, registered materials, and
material aliases. Material declarations are filtered before assignments are
validated, so a deprecated declaration produces neither a generated member nor
a missing-assignment diagnostic.

Rendering matches the `RefJob` variant and therefore cannot accidentally render
a path-only symbol as a material. Generated members remain parameterless `def`s
so large catalogs do not allocate every ref in the Scala object initializer:

```scala
def Carbon: MaterialRef =
  MaterialRef(
    ResourceId("gtceu", "carbon"),
    ScalaSymbolPath(Vector(...))
  )
```

Only `GTMaterialsRef` has an id lookup API:

```scala
def resolve(id: ResourceId): Option[MaterialRef] =
  byIdIndex.get(id)

private lazy val byIdIndex: Map[ResourceId, MaterialRef] =
  byIdEntries.iterator.map(ref => ref.id -> ref).toMap

private def byIdEntries: Vector[MaterialRef] =
  byIdEntries0 ++ byIdEntries1

private def byIdEntries0: Vector[MaterialRef] =
  Vector(Carbon, ...)
```

The `lazy val` delays construction until code actually performs an id lookup.
Ordinary DSL completion and path-based codegen do not pay for the index. The
entries list names each registered ref exactly once and is split into chunks to
avoid oversized JVM methods; `byIdIndex` derives the `ResourceId` keys from each
ref at first lookup, so the generated text does not repeat every name twice.
There is no public `all`: it adds eager bulk allocation and an API surface that
neither ref completion nor path rendering requires.

Flag presets such as `STD_METAL`, `EXT_METAL`, and `EXT2_METAL` live on
`GTMaterials` and are collections of flags, not single `MaterialFlag` values.
They are out of scope for the first generated-ref slice. Add them later as a
separate target and render kind if the DSL needs preset completion.

Only two places perform I/O:

- `SourceArchiveReader.read(sourcesJar)` reads the jar into `SourceArchive`.
- `GeneratedSourceWriter.sync(outDir, files)` transactionally replaces the
  generated refs directory through staging and rollback-safe backup paths.

Everything between those two boundaries should be ordinary data transformation.

Recommended source-set graph:

```text
symbolgen
  -> compileSymbolgen
  -> generateGtRefs
  -> generated refs source dir
  -> compileCodegen
  -> compileGcyDsl
  -> generateGcyDslSources
  -> compileScala
```

`symbolgen` contains only the artifact scanner and ref renderer. It does not
depend on main, generated refs, or the `codegen` source set. Its classpath names
Scala 3 and JavaParser explicitly. It renders source text that imports the
`codegen.dsl.model` value types. `codegen` and `gcyDsl` may depend on the
generated ref source directory.

Generated refs should be available to both:

- authored DSL files, for completion and type checking;
- codegen code, for the same pure `MaterialRef`, `ElementRef`,
  `MaterialIconRef`, `MaterialFlagRef`, and similar types.

When local DSL files exist, add a later declaration scan for `Local` symbols.
That scan can read easy-to-find DSL declarations and generate local
`MaterialRef` values with `ResourceId("gregicality", id)` plus the derived
`GCYMaterials*` path. This document intentionally scopes the first generated-ref
slice to GTCEu artifacts only and does not require scanning authored DSL before
`compileGcyDsl`.

The convention plugin in `build-logic/convention/src/main/kotlin/codegen.gradle.kts`
creates a non-transitive `gtceuSources` configuration and requests Gradle's
documentation/sources variant of `deps.gtceu`. The task receives that resolved
artifact through a path-insensitive `ConfigurableFileCollection`; it does not
walk Gradle's cache or construct a classifier path manually. `codegen` compiles
the hand-written models together with the generated catalog sources and depends
on `generateGtRefs`.

If the generator reads id maps, schema files, package tables, or old source
extracts, declare them as Gradle inputs. Otherwise Gradle may reuse stale
generated files.

Generator requirements:

- stable output path per generated ref object;
- deterministic sort order;
- the generated refs directory is owned by `generateGtRefs`;
- stale output deletion when a ref object disappears;
- no timestamp- or host-path-dependent output;
- no runtime GTCEu access.

## Macro Usage

Macros are useful, but they should not own file scanning or top-level source
generation.

Recommended macro use:

- literal validation: `materialId"polyimide"`, `packageId"chemistry/polymers"`,
  `rgb"2d2d2d"`;
- derived schema helpers for DSL keys and diagnostics;
- optional compile-time checks for inline DSL blocks;
- ergonomic typed config syntax.

Avoid macro use for:

- scanning directories;
- reading external DSL files;
- writing generated sources;
- creating stable top-level objects that other files import;
- replacing Gradle input/output tracking.

Reason: Gradle and Zinc can track declared task inputs and generated source
outputs. A macro that secretly reads files makes incremental compilation and
build cache correctness difficult to reason about.

## Dependent-Type Techniques

Scala 3 can help encode the domain boundary without making the DSL unpleasant.

Recommended:

- path-dependent domain members via `RegisterDomain`;
- opaque validated identifiers for paths, fields, colors, and constrained
  numbers;
- singleton literal helpers through macros for static values.

Avoid heavy type-state builders in the first version. They can prove that
required fields were supplied, but they make the config DSL less TOML-like and
often produce worse error messages than a good `ValidatedNec` validator.

## Tests

Application unit tests belong in `src/test/scala`. Symbol-generator tests use
the isolated `src/symbolgenTest/scala` source set and the `testSymbolgen` task.

Test the pure parts there:

- opaque type constructors;
- Raw-to-Verified validators;
- duplicate key and duplicate id reporting;
- planner output;
- code AST rendering snapshots.

For symbol generation, test scanner completeness and rejected AST shapes,
canonical material aliases, lookup-index rendering, aggregate exports, and
transactional replacement of generated output. The normal build must also run
`generateGtRefs` against the resolved GTCEu sources artifact and compile the
result with the hand-written codegen models.

Do not put real DSL input packages in `src/test/scala`. If compiled test DSL
fixtures are needed later, add a separate `src/testGcyDsl/scala` source set or
use small fixtures in `src/test/resources`.

## First Implementation Slice

1. Add `symbolgen` source set with a GTCEu artifact scanner and generated-ref
   renderer.
2. Add `generateGtRefs` for the first GTCEu generated-ref slice:
   - `GTMaterialsRef`;
   - `GTElementsRef`;
   - `MaterialIconSetsRef`;
   - `FluidAttributesRef`;
   - `MaterialFlagsRef`;
   - `GTRefs`.
3. Add `codegen` source set with material DSL API, ADTs, validation, planning,
   and rendering.
4. Add `gcyDsl` source set with one material package containing the current test
   material.
5. Implement Raw and Verified material ADTs for:
   - `RegisterNew`;
   - `MaterialKind.Ingot`, `Dust`, `Gem`, and `Polymer`;
   - `FluidSpec`;
   - `HexRgb`;
   - `MaterialIconRef`;
   - `MaterialFlagRef`;
   - `ComponentSpec`;
   - `BlastSpec`;
   - `MaterialRef`.
6. Add validators for ids, fields, required material kind, duplicate ids,
   duplicate fields, and material-kind conflicts.
7. Add validation or rendering helpers that consume generated refs directly:
   material refs provide `ResourceId + ScalaSymbolPath`, while other refs
   provide `ScalaSymbolPath`.
8. Add pure planner and code AST renderer for the subset already shown in
   `GCYMaterials.scala`: `ingot`, `fluid`, `langValue`, `color`, `iconSet`,
   `appendFlags`, `components`, and `blast`.
9. Add `generateGcyDslSources`.
10. Generate `GCYMaterialsChemistryPolymers` or an equivalent first package plus
   `GCYMaterialsGeneratedIndex`.
11. Wire generated sources into `compileScala`.
12. Run `compileScala` and confirm unchanged DSL files do not cause regenerated
   source churn.

Only after that slice is stable should the system start importing larger
material packages from the migration tables or old Gregicality source.

# Compile-Time Scala DSL Codegen Design

Status: architecture record plus deferred DSL sketches. Material content,
validation, planning, rendering, and generated-source integration are
implemented through Phase 4. Their binding design is
`material-adt-design.md`; the Raw ADT, DSL syntax, file routing, SourceTrace,
Deferred declarations, and local-material scan described below remain future
work and are not current APIs.

Current Gradle ownership: `scala-project` provides built-in Scala setup and
generic resource ordering; `scalafmt` owns formatting tasks; and `codegen` owns
the core, generatorSupport, symbolgen, codegen, and isolated test source sets
plus both generation tasks.

The design goal is to let migration authors write typed Scala configuration
files that feel closer to TOML or JSON than handwritten GTCEu builder code,
while still getting IDE completion, type checking, navigation, generated source
visibility, and Gradle incremental builds.

Initial target: material registration.

Expected extension targets: recipe registration, machine definitions, tag prefix
registration, worldgen definitions, and other generated registration surfaces.

## Current Implementation

The first implemented slice starts with directly constructed authored
`MaterialSet` values:

```text
GCYMaterialSets
  -> MaterialValidator
  -> MaterialPlanner
  -> MaterialRenderer
  -> GeneratedSourceWriter
  -> build/generated/sources/materials/scala/main/*.scala
  -> compileScala
```

The authored ADT records only author intent. Validation derives temporary
effective properties for checks but returns the same values; rendering emits no
inferred properties, flags, or builder defaults. Generated `.scala` files are a
`sourceSets.main.scala` root and compile as normal mod classes.

## Deferred DSL Direction

A later plan may prepend compiled Scala DSL input and Raw conversion:

```text
src/gcyDsl/scala/*.scala
  -> compileGcyDsl
  -> Raw ADT
  -> authored MaterialSet
  -> current implemented pipeline
```

That source set, syntax, routing, and conversion layer do not exist yet. The
sketches below preserve ideas for that later design but must not override the
implemented ADT in `material-adt-design.md`.

## Directory Layout

Current single-project layout:

```text
src/main/scala/
  com/pixdane/gregicality/
    Gregicality.scala
    common/data/GCYMaterials.scala

src/codegen/scala/
  com/pixdane/gregicality/codegen/
    Codegen.scala
    GCYMaterialSets.scala
    core/materials/  # authored ADT, validator, planner, renderer

src/generatorSupport/scala/
  com/pixdane/gregicality/generator/
    GeneratedScalaFile.scala
    GeneratedSourceWriter.scala

build/generated/sources/materials/scala/main/
  com/pixdane/gregicality/common/data/materials/GCYMaterialsChemistryPolymers.scala
  com/pixdane/gregicality/common/data/materials/GCYMaterialsGeneratedIndex.scala
```

Important package distinction:

- future `com.pixdane.gregicality.gcydsl.*` is input configuration for the
  generator.
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

Deferred design sketch: none of the DSL syntax or `MaterialPackage` APIs in this
section are implemented. Current authored values use the case classes and refs
documented by `material-adt-design.md`.

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

The implemented material pipeline is:

```text
MaterialSet
  -> MaterialValidator
  -> MaterialPlan
  -> Rendered Scala source
  -> GeneratedSourceWriter
```

Only final output synchronization is impure. A future DSL may prepend loading
and Raw conversion without changing this boundary.

### Deferred Stage 1: DSL Files And Routing

A future Gradle layer would scan `src/gcyDsl/scala` and route files by directory
and file name.

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

### Deferred Stage 2: DSL To ADT

This future stage would be pure after Gradle loads a compiled DSL value.

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

### Implemented Stage 3: ADT To Code Plan

The current types are `MaterialPlan`, `MaterialDeclarationPlan`,
`NewMaterialPlan`, `MaterialPatchPlan`, `BuilderCall`, and `ScalaExpr`. See
`material-adt-design.md` for their invariants. The generic AST below is a
discarded exploratory shape, not the current API.

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

### Implemented Stage 4: Gradle Generate And Write

Gradle owns task orchestration and input/output declarations. The source reader
and shared writer are the explicit filesystem boundaries:

- compiling codegen and generated GTCEu refs;
- invoking the pure material pipeline;
- synchronizing generated `.scala` files through `GeneratedSourceWriter`;
- deleting stale generated files;
- wiring generated sources into `compileScala`;
- declaring inputs and outputs for up-to-date checks and build cache.

The generator should write files only when content changed.

## Deferred Raw And DSL ADTs

This entire section is a future input-layer sketch. The implemented authored
material ADT is the keyed-property model in `material-adt-design.md`; it does
not use `MaterialKind`, `RawMaterialSpec`, `MaterialPackageSpec`, RouteInfo, or
SourceTrace.

A future DSL could use two input ADT layers.

Raw ADTs are tolerant. They preserve enough bad input to report multiple errors:
missing fields, repeated keys, invalid combinations, and invalid literals.

The converted authored ADT is strict. The implemented material pipeline accepts
only `MaterialSet`; it never consumes these Raw values directly.

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

### Historical Verified Material Sketch

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

For icon sets, flags, fluid attributes, and elements, the first
implementation can be path-only:

```scala
MaterialIconSetsRef.METALLIC
// MaterialIconRef(path = ScalaSymbolPath(..., "MaterialIconSet", "METALLIC"))
```

For IDE completion, generate Scala ref objects whose values still produce these
same typed refs:

```scala
import com.pixdane.gregicality.core.refs.gtceu.GTMaterialsRef.*
import com.pixdane.gregicality.core.refs.gtceu.MaterialIconSetsRef.*

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

## Deferred Raw-Layer Validation Sketch

Current validation rules and issue types are specified in
`material-adt-design.md`. The rules below apply only if a future tolerant Raw
layer is introduced.

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

## Historical Code AST Sketch

Current rendering uses `MaterialPlan` and `ScalaExpr`; see
`material-adt-design.md`. The generic AST below is retained only as design
history.

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

Current generated material object:

```scala
package com.pixdane.gregicality.common.data.materials

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.pixdane.gregicality.Gregicality

object GCYMaterialsChemistryPolymers:
  var Polyimide: Material = _

  def register(): Unit =
    Polyimide = new Material.Builder(Gregicality.id("polyimide"))
      .polymer(1)
      .liquid()
      .color(0xff7f50)
      .iconSet(MaterialIconSet.DULL)
      .flags(MaterialFlags.GENERATE_PLATE)
      .components(
        GTMaterials.Carbon, 22,
        GTMaterials.Hydrogen, 12,
        GTMaterials.Nitrogen, 2,
        GTMaterials.Oxygen, 6
      )
      .buildAndRegister()

  def patch(): Unit =
    ()
```

Generated material code follows the current GTCEu lifecycle:

- `MaterialRegistryEvent`: create the Gregicality material registry.
- `MaterialEvent`: call generated `register()` methods.
- `PostMaterialEvent`: call generated `patch()` methods.

## Gradle Integration

Use dedicated source sets. Current authored `MaterialSet` values live in
`codegen`, while main compilation consumes only generated runtime registration
code. A future DSL source set must also remain outside `src/main/scala`.

### GTCEu Symbol Table And Generated Refs

Before compiling material codegen (and any future authored DSL), generate stable
Scala ref objects from GTCEu artifacts. This is an offline source-artifact scan,
not runtime access to GTCEu classes.

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

Current generator support uses four production source sets and two generation
tasks:

- foundation source set: `core`;
- foundation source path: `src/core/scala`;
- stable ref package: `com.pixdane.gregicality.core.refs`;
- shared source-writing source set: `generatorSupport`;
- shared package: `com.pixdane.gregicality.generator`;
- generator source set: `symbolgen`;
- generator source path: `src/symbolgen/scala`;
- generator framework package: `com.pixdane.gregicality.symbolgen.framework`;
- symbolgen-local mechanisms: source archive `io` and ref `render`;
- backend registry package: `com.pixdane.gregicality.symbolgen.backends`;
- GTCEu backend package: `backends.gtceu`, with specs and scanners under
  `backends.gtceu.scan`;
- entrypoint: `com.pixdane.gregicality.symbolgen.GenerateRefs`;
- parsed CLI arguments are carried by `GenerateRefsArgs` (not a bare `Args`
  type);
- Gradle task: `generateGtRefs`;
- generated source directory: `build/generated/sources/gcyDslRefs/scala/main`;
- generated refs package: `com.pixdane.gregicality.core.refs.gtceu`;
- material generator source set: `codegen`;
- material task: `runCodegen`;
- material output:
  `build/generated/sources/materials/scala/main`.

Generator dependency direction is:

```text
core -------------> symbolgen
  \----------------> codegen
generatorSupport -> symbolgen
  \----------------> codegen
symbolgen -> generateGtRefs -> generated refs -> codegen
codegen -> runCodegen -> generated materials -> main
```

`framework` contains symbolgen contracts, not the concrete backend list.
Symbolgen `io` owns sources-jar loading; `generatorSupport` owns transactional
output replacement; symbolgen `render` owns ref source rendering; and
`backends` owns the concrete registry. Material validation and rendering remain
inside `codegen`.

`core` is the bottom of the code-generation dependency graph. It may contain
pure value types and shared ADTs, but it must not depend on Minecraft, GTCEu,
JavaParser, Cats, `symbolgen`, or `codegen`. Scanner, validation, rendering, and
I/O behavior remain in their owning source sets. This invariant lets
`symbolgen`, generated refs, `codegen`, authored DSL source sets, and main code
depend on the same vocabulary without creating a cycle.

The generated package contains completion surfaces and one aggregate export:

```scala
object GTMaterialsRef
object GTElementsRef
object MaterialIconSetsRef
object FluidAttributesRef
object FluidStorageKeysRef
object MaterialFlagsRef
object MaterialFlagPresetsRef

object GTRefs:
  export FluidAttributesRef.*
  export FluidStorageKeysRef.*
  export GTElementsRef.*
  export GTMaterialsRef.*
  export MaterialFlagPresetsRef.*
  export MaterialFlagsRef.*
  export MaterialIconSetsRef.*
```

The stable value types are hand-written `core` source, not scanner output:

```scala
final case class ScalaSymbolPath(parts: Vector[String])
final case class ResourceId(namespace: String, path: String)

final case class MaterialRef(id: ResourceId, path: ScalaSymbolPath)
final case class ElementRef(path: ScalaSymbolPath)
final case class MaterialIconRef(path: ScalaSymbolPath)
final case class MaterialFlagRef(path: ScalaSymbolPath)
final case class MaterialFlagPresetRef(path: ScalaSymbolPath)
final case class FluidAttributeRef(path: ScalaSymbolPath)
final case class FluidStorageKeyRef(path: ScalaSymbolPath)
```

The internal scan model keeps the common symbol fields in a small trait while
representing the material id only on material refs:

```scala
sealed trait ScannedRef:
  def name: String
  def path: ScalaSymbolPath

final case class ScannedMaterialRef(
  name: String,
  id: ResourceId,
  path: ScalaSymbolPath
) extends ScannedRef

final case class ScannedPathRef(
  name: String,
  path: ScalaSymbolPath
) extends ScannedRef
```

The source reader loads the GTCEu sources jar into a `SourceArchive`. Scanners
parse only configured Java sources with JavaParser. Jobs compose typed,
replaceable stages rather than selecting implementations through an enum and
two pattern-matching dispatch objects.

Diagnostics are structured values, not strings. A common `Diagnostic` trait
describes what every backend diagnostic must expose; each backend's diagnostic
enum implements it. `SymbolGenerator` is parameterized by the backend's
concrete diagnostic type so structured diagnostics flow through scan and
preprocess and are only rendered to text at the CLI boundary:

```scala
trait Diagnostic:
  def render: String

final case class SymbolGenerator[D <: Diagnostic](
  kind: String,
  generate: SourceArchive => IorNec[D, Vector[GeneratedScalaFile]]
)
```

The top-level `GenerateRefs` entrypoint asks the backend registry
(`SymbolGenerators`) for a generator by `kind`. The registry belongs to
`backends` because it knows the concrete backend list; `framework` remains free
of backend dependencies. The entrypoint receives an `IorNec[D, _]` result from
the selected generator, renders the surviving diagnostics last, and never
stringifies a diagnostic before that final step.

```scala
final case class SymbolJob[E, A, B](
  id: String,
  target: RefOutputSpec,
  scan: SourceArchive => IorNec[E, A],
  preprocess: A => IorNec[E, B],
  render: (RefOutputSpec, B) => GeneratedScalaFile
):
  def run(archive: SourceArchive): IorNec[E, GeneratedScalaFile] =
    scan(archive)
      .flatMap(preprocess)
      .map(value => render(target, value))
```

The type parameters preserve each job's scan and preprocess types while `run`
exposes one uniform result. `GtceuBackend` owns the concrete GTCEu job
definitions and can therefore keep a heterogeneous
`Vector[SymbolJob[GtceuScanDiagnostic, ?, ?]]` without a separate job-registry
file. The backend traverses those jobs without erasing the middleware types
inside each job. `GtceuScanDiagnostic` is the GTCEu enum implementing
`Diagnostic`; `type GtceuScanResult[A] = IorNec[GtceuScanDiagnostic, A]` is
kept as a convenience alias local to `backends.gtceu.scan` and is not shared
across backends.

Material scanning has a real stage boundary:

```scala
final case class MaterialScanInput(
  declarations: Map[String, SourceSite],
  assignments: Vector[LocatedMaterialAssignment],
  rejectedAssignments: Vector[RejectedMaterialAssignment]
)
```

`MaterialScanner.scan` extracts declarations and assignment syntax.
`MaterialScanner.preprocess` joins declarations to assignments, sorts refs, and
accumulates duplicate assignment, duplicate registry id, rejected assignment,
and missing assignment diagnostics. It returns
`IorNec[GtceuScanDiagnostic, Vector[ScannedMaterialRef]]`: `Right` is clean,
while `Both` preserves valid refs alongside diagnostics. The CLI never writes
output for `Left` or `Both`.

Stage responsibilities are split by what kind of failure each owns. `scan`
owns source-archive failures: a missing source file or a JavaParser parse
failure is a scan-stage diagnostic, not a thrown exception. `preprocess` owns
relational and semantic diagnostics: duplicate assignments, duplicate registry
ids, rejected assignment shapes, and missing assignments. An empty scan result
(no matching symbols) is a valid outcome, not a diagnostic.

Material-scan naming follows the approved target design: the occurrence record
used by duplicate-id diagnostics is `IdOccurrence` (not `MaterialIdOccurrence`),
and `MaterialAssignmentTarget` lives in the same file as the material expression
parser that produces it.

Path-only jobs reuse one identity preprocess function and one renderer while
supplying different `StaticFieldScanSpec` and `RefOutputSpec` values. Adding a
job with a new intermediate type means supplying another typed scan,
preprocess, and render combination; it does not require adding cases to shared
dispatch code.

The current GTCEu jobs are:

| Job id | Output object | Value type | Scan input |
| --- | --- | --- | --- |
| `gt-materials` | `GTMaterialsRef` | `MaterialRef` | material declarations plus builder assignments |
| `gt-elements` | `GTElementsRef` | `ElementRef` | static `Element` fields in `GTElements.java` |
| `material-icon-sets` | `MaterialIconSetsRef` | `MaterialIconRef` | static `MaterialIconSet` fields in `MaterialIconSet.java` |
| `fluid-attributes` | `FluidAttributesRef` | `FluidAttributeRef` | static `FluidAttribute` fields in `FluidAttributes.java` |
| `fluid-storage-keys` | `FluidStorageKeysRef` | `FluidStorageKeyRef` | static `FluidStorageKey` fields in `FluidStorageKeys.java` |
| `material-flags` | `MaterialFlagsRef` | `MaterialFlagRef` | static `MaterialFlag` fields in `MaterialFlags.java` |
| `material-flag-presets` | `MaterialFlagPresetsRef` | `MaterialFlagPresetRef` | static flag collections initialized in `GTMaterials.java` |

For material refs, the scanner accepts assignments rooted at the exact
`new Material.Builder(<id factory>.id("..."))` shape. The id-factory owner
comes from `GtMaterialsScanSpec.idFactoryFqcn`; the GTCEu job configures
`com.gregtechceu.gtceu.GTCEu`. Missing assignments, duplicate assignments,
duplicate registered ids, unsupported builder shapes, non-builder values, and
assignments through another owner are structured diagnostics rather than
silently dropped symbols. Material field aliases are intentionally not resolved
until a concrete DSL use case requires them.

Fields annotated with `@Deprecated` are excluded from every generated ref
surface. Material declarations are filtered before assignments are validated,
so a deprecated declaration produces neither a generated member nor a
missing-assignment diagnostic.

The renderer knows nothing about source archives or GTCEu job variants. Each job
selects its renderer through a typed function value. Generated members remain
parameterless `def`s so large catalogs do not allocate every ref in the Scala
object initializer:

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
entries list names each material ref exactly once and is split into chunks to
avoid oversized JVM methods; `byIdIndex` derives the `ResourceId` keys from each
ref at first lookup, so the generated text does not repeat every name twice.
There is no public `all`: it adds eager bulk allocation and an API surface that
neither ref completion nor path rendering requires.

Flag presets such as `STD_METAL`, `EXT_METAL`, and `EXT2_METAL` live on
`GTMaterials` and are collections of flags, not single `MaterialFlag` values.
They are scanned as their own target, and the generated preset object exposes
their flattened flag members for validator dependency checks.

Only two symbol-generation boundaries perform I/O:

- `SourceArchiveReader.readJar(sourcesJar)` reads the jar into `SourceArchive`.
- shared `generator.GeneratedSourceWriter.sync(outDir, files)` transactionally
  replaces an owned generated-source directory through staging and
  rollback-safe backup paths. Symbolgen and material codegen reuse this one
  implementation.

Everything between those two boundaries should be ordinary data transformation.

`SourceArchive` does not express missing or unparseable sources through
exceptions. Archive loading and per-file parsing report a structured
`SourceArchiveError`; directory scanning is partial-success and accumulates
errors rather than failing whole on the first bad file.

Current generator source-set graph:

```text
core ----------------> compileSymbolgen
  \------------------> compileCodegen
generatorSupport ----> compileSymbolgen
  \------------------> compileCodegen
compileSymbolgen
  -> generateGtRefs
  -> generated refs source dir
  -> compileCodegen
compileCodegen
  -> runCodegen
  -> generated material source dir
  -> compileScala

```

`generatorSupport` contains the generated-file value and transactional writer.
It depends only on Scala 3. `symbolgen` contains the framework contracts,
artifact scanners, backend job composition, backend registry, source-archive
reader, and renderers. It depends on `core`, `generatorSupport`, Scala 3,
JavaParser, and Cats, but not on main, generated refs, or `codegen`. Generated
source imports
`com.pixdane.gregicality.core.refs`. `codegen` compiles against `core.output`
and `generatorSupport.output` plus the generated refs source directory; it does
not compile against `symbolgen.output`. `runCodegen` validates the directly
authored material sets, renders the complete owned file set, and synchronizes
`build/generated/sources/materials/scala/main`. That directory is a
`sourceSets.main.scala` root, and `compileScala` depends on `runCodegen`.

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
walk Gradle's cache or construct a classifier path manually. Its `JavaExec`
classpath is `symbolgen.runtimeClasspath`, which Gradle already tracks as a
classpath input. `codegen` compiles against `core.output`, adds the generated
catalog directory as Scala source, and depends on `generateGtRefs`.

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

Material-codegen tests use the isolated `src/codegenTest/scala` source set and
the `testCodegen` task. Symbol-generator tests use
`src/symbolgenTest/scala` and `testSymbolgen`.

Current material tests cover:

- opaque type constructors;
- authored-content preservation and defaults;
- accumulated semantic, identity, and generated-flag-metadata validation;
- planner output;
- renderer golden snapshots;
- complete Polyimide package/index generation;
- unchanged-output timestamps through the shared writer.

For symbol generation, test scanner completeness, rejected AST shapes,
accumulated diagnostics with partial refs, `SymbolJob` stage composition and
short-circuiting, lookup-index rendering, aggregate exports, the real GTCEu job
registry, and transactional replacement of generated output. Symbol-generator
tests are organized under packages mirroring their production packages, so test
layout tracks production layout. Verification runs only through IDEA MCP run
configurations and class/main run points. The normal build runs `generateGtRefs`
against the resolved GTCEu sources artifact, runs `runCodegen`, and compiles the
generated runtime source through `compileScala`.

If compiled DSL fixtures are needed later, add a dedicated future test source
set. Raw-to-authored conversion tests arrive with that deferred layer.

## Implemented Checkpoint

Complete as of July 16, 2026:

1. `core` provides pure ref values.
2. `generatorSupport` provides the shared generated-file value and transactional
   writer.
3. `symbolgen` scans GTCEu 7.5.3 and generates material, element, icon, fluid
   attribute, fluid storage key, material flag, and flag preset refs plus
   metadata lookups and `GTRefs`.
4. `codegen` provides the authored material ADT, accumulating validator,
   deterministic planner, renderer, direct `GCYMaterialSets`, and executable
   entrypoint.
5. `runCodegen` generates `GCYMaterialsChemistryPolymers` and
   `GCYMaterialsGeneratedIndex` under the main generated-source root.
6. `GCYMaterials` routes MaterialEvent/PostMaterialEvent through the generated
   index.
7. The real Polyimide migration compiles through `compileScala`, and unchanged
   outputs make both `runCodegen` and `compileScala` UP-TO-DATE.

Still deferred:

- human-facing DSL syntax and `gcyDsl` source set;
- Raw ADT and Raw-to-authored conversion;
- file routing, SourceTrace, Deferred declarations, and local material refs;
- additional property slots and larger migration packages listed in
  `material-adt-design.md`.

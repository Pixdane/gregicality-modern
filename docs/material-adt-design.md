# Material Registration ADT Design

Status: implemented through generated-source integration (Phase 4). This
document defines the material-content ADT, validation boundary, planning,
rendering, and generated-source boundary. DSL syntax, Raw input, file routing,
and source tracing are deliberately deferred. This document supersedes the
exploratory material shapes in
compile-time-scala-dsl-design.md and the stubs in
src/codegen/scala/.../core/materials/{MaterialSpec,MaterialForms,MaterialVisual}.scala.

The design is grounded in a full read of the GTCEu Modern 7.5.3 sources JAR
that this project depends on: Material.java, all 17 MaterialProperty classes,
FluidBuilder, FluidStorageKey(s), MaterialFlag(s), and the seven material
registration files totaling 654 Material.Builder registrations. See
material-builder-api.md for the raw API inventory.

## TL;DR

A GTCEu material is not "one kind plus a few flags". It is a named identity
carrying an extensible set of properties, a flag set, an ordered composition,
and a set of build-time normalizations. The ADT must mirror that structure
instead of collapsing it into a single MaterialKind sum.

Four decisions drive everything below:

1. Model properties as unique keyed slots, not as variants of one MaterialKind.
2. The ADT records authored content only. It does not materialize GTCEu
   defaults, induced properties, auto-flags, or decomposition choices.
3. Give post-registration mutation its own ADT. Builder-time registration and
   PostMaterialEvent patching are different lifecycles and must not share one
   shape.
4. Validation may derive a temporary effective-property view to predict GTCEu
   runtime conflicts and satisfy flag requiredProps checks. It never writes
   that view back to the ADT, and the renderer never emits inferred calls.

## Why Not a Single MaterialKind

The superseded stubs modeled a material as MaterialSpec(forms, visual), where
MaterialForms holds a Vector[SolidForm] and Vector[FluidForm]. That shape
cannot represent GTCEu accurately:

- GTCEu stores properties in a Map[PropertyKey, MaterialProperty]. Each key is
  a unique slot. Setting the same key twice throws at runtime.
- A material can legitimately have no base form at all. GTCEu auto-assigns an
  EMPTY placeholder property; 654 registered materials include element
  materials with only color/element and no dust/ingot/gem/fluid.
- ingot and gem are mutually exclusive, but dust, fluid, ore, wood, polymer,
  blast, tool, armor, rotor, wire, fluidPipe, itemPipe, and hazard can all
  coexist independently.
- polymer is not just a kind: it forces dust + ingot and adds
  FLAMMABLE + NO_SMASHING + DISABLE_DECOMPOSITION. Encoding it as a bare
  SolidKind loses those induced properties, which the validator would then have
  to either expand (duplicating GTCEu runtime) or ignore (dropping the author's
  intent). Keyed slots avoid that choice by recording polymer as one property
  among many, with its induction left to GTCEu.

A single sum type would either forbid valid combinations or lose the
independence between properties like fluid, ore, and blast. Unique keyed slots
preserve that. Induced properties (ingot -> dust, blast -> ingot) are left to
GTCEu's runtime; the ADT only records what the author authored.

## Pipeline Boundary

This plan starts after parsing/loading. The authored material ADT is validated
directly; a future DSL plan may add Raw -> authored conversion in front of it.

```text
MaterialSet
  -> MaterialValidator        (scalar, identity, conflict, dependency checks)
  -> validated MaterialSet    (same values; validation never rewrites them)
  -> MaterialPlan             (builder overload selection, ordering, imports)
  -> RenderedScalaSource
  -> GeneratedScalaFile       (owned relative path + complete content)
  -> GeneratedSourceWriter    (atomic directory synchronization)
  -> sourceSets.main.scala
  -> compileScala
```

The validator checks conflicts that GTCEu would throw at runtime and
flag-dependency gaps that GTCEu would only log. For those checks it may derive
an immutable effective-property set (for example blast implies ingot implies
dust), but successful validation returns the original MaterialSet unchanged.

## Common Value Types

The first implementation keeps validated scalar types in codegen, where cats
ValidatedNec is used. core remains a pure symbol-ref vocabulary.

```scala
opaque type RegistryPath = String
opaque type ScalaIdent = String
opaque type HexRgb = Int
opaque type PositiveInt = Int
opaque type NonNegativeInt = Int
opaque type Kelvin = Int             // > 0; blast temperature
opaque type DurationTicks = Int       // > 0; authored recipe override
opaque type HarvestLevel = Int       // >= 0; wood() legitimately uses 0
opaque type BurnTimeTicks = Int      // >= 0
opaque type Voltage = Long           // > 0; literal EU/t
opaque type FluidTemperature = Int   // >= 0 Kelvin
opaque type ProgressionMultiplier = Float

sealed trait ValidationIssue
type ValidationResult[A] = ValidatedNec[ValidationIssue, A]

enum VoltageExpr:
  case Tier(name: ScalaIdent)         // GTValues.V[GTValues.<name>]
  case VA(name: ScalaIdent)           // GTValues.VA(GTValues.<name>)
  case Literal(value: Voltage)
```

Each opaque scalar exposes a `from` constructor returning ValidationResult.
RegistryPath is the path part only, so it accepts Minecraft's lowercase path
grammar and never includes a namespace. Material-specific naming policy, such
as rejecting a trailing underscore on a material id, remains a Phase 2 check.

Refs come from the generated symbol scan and live in core.refs:

```scala
final case class MaterialRef(id: ResourceId, path: ScalaSymbolPath)
final case class ElementRef(path: ScalaSymbolPath)
final case class MaterialIconRef(path: ScalaSymbolPath)
final case class MaterialFlagRef(path: ScalaSymbolPath)
final case class MaterialFlagPresetRef(path: ScalaSymbolPath)
final case class MaterialPropertyKeyRef(path: ScalaSymbolPath)
final case class MaterialFlagRequirements(
  requiredFlags: Vector[MaterialFlagRef],
  requiredProperties: Vector[MaterialPropertyKeyRef]
)
final case class FluidAttributeRef(path: ScalaSymbolPath)
final case class FluidStorageKeyRef(path: ScalaSymbolPath)
final case class GasTierRef(path: ScalaSymbolPath)
final case class TagPrefixRef(path: ScalaSymbolPath)
final case class HazardTriggerRef(path: ScalaSymbolPath)
final case class MedicalConditionRef(path: ScalaSymbolPath)
final case class ToolTypeRef(path: ScalaSymbolPath)
final case class ItemTagRef(path: ScalaSymbolPath)
```

FluidStorageKeyRef is a hand-written pure ref type; `FluidStorageKeysRef`
generates values for the four built-in GTCEu keys (LIQUID, GAS, PLASMA,
MOLTEN). Addons can register more, so fluid identity cannot be a closed
three-variant enum.

## Material Content ADT

```scala
enum MaterialDeclaration:
  case NewMaterial(spec: NewMaterialSpec)
  case MarkerMaterial(spec: MarkerMaterialSpec)
  case ExistingPatch(spec: MaterialPatchSpec)
  case Deferred(spec: DeferredMaterialSpec)

final case class MaterialSet(
  declarations: NonEmptyVector[MaterialDeclaration]
)
```

MarkerMaterial is a first-class case because GTCEu MarkerMaterial bypasses
Material.Builder entirely: it skips registration, skips verification, and is
used only as a recipe tag. Folding it into NewMaterial would force the
renderer to emit invalid new Material.Builder(...) calls.

ExistingPatch is specified below. DeferredMaterialSpec is part of the complete
lifecycle model, but the current implementation plan deliberately leaves it
out until migration metadata needs it.

### NewMaterialSpec

```scala
final case class NewMaterialSpec(
  id: RegistryPath,
  field: ScalaIdent,
  identity: MaterialIdentity,
  visuals: VisualSpec,
  composition: CompositionSpec,
  properties: MaterialProperties,
  flags: MaterialFlagSpec,
  tags: MaterialTagConfig
)
```

identity collects the non-property, non-visual identity fields:

```scala
final case class MaterialIdentity(
  displayName: Option[String],          // langValue override; None = auto from id
  element: Option[ElementRef]
)
```

GTCEu's builder does not reject element + components. The validator therefore
does not impose an exclusivity rule that the upstream API does not have.

VisualSpec models GTCEu's three color modes explicitly, because
Option[HexRgb] cannot distinguish "unset, default to white" from "average
components":

```scala
final case class VisualSpec(
  primaryColor: ColorSpec,
  secondaryColor: Option[HexRgb],
  iconSet: Option[MaterialIconRef],
  fluidColor: FluidColorPolicy
)

enum ColorSpec:
  case Default             // no color call; GTCEu defaults to 0xFFFFFF
  case Explicit(rgb: HexRgb)
  case AverageComponents   // colorAverage(); weighted average of components

enum FluidColorPolicy:
  case InheritMaterial     // default: fluid colored by material RGB
  case Disabled            // hasFluidColor = false
```

CompositionSpec keeps the authored formula separate from the auto-derived one:

```scala
final case class CompositionSpec(
  components: Vector[ComponentSpec],    // ordered; GTCEu preserves order
  formulaOverride: Option[FormulaOverride]
)

final case class ComponentSpec(
  material: MaterialRef,
  amount: PositiveInt
)

final case class FormulaOverride(
  text: String,
  formatSubscripts: Boolean             // formula(s, withFormatting)
)

final case class MaterialFlagSpec(
  presets: Vector[MaterialFlagPresetRef],
  flags: Set[MaterialFlagRef]
)
```

MaterialFlagsRef keeps each ordinary flag accessor and adds a requirements
lookup scanned from MaterialFlags.java. MaterialFlagPresetsRef is generated
separately from the collection fields and ordered static-initializer operations
in GTMaterials.java; its members lookup returns the flattened authored preset
members. The refs remain path-only. Metadata lives in generated lookup tables
and is never attached to or written back into MaterialFlagSpec.

### MaterialProperties

This is the core change. Properties are unique keyed slots:

```scala
final case class MaterialProperties(
  dust: Option[DustPropertySpec],
  ingot: Option[IngotPropertySpec],
  gem: Option[GemPropertySpec],
  wood: Option[WoodPropertySpec],
  polymer: Option[PolymerPropertySpec],
  fluid: Option[FluidPropertySpec],
  ore: Option[OrePropertySpec],
  blast: Option[BlastPropertySpec],
  tool: Option[ToolPropertySpec],
  armor: Option[ArmorPropertySpec],
  rotor: Option[RotorPropertySpec],
  wire: Option[WirePropertySpec],
  fluidPipe: Option[FluidPipePropertySpec],
  itemPipe: Option[ItemPipePropertySpec],
  hazard: Option[HazardPropertySpec]
)
```

Every field is Option because a material may author any subset. A material with
zero properties is legal; GTCEu assigns EMPTY at build time.

The first implementation slice contains only dust, ingot, gem, wood, polymer,
fluid, ore, and blast slots. The remaining slots above document the stable
extension direction and are added only when their migration is implemented.

The validator computes a check-only effective-property view:

- first slice: ingot, gem, wood, polymer, and ore imply dust; blast and
  polymer imply ingot;
- later slots: wire implies dust; rotor implies ingot; tool implies ingot
  unless wood or gem is present; fluidPipe/itemPipe imply ingot unless wood is
  present.

That view is used to detect effective ingot+gem and fluidPipe+itemPipe conflicts
and to satisfy flag requiredProps checks. It is never stored in MaterialProperties.
Successful validation returns the original authored values unchanged.

### Property Specs

```scala
final case class DustPropertySpec(
  harvestLevel: Option[HarvestLevel],
  burnTime: Option[BurnTimeTicks]
)

final case class IngotPropertySpec(
  smeltingInto: Option[MaterialRef],
  arcSmeltingInto: Option[MaterialRef],
  macerateInto: Option[MaterialRef],
  magneticMaterial: Option[MaterialRef]
)

final case class GemPropertySpec()
final case class WoodPropertySpec()
final case class PolymerPropertySpec()
```

Dust settings are recorded once. Ingot/gem/polymer/wood are property-presence
slots. The planner maps dust settings onto the appropriate builder overload:
ingot(harvest,burn), gem(...), or wood(...). GTCEu 7.5.3's
polymer(harvest,burn) implementation ignores its burnTime argument, so polymer
uses polymer(harvest) plus a separate burnTime(...) call when burn time is
authored. If none of those forms is present, the planner emits dust(...). This
avoids materializing runtime defaults or emitting duplicate setters.

```scala
final case class FluidPropertySpec(
  fluids: NonEmptyVector[FluidEntry],   // unique FluidStorageKeyRef per entry
  primaryKey: Option[FluidStorageKeyRef] // None = first registered key at runtime
)

final case class FluidEntry(
  key: FluidStorageKeyRef,
  builder: FluidBuilderSpec
)

final case class FluidBuilderSpec(
  temperature: Option[FluidTemperature],
  state: Option[FluidState],
  color: FluidColor,
  density: Option[FluidDensity],
  luminosity: Option[Int],               // 0..15
  viscosity: Option[FluidViscosity],
  attributes: Vector[FluidAttributeRef],
  textures: FluidTextures,
  createBlock: Boolean,                  // true emits block()
  disableBucket: Boolean,                // true emits disableBucket()
  burnTime: Option[BurnTimeTicks],
  name: Option[RegistryPath],
  translation: Option[String]
)

enum FluidColor:
  case Inferred                          // from material RGB
  case Explicit(rgb: HexRgb)
  case Disabled

enum FluidState:
  case Liquid, Gas, Plasma

enum FluidDensity:
  case GramsPerCubicCentimeter(value: Double)
  case Minecraft(value: Int)

enum FluidViscosity:
  case Poise(value: Double)
  case Minecraft(value: Int)

enum FluidTextures:
  case Inferred                          // from FluidStorageKey icon type
  case CustomStill
  case CustomStillAndFlowing
```

primaryKey is optional because GTCEu auto-assigns the first registered key.
Explicit values cover post-registration overrides such as Helium and Oxygen
using GAS.

```scala
final case class OrePropertySpec(
  multipliers: Option[OreMultipliers],
  emissive: Boolean,                    // true selects an emissive ore overload
  directSmeltResult: Option[MaterialRef],
  washedIn: Option[OreWashSpec],
  separatedInto: Vector[MaterialRef],   // additive; GTCEu appends
  byproducts: Vector[MaterialRef]       // set, not append; setter clears first
)

final case class OreMultipliers(
  ore: PositiveInt,
  byproduct: PositiveInt
)

final case class OreWashSpec(
  material: MaterialRef,
  amount: Option[PositiveInt]           // None selects washedIn(material)
)

final case class BlastPropertySpec(
  temperature: Kelvin,
  gasTier: Option[GasTierRef],          // None = no gas EBF recipe
  eutOverride: Option[VoltageExpr],     // None = default 120 EU/t
  durationOverride: Option[DurationTicks],
  vacuumEutOverride: Option[VoltageExpr],
  vacuumDurationOverride: Option[DurationTicks]
)

final case class ToolPropertySpec(
  harvestSpeed: Float,
  attackDamage: Float,
  durability: NonNegativeInt,
  harvestLevel: HarvestLevel,
  attackSpeed: Option[Float],
  enchantability: Option[Int],          // default 10
  prospectingDepth: Option[Int],
  ignoreCraftingTools: Boolean,
  unbreakable: Boolean,
  magnetic: Boolean,
  durabilityMultiplier: Option[Int],
  types: Vector[ToolTypeRef],           // default = all GTToolType
  enchantments: Vector[(EnchantmentRef, Int)]
)

final case class ArmorPropertySpec(
  durabilityMultiplier: NonNegativeInt,
  protectionValues: Map[ArmorSlot, Int],// exactly 4 entries
  enchantability: Option[Int],
  toughness: Option[Float],
  knockbackResistance: Option[Float],
  unbreakable: Boolean,
  dyeable: Boolean
)

enum ArmorSlot:
  case Helmet, Chestplate, Leggings, Boots

final case class RotorPropertySpec(
  power: PositiveInt,
  efficiency: PositiveInt,
  damage: NonNegativeFloat,
  durability: PositiveInt
)

final case class WirePropertySpec(
  voltage: VoltageExpr,
  amperage: PositiveInt,
  lossPerBlock: NonNegativeInt,         // forced 0 when isSuperconductor
  isSuperconductor: Boolean,
  criticalTemperature: Option[Int]      // only when isSuperconductor
)

final case class FluidPipePropertySpec(
  maxFluidTemperature: Int,
  throughput: PositiveInt,
  channels: Int,                        // 1..9, default 1
  gasProof: Boolean,
  acidProof: Boolean,
  cryoProof: Boolean,
  plasmaProof: Boolean
)

final case class ItemPipePropertySpec(
  priority: Int,
  transferRate: PositiveFloat           // stacks per second
)

final case class HazardPropertySpec(
  trigger: HazardTriggerRef,
  condition: MedicalConditionRef,
  progressionMultiplier: ProgressionMultiplier,
  applyToDerivatives: Boolean
)
```

EnchantmentRef and NonNegativeFloat/PositiveFloat are new opaque types;
enchantments are rare (GTCEu uses addDefaultEnchant only in deprecated paths)
so they can be stubbed in the first slice.

### Tags

```scala
final case class MaterialTagConfig(
  ignoredTagPrefixes: Vector[TagPrefixRef],
  customItemTags: Vector[ItemTagRef]
)
```

ItemTagRef is a new path-only ref; GTCEu's customTags takes a TagKey<Item>
which is not a generated symbol, so the DSL will need a small literal form for
it. Leave the literal syntax for the DSL layer; the ADT only carries the path.

## Marker Material

```scala
final case class MarkerMaterialSpec(
  id: RegistryPath,
  field: ScalaIdent
)
```

No properties, no flags, no composition. The renderer emits
new MarkerMaterial(GTCEu.id("...")) (or the addon's id factory), never
new Material.Builder(...). This matches GTCEu's 18 marker materials plus
GTMaterials.NULL.

## Post-Registration Patch

GTCEu's MaterialFlagAddition.register() and addon PostMaterialEvent handlers
mutate already-registered materials. This is a distinct lifecycle from
builder-time registration and needs its own ADT:

```scala
final case class MaterialPatchSpec(
  target: MaterialRef,
  operations: NonEmptyVector[PatchOperation]
)

enum PatchOperation:
  case AddFlags(flags: NonEmptyList[MaterialFlagRef])
  case SetFormula(text: String, formatSubscripts: Boolean)
  case SetMaterialColor(rgb: HexRgb)
  case SetSecondaryColor(rgb: HexRgb)
  case SetIconSet(iconSet: MaterialIconRef)
  case SetPrimaryKey(fluid: FluidStorageKeyRef)
  case SetIngotSmeltingInto(target: MaterialRef)
  case SetArcSmeltingInto(target: MaterialRef)
  case SetMacerateInto(target: MaterialRef)
  case SetMagneticMaterial(target: MaterialRef)
  case SetOreByproducts(byproducts: Vector[MaterialRef])
  case AddOreByproducts(byproducts: Vector[MaterialRef])
  case SetWashedIn(material: MaterialRef, amount: PositiveInt)
  case SetSeparatedInto(materials: Vector[MaterialRef])
  case SetDirectSmeltResult(material: MaterialRef)
  case RemoveHazard
```

This is deliberately a closed enum of the operations GTCEu actually performs in
MaterialFlagAddition and in PostMaterialEvent addon code. It does not model
arbitrary setProperty because patch-time property changes are a runtime concern
and the codegen does not expand property induction.

Patches render into PostMaterialEvent handler bodies, not into MaterialEvent
registration bodies.

## Deferred Materials

```scala
final case class DeferredMaterialSpec(
  id: RegistryPath,
  field: Option[ScalaIdent],
  reason: String
)
```

Deferred materials produce no generated registration code. They are optional
migration metadata, not part of DSL routing.

## Validation Rules

Validation accumulates issues via ValidatedNec:

1. Scalar: registry path syntax, Scala identifier validity, RGB range, and
   numeric bounds.
2. Identity: material ids and fields are unique in MaterialSet; material ids do
   not end in an underscore; new ids do not collide with known canonical GTCEu
   ids.
3. Semantic: fluid storage keys are unique within a material; check-only
   effective properties contain neither ingot+gem nor fluidPipe+itemPipe; an
   explicitly authored fluid primary key names one of that material's entries;
   disabled material-level fluid tinting is paired with an explicit primary
   color because `Material.Builder` has no standalone fluid-color switch.
4. Flag dependency: each authored flag's required flags are authored, and its
   required properties are present in the check-only effective-property view.
   Presets are expanded through generated metadata for checks only. Unknown
   flag or preset refs are errors because their dependencies cannot be checked.
5. Target representability: blast duration overrides require their matching
   EU/t override. GTCEu exposes `blastStats(eut, duration)` and
   `vacuumStats(eut, duration)`, but no duration-only overload; validation
   rejects the unrenderable state instead of materializing GTCEu's internal
   `-1` sentinel.

MaterialValidator receives a MaterialValidationSymbols lookup boundary. The
production implementation delegates to generated MaterialFlagsRef,
MaterialFlagPresetsRef, and GTMaterialsRef; tests can provide map-backed
lookups. validateSpec and validateSet return the exact input object on success.
MaterialSet uniqueness covers NewMaterial and MarkerMaterial ids/fields, while
canonical GTCEu path collisions apply only to NewMaterial registrations.

The validator returns the original MaterialSet on success. It never adds
properties or flags. GTCEu runtime handles ensureSet, flags.verify, polymer
auto-flags, wire auto-flags, and decomposition assignment.

## What Stays Runtime-Only

The ADT intentionally does not store, and the renderer intentionally does not
emit:

- Property induction (ingot -> dust, blast -> ingot, polymer -> dust + ingot,
  tool -> ingot, pipes -> ingot). GTCEu's ensureSet handles this at build time.
- Flag closure (GENERATE_GEAR pulling in GENERATE_PLATE + GENERATE_ROD).
  GTCEu's flags.verify() handles this; the validator only checks required flags
  and checks required properties against its temporary effective view.
- Polymer auto-flags (FLAMMABLE, NO_SMASHING, DISABLE_DECOMPOSITION). Added by
  PolymerProperty.verifyProperty() at runtime.
- GENERATE_FOIL auto-add for high-voltage non-supercon wires. Added by
  WireProperties.verifyProperty() at runtime.
- Cross-material ensureSet side effects (e.g. oreProp.setWashedIn(X) forcing X
  to gain a FLUID property). These require the target material to already be
  registered.
- RGB weighted-average computation when ColorSpec.AverageComponents is set.
- IconSet default derivation from the final property set.
- Decomposition type auto-assignment (centrifuge vs electrolyzing).
- TagPrefix setIgnored / modifyMaterialAmount / addSecondaryMaterial.
- Recipe, item, and block generation.

Everything that depends on runtime material state or GTCEu build-time
normalization stays in GTCEu. The validator may predict that normalization for
checks, but does not persist or render the prediction.

## Rendering Implications

`MaterialPlanner.plan(materials, output)` receives a validated `MaterialSet` and
a `MaterialOutputSpec` containing the generated package, object name, and
addon's id-factory symbol. It produces a `MaterialPlan` containing deterministic
imports, field declarations, registration plans, and patch plans. It never
revalidates by mutating content and never expands presets, flags, or properties.
The codegen source set owns a small local `ScalaCode` value rather than depending
on symbolgen internals. `GeneratedScalaFile` and the transactional
`GeneratedSourceWriter` live in the shared `generatorSupport` source set because
both symbolgen and material codegen need them; `core` remains pure data.

References render as `Owner.Member` with an exact import of `Owner`, rather than
depending on wildcard-import order. Preset order, fluid order, component order,
tag order, declaration order, and patch-operation order remain authored order.
The unordered `Set[MaterialFlagRef]` is sorted by symbol path before rendering.

The renderer must emit builder calls in a canonical order that respects GTCEu's
property-creation side effects. The order derived from the 654 registrations and
the source code is:

```text
1. langValue
2. solid properties and authored ingot transformation targets
3. burnTime       (when dust burn time is authored separately)
4. fluids:        fluid | liquid | gas | plasma          (each unique key)
5. ore plus oreSmeltInto, washedIn, separatedInto, addOreByproducts
6. color, secondaryColor, colorAverage, iconSet
7. flags, appendFlags
8. element, components, componentStacks, formula
9. toolStats, armorStats, rotorStats
10. cableProperties, fluidPipeProperties, itemPipeProperties
11. blast, blastTemp
12. hazard, radioactiveHazard, removeHazard
13. ignoredTagPrefixes, customTags
14. buildAndRegister()
15. setPrimaryKey(...)        (separate post-build statement, when authored)
```

Steps 2-5 require overload selection:

- Dust settings have exactly one carrier. `wood` has first priority because its
  overload directly installs the dust slot; the remaining priority is `ingot`,
  `gem`, `polymer`, then bare `dust`. Other authored solid slots emit their bare
  call. This permits authored combinations such as wood+ingot without emitting
  a duplicate dust setter.
- A harvest-only value selects the one-argument carrier overload. Harvest plus
  burn selects the two-argument ingot/gem/wood overload. Polymer uses
  `polymer(harvest)` plus `burnTime(...)` because GTCEu 7.5.3 ignores the
  `polymer(harvest, burn)` burn argument. Burn-only content uses
  `burnTime(...)`; that builder call already ensures dust, so the renderer does
  not add `dust()`.
- Built-in LIQUID, GAS, and PLASMA keys select `liquid`, `gas`, and `plasma`
  overloads when their exact owner and authored state are compatible. Matching
  by exact owner prevents an addon member such as `CustomFluidKeys.GAS` from
  being mistaken for `FluidStorageKeys.GAS`. A temperature-only fluid selects
  the integer overload. Other fluid-builder content selects the `FluidBuilder`
  overload. Arbitrary keys select `fluid(key, state)` when only state is
  authored, otherwise `fluid(key, new FluidBuilder()...)`.
- `ore()` and `ore(emissive)` preserve GTCEu's multiplier defaults when
  multipliers are absent. Explicit multipliers select the two- or three-argument
  overload. `washedIn(material)` preserves the builder's 100 mB default; the
  two-argument overload is emitted only when amount is authored.
- A simple blast selects `blast(temp)` or `blast(temp, gasTier)`. Any authored
  recipe-stat override selects the lambda builder and emits only the authored
  `blastStats`/`vacuumStats` calls.

Formula overrides render as the builder call
`formula(text, formatSubscripts)`. `buildAndRegister()` already applies that
stored override through `Material.setFormula`, so the renderer does not emit a
second post-build formula call. `FluidProperty` assigns the first registered key
internally and `Material.Builder` has no primary-key setter, so an explicit
primary key renders afterward as
`Field.getProperty(PropertyKey.FLUID).setPrimaryKey(key)`.

Flag presets are never expanded by the planner. With no preset, authored flags
use one `flags(...)` call. With presets, each authored preset uses
`appendFlags(...)`; sorted individual flags are attached to the first preset.
No inferred dust property, required flag, polymer flag, default multiplier,
default wash amount, default color, or internal sentinel is emitted.

## Implemented First Slice

The design is intentionally broader than the current migration. The implemented
first slice contains:

1. NewMaterialSpec with dust settings, ingot, gem, wood, polymer, fluid, ore, blast,
   composition, visuals, and flag presets/flags.
2. MarkerMaterialSpec for NULL and color markers.
3. MaterialPatchSpec with SetOreByproducts, SetWashedIn, SetSeparatedInto,
   SetDirectSmeltResult, SetMagneticMaterial, SetArcSmeltingInto, SetPrimaryKey.
4. Validators for scalar, identity, semantic, and flag-dependency checks. The
   effective-property view is temporary and never written back.
5. Renderer for the canonical order above, restricted to the implemented
   subset.

tool, armor, rotor, wire, fluidPipe, itemPipe, and hazard are designed but
deferred until the migration reaches materials that need them.

Phase 3 implementation uses `MaterialOutputSpec`, `MaterialPlan`,
`MaterialDeclarationPlan`, `BuilderCall`, and `ScalaExpr` as a small code-plan
boundary. `MaterialRenderer` emits one object with stable fields plus separate
`register()` and `patch()` methods. Marker fields use `MarkerMaterial`, new
fields use `Material`, and patch-only objects import no id factory.

## Phase 4 Integration

The first runtime integration directly constructs authored `MaterialSet` values
in `GCYMaterialSets`; it does not load a DSL or Raw ADT. `Codegen` validates,
plans, and renders each package, then atomically owns:

```text
build/generated/sources/materials/scala/main/
  com/pixdane/gregicality/common/data/materials/
    GCYMaterialsChemistryPolymers.scala
    GCYMaterialsGeneratedIndex.scala
```

`GCYMaterialsGeneratedIndex.registerAll()` is called during `MaterialEvent`;
`patchAll()` is called during `PostMaterialEvent`. The first migrated material
is Polyimide with authored polymer/harvest, LIQUID fluid, color, DULL icon,
C22/H12/N2/O6 composition, and `GENERATE_PLATE`. The renderer emits no display
name, formula, inferred properties, polymer auto-flags, or obsolete
`SMELT_INTO_FLUID` flag.

Gradle declares the generated directory as both the `runCodegen` output and a
`sourceSets.main.scala` root. `compileScala` depends on `runCodegen`; unchanged
content leaves the owned directory untouched and lets both tasks become
UP-TO-DATE.

## Open Questions

- ItemTagRef input syntax is deferred. GTCEu customTags takes a TagKey<Item>
  which is not currently generated by symbolgen.
- EnchantmentRef: enchantments are registry objects, not generated symbols.
  Same literal-syntax question. Defer; GTCEu's addDefaultEnchant is deprecated.
- Local material refs require a later declaration-index plan. The first slice
  only consumes GTCEu refs from generateGtRefs.
- AlloyBlastProperty has no builder method; it is set only via internal
  properties.setProperty. The ADT omits it until a migration needs alloy
  blast recipes, at which point it can be added as an additional property slot
  without breaking the existing shape.

# Material ADT Implementation Plan

Status: active plan. Tracks implementation of the material ADT defined in
material-adt-design.md. The authored-only principle from that doc is binding:
the ADT is never expanded with inferred properties or flags. Validation may
derive a temporary effective-property view to predict GTCEu runtime checks, but
that view is discarded after validation. The renderer emits only authored
content.

## Guiding Rules

- Validator only checks, never mutates or expands the ADT. It may derive an
  effective-property view for checks such as blast+gem (blast implies ingot,
  then ingot conflicts with gem) and flag requiredProps.
- Flag dependency gaps are reported as errors. GTCEu only warns; codegen is
  stricter. The required-flags and required-props come from GTCEu's
  MaterialFlags declarations (scanned, not hardcoded).
- dust+ingot is valid. Dust settings are material content; the planner selects
  ingot(harvest,burn) and does not emit a second dust setter.
- core stays a pure ref vocabulary. cats belongs to codegen validation unless
  a future core API genuinely exposes cats types.
- Tests follow the symbolgenTest JUnit5 pattern. All build/test/debug execution
  is performed by the main agent through IDEA MCP run configurations.

## Phase 0 - Build Infrastructure

Goal: codegen can compile with cats; pure refs and the test source set exist.

Status: complete on 2026-07-16.

1. codegen.gradle.kts: add scala3 + cats to codegen; add codegenTest source set
   mirroring symbolgenTest (junit5, platform launcher).
2. core: add new ref types missing from Refs.scala: FluidStorageKeyRef,
   GasTierRef, TagPrefixRef, HazardTriggerRef, MedicalConditionRef,
   ToolTypeRef, MaterialFlagPresetRef, ItemTagRef.
3. Add Scaladoc to the shared paths and refs; add JUnit tests for path/ref
   purity and source-set wiring.
4. Add testCodegen task wired into check.

Verify: run RefsTest through an IDEA MCP test run configuration; this compiles
core, codegen, and codegenTest. core remains free of cats and runtime GTCEu
objects.

Verification result: IDEA imported `Gregicality.codegenTest`; the RefsTest
class run point completed with exit code 0. IDEA inspections reported no
problems in the build script, Refs.scala, or RefsTest.scala.

## Phase 1 - Verified ADT

Goal: authored material content compiles as pure data, without DSL routing.

Status: complete on 2026-07-16.

1. Delete stubs: MaterialForms.scala, MaterialVisual.scala, MaterialSpec.scala.
2. Add codegen-local validated scalar types and ValidationIssue:
   RegistryPath, ScalaIdent, HexRgb, PositiveInt, NonNegativeInt, Kelvin,
   DurationTicks, HarvestLevel, BurnTimeTicks, Voltage, FluidTemperature.
   These use cats ValidatedNec and do not enter core. RegistryPath is the
   namespace-free Minecraft path; material-id naming policy remains in Phase 2.
3. codegen/core/materials/: NewMaterialSpec, MaterialProperties, VisualSpec +
   ColorSpec + FluidColorPolicy, CompositionSpec + ComponentSpec +
   FormulaOverride, MaterialTagConfig, MaterialIdentity.
4. Property specs (first slice subset):
   - DustPropertySpec owns optional harvest/burn settings.
   - IngotPropertySpec owns only optional transformation targets.
   - GemPropertySpec, WoodPropertySpec, PolymerPropertySpec are property
     presence markers; their dust settings are represented once in dust.
   - FluidPropertySpec uses NonEmptyVector[FluidEntry] and
     Option[FluidStorageKeyRef] primaryKey.
   - FluidBuilderSpec records createBlock/disableBucket call intent rather than
     materializing FluidBuilder defaults.
   - OrePropertySpec uses optional OreMultipliers and an OreWashSpec with an
     optional amount so `.ore()` and `.washedIn(material)` keep their defaults
     inside the GTCEu builder.
   - BlastPropertySpec.
5. MarkerMaterialSpec and MaterialPatchSpec + PatchOperation. DSL-oriented
   MaterialPackageSpec, RouteInfo, SourceTrace, Raw ADTs, and Deferred are not
   implemented in this plan.
6. VoltageExpr sum (Tier / VA / Literal).
7. Add Scaladoc to public ADT types and unit tests for authored-state
   preservation/default values.

Verify: run the Phase 1 test class through IDEA MCP. ADT remains pure case
classes and contains no runtime GTCEu objects.

Verification result: the IDEA run points for MaterialValuesTest,
MaterialContentTest, and MaterialDeclarationsTest all completed with exit code
0. The 14 tests cover scalar bounds, neutral authored defaults, property and
fluid shapes, runtime-default omission, induced-property non-materialization,
and non-empty declaration/patch collections. The codegen material package has
no GTCEu runtime imports.

## Phase 2 - Validator (checks only)

Goal: report conflicts without changing the ADT. This is the core value of the
codegen.

Status: Phase 2A symbol metadata complete on 2026-07-16; validator remains.

1. MaterialValidator accumulating ValidatedNec:
   - lexical: opaque type from() checks (path format, ident, RGB, bounds)
   - semantic: fluid keys unique within a material, ingot+gem conflict,
     fluidPipe+itemPipe conflict
   - effective view: derive implied property presence for checks only; never
     write it back. This catches blast+gem, polymer+gem, rotor+gem, and
     non-wood pipe+gem conflicts.
   - flag dependency: for each authored flag, required flags and required
     properties must be satisfied by the effective view
2. Extend symbolgen so flag dependency data is scanned from MaterialFlags.java,
   while MaterialFlag collection presets are scanned from the List<MaterialFlag>
   fields and ordered static initializer in GTMaterials.java. Generate
   MaterialFlagsRef.requirements(flag) and
   MaterialFlagPresetsRef.members(preset); do not attach metadata to path-only
   refs or hardcode dependency tables. Examples:
   GENERATE_GEAR needs GENERATE_PLATE + GENERATE_ROD + DUST;
   GENERATE_FOIL needs GENERATE_PLATE + INGOT.
3. Tests (highest-value phase):
   - ingot + gem -> error
   - blast + gem -> error through check-only implied ingot
   - polymer + gem -> error through check-only implied ingot
   - fluidPipe + itemPipe -> error
   - dust + ingot -> valid; ADT unchanged
   - GENERATE_PLATE + ingot -> required DUST satisfied by effective view
   - GENERATE_GEAR without GENERATE_PLATE -> flag-dependency error
   - zero properties -> valid (EMPTY is runtime)
   - fluid with duplicate storage key -> error

Phase 2A verification result: IDEA run points for
MaterialFlagScannerTest, MaterialFlagPresetScannerTest, RefOutputRendererTest,
GtceuBackendTest, and RefsTest all completed with exit code 0. The
parameterized GenerateRefs main run point also completed with exit code 0
against the GTCEu 7.5.3 sources JAR, producing MaterialFlagsRef requirements
for all scanned flags and flattened STD_METAL/EXT_METAL/EXT2_METAL preset
members. MaterialFlagMetadataIntegrationTest consumed those generated lookup
tables and completed with exit code 0.

Verify: run MaterialValidatorTest through IDEA MCP. Assert the returned
NewMaterialSpec equals the input for every valid case.

## Phase 3 - Planner + Renderer

Goal: authored ADT -> Scala source in canonical order.

1. MaterialPlanner: Verified -> MaterialPlan (builder step ordering per the
   15-step canonical order in the design doc, import collection, generated
   object naming).
2. MaterialRenderer: MaterialPlan -> ScalaCode. Reuse or fork the ScalaCode
   helper from symbolgen/render.
3. Dust-settings rule: when ingot/gem/polymer/wood is present, planner folds
   DustPropertySpec settings into the applicable builder overload and emits no
   separate dust call. GTCEu 7.5.3 ignores polymer(harvest,burn)'s burnTime
   argument, so polymer burn time uses a separate burnTime(...) call. Bare dust
   emits dust(...); other burn-only settings also use burnTime(...).
4. MaterialPatchRenderer: PatchOperation -> PostMaterialEvent handler body.
5. MarkerMaterialRenderer: emit new MarkerMaterial(...), never Material.Builder.
6. Tests: golden-file string assertions for a representative NewMaterial (e.g.
   polyimide), a MarkerMaterial, and a MaterialPatch.

Verify: run renderer golden tests through IDEA MCP.

## Phase 4 - Codegen Integration

Goal: generated .scala compiles as part of the mod.

1. runCodegen consumes a directly constructed MaterialSet; DSL loading and Raw
   conversion are outside this plan.
2. Add a material source generation task that writes to
   build/generated/sources/materials/scala/main/.
3. Generate GCYMaterialsChemistryPolymers + GCYMaterialsGeneratedIndex; wire
   into compileScala.
4. Incremental check: unchanged material input does not rewrite generated
   sources.

Verify: run the generation and compilation Gradle run configurations through
IDEA MCP; generated sources compile.

## Phase 5 - Cleanup + Doc Sync

1. Delete Codegen.scala hello-world stub; replace with real entrypoint.
2. compile-time-scala-dsl-design.md: mark Raw/DSL routing as deferred and point
   material content/validation/rendering to material-adt-design.md.
3. material-builder-api.md: add property-verify fixpoint behavior, EMPTY
   material, post-registration patch facts from the source read.

## What This Plan Does Not Do

- No DSL syntax, Raw ADT, package routing, SourceTrace, or local declaration
  scan. These belong to a later DSL plan.
- No tool/armor/rotor/wire/fluidPipe/itemPipe/hazard property specs. Designed
  in material-adt-design.md, deferred until migration needs them.
- No property/flag expansion of the ADT. Validator may derive a temporary
  effective-property view for checks, then discards it.
- No local material refs (Gregicality-authored MaterialRef). First slice
  consumes only GTCEu refs from generateGtRefs.

## Execution Order

Phase 0 -> 1 -> 2 is the brain. Phase 3-4 is the output layer. Each phase
ends with the relevant IDEA MCP run configurations. Phase 2 tests are the
highest-value deliverable: they encode the conflict rules that make the
codegen worth having.

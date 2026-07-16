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

Status: complete on 2026-07-16.

1. MaterialValidator accumulating ValidatedNec:
   - lexical: opaque type from() checks (path format, ident, RGB, bounds)
   - identity: NewMaterial/MarkerMaterial ids and fields unique; trailing
     underscore rejected; canonical GTCEu path collisions rejected only for
     NewMaterial
   - semantic: fluid keys unique within a material, explicit primary key belongs
     to its entries, ingot+gem conflict; fluidPipe+itemPipe remains deferred
   - effective view: derive implied property presence for checks only; never
     write it back. The first slice catches blast+gem and polymer+gem; rotor and
     pipe checks arrive with those deferred slots.
   - flag dependency: for each authored flag, required flags and required
     properties must be satisfied by the effective view; presets expand through
     generated metadata; unknown flags/presets fail closed
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
   - explicit fluid primary key absent from entries -> error
   - duplicate id / duplicate field / trailing underscore / canonical path
     collision -> accumulated errors
   - unknown flag or preset ref -> error

Phase 2A verification result: IDEA run points for
MaterialFlagScannerTest, MaterialFlagPresetScannerTest, RefOutputRendererTest,
GtceuBackendTest, and RefsTest all completed with exit code 0. The
parameterized GenerateRefs main run point also completed with exit code 0
against the GTCEu 7.5.3 sources JAR, producing MaterialFlagsRef requirements
for all scanned flags and flattened STD_METAL/EXT_METAL/EXT2_METAL preset
members. MaterialFlagMetadataIntegrationTest consumed those generated lookup
tables and completed with exit code 0.

Phase 2B verification result: MaterialValidatorTest completed with exit code 0
for all 12 tests after IDEA reformat. It covers identity error accumulation,
effective ingot/gem conflicts, fluid key and primary-key checks, direct and
recursive flag requirements, preset-only authored flags, unknown metadata, and
the generated production lookup adapter. MaterialValuesTest and
MaterialContentTest were rerun and completed with exit code 0. Every valid
validator case asserts reference identity with the input and induced properties
remain absent from the authored ADT.

Verify: run MaterialValidatorTest through IDEA MCP. Assert the returned
NewMaterialSpec equals the input for every valid case.

## Phase 3 - Planner + Renderer

Goal: authored ADT -> Scala source in canonical order.

Status: complete on 2026-07-16.

1. MaterialPlanner: validated MaterialSet + MaterialOutputSpec -> MaterialPlan
   (builder step ordering per the 15-step canonical order in the design doc,
   deterministic exact-owner imports, generated object naming).
2. MaterialRenderer: MaterialPlan -> ScalaCode. Reuse or fork the ScalaCode
   helper from symbolgen/render.
3. Dust-settings rule: when ingot/gem/polymer/wood is present, planner folds
   DustPropertySpec settings into the applicable builder overload and emits no
   separate dust call. GTCEu 7.5.3 ignores polymer(harvest,burn)'s burnTime
   argument, so polymer burn time uses a separate burnTime(...) call. Bare dust
   emits dust(...); other burn-only settings also use burnTime(...).
4. Fluid/ore overload rule: keep LIQUID/GAS/PLASMA and temperature shorthands;
   use FluidBuilder only for authored builder content. Keep ore multiplier and
   washed amount defaults inside GTCEu by selecting the no-default overloads.
5. Builder-owned rule: formula uses `.formula(...)` and is applied internally by
   `buildAndRegister()`; the renderer does not emit a duplicate `setFormula`.
   Primary fluid key remains a separate `PropertyKey.FLUID` statement because
   no builder setter exists.
6. MaterialPatchRenderer: PatchOperation -> PostMaterialEvent handler body.
7. MarkerMaterialRenderer: emit new MarkerMaterial(...), never Material.Builder.
8. Tests:
   - planner ordering and overload selection for dust carriers, polymer burn,
     fluid shorthand/custom builder, ore defaults, blast stats, and flags
   - golden-file string assertions for a representative NewMaterial
     (polyimide), a MarkerMaterial, and a MaterialPatch
   - negative checks for target states the public builder cannot represent
     without materializing defaults or sentinels

Verify: run renderer golden tests through IDEA MCP.

Verification result: MaterialPlannerTest completed with exit code 0 for all 4
tests, covering dust-carrier selection, polymer burn handling, exact built-in
fluid-key matching, fluid/ore overloads, canonical ordering, no inferred calls,
and multiple preset calls. MaterialRendererTest completed with exit code 0 for
all 3 golden fixtures: representative new material, marker material, and
post-registration patch. MaterialValidatorTest completed with exit code 0 for
all 13 tests after adding unrenderable fluid-color and blast-stat checks.
MaterialContentTest, MaterialDeclarationsTest, MaterialValuesTest,
MaterialFlagMetadataIntegrationTest, and RefsTest were rerun through IDEA and
completed with exit code 0. Adding golden resources exposed a duplicate default
resource root in the codegenTest source set; `resources.setSrcDirs(...)` now
declares that root once.

## Phase 4 - Codegen Integration

Goal: generated .scala compiles as part of the mod.

Status: complete on 2026-07-16.

### Phase 4A - Shared generator support and fluid-key refs

Goal: symbolgen and material codegen share one transactional source writer, and
material declarations consume a generated ref for GTCEu fluid storage keys.

Status: complete on 2026-07-16.

1. Add a `generatorSupport` source set between `core` and the two generators:

   ```text
   core -> symbolgen
   core -> codegen
   generatorSupport -> symbolgen
   generatorSupport -> codegen
   ```

   `generatorSupport` owns generator infrastructure, not domain data. It may
   perform file I/O; `core` remains a pure ref vocabulary and must not depend on
   it.
2. Move `GeneratedScalaFile` and `GeneratedSourceWriter` into
   `com.pixdane.gregicality.generator`. Both symbolgen and codegen use this
   shared implementation; do not copy the writer.
3. Preserve the writer's transactional replacement, rollback behavior, path
   validation, stale-file cleanup, and unchanged-output no-op semantics. Keep
   the fault-injection seam package-private to `generator` for tests.
4. Add a `fluid-storage-keys` static-field job to `GtceuBackend`:
   - source:
     `com/gregtechceu/gtceu/api/fluids/store/FluidStorageKeys.java`
   - owner:
     `com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys`
   - member type: `FluidStorageKey`
   - output: `FluidStorageKeysRef`
   - value type: `FluidStorageKeyRef`
5. Regenerate refs and verify that `GTRefs` exports
   `FluidStorageKeysRef.*`.

Verify through IDEA MCP run configurations:
`GeneratedSourceWriterTest`, `GtceuBackendTest`, `RefOutputRendererTest`,
`RefsTest`, and the parameterized `GenerateRefs` main run point.

Verification result: the IDEA run points for `GeneratedSourceWriterTest` (6
tests), `GtceuBackendTest`, `RefOutputRendererTest` (6 tests),
`RefAggregateRendererTest` (2 tests), `RefsTest` (10 tests), and
`MaterialRendererTest` (3 tests) completed with exit code 0. The parameterized
`GenerateRefs` main run point completed twice against the GTCEu 7.5.3 sources
JAR. It generated `FluidStorageKeysRef` with GAS, LIQUID, MOLTEN, and PLASMA,
and the real `GTRefs` output exports `FluidStorageKeysRef.*`. The second run
left both generated files' modification times unchanged, exercising the shared
writer's no-op path on production-sized output.

### Phase 4B - Material generation and main-source wiring

Goal: generate the first real migrated material and compile it as mod source.

Status: complete on 2026-07-16.

1. `runCodegen` consumes a directly constructed `MaterialSet`; DSL loading and
   Raw conversion are outside this plan.
2. Write owned sources to
   `build/generated/sources/materials/scala/main/` with the shared
   `GeneratedSourceWriter`.
3. Generate `GCYMaterialsChemistryPolymers` containing the authored Polyimide
   definition and `GCYMaterialsGeneratedIndex`; wire the generated directory
   into `main` and `compileScala`.
4. Replace the temporary hand-written material registration entry with
   `GCYMaterialsGeneratedIndex.registerAll()` and
   `GCYMaterialsGeneratedIndex.patchAll()`.
5. Incremental check: unchanged material input does not rewrite generated
   sources.

The first authored package contains only Polyimide, migrated from the TJFork
registration rather than the synthetic Phase 3 renderer fixture:

- id/field: `polyimide` / `Polyimide`
- polymer with dust harvest level 1
- one LIQUID fluid entry
- color `0xFF7F50`, DULL icon
- components C22, H12, N2, O6
- explicit flag: `GENERATE_PLATE`
- no explicit display name or formula override

`FLAMMABLE`, `NO_SMASHING`, and `DISABLE_DECOMPOSITION` are not authored again
because GTCEu's `PolymerProperty.verifyProperty` adds them. Source inspection
also confirms that GTCEu 7.5.3 no longer declares the historical
`SMELT_INTO_FLUID` flag at all, so codegen does not invent a nonexistent modern
ref for it. Codegen validates the authored result but does not expand it.

Verify: run the generation and compilation Gradle run configurations through
IDEA MCP; generated sources compile.

Verification result: `CodegenTest` completed with exit code 0 for all 3 tests,
covering authored-only Polyimide data, exact material/index golden output, and
unchanged-output timestamps through the real writer. The parameterized codegen
main run point generated both runtime files under
`build/generated/sources/materials/scala/main`. An IDEA Gradle `compileScala`
run configuration then completed with exit code 0 and compiled those generated
files with `GCYMaterials`; a second run reported `runCodegen` and
`compileScala` UP-TO-DATE. `MaterialValidatorTest` (13 tests) and
`MaterialRendererTest` (3 golden tests) were rerun and completed with exit code
0. GTCEu 7.5.3 source inspection confirmed the three PolymerProperty-added
flags and the absence of the historical `SMELT_INTO_FLUID` declaration.

## Phase 5 - Cleanup + Doc Sync

1. Complete in Phase 4B: replace the Codegen.scala hello-world stub with the
   real material generation entrypoint.
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

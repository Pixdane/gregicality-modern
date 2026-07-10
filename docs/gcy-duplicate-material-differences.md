# GCY Duplicate Material Difference Notes

Scope: this compares only the 109 non-nuclear `exists_in_gtceu` rows from
`docs/gcy-material-dedupe.csv`. Nuclear-like rows remain deferred.

Comparison source:

- Original GCY: `/Users/pixdane/Documents/gregicality-TJFork/src/main/java/gregicadditions/GAMaterials.java`
- GTCEu Modern 7.5.3 source jar:
  `com/gregtechceu/gtceu/gtceu-1.20.1/7.5.3/.../gtceu-1.20.1-7.5.3-sources.jar`

Comparison dimensions: registry id, material form, components/formula, generation
flags, element binding, blast/tool/pipe/cable/hazard properties, and visual
color/icon differences.

## Summary

| Result | Count |
|---|---:|
| Duplicate rows checked | 109 |
| Behavior-affecting difference found | 96 |
| Visual-only difference found | 12 |
| No difference found by this pass | 1 |

High-level result: most duplicates are not "same material, same properties".
They should not be blindly skipped without checking recipes that reference the
old GCY field or registry id.

## Must Track Registry Id Changes

These are the duplicates that need recipe-id remapping. Re-registering the old
id would collide or create a second semantic material. Use the GTCEu Modern id
as canonical for all overlapping materials.

| GCY field | Original id | GTCEu Modern id | Note |
|---|---|---|---|
| `Thallium` | `thalium` | `thallium` | Old typo id. |
| `HighOctaneGasoline` | `high_octane` | `high_octane_gasoline` | Same field match, different registry id. |
| `EthylTertButylEther` | `ethyl_tert_butyl_ether` | `ethyl_tertbutyl_ether` | GTM drops the second underscore. |
| `PhthalicAcid` | `phthalicacid` | `phthalic_acid` | Old compact id. |
| `Naphthalene` | `naphtalene` | `naphthalene` | Old misspelling. |
| `QuartzSand` | `sand` | `quartz_sand` | Old id is generic. |
| `IncoloyMA956` | `incoloy_ma` | `incoloy_ma_956` | GCYM/GTM material is more specific. |
| `Zeron100` | `zeron` | `zeron_100` | GCYM/GTM material is more specific. |
| `SamariumMagnetic` | `samarium_magnetic` | `magnetic_samarium` | GTM uses magnetic-prefix naming. |

## Form Changed Or Missing

Original GCY often registered late elements as `IngotMaterial`; GTCEu Modern has
many of the same element ids only as element/color entries with no generated
ingot/dust/fluid form. If old recipes use item forms for these, they need an
explicit decision instead of relying on the GTM material as-is.

Element-like old ingots that are GTM element-only:

`Zirconium`, `Polonium`, `Copernicium`, `Francium`, `Radium`, `Actinium`,
`Hafnium`, `Rhenium`, `Technetium`, `Thallium`, `Germanium`, `Selenium`,
`Astatine`, `Rutherfordium`, `Dubnium`, `Seaborgium`, `Bohrium`, `Tennessine`,
`Livermorium`, `Moscovium`, `Nihonium`, `Roentgenium`, `Meitnerium`.

Other form mismatches:

| Material | Original | GTCEu Modern | Note |
|---|---|---|---|
| `AmmoniumChloride` | fluid | dust | Old registered it as a fluid material. |
| `Polybenzimidazole` | ingot/polymer item | liquid plus tool/pipe properties | GTM treats it as polymer/liquid material surface. |
| `PotassiumHydroxide` | simple fluid | dust | Recipes must be checked for fluid vs dust use. |
| `TantalumCarbide` | simple dust | ingot plus fluid | GTM has a fuller material, not just a dust. |
| `Xenon`, `Neon`, `Krypton` | old fluid material with fluid-block/plasma-style flags | gas | GTM treats them as gas-state elements. |

## Components Differ

These have components on both sides, but the composition is materially different.
Migration decision: use GTCEu Modern as canonical and do not re-register the GCY
material. Recipe-level behavior can still be reviewed later when designing each
production line.

| Material | Difference |
|---|---|
| `AquaRegia` | GCY uses `NitricAcid:1 + HydrochloricAcid:1`; GTM uses `NitricAcid:1 + HydrochloricAcid:2`. |
| `AmmoniumChloride` | GCY elemental `N/H/Cl`; GTM compound route `Ammonia + HydrochloricAcid`. |
| `RhodiumSulfate` | GCY `Rhodium + Sulfur + Oxygen + RareEarth`; GTM `Rhodium:2 + Sulfur:3 + Oxygen:12`. |
| `QuartzSand` | GCY `NetherQuartz + RareEarth`; GTM `CertusQuartz + Quartzite`. |
| `IrMetalResidue` | GCY `Iridium/Oxygen/SiliconDioxide/Gold`; GTM `IridiumChloride + PlatinumSludgeResidue`. |
| `Pyrochlore` | GCY uses a large rare-earth/Th/U-bearing mix; GTM uses compact `Ca/Nb/O/F`. Nuclear-bearing pieces stay deferred, but this duplicate itself differs. |
| `Potin` | GCY `Lead + Bronze + Tin`; GTM `Copper + Tin + Lead`. |
| `IncoloyMA956` | GCY `Iron/Aluminium/Chrome/Yttrium`; GTM `VanadiumSteel/Manganese/Aluminium/Yttrium`. |
| `Zeron100` | GCY `Chrome/Nickel/Molybdenum/Copper/Tungsten/Steel`; GTM `Iron/Nickel/Tungsten/Niobium/Cobalt`. |

Near-equivalent naming differences:

- `ChromiumTrioxide`: GCY uses `Chrome`; GTM uses `Chromium`.
- `PotassiumDichromate`: GCY uses `Chrome`; GTM uses `Chromium`.

Migration decision: treat this as a naming/API migration from old GTCE `Chrome`
to GTCEu Modern `Chromium`, not as a separate material fork. Use GTM naming in
new components and recipe code.

## Old Components Dropped In GTM

These GCY fluid materials have legacy placeholder components, usually including
`RareEarth`, while GTM treats them as unknown-composition or hazard/flammability
materials. Keep the GTM version unless a GCY recipe specifically depends on the
old decomposition/formula surface.

`FishOil`, `RawGrowthMedium`, `SterileGrowthMedium`, `HighOctaneGasoline`,
`Gasoline`, `RawGasoline`, `CoalTar`.

## GTM Has Richer Chemistry

These original `SimpleFluidMaterial` / `SimpleDustMaterial` duplicates mostly
had only color plus display formula. GTM already has components and sometimes
decomposition or hazard flags. Prefer the GTM material unless ported recipes
require the exact old fluid-vs-dust form.

`FluoroantimonicAcid`, `Butyraldehyde`, `Formaldehyde`, `HydrogenCyanide`,
`AceticAnhydride`, `PotassiumHydroxide`, `NitrousOxide`, `Formamide`,
`Aminophenol`, `Iron2Chloride`, `AntimonyTrifluoride`, `CalciumHydroxide`,
`PotassiumCyanide`, `SodiumNitrite`, `BariumSulfide`, `PotassiumCarbonate`,
`LithiumChloride`, `PotassiumSulfate`, `Biphenyl`, `SodiumBicarbonate`,
`PotassiumIodide`, `OsmiumTetroxide`, `TantalumCarbide`,
`PotassiumFerrocyanide`, `PrussianBlue`, `ZincSulfide`, `SamariumMagnetic`.

Formula-only or display-formula-only differences:

`NaquadriaSolution`, `RubySlurry`, `SapphireSlurry`, `GreenSapphireSlurry`,
`Agar`, plus GTM-only formulas on `Diaminobenzidine` and `Dichlorobenzidine`.

Migration decision: use GTM as canonical for all formula-only or
display-formula-only differences. Do not re-register GCY materials only to
preserve old formula text.

## Stats, Flags, Or Special Properties Differ

These need extra attention because they can affect generated parts, machines,
tools, pipes, cables, hazards, or blast processing.

| Material | Important difference |
|---|---|
| `Neutronium` | GCY tool stats are much smaller and has broad metal flags; GTM adds liquid, unbreakable tool stats, rotor stats, fluid pipe properties, and radioactive hazard. |
| `Trinium` | GCY has `GENERATE_ORE` and blast temp `8600`; GTM has no ore flag, blast temp `7200`, spring flag, and ZPM cable properties. |
| `Ruthenium` / `Rhodium` | Blast temperatures align with GTM, but GTM has explicit EBF stats and a narrower/different generated-part surface. |
| `Potin` | GTM adds fluid pipe properties and has different composition/flags. |
| `IncoloyMA956` | Different id, composition, blast temp, and generated-part flags. |
| `RhodiumPlatedPalladium` | GTM adds rotor stats; GCY has tool/blast args and disables autogenerated mixer/decomposition. |
| `Ruridit` | Same blast temp but different generated flags and GTM EBF stats. |
| `Polybenzimidazole` | GTM adds tool and fluid pipe properties while changing old ingot-like surface to liquid/polymer behavior. |
| `Zeron100` | Different id, composition, blast temp, and generated-part flags. |
| `SamariumMagnetic` | Different id; GTM uses `magnetic_samarium`, `IS_MAGNETIC`, and explicit magnetic reverse-link style behavior. |
| `Polonium`, `Copernicium`, `Radium`, `Technetium` | GTM uses radioactive icon/hazard comments or no generated form; GCY had old ingot/fluid flags. Do not pre-add forms during material dedupe; handle required item/fluid forms when migrating concrete recipes. |

Legacy flag-only differences:

`Xenon`, `Neon`, `Krypton`, `Octane`, `Meat`, `RutheniumTetroxide`,
`IndiumPhospide`.

Most of these are old `NO_RECYCLING`, `GENERATE_FLUID_BLOCK`,
`GENERATE_PLASMA`, or `DISABLE_DECOMPOSITION` differences. They should be
reviewed only when generated recipes or fluid blocks matter.

## Visual-Only Differences

No behavior-affecting difference was found in this pass, but color/icon differs:

`Bromine`, `Iodine`, `AcidicOsmiumSolution`, `Massicot`,
`AntimonyTrioxide`, `Zincite`, `CobaltOxide`, `ArsenicTrioxide`,
`Ferrosilite`, `CalciumChloride`, `IridiumChloride`, `TungsticAcid`.

## No Difference Found

`Nitrochlorobenzene` matched closely enough in this pass.

## Migration Guidance

Do not add a new material for a duplicate because of an old GCY id. Old ids are
always remapped to GTCEu Modern ids for overlapping materials.

Only consider adding material properties/forms for a duplicate when one of these
is true:

- old GCY recipes require an item/fluid form that GTM does not generate;
- old behavior depends on stats, pipe/cable/tool properties, ore generation, or
  decomposition flags not present in GTM;
- the project intentionally chooses GCY chemistry over GTM chemistry for a
  specific material.

Otherwise, prefer GTCEu Modern's existing material and port recipes to that
canonical id/field. Old GCY ids are migration inputs only, not runtime material
ids.

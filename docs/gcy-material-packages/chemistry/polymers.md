# GCY Material Package: chemistry/polymers

Polymers, resins, rubbers, foams, fibers, and polymer precursors/intermediates.

This package table excludes materials whose `status` is `exists_in_gtceu`; those remain only in the master dedupe CSV/id map.

Source: `docs/gcy-material-dedupe.csv`, `migration_package=chemistry/polymers`.

| GCY Field | Old Id | Modern/Canonical Id | Status | Source Category | Source Section | Note |
|---|---|---|---|---|---|---|
| `FluorinatedEthylenePropylene` | `fluorinated_ethylene_propylene` | `fluorinated_ethylene_propylene` | `new_candidate` | `Ingot / Alloy Materials` | `INGOT MATERIALS` |  |
| `Polyetheretherketone` | `polyetheretherketone` | `polyetheretherketone` | `new_candidate` | `Ingot / Alloy Materials` | `INGOT MATERIALS` |  |
| `Polyimide` | `polyimide` | `polyimide` | `new_candidate` | `Ingot / Alloy Materials` | `INGOT MATERIALS` |  |
| `Polyurethane` | `polyurethane` | `polyurethane` | `new_candidate` | `Ingot / Alloy Materials` | `INGOT MATERIALS` |  |
| `Zylon` | `zylon` | `zylon` | `new_candidate` | `Ingot / Alloy Materials` | `INGOT MATERIALS` |  |
| `AcrylicFibers` | `acrylic_fibers` | `acrylic_fibers` | `new_candidate` | `Simple Dust Materials` | `SIMPLE DUSTS` |  |
| `PolycyclicAromaticMix` | `polycyclic_aromatic_mix` | `polycyclic_aromatic_mix` | `new_candidate` | `Simple Dust Materials` | `SIMPLE DUSTS` |  |
| `PolystyreneNanoParticles` | `polystryrene_nanoparticles` | `polystryrene_nanoparticles` | `new_candidate` | `Simple Dust Materials` | `SIMPLE DUSTS` |  |
| `PreZylon` | `pre_zylon` | `pre_zylon` | `new_candidate` | `Simple Dust Materials` | `SIMPLE DUSTS` |  |
| `AcidifiedPolyphenolMix` | `acidified_polyphenol_mix` | `acidified_polyphenol_mix` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `AcryloNitrile` | `acrylonitrile` | `acrylonitrile` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `PolyacrylonitrileSolution` | `polyacrylonitrile_solution` | `polyacrylonitrile_solution` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `PolyamicAcid` | `polyamic_acid` | `polyamic_acid` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `PolyphenolMix` | `polyphenol_mix` | `polyphenol_mix` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `Resin` | `resin` | `resin` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `ViscoelasticPolyurethane` | `viscoelastic_polyurethane` | `viscoelastic_polyurethane` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |
| `ViscoelasticPolyurethaneFoam` | `viscoelastic_polyurethane_foam` | `viscoelastic_polyurethane_foam` | `new_candidate` | `Simple Fluid Materials` | `SIMPLE FLUID MATERIALS` |  |

## Implemented Slice

`Polyimide` is the first material generated from the authored material ADT.
Its migration source is the TJFork `GAMaterials` registration:

- registry id `polyimide`, generated field `Polyimide`
- polymer property with dust harvest level 1 and one LIQUID fluid
- material color `0xFF7F50` and DULL icon set
- components C22, H12, N2, O6
- explicitly authored `GENERATE_PLATE`

The historical flags `FLAMMABLE`, `NO_SMASHING`, and
`DISABLE_DECOMPOSITION` are omitted from authored data because modern GTCEu's
`PolymerProperty.verifyProperty` adds them internally. GTCEu 7.5.3 no longer
declares the historical `SMELT_INTO_FLUID` flag, so the migration does not
invent a replacement ref. The generated registration is owned by
`GCYMaterialsChemistryPolymers`; runtime registration and patch calls are
routed through `GCYMaterialsGeneratedIndex`.

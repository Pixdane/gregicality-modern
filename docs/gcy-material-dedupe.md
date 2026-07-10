# GCY Material Dedupe Against GTCEu Modern 7.5.3

Scope: nuclear content is intentionally deferred. This excludes the original `NUCLEAR MATERIALS` section, `RadioactiveMaterial` / `IsotopeMaterial`, and obvious uranium/thorium/depleted/fuel-style simple intermediates. Radioactive elements that GTCEu Modern already treats as normal element materials are still compared when they are in the original `NEW ELEMENTS` block, unless they matched the nuclear-like defer rule.

Comparison keys: first by material registry id string when present, then by Java field name. `exists_in_gtceu` means GTCEu Modern already has a material with that id or field in `GTMaterials` / split material files, including built-in `GCYMMaterials`. The `GCY Id` column keeps the original id; when an overlapping material has an id mismatch, the `Reason` column records the GTM canonical id as `old-id->...`.

The CSV companion file also contains `migration_package`, a first-pass implementation routing category for every listed material. See `docs/gcy-material-packages.md` for the complete package index.

## Summary

| Bucket | Total | Exists in GTCEu | New Candidate | Deferred |
|---|---:|---:|---:|---:|
| Elements | 35 | 32 | 3 | 0 |
| Fluid Materials | 51 | 22 | 29 | 0 |
| Dust Materials | 82 | 17 | 65 | 0 |
| Gem Materials | 6 | 0 | 6 | 0 |
| Ingot / Alloy Materials | 93 | 7 | 86 | 0 |
| Simple Fluid Materials | 525 | 14 | 511 | 0 |
| Simple Dust Materials | 434 | 17 | 417 | 0 |
| Markers | 5 | 0 | 5 | 0 |
| Deferred Nuclear-Like | 101 | 0 | 0 | 101 |
| **Total** | **1332** | **109** | **1122** | **101** |

## Existing In GTCEu

### Elements (32)

| GCY Field | GCY Id | Type | GTCEu Match | Reason | Source Line |
|---|---|---|---|---|---:|
| `Actinium` | `actinium` | `IngotMaterial` | `Actinium` | `id->Actinium, field` | 91 |
| `Astatine` | `astatine` | `IngotMaterial` | `Astatine` | `id->Astatine, field` | 100 |
| `Bohrium` | `bohrium` | `IngotMaterial` | `Bohrium` | `id->Bohrium, field` | 104 |
| `Bromine` | `bromine` | `FluidMaterial` | `Bromine` | `id->Bromine, field` | 98 |
| `Copernicium` | `copernicium` | `IngotMaterial` | `Copernicium` | `id->Copernicium, field` | 78 |
| `Dubnium` | `dubnium` | `IngotMaterial` | `Dubnium` | `id->Dubnium, field` | 102 |
| `Francium` | `francium` | `IngotMaterial` | `Francium` | `id->Francium, field` | 89 |
| `Germanium` | `germanium` | `IngotMaterial` | `Germanium` | `id->Germanium, field` | 96 |
| `Hafnium` | `hafnium` | `IngotMaterial` | `Hafnium` | `id->Hafnium, field` | 92 |
| `Iodine` | `iodine` | `DustMaterial` | `Iodine` | `id->Iodine, field` | 99 |
| `Krypton` | `krypton` | `FluidMaterial` | `Krypton` | `id->Krypton, field` | 75 |
| `Livermorium` | `livermorium` | `IngotMaterial` | `Livermorium` | `id->Livermorium, field` | 106 |
| `Meitnerium` | `meitnerium` | `IngotMaterial` | `Meitnerium` | `id->Meitnerium, field` | 110 |
| `Moscovium` | `moscovium` | `IngotMaterial` | `Moscovium` | `id->Moscovium, field` | 107 |
| `Neon` | `neon` | `FluidMaterial` | `Neon` | `id->Neon, field` | 74 |
| `Neutronium` | `neutronium` | `IngotMaterial` | `Neutronium` | `id->Neutronium, field` | 63 |
| `Nihonium` | `nihonium` | `IngotMaterial` | `Nihonium` | `id->Nihonium, field` | 108 |
| `Polonium` | `polonium` | `IngotMaterial` | `Polonium` | `id->Polonium, field` | 77 |
| `Radium` | `radium` | `IngotMaterial` | `Radium` | `id->Radium, field` | 90 |
| `Rhenium` | `rhenium` | `IngotMaterial` | `Rhenium` | `id->Rhenium, field` | 93 |
| `Rhodium` | `rhodium` | `IngotMaterial` | `Rhodium` | `id->Rhodium, field` | 65 |
| `Roentgenium` | `roentgenium` | `IngotMaterial` | `Roentgenium` | `id->Roentgenium, field` | 109 |
| `Ruthenium` | `ruthenium` | `IngotMaterial` | `Ruthenium` | `id->Ruthenium, field` | 64 |
| `Rutherfordium` | `rutherfordium` | `IngotMaterial` | `Rutherfordium` | `id->Rutherfordium, field` | 101 |
| `Seaborgium` | `seaborgium` | `IngotMaterial` | `Seaborgium` | `id->Seaborgium, field` | 103 |
| `Selenium` | `selenium` | `IngotMaterial` | `Selenium` | `id->Selenium, field` | 97 |
| `Technetium` | `technetium` | `IngotMaterial` | `Technetium` | `id->Technetium, field` | 94 |
| `Tennessine` | `tennessine` | `IngotMaterial` | `Tennessine` | `id->Tennessine, field` | 105 |
| `Thallium` | `thalium` | `IngotMaterial` | `Thallium` | `field, old-id->thallium` | 95 |
| `Trinium` | `trinium` | `IngotMaterial` | `Trinium` | `id->Trinium, field` | 66 |
| `Xenon` | `xenon` | `FluidMaterial` | `Xenon` | `id->Xenon, field` | 73 |
| `Zirconium` | `zirconium` | `IngotMaterial` | `Zirconium` | `id->Zirconium, field` | 76 |

### Fluid Materials (22)

| GCY Field | GCY Id | Type | GTCEu Match | Reason | Source Line |
|---|---|---|---|---|---:|
| `AcidicOsmiumSolution` | `acidic_osmium_solution` | `FluidMaterial` | `AcidicOsmiumSolution` | `id->AcidicOsmiumSolution, field` | 234 |
| `AmmoniumChloride` | `ammonium_chloride` | `FluidMaterial` | `AmmoniumChloride` | `id->AmmoniumChloride, field` | 225 |
| `AquaRegia` | `aqua_regia` | `FluidMaterial` | `AquaRegia` | `id->AquaRegia, field` | 224 |
| `Chlorobenzene` | `chlorobenzene` | `FluidMaterial` | `Chlorobenzene` | `id->Chlorobenzene, field` | 243 |
| `CoalTar` | `coal_tar` | `FluidMaterial` | `CoalTar` | `id->CoalTar, field` | 203 |
| `Diaminobenzidine` | `diaminobenzidine` | `FluidMaterial` | `Diaminobenzidine` | `id->Diaminobenzidine, field` | 241 |
| `Dichlorobenzidine` | `dichlorobenzidine` | `FluidMaterial` | `Dichlorobenzidine` | `id->Dichlorobenzidine, field` | 240 |
| `EthylBenzene` | `ethylbenzene` | `FluidMaterial` | `Ethylbenzene` | `id->Ethylbenzene` | 208 |
| `EthylTertButylEther` | `ethyl_tert_butyl_ether` | `FluidMaterial` | `EthylTertButylEther` | `field, old-id->ethyl_tertbutyl_ether` | 198 |
| `FishOil` | `fish_oil` | `FluidMaterial` | `FishOil` | `id->FishOil, field` | 189 |
| `FormicAcid` | `formic_acid` | `FluidMaterial` | `FormicAcid` | `id->FormicAcid, field` | 228 |
| `Gasoline` | `gasoline` | `FluidMaterial` | `Gasoline` | `id->Gasoline, field` | 199 |
| `HighOctaneGasoline` | `high_octane` | `FluidMaterial` | `HighOctaneGasoline` | `field, old-id->high_octane_gasoline` | 196 |
| `HydrogenPeroxide` | `hydrogen_peroxide` | `FluidMaterial` | `HydrogenPeroxide` | `id->HydrogenPeroxide, field` | 211 |
| `Naphthalene` | `naphtalene` | `FluidMaterial` | `Naphthalene` | `field, old-id->naphthalene` | 215 |
| `Nitrochlorobenzene` | `nitrochlorobenzene` | `FluidMaterial` | `Nitrochlorobenzene` | `id->Nitrochlorobenzene, field` | 239 |
| `Octane` | `octane` | `FluidMaterial` | `Octane` | `id->Octane, field` | 197 |
| `PhthalicAcid` | `phthalicacid` | `FluidMaterial` | `PhthalicAcid` | `field, old-id->phthalic_acid` | 214 |
| `RawGasoline` | `raw_gasoline` | `FluidMaterial` | `RawGasoline` | `id->RawGasoline, field` | 200 |
| `RawGrowthMedium` | `raw_growth_medium` | `FluidMaterial` | `RawGrowthMedium` | `id->RawGrowthMedium, field` | 190 |
| `RhodiumSulfate` | `rhodium_sulfate` | `FluidMaterial` | `RhodiumSulfate` | `id->RhodiumSulfate, field` | 229 |
| `SterileGrowthMedium` | `sterilized_growth_medium` | `FluidMaterial` | `SterileGrowthMedium` | `id->SterileGrowthMedium, field` | 191 |

### Dust Materials (17)

| GCY Field | GCY Id | Type | GTCEu Match | Reason | Source Line |
|---|---|---|---|---|---:|
| `AntimonyTrioxide` | `antimony_trioxide` | `DustMaterial` | `AntimonyTrioxide` | `id->AntimonyTrioxide, field` | 271 |
| `ArsenicTrioxide` | `arsenic_trioxide` | `DustMaterial` | `ArsenicTrioxide` | `id->ArsenicTrioxide, field` | 274 |
| `CalciumChloride` | `calcium_chloride` | `DustMaterial` | `CalciumChloride` | `id->CalciumChloride, field` | 297 |
| `ChromiumTrioxide` | `chromium_trioxide` | `DustMaterial` | `ChromiumTrioxide` | `id->ChromiumTrioxide, field` | 312 |
| `CobaltOxide` | `cobalt_oxide` | `DustMaterial` | `CobaltOxide` | `id->CobaltOxide, field` | 273 |
| `Ferrosilite` | `ferrosilite` | `DustMaterial` | `Ferrosilite` | `id->Ferrosilite, field` | 276 |
| `IndiumPhospide` | `indium_phosphide` | `DustMaterial` | `IndiumPhosphide` | `id->IndiumPhosphide` | 341 |
| `IridiumChloride` | `iridium_chloride` | `DustMaterial` | `IridiumChloride` | `id->IridiumChloride, field` | 303 |
| `IrMetalResidue` | `iridium_metal_residue` | `DustMaterial` | `IridiumMetalResidue` | `id->IridiumMetalResidue` | 301 |
| `Massicot` | `massicot` | `DustMaterial` | `Massicot` | `id->Massicot, field` | 270 |
| `Meat` | `meat` | `DustMaterial` | `Meat` | `id->Meat, field` | 264 |
| `PotassiumDichromate` | `potassium_dichromate` | `DustMaterial` | `PotassiumDichromate` | `id->PotassiumDichromate, field` | 313 |
| `Pyrochlore` | `pyrochlore` | `DustMaterial` | `Pyrochlore` | `id->Pyrochlore, field` | 336 |
| `QuartzSand` | `sand` | `DustMaterial` | `QuartzSand` | `field, old-id->quartz_sand` | 269 |
| `RutheniumTetroxide` | `ruthenium_tetroxide` | `DustMaterial` | `RutheniumTetroxide` | `id->RutheniumTetroxide, field` | 299 |
| `TungsticAcid` | `tungstic_acid` | `DustMaterial` | `TungsticAcid` | `id->TungsticAcid, field` | 326 |
| `Zincite` | `zincite` | `DustMaterial` | `Zincite` | `id->Zincite, field` | 272 |

### Ingot / Alloy Materials (7)

| GCY Field | GCY Id | Type | GTCEu Match | Reason | Source Line |
|---|---|---|---|---|---:|
| `IncoloyMA956` | `incoloy_ma` | `IngotMaterial` | `IncoloyMA956` | `field, old-id->incoloy_ma_956` | 389 |
| `Polybenzimidazole` | `polybenzimidazole` | `IngotMaterial` | `Polybenzimidazole` | `id->Polybenzimidazole, field` | 393 |
| `Potin` | `potin` | `IngotMaterial` | `Potin` | `id->Potin, field` | 367 |
| `RhodiumPlatedPalladium` | `rhodium_plated_palladium` | `IngotMaterial` | `RhodiumPlatedPalladium` | `id->RhodiumPlatedPalladium, field` | 391 |
| `Ruridit` | `ruridit` | `IngotMaterial` | `Ruridit` | `id->Ruridit, field` | 392 |
| `SamariumMagnetic` | `samarium_magnetic` | `IngotMaterial` | `SamariumMagnetic` | `field, old-id->magnetic_samarium` | 1459 |
| `Zeron100` | `zeron` | `IngotMaterial` | `Zeron100` | `field, old-id->zeron_100` | 415 |

### Simple Fluid Materials (14)

| GCY Field | GCY Id | Type | GTCEu Match | Reason | Source Line |
|---|---|---|---|---|---:|
| `AceticAnhydride` | `acetic_anhydride` | `SimpleFluidMaterial` | `AceticAnhydride` | `id->AceticAnhydride, field` | 601 |
| `Aminophenol` | `aminophenol` | `SimpleFluidMaterial` | `AminoPhenol` | `id->AminoPhenol` | 789 |
| `Butyraldehyde` | `butyraldehyde` | `SimpleFluidMaterial` | `Butyraldehyde` | `id->Butyraldehyde, field` | 521 |
| `FluoroantimonicAcid` | `fluoroantimonic_acid` | `SimpleFluidMaterial` | `FluoroantimonicAcid` | `id->FluoroantimonicAcid, field` | 457 |
| `Formaldehyde` | `formaldehyde` | `SimpleFluidMaterial` | `Formaldehyde` | `id->Formaldehyde, field` | 533 |
| `Formamide` | `formamide` | `SimpleFluidMaterial` | `Formamide` | `id->Formamide, field` | 691 |
| `GreenSapphireSlurry` | `green_sapphire_slurry` | `SimpleFluidMaterial` | `GreenSapphireSlurry` | `id->GreenSapphireSlurry, field` | 763 |
| `HydrogenCyanide` | `hydrogen_cyanide` | `SimpleFluidMaterial` | `HydrogenCyanide` | `id->HydrogenCyanide, field` | 580 |
| `Iron2Chloride` | `iron_ii_chloride` | `SimpleFluidMaterial` | `Iron2Chloride` | `id->Iron2Chloride, field` | 923 |
| `NaquadriaSolution` | `naquadria_solution` | `SimpleFluidMaterial` | `NaquadriaSolution` | `id->NaquadriaSolution, field` | 512 |
| `NitrousOxide` | `nitrous_oxide` | `SimpleFluidMaterial` | `NitrousOxide` | `id->NitrousOxide, field` | 685 |
| `PotassiumHydroxide` | `potassium_hydroxide` | `SimpleFluidMaterial` | `PotassiumHydroxide` | `id->PotassiumHydroxide, field` | 620 |
| `RubySlurry` | `ruby_slurry` | `SimpleFluidMaterial` | `RubySlurry` | `id->RubySlurry, field` | 761 |
| `SapphireSlurry` | `sapphire_slurry` | `SimpleFluidMaterial` | `SapphireSlurry` | `id->SapphireSlurry, field` | 762 |

### Simple Dust Materials (17)

| GCY Field | GCY Id | Type | GTCEu Match | Reason | Source Line |
|---|---|---|---|---|---:|
| `Agar` | `agar` | `SimpleDustMaterial` | `Agar` | `id->Agar, field` | 1049 |
| `AntimonyTrifluoride` | `antimony_trifluoride` | `SimpleDustMaterial` | `AntimonyTrifluoride` | `id->AntimonyTrifluoride, field` | 1010 |
| `BariumSulfide` | `barium_sulfide` | `SimpleDustMaterial` | `BariumSulfide` | `id->BariumSulfide, field` | 1102 |
| `Biphenyl` | `biphenyl` | `SimpleDustMaterial` | `Biphenyl` | `id->Biphenyl, field` | 1167 |
| `CalciumHydroxide` | `calcium_hydroxide` | `SimpleDustMaterial` | `CalciumHydroxide` | `id->CalciumHydroxide, field` | 1034 |
| `LithiumChloride` | `lithium_chloride` | `SimpleDustMaterial` | `LithiumChloride` | `id->LithiumChloride, field` | 1120 |
| `OsmiumTetroxide` | `osmium_tetroxide` | `SimpleDustMaterial` | `OsmiumTetroxide` | `id->OsmiumTetroxide, field` | 1245 |
| `PotassiumCarbonate` | `potassium_carbonate` | `SimpleDustMaterial` | `PotassiumCarbonate` | `id->PotassiumCarbonate, field` | 1114 |
| `PotassiumCyanide` | `potassium_cyanide` | `SimpleDustMaterial` | `PotassiumCyanide` | `id->PotassiumCyanide, field` | 1072 |
| `PotassiumFerrocyanide` | `potassium_ferrocyanide` | `SimpleDustMaterial` | `PotassiumFerrocyanide` | `id->PotassiumFerrocyanide, field` | 1289 |
| `PotassiumIodide` | `potassium_iodide` | `SimpleDustMaterial` | `PotassiumIodide` | `id->PotassiumIodide, field` | 1220 |
| `PotassiumSulfate` | `potassium_sulfate` | `SimpleDustMaterial` | `PotassiumSulfate` | `id->PotassiumSulfate, field` | 1123 |
| `PrussianBlue` | `prussian_blue` | `SimpleDustMaterial` | `PrussianBlue` | `id->PrussianBlue, field` | 1290 |
| `SodiumBicarbonate` | `sodium_bicarbonate` | `SimpleDustMaterial` | `SodiumBicarbonate` | `id->SodiumBicarbonate, field` | 1201 |
| `SodiumNitrite` | `sodium_nitrite` | `SimpleDustMaterial` | `SodiumNitrite` | `id->SodiumNitrite, field` | 1085 |
| `TantalumCarbide` | `tantalum_carbide` | `SimpleDustMaterial` | `TantalumCarbide` | `id->TantalumCarbide, field` | 1261 |
| `ZincSulfide` | `zinc_sulfide` | `SimpleDustMaterial` | `ZincSulfide` | `id->ZincSulfide, field` | 1319 |

## New Non-Nuclear Candidates

### Elements (3)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `Adamantium` | `adamantium` | `IngotMaterial` | 67 |
| `Taranium` | `taranium` | `IngotMaterial` | 69 |
| `Vibranium` | `vibranium` | `IngotMaterial` | 68 |

### Fluid Materials (29)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `AcidicIridiumSolution` | `acidic_iridium_solution` | `FluidMaterial` | 236 |
| `Anthracene` | `anthracene` | `FluidMaterial` | 206 |
| `CoalTarOil` | `coal_tar_oil` | `FluidMaterial` | 204 |
| `Diphenylisophtalate` | `diphenylisophtalate` | `FluidMaterial` | 242 |
| `EthylAnthraHydroQuinone` | `ethylanthrahydroquinone` | `FluidMaterial` | 213 |
| `EthylAnthraQuinone` | `ethylanthraquinone` | `FluidMaterial` | 212 |
| `FermentationBase` | `fermentation_base` | `FluidMaterial` | 221 |
| `HighPressureSteam` | `high_pressure_steam` | `FluidMaterial` | 195 |
| `HotRutheniumTetroxideSolution` | `hot_ruthenium_tetroxide_solution` | `FluidMaterial` | 232 |
| `Hydrazine` | `hydrazine` | `FluidMaterial` | 210 |
| `IodizedOil` | `iodized_oil` | `FluidMaterial` | 245 |
| `IronChloride` | `iron_chloride` | `FluidMaterial` | 194 |
| `Kerosene` | `kerosene` | `FluidMaterial` | 207 |
| `LiquidHydrogen` | `liquid_hydrogen` | `FluidMaterial` | 222 |
| `LiquidOxygen` | `liquid_oxygen` | `FluidMaterial` | 220 |
| `MonoMethylHydrazine` | `monomethylhydrazine` | `FluidMaterial` | 209 |
| `NeutralMatter` | `neutral_matter` | `FluidMaterial` | 192 |
| `OsmiumSolution` | `osmium_solution` | `FluidMaterial` | 233 |
| `PalladiumAmmonia` | `palladium_enriched_ammonia` | `FluidMaterial` | 226 |
| `PlatinumConcentrate` | `platinum_concentrate` | `FluidMaterial` | 223 |
| `PositiveMatter` | `positive_matter` | `FluidMaterial` | 193 |
| `RhodiumFilterCakeSolution` | `rhodium_filter_cake_solution` | `FluidMaterial` | 238 |
| `RhodiumSaltSolution` | `rhodium_salt_solution` | `FluidMaterial` | 237 |
| `RhodiumSulfateSolution` | `rhodium_sulfate_solution` | `FluidMaterial` | 230 |
| `RP1` | `rp` | `FluidMaterial` | 219 |
| `RutheniumTetroxideSolution` | `ruthenium_tetroxide_solution` | `FluidMaterial` | 231 |
| `SodiumFormate` | `sodium_formate` | `FluidMaterial` | 227 |
| `SodiumTungstate` | `sodium_tungstate` | `FluidMaterial` | 244 |
| `SulfuricCoalTarOil` | `sulfuric_coal_tar_oil` | `FluidMaterial` | 205 |

### Dust Materials (65)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `AluminoSilicateWool` | `alumino_silicate_wool` | `DustMaterial` | 268 |
| `Arsenopyrite` | `arsenopyrite` | `DustMaterial` | 344 |
| `Barytocalcite` | `barytocalcite` | `DustMaterial` | 342 |
| `BismuthTellurite` | `bismuth_tellurite` | `DustMaterial` | 282 |
| `Blizz` | `blizz` | `DustMaterial` | 277 |
| `Bowieite` | `bowieite` | `DustMaterial` | 346 |
| `CalciumTungstate` | `calcium_tungstate` | `DustMaterial` | 325 |
| `Caliche` | `caliche` | `DustMaterial` | 332 |
| `Celestine` | `celestine` | `DustMaterial` | 347 |
| `CircuitCompoundMK3` | `circuit_compound_mkc` | `DustMaterial` | 283 |
| `Columbite` | `columbite` | `DustMaterial` | 335 |
| `CrudeRhodiumMetal` | `crude_rhodium_metal` | `DustMaterial` | 305 |
| `Cryotheum` | `cryotheum` | `DustMaterial` | 279 |
| `Dibismusthydroborat` | `dibismuthhydroborat` | `DustMaterial` | 281 |
| `EglinSteelBase` | `eglin_steel_base` | `DustMaterial` | 266 |
| `EnrichedNaquadricCompound` | `enriched_naquadric_compound` | `DustMaterial` | 330 |
| `FLiBe` | `flibe` | `DustMaterial` | 323 |
| `FLiNaK` | `flinak` | `DustMaterial` | 322 |
| `Fluorite` | `fluorite` | `DustMaterial` | 334 |
| `FluoroApatite` | `fluoroapatite` | `DustMaterial` | 337 |
| `Gallite` | `gallite` | `DustMaterial` | 345 |
| `IridiumDioxide` | `iridium_dioxide` | `DustMaterial` | 235 |
| `LeachResidue` | `leach_residue` | `DustMaterial` | 296 |
| `LeadNitrate` | `lead_nitrate` | `DustMaterial` | 318 |
| `LuTmYVO` | `lutm_yvo` | `DustMaterial` | 340 |
| `MicaPulp` | `mica_based` | `DustMaterial` | 267 |
| `NaquadriaticCompound` | `naquadriatic_compound` | `DustMaterial` | 331 |
| `NaquadricCompound` | `naquadric_compound` | `DustMaterial` | 329 |
| `NdYAG` | `nd_yag` | `DustMaterial` | 338 |
| `OrganicFertilizer` | `organic_fertilizer` | `DustMaterial` | 324 |
| `PalladiumMetallicPowder` | `palladium_metallic_powder` | `DustMaterial` | 291 |
| `PalladiumRawPowder` | `reprecipitated_palladium` | `DustMaterial` | 292 |
| `PalladiumSalt` | `palladium_salt` | `DustMaterial` | 293 |
| `PGSDResidue` | `sludge_dust_residue` | `DustMaterial` | 302 |
| `PGSDResidue2` | `metallic_sludge_dust_residue` | `DustMaterial` | 304 |
| `PhthalicAnhydride` | `phthalicanhydride` | `DustMaterial` | 280 |
| `PlatinumMetallicPowder` | `platinum_metallic_powder` | `DustMaterial` | 288 |
| `PlatinumRawPowder` | `reprecipitated_platinum` | `DustMaterial` | 290 |
| `PlatinumResidue` | `platinum_residue` | `DustMaterial` | 289 |
| `PlatinumSalt` | `platinum_salt` | `DustMaterial` | 286 |
| `PlatinumSaltRefined` | `refined_platinum_salt` | `DustMaterial` | 287 |
| `PotassiumDisulfate` | `potassium_disulfate` | `DustMaterial` | 295 |
| `PotassiumFluoride` | `potassium_fluoride` | `DustMaterial` | 321 |
| `PotassiumMetabisulfite` | `potassium_metabisulfite` | `DustMaterial` | 317 |
| `PrHoYLF` | `prho_ylf` | `DustMaterial` | 339 |
| `Pyrotheum` | `pyrotheum` | `DustMaterial` | 265 |
| `RarestMetalResidue` | `rarest_metal_residue` | `DustMaterial` | 300 |
| `RhodiumFilterCake` | `rhodium_filter_cake` | `DustMaterial` | 309 |
| `RhodiumNitrate` | `rhodium_nitrate` | `DustMaterial` | 307 |
| `Rhodocrosite` | `rhodocrosite` | `DustMaterial` | 333 |
| `SilverChloride` | `silver_chloride` | `DustMaterial` | 316 |
| `SilverOxide` | `silver_oxide` | `DustMaterial` | 315 |
| `Snow` | `snow` | `DustMaterial` | 278 |
| `SodiumFluoride` | `sodium_fluoride` | `DustMaterial` | 320 |
| `SodiumNitrate` | `sodium_nitrate` | `DustMaterial` | 306 |
| `SodiumPotassiumAlloy` | `sodium_potassium_alloy` | `DustMaterial` | 319 |
| `SodiumRuthenate` | `sodium_ruthenate` | `DustMaterial` | 298 |
| `SodiumSulfate` | `sodium_sulfate` | `DustMaterial` | 294 |
| `Triniite` | `triniite` | `DustMaterial` | 314 |
| `TungstenHexachloride` | `tungsten_hexachloride` | `DustMaterial` | 328 |
| `TungstenTrioxide` | `tungsten_trioxide` | `DustMaterial` | 327 |
| `Witherite` | `witherite` | `DustMaterial` | 343 |
| `YttriumOxide` | `yttrium_oxide` | `DustMaterial` | 284 |
| `ZincSulfate` | `zinc_sulfate` | `DustMaterial` | 308 |
| `Zirkelite` | `zirkelite` | `DustMaterial` | 285 |

### Gem Materials (6)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `CubicZirconia` | `cubic_zirconia` | `GemMaterial` | 350 |
| `LeadZirconateTitanate` | `lead_zirconate_titanate` | `GemMaterial` | 355 |
| `MagnetoResonatic` | `magneto_resonatic` | `GemMaterial` | 352 |
| `Prasiolite` | `prasiolite` | `GemMaterial` | 351 |
| `RhodiumSalt` | `rhodium_salt` | `GemMaterial` | 353 |
| `Zircon` | `zircon` | `GemMaterial` | 354 |

### Ingot / Alloy Materials (86)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `AbyssalAlloy` | `abyssal_alloy` | `IngotMaterial` | 399 |
| `BabbittAlloy` | `babbitt_alloy` | `IngotMaterial` | 387 |
| `BariumTitanate` | `barium_titanate` | `IngotMaterial` | 426 |
| `BerylliumFluoride` | `beryllium_fluoride` | `IngotMaterial` | 397 |
| `BismuthIridiate` | `bismuth_iridiate` | `IngotMaterial` | 429 |
| `BismuthRuthenate` | `bismuth_ruthenate` | `IngotMaterial` | 428 |
| `BlackTitanium` | `black_titanium` | `IngotMaterial` | 409 |
| `CarbonNanotubes` | `carbon_nanotubes` | `IngotMaterial` | 408 |
| `Cinobite` | `cinobite` | `IngotMaterial` | 416 |
| `CosmicNeutronium` | `cosmic_neutronium` | `IngotMaterial` | 439 |
| `EglinSteel` | `eglin_steel` | `IngotMaterial` | 363 |
| `ElectricallyImpureCopper` | `electrically_impure_copper` | `IngotMaterial` | 422 |
| `Enderium` | `enderium` | `IngotMaterial` | 385 |
| `EnrichedNaquadahAlloy` | `enriched_naquadah_alloy` | `IngotMaterial` | 402 |
| `EVSuperconductor` | `ev_superconductor` | `IngotMaterial` | 381 |
| `EVSuperconductorBase` | `ev_superconductor_base` | `IngotMaterial` | 375 |
| `FluorinatedEthylenePropylene` | `fluorinated_ethylene_propylene` | `IngotMaterial` | 442 |
| `FullerenePolymerMatrix` | `fullerene_polymer_matrix` | `IngotMaterial` | 407 |
| `GermaniumTungstenNitride` | `germanium_tungsten_nitride` | `IngotMaterial` | 432 |
| `GoldAlloy` | `gold_alloy` | `IngotMaterial` | 394 |
| `Grisium` | `grisium` | `IngotMaterial` | 364 |
| `HastelloyK243` | `hastelloy_k243` | `IngotMaterial` | 404 |
| `HastelloyN` | `hastelloy_n` | `IngotMaterial` | 369 |
| `HastelloyX78` | `hastelloy_x78` | `IngotMaterial` | 403 |
| `HDCS` | `hdcs` | `IngotMaterial` | 417 |
| `HeavyQuarkDegenerateMatter` | `heavy_quark_degenerate_matter` | `IngotMaterial` | 434 |
| `HG1223` | `hg_alloy` | `IngotMaterial` | 388 |
| `HVSuperconductor` | `hv_superconductor` | `IngotMaterial` | 380 |
| `HVSuperconductorBase` | `hv_superconductor_base` | `IngotMaterial` | 374 |
| `Incoloy813` | `incoloy813` | `IngotMaterial` | 401 |
| `Inconel625` | `inconel_a` | `IngotMaterial` | 365 |
| `Inconel792` | `inconel_b` | `IngotMaterial` | 412 |
| `IVSuperconductor` | `iv_superconductor` | `IngotMaterial` | 382 |
| `IVSuperconductorBase` | `iv_superconductor_base` | `IngotMaterial` | 376 |
| `Lafium` | `lafium` | `IngotMaterial` | 414 |
| `LeadBismuthEutectic` | `lead_bismuth_eutatic` | `IngotMaterial` | 398 |
| `LithiumFluoride` | `lithium_fluoride` | `IngotMaterial` | 396 |
| `LithiumNiobate` | `lithium_niobate` | `IngotMaterial` | 433 |
| `LithiumTitanate` | `lithium_titanate` | `IngotMaterial` | 420 |
| `LuVSuperconductor` | `luv_superconductor` | `IngotMaterial` | 383 |
| `LuVSuperconductorBase` | `luv_superconductor_base` | `IngotMaterial` | 377 |
| `MaragingSteel250` | `maraging_steel_a` | `IngotMaterial` | 366 |
| `MVSuperconductor` | `mv_superconductor` | `IngotMaterial` | 379 |
| `MVSuperconductorBase` | `mv_superconductor_base` | `IngotMaterial` | 373 |
| `NaquadriaticTaranium` | `naquadriatic_taranium` | `IngotMaterial` | 440 |
| `Nitinol60` | `nitinol_a` | `IngotMaterial` | 386 |
| `PEDOT` | `pedot` | `IngotMaterial` | 430 |
| `Periodicium` | `periodicium` | `IngotMaterial` | 438 |
| `Pikyonium` | `pikyonium` | `IngotMaterial` | 413 |
| `Polyetheretherketone` | `polyetheretherketone` | `IngotMaterial` | 405 |
| `Polyimide` | `polyimide` | `IngotMaterial` | 441 |
| `Polyurethane` | `polyurethane` | `IngotMaterial` | 423 |
| `PreciousMetal` | `precious_metal` | `IngotMaterial` | 395 |
| `ProtoAdamantium` | `proto_adamantium` | `IngotMaterial` | 418 |
| `QCDMatter` | `qcd_confined_matter` | `IngotMaterial` | 437 |
| `Quantum` | `quantum` | `IngotMaterial` | 1460 |
| `ReactorSteel` | `reactor_steel` | `IngotMaterial` | 400 |
| `RutheniumDioxide` | `ruthenium_dioxide` | `IngotMaterial` | 431 |
| `Staballoy` | `staballoy` | `IngotMaterial` | 368 |
| `Stellite` | `stellite` | `IngotMaterial` | 371 |
| `SuperheavyHAlloy` | `superheavy_h_alloy` | `IngotMaterial` | 435 |
| `SuperheavyLAlloy` | `superheavy_l_alloy` | `IngotMaterial` | 436 |
| `Talonite` | `talonite` | `IngotMaterial` | 372 |
| `TantalumHafniumSeaborgiumCarbide` | `tantalum_hafnium_seaborgium_carbide` | `IngotMaterial` | 427 |
| `Titanium50` | `titanium50` | `IngotMaterial` | 421 |
| `TitanSteel` | `titan_steel` | `IngotMaterial` | 411 |
| `TriniumTitanium` | `trinium_titanium` | `IngotMaterial` | 419 |
| `Tumbaga` | `tumbaga` | `IngotMaterial` | 370 |
| `TungstenTitaniumCarbide` | `tungsten_titanium_carbide` | `IngotMaterial` | 410 |
| `UEVSuperconductor` | `uev_superconductor` | `IngotMaterial` | 1466 |
| `UEVSuperconductorBase` | `uev_superconductor_base` | `IngotMaterial` | 1465 |
| `UHVSuperconductor` | `uhv_superconductor` | `IngotMaterial` | 1464 |
| `UHVSuperconductorBase` | `uhv_superconductor_base` | `IngotMaterial` | 1463 |
| `UIVSuperconductor` | `uiv_superconductor` | `IngotMaterial` | 1468 |
| `UIVSuperconductorBase` | `uiv_superconductor_base` | `IngotMaterial` | 1467 |
| `UMVSuperconductor` -> `UXVSuperconductor` | `umv_superconductor` -> `uxv_superconductor` | `IngotMaterial` | 1470 |
| `UMVSuperconductorBase` -> `UXVSuperconductorBase` | `umv_superconductor_base` -> `uxv_superconductor_base` | `IngotMaterial` | 1469 |
| `UVSuperconductor` | `uv_superconductor` | `IngotMaterial` | 1462 |
| `UVSuperconductorBase` | `uv_superconductor_base` | `IngotMaterial` | 1461 |
| `UXVSuperconductor` -> `OpVSuperconductor` | `uxv_superconductor` -> `opv_superconductor` | `IngotMaterial` | 1472 |
| `UXVSuperconductorBase` -> `OpVSuperconductorBase` | `uxv_superconductor_base` -> `opv_superconductor_base` | `IngotMaterial` | 1471 |
| `WoodsGlass` | `woods_glass` | `IngotMaterial` | 425 |
| `ZirconiumCarbide` | `zirconium_carbide` | `IngotMaterial` | 390 |
| `ZPMSuperconductor` | `zpm_superconductor` | `IngotMaterial` | 384 |
| `ZPMSuperconductorBase` | `zpm_superconductor_base` | `IngotMaterial` | 378 |
| `Zylon` | `zylon` | `IngotMaterial` | 406 |

### Simple Fluid Materials (511)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `Acetaldehyde` | `acetaldehyde` | `SimpleFluidMaterial` | 718 |
| `Acetoacetanilide` | `acetoacetanilide` | `SimpleFluidMaterial` | 838 |
| `Acetothienone` | `acetothieone` | `SimpleFluidMaterial` | 976 |
| `AcetylatingReagent` | `acetylating_reagent` | `SimpleFluidMaterial` | 854 |
| `AcetylChloride` | `acetyl_chloride` | `SimpleFluidMaterial` | 981 |
| `Acetylene` | `acetylene` | `SimpleFluidMaterial` | 532 |
| `AcetylsulfanilylChloride` | `acetylsulfanilyl_chloride` | `SimpleFluidMaterial` | 921 |
| `AcidicBrominatedBrine` | `acidic_brominated_brine` | `SimpleFluidMaterial` | 716 |
| `AcidicMetalSlurry` | `acidic_metal_slurry` | `SimpleFluidMaterial` | 664 |
| `AcidicSaltWater` | `acidic_salt_water` | `SimpleFluidMaterial` | 517 |
| `AcidifiedPolyphenolMix` | `acidified_polyphenol_mix` | `SimpleFluidMaterial` | 675 |
| `AcryloNitrile` | `acrylonitrile` | `SimpleFluidMaterial` | 686 |
| `ActiniumRadiumHydroxideSolution` | `actinium_radium_hydroxide_solution` | `SimpleFluidMaterial` | 979 |
| `ActiniumRadiumNitrateSolution` | `actinium_radium_nitrate_solution` | `SimpleFluidMaterial` | 978 |
| `AlkalineEarthSulfateSolution` | `alkalineearth_sulfate` | `SimpleFluidMaterial` | 773 |
| `AluminaSolution` | `alumina_solution` | `SimpleFluidMaterial` | 821 |
| `Amidoxime` | `amidoxime` | `SimpleFluidMaterial` | 694 |
| `AminatedFullerene` | `aminated_fullerene` | `SimpleFluidMaterial` | 870 |
| `AmineMixture` | `amine_mixture` | `SimpleFluidMaterial` | 697 |
| `Amino3phenol` | `3_aminophenol` | `SimpleFluidMaterial` | 851 |
| `AmmoniaRichMix` | `ammonia_rich_mix` | `SimpleFluidMaterial` | 641 |
| `AmmoniumBifluorideSolution` | `ammonium_bifluoride_solution` | `SimpleFluidMaterial` | 832 |
| `AmmoniumCyanate` | `ammonium_cyanate` | `SimpleFluidMaterial` | 823 |
| `AmmoniumFluoride` | `ammonium_fluoride` | `SimpleFluidMaterial` | 831 |
| `AmmoniumNiobiumOxalateSolution` | `ammonium_niobium_oxalate_solution` | `SimpleFluidMaterial` | 846 |
| `AmmoniumNitrate` | `ammonium_nitrate` | `SimpleFluidMaterial` | 497 |
| `AmmoniumPerrhenate` | `ammonium_perrhenate` | `SimpleFluidMaterial` | 587 |
| `AmmoniumPersulfate` | `ammonium_persulfate` | `SimpleFluidMaterial` | 704 |
| `AmmoniumSulfate` | `ammonium_sulfate` | `SimpleFluidMaterial` | 586 |
| `Aniline` | `aniline` | `SimpleFluidMaterial` | 551 |
| `AnimalCells` | `animal_cells` | `SimpleFluidMaterial` | 560 |
| `AntimonyPentafluoride` | `antimony_pentafluoride` | `SimpleFluidMaterial` | 456 |
| `ApatiteAcidicLeach` | `apatite_acidic_leach` | `SimpleFluidMaterial` | 957 |
| `AscorbicAcid` | `ascorbic_acid` | `SimpleFluidMaterial` | 930 |
| `AstatideSolution` | `astatide_solution` | `SimpleFluidMaterial` | 800 |
| `ATL` | `atl` | `SimpleFluidMaterial` | 778 |
| `ATLEthylene` | `atl_ethylene_mixture` | `SimpleFluidMaterial` | 934 |
| `AuricChloride` | `auric_chloride` | `SimpleFluidMaterial` | 477 |
| `Azafullerene` | `azafullerene` | `SimpleFluidMaterial` | 871 |
| `B27Supplement` | `b27_supplement` | `SimpleFluidMaterial` | 542 |
| `BacterialGrowthMedium` | `bacterial_growth_medium` | `SimpleFluidMaterial` | 558 |
| `BariumChlorideSolution` | `barium_chloride_solution` | `SimpleFluidMaterial` | 805 |
| `BariumStrontiumAcetateSolution` | `basr_acetate_solution` | `SimpleFluidMaterial` | 803 |
| `BariumStrontiumTitanatePreparation` | `basr_titanate_preparation` | `SimpleFluidMaterial` | 810 |
| `BariumSulfateSolution` | `barium_sulfate_solution` | `SimpleFluidMaterial` | 783 |
| `BariumTitanatePreparation` | `barium_titanate_preparation` | `SimpleFluidMaterial` | 809 |
| `BariumTriflateSolution` | `barium_triflate_solution` | `SimpleFluidMaterial` | 802 |
| `BentoniteClaySlurry` | `bentonite_clay_solution` | `SimpleFluidMaterial` | 784 |
| `Benzaldehyde` | `benzaldehyde` | `SimpleFluidMaterial` | 719 |
| `BenzenediazoniumTetrafluoroborate` | `benzenediazonium_tetrafluoroborate` | `SimpleFluidMaterial` | 592 |
| `Benzonitrile` | `benzonitrile` | `SimpleFluidMaterial` | 843 |
| `BenzoylChloride` | `benzoyl_chloride` | `SimpleFluidMaterial` | 612 |
| `BenzoylPeroxide` | `benzoyl_peroxide` | `SimpleFluidMaterial` | 922 |
| `Benzylamine` | `benzylamine` | `SimpleFluidMaterial` | 964 |
| `BenzylChloride` | `benzyl_chloride` | `SimpleFluidMaterial` | 963 |
| `BetaIonone` | `beta_ionone` | `SimpleFluidMaterial` | 537 |
| `BFGF` | `bfgf` | `SimpleFluidMaterial` | 548 |
| `Biotin` | `biotin` | `SimpleFluidMaterial` | 541 |
| `Biperfluoromethanedisulfide` | `biperfluoromethanedisulfide` | `SimpleFluidMaterial` | 801 |
| `BismuthNitrateSoluton` | `bismuth_nitrate_solution` | `SimpleFluidMaterial` | 808 |
| `BismuthVanadateSolution` | `bismuth_vanadate_solution` | `SimpleFluidMaterial` | 835 |
| `Blood` | `blood` | `SimpleFluidMaterial` | 545 |
| `BloodCells` | `blood_cells` | `SimpleFluidMaterial` | 546 |
| `BloodPlasma` | `blood_plasma` | `SimpleFluidMaterial` | 547 |
| `BoraneDimethylsulfide` | `borane_dimethylsulfide` | `SimpleFluidMaterial` | 969 |
| `BoricAcid` | `boric_acid` | `SimpleFluidMaterial` | 590 |
| `BoronFluoride` | `boron_fluoride` | `SimpleFluidMaterial` | 593 |
| `BoronFreeSolution` | `boron_free_solution` | `SimpleFluidMaterial` | 711 |
| `BoronTrifluorideEtherate` | `boron_trifluoride_etherate` | `SimpleFluidMaterial` | 968 |
| `BosonicUUMatter` | `bosonic_uu_matter` | `SimpleFluidMaterial` | 940 |
| `Brine` | `brine` | `SimpleFluidMaterial` | 653 |
| `BrominatedBrine` | `brominated_brine` | `SimpleFluidMaterial` | 715 |
| `BromineTrifluoride` | `bromine_trifluoride` | `SimpleFluidMaterial` | 478 |
| `Bromobutane` | `bromobutane` | `SimpleFluidMaterial` | 799 |
| `Bromohydrothiine` | `bromodihydrothiine` | `SimpleFluidMaterial` | 798 |
| `Butanol` | `butanol` | `SimpleFluidMaterial` | 578 |
| `ButanolGas` | `butanol_gas` | `SimpleFluidMaterial` | 818 |
| `Butylaniline` | `butylaniline` | `SimpleFluidMaterial` | 860 |
| `ButylLithium` | `butyl_lithium` | `SimpleFluidMaterial` | 717 |
| `CaCBaSMixture` | `cacbas_mixture` | `SimpleFluidMaterial` | 932 |
| `CadmiumSulfateSolution` | `cadmium_sulfate` | `SimpleFluidMaterial` | 672 |
| `CadmiumThalliumLiquor` | `cdtl_liquor` | `SimpleFluidMaterial` | 670 |
| `Calcium44` | `calcium_44` | `SimpleFluidMaterial` | 735 |
| `CalciumCarbonateSolution` | `calcium_carbonate_solution` | `SimpleFluidMaterial` | 782 |
| `CalciumFreeBrine` | `calcium_free_brine` | `SimpleFluidMaterial` | 708 |
| `CalicheIodateBrine` | `caliche_iodate_brine` | `SimpleFluidMaterial` | 645 |
| `CalicheIodineBrine` | `caliche_iodine_brine` | `SimpleFluidMaterial` | 648 |
| `CalicheNitrateSolution` | `caliche_nitrate_solution` | `SimpleFluidMaterial` | 647 |
| `Carbon12` | `carbon_12` | `SimpleFluidMaterial` | 730 |
| `Carbon13` | `carbon_13` | `SimpleFluidMaterial` | 731 |
| `CarbonatedEthanolamine` | `carbonated_ethanolamine` | `SimpleFluidMaterial` | 640 |
| `CarbonFluoride` | `carbone_fluoride` | `SimpleFluidMaterial` | 574 |
| `CarbonSulfide` | `carbon_sulfide` | `SimpleFluidMaterial` | 696 |
| `CarbonTetrachloride` | `carbon_tetrachloride` | `SimpleFluidMaterial` | 811 |
| `Cas9` | `cas_9` | `SimpleFluidMaterial` | 566 |
| `Catalase` | `catalase` | `SimpleFluidMaterial` | 544 |
| `CesiumBromideSolution` | `cesium_bromide_solution` | `SimpleFluidMaterial` | 1004 |
| `CesiumFluoride` | `cesium_fluoride` | `SimpleFluidMaterial` | 468 |
| `CesiumXenontrioxideFluoride` | `cesium_xenontrioxide_fluoride` | `SimpleFluidMaterial` | 469 |
| `CetaneTrimethylAmmoniumBromide` | `cetane_trimethyl_ammonium_bromide` | `SimpleFluidMaterial` | 703 |
| `ChilledBrine` | `chilled_brine` | `SimpleFluidMaterial` | 713 |
| `Chitin` | `chitin` | `SimpleFluidMaterial` | 568 |
| `Chitosan` | `chitosan` | `SimpleFluidMaterial` | 569 |
| `ChlorideLeachedSolution` | `chloride_leached_solution` | `SimpleFluidMaterial` | 583 |
| `ChlorinatedSolvents` | `chlorinated_solvents` | `SimpleFluidMaterial` | 816 |
| `ChloroauricAcid` | `chloroauric_acid` | `SimpleFluidMaterial` | 937 |
| `ChlorodiisopropylPhosphine` | `chlorodiisopropyl_phosphine` | `SimpleFluidMaterial` | 1003 |
| `Chloroethane` | `chloroethane` | `SimpleFluidMaterial` | 812 |
| `Chloroethanol` | `chloroethanol` | `SimpleFluidMaterial` | 776 |
| `ChloroPlatinicAcid` | `chloroplatinic_acid` | `SimpleFluidMaterial` | 722 |
| `ChlorosulfonicAcid` | `chlorosulfonic_acid` | `SimpleFluidMaterial` | 552 |
| `ChlorousAcid` | `chlorous_acid` | `SimpleFluidMaterial` | 849 |
| `Choline` | `choline` | `SimpleFluidMaterial` | 777 |
| `Chromium48` | `chromium48` | `SimpleFluidMaterial` | 740 |
| `Citral` | `citral` | `SimpleFluidMaterial` | 536 |
| `CitricAcid` | `citric_acid` | `SimpleFluidMaterial` | 794 |
| `CleanAmmoniaSolution` | `clear_ammonia_solution` | `SimpleFluidMaterial` | 543 |
| `ClearENaquadahLiquid` | `clear_e_naquadah_liquid` | `SimpleFluidMaterial` | 499 |
| `ClearNaquadahLiquid` | `clear_naquadah_liquid` | `SimpleFluidMaterial` | 482 |
| `CNOcatalyst` | `cno_catalyst` | `SimpleFluidMaterial` | 734 |
| `ComplicatedHeavyENaquadah` | `complicated_heavy_e_naquadah` | `SimpleFluidMaterial` | 500 |
| `ComplicatedHeavyNaquadah` | `complicated_heavy_naquadah` | `SimpleFluidMaterial` | 484 |
| `ComplicatedLightENaquadah` | `complicated_light_e_naquadah` | `SimpleFluidMaterial` | 502 |
| `ComplicatedLightNaquadah` | `complicated_light_naquadah` | `SimpleFluidMaterial` | 486 |
| `ComplicatedMediumENaquadah` | `complicated_medium_e_naquadah` | `SimpleFluidMaterial` | 501 |
| `ComplicatedMediumNaquadah` | `complicated_medium_naquadah` | `SimpleFluidMaterial` | 485 |
| `ComplicatedNaquadahGas` | `complicated_naquadah_gas` | `SimpleFluidMaterial` | 483 |
| `ConcentratedBrine` | `concentrated_brine` | `SimpleFluidMaterial` | 707 |
| `CopperRefiningSolution` | `copper_refining_solution` | `SimpleFluidMaterial` | 752 |
| `CosmicComputingMix` | `cosmic_computing_mix` | `SimpleFluidMaterial` | 880 |
| `CosmicMeshPlasma` | `cosmic_mesh_plasma` | `SimpleFluidMaterial` | 897 |
| `CrudeAluminaSolution` | `crude_alumina_solution` | `SimpleFluidMaterial` | 820 |
| `Cyclooctadiene` | `cyclooctadiene` | `SimpleFluidMaterial` | 723 |
| `Cycloparaphenylene` | `cycloparaphenylene` | `SimpleFluidMaterial` | 724 |
| `Cyclopentadiene` | `cyclopentadiene` | `SimpleFluidMaterial` | 936 |
| `DampBromine` | `damp_bromine` | `SimpleFluidMaterial` | 520 |
| `DebrominatedWater` | `debrominated_brine` | `SimpleFluidMaterial` | 705 |
| `DeglyceratedSoap` | `deglyceratedsoap` | `SimpleFluidMaterial` | 949 |
| `DehydroascorbicAcid` | `dehydroascorbic_acid` | `SimpleFluidMaterial` | 931 |
| `DenseNeutronPlasma` | `dense_neutron_plasma` | `SimpleFluidMaterial` | 896 |
| `DeuteriumSuperheavyMix` | `deuterium_superheavy_mix` | `SimpleFluidMaterial` | 882 |
| `Dibenzylideneacetone` | `dibenzylideneacetone` | `SimpleFluidMaterial` | 720 |
| `Diborane` | `diborane` | `SimpleFluidMaterial` | 814 |
| `Dibromoacrolein` | `dibromoacrolein` | `SimpleFluidMaterial` | 797 |
| `Dibromomethylbenzene` | `dibromomethylbenzene` | `SimpleFluidMaterial` | 600 |
| `Dichlorodicyanobenzoquinone` | `dichlorodicyanobenzoquinone` | `SimpleFluidMaterial` | 856 |
| `Dichlorodicyanohydroquinone` | `dichlorodicyanohidroquinone` | `SimpleFluidMaterial` | 857 |
| `Dichloromethane` | `dichloromethane` | `SimpleFluidMaterial` | 817 |
| `DielectricMirrorFormationMix` | `dielectric_mirror_formation_mix` | `SimpleFluidMaterial` | 847 |
| `Diethoxythiophene` | `dietoxythiophene` | `SimpleFluidMaterial` | 792 |
| `Diethylether` | `diethylether` | `SimpleFluidMaterial` | 676 |
| `DiethylhexylPhosphoricAcid` | `di_ethylhexyl_phosphoric_acid` | `SimpleFluidMaterial` | 523 |
| `Diethylthiourea` | `diethylthiourea` | `SimpleFluidMaterial` | 927 |
| `Difluoroaniline` | `difluoroaniline` | `SimpleFluidMaterial` | 886 |
| `Dihydroiodotetracene` | `dihydroiodotetracene` | `SimpleFluidMaterial` | 855 |
| `Diisopropylcarbodiimide` | `diisopropylcarbodiimide` | `SimpleFluidMaterial` | 608 |
| `DiluteHexafluorosilicicAcid` | `dilute_hexafluorosilicic_acid` | `SimpleFluidMaterial` | 983 |
| `DiluteHydrofluoricAcid` | `dilute_hydrofluoric_acid` | `SimpleFluidMaterial` | 985 |
| `DiluteNitricAcid` | `dilute_nitric_acid` | `SimpleFluidMaterial` | 764 |
| `Dimethoxyethane` | `dimethoxyethane` | `SimpleFluidMaterial` | 945 |
| `Dimethylether` | `dimethylether` | `SimpleFluidMaterial` | 944 |
| `Dimethylformamide` | `dimethylformamide` | `SimpleFluidMaterial` | 701 |
| `Dimethylnaphthalene` | `dimethylnaphthalene` | `SimpleFluidMaterial` | 852 |
| `Dimethylsulfide` | `dimethylsulfide` | `SimpleFluidMaterial` | 611 |
| `DimethylthiocarbamoilChloride` | `dimethylthiocarbamoil_chloride` | `SimpleFluidMaterial` | 698 |
| `Dinitrodipropanyloxybenzene` | `dinitrodipropanyloxybenzene` | `SimpleFluidMaterial` | 604 |
| `Dioxygendifluoride` | `dioxygen_difluoride` | `SimpleFluidMaterial` | 984 |
| `DirtyHexafluorosilicicAcid` | `dirty_hexafluorosilicic_acid` | `SimpleFluidMaterial` | 982 |
| `DissolvedLithiumOre` | `dissolved_lithium_ores` | `SimpleFluidMaterial` | 642 |
| `DrillingMud` | `drilling_mud` | `SimpleFluidMaterial` | 785 |
| `DrillingMudMixture` | `drilling_mud_mixture` | `SimpleFluidMaterial` | 935 |
| `DustyLiquidHelium3` | `dusty_liquid_helium3` | `SimpleFluidMaterial` | 991 |
| `EDOT` | `ethylenedioxythiophene` | `SimpleFluidMaterial` | 793 |
| `EDTA` | `edta` | `SimpleFluidMaterial` | 826 |
| `EDTASolution` | `edta_solution` | `SimpleFluidMaterial` | 825 |
| `EGF` | `egf` | `SimpleFluidMaterial` | 549 |
| `ElectronDegenerateRheniumPlasma` | `degenerate_rhenium_plasma` | `SimpleFluidMaterial` | 588 |
| `ENaquadahSolution` | `e_naquadah_solution` | `SimpleFluidMaterial` | 498 |
| `EnrichedFluoronaquadricAcid` | `enriched_fluoronaquadric_acid` | `SimpleFluidMaterial` | 459 |
| `EnrichedNaquadahDifluoride` | `enriched_naquadah_difluoride` | `SimpleFluidMaterial` | 462 |
| `EnrichedNaquadahhexafluoride` | `enriched_naquadahhexafluoride` | `SimpleFluidMaterial` | 475 |
| `EnrichedNaquadricSolution` | `enriched_naquadric_solution` | `SimpleFluidMaterial` | 454 |
| `EnrichedXenonHexafluoronaquadate` | `enriched_xenon_hexafluoronaquadate` | `SimpleFluidMaterial` | 476 |
| `ErLuOxidesSolution` | `er_lu_oxides_solution` | `SimpleFluidMaterial` | 529 |
| `Ethanol100` | `ethanol_100` | `SimpleFluidMaterial` | 555 |
| `Ethanolamine` | `ethanolamine` | `SimpleFluidMaterial` | 540 |
| `Ethylamine` | `ethylamine` | `SimpleFluidMaterial` | 872 |
| `Ethylenediamine` | `ethylenediamine` | `SimpleFluidMaterial` | 824 |
| `EthyleneGlycol` | `ethylene_glycol` | `SimpleFluidMaterial` | 775 |
| `EthyleneOxide` | `ethylene_oxide` | `SimpleFluidMaterial` | 539 |
| `Ethylhexanol` | `ethylhexanol` | `SimpleFluidMaterial` | 522 |
| `EthylTrifluoroacetate` | `ethyl_trifluoroacetate` | `SimpleFluidMaterial` | 975 |
| `FCrackedHeavyNaquadah` | `fl_cracked_heavy_naquadah` | `SimpleFluidMaterial` | 493 |
| `FCrackedLightNaquadah` | `fl_cracked_light_naquadah` | `SimpleFluidMaterial` | 491 |
| `FCrackedMediumNaquadah` | `fl_cracked_medium_naquadah` | `SimpleFluidMaterial` | 492 |
| `FermionicUUMatter` | `fermionic_uu_matter` | `SimpleFluidMaterial` | 939 |
| `FerricREEChloride` | `ferric_ree_chloride` | `SimpleFluidMaterial` | 758 |
| `Ferrocene` | `ferrocene` | `SimpleFluidMaterial` | 616 |
| `Ferrocenylfulleropyrrolidine` | `ferrocenylfulleropyrddolidine` | `SimpleFluidMaterial` | 617 |
| `FluoroapatiteAcidicLeach` | `fluoroapatite_acidic_leach` | `SimpleFluidMaterial` | 958 |
| `FluoroBenzene` | `fluoro_benzene` | `SimpleFluidMaterial` | 594 |
| `FluoroBoricAcid` | `fluoroboric_acid` | `SimpleFluidMaterial` | 591 |
| `FluoronaquadriaticAcid` | `fluoronaquadriatic_acid` | `SimpleFluidMaterial` | 460 |
| `FluoronaquadricAcid` | `fluoronaquadric_acid` | `SimpleFluidMaterial` | 458 |
| `FluoroniobicAcid` | `fluroniobic_acid` | `SimpleFluidMaterial` | 766 |
| `FluorophosphoricAcid` | `fluorophosphoric_acid` | `SimpleFluidMaterial` | 925 |
| `FluorosilicicAcid` | `fluorosilicic_acid` | `SimpleFluidMaterial` | 830 |
| `FluorotantalicAcid` | `flurotantalic_acid` | `SimpleFluidMaterial` | 767 |
| `Fluorotoluene` | `fluorotoluene` | `SimpleFluidMaterial` | 596 |
| `FlYbPlasma` | `flyb_plasma` | `SimpleFluidMaterial` | 739 |
| `FreeAlphaGas` | `free_alpha_gas` | `SimpleFluidMaterial` | 918 |
| `FreeElectronGas` | `free_electron_gas` | `SimpleFluidMaterial` | 919 |
| `FullereneDopedNanotubes` | `fullerene_doped_nanotubes` | `SimpleFluidMaterial` | 845 |
| `FumingNitricAcid` | `fuming_nitric_acid` | `SimpleFluidMaterial` | 980 |
| `GenePlasmids` | `pluripotency_induction_gene_plasmids` | `SimpleFluidMaterial` | 567 |
| `GeneTherapyFluid` | `pluripotency_induction_gene_therapy_fluid` | `SimpleFluidMaterial` | 570 |
| `GermanicAcidSolution` | `germanic_acid_solution` | `SimpleFluidMaterial` | 678 |
| `GermaniumChloride` | `germanium_chloride` | `SimpleFluidMaterial` | 679 |
| `GlucoseIronSolution` | `glucose_iron_solution` | `SimpleFluidMaterial` | 891 |
| `Gluons` | `gluons` | `SimpleFluidMaterial` | 878 |
| `Glycine` | `glycine` | `SimpleFluidMaterial` | 827 |
| `Glyoxal` | `glyoxal` | `SimpleFluidMaterial` | 962 |
| `GoldCyanide` | `gold_cyanide` | `SimpleFluidMaterial` | 582 |
| `GrapheneOxidationSolution` | `graphene_oxidation_solution` | `SimpleFluidMaterial` | 892 |
| `HeavilyFluorinatedTriniumSolution` | `heavily_fluorinated_trinium_solution` | `SimpleFluidMaterial` | 973 |
| `HeavyENaquadah` | `heavy_e_naquadah` | `SimpleFluidMaterial` | 505 |
| `HeavyLeptonMix` | `heavy_lepton_mix` | `SimpleFluidMaterial` | 879 |
| `HeavyNaquadah` | `heavy_naquadah` | `SimpleFluidMaterial` | 490 |
| `HeavyQuarkEnrichedMix` | `heavy_quark_enriched_mix` | `SimpleFluidMaterial` | 881 |
| `HeavyQuarks` | `heavy_quarks` | `SimpleFluidMaterial` | 876 |
| `Helium3Hydride` | `helium_iii_hydride` | `SimpleFluidMaterial` | 988 |
| `Helium4` | `helium4` | `SimpleFluidMaterial` | 938 |
| `HeliumCNO` | `helium_rich_cno` | `SimpleFluidMaterial` | 744 |
| `HeptafluoroTantalate` | `heptafluorotantalate` | `SimpleFluidMaterial` | 770 |
| `Hexafluoropropylene` | `hexafluoropropylene` | `SimpleFluidMaterial` | 943 |
| `Hexamethylenediamine` | `hexamethylenediamine` | `SimpleFluidMaterial` | 866 |
| `Hexanediol` | `hexanediol` | `SimpleFluidMaterial` | 865 |
| `HighEnergyQGP` | `high_energy_qgp` | `SimpleFluidMaterial` | 920 |
| `HotNitrogen` | `hot_nitrogen` | `SimpleFluidMaterial` | 779 |
| `HotVapourMixture` | `hot_vapour_mixture` | `SimpleFluidMaterial` | 519 |
| `HydrobromicAcid` | `hydrobromic_acid` | `SimpleFluidMaterial` | 606 |
| `HydroiodicAcid` | `hydroiodic_acid` | `SimpleFluidMaterial` | 1002 |
| `Hydroquinone` | `hydroquinone` | `SimpleFluidMaterial` | 618 |
| `HydroselenicAcid` | `hydroselenic_acid` | `SimpleFluidMaterial` | 788 |
| `Hydroxylamine` | `hydroxylamine` | `SimpleFluidMaterial` | 693 |
| `HydroxylamineDisulfate` | `hydroxylamine_disulfate` | `SimpleFluidMaterial` | 692 |
| `HydroxylamineHydrochloride` | `hydroxylamine_hydrochloride` | `SimpleFluidMaterial` | 960 |
| `Hydroxyquinoline` | `hydroxyquinoline` | `SimpleFluidMaterial` | 790 |
| `ImpureAluminiumHydroxideSolution` | `impure_aloh3_soution` | `SimpleFluidMaterial` | 754 |
| `IndiumHydroxideConcentrate` | `indium_hydroxide_concentrate` | `SimpleFluidMaterial` | 669 |
| `IodideSolution` | `iodide_solution` | `SimpleFluidMaterial` | 646 |
| `IodineBrineMix` | `iodine_brine_mix` | `SimpleFluidMaterial` | 651 |
| `IodineMonochloride` | `iodine_monochloride` | `SimpleFluidMaterial` | 853 |
| `IodineSlurry` | `iodine_slurry` | `SimpleFluidMaterial` | 652 |
| `IodizedBrine` | `iodized_brine` | `SimpleFluidMaterial` | 650 |
| `Iodobenzene` | `iodobenzene` | `SimpleFluidMaterial` | 850 |
| `IodobenzoicAcid` | `iodobenzoic_acid` | `SimpleFluidMaterial` | 858 |
| `IridiumTrichlorideSolution` | `iridiumtrichloridesolution` | `SimpleFluidMaterial` | 953 |
| `Iron52` | `iron52` | `SimpleFluidMaterial` | 741 |
| `IronCarbonyl` | `iron_carbonyl` | `SimpleFluidMaterial` | 806 |
| `IronPoorMix` | `iron_poor_mix` | `SimpleFluidMaterial` | 668 |
| `IsoamylAlcohol` | `isoamyl_alcohol` | `SimpleFluidMaterial` | 905 |
| `Isochloropropane` | `isochloropropane` | `SimpleFluidMaterial` | 602 |
| `Isophthaloylbisdiethylthiourea` | `isophthaloylbisdiethylthiourea` | `SimpleFluidMaterial` | 928 |
| `IsopropylAcetate` | `isopropyl_acetate` | `SimpleFluidMaterial` | 815 |
| `IsopropylAlcohol` | `isopropyl_alcohol` | `SimpleFluidMaterial` | 623 |
| `Isopropylsuccinate` | `isopropylsuccinate` | `SimpleFluidMaterial` | 841 |
| `KeroseneIodineSolution` | `kerosene_iodine_solution` | `SimpleFluidMaterial` | 649 |
| `KFL4Gene` | `kfl_4_gene` | `SimpleFluidMaterial` | 565 |
| `KryptonDifluoride` | `krypton_difluoride` | `SimpleFluidMaterial` | 874 |
| `LaNdOxidesSolution` | `la_nd_oxides_solution` | `SimpleFluidMaterial` | 526 |
| `LightENaquadah` | `light_e_naquadah` | `SimpleFluidMaterial` | 503 |
| `LightNaquadah` | `light_naquadah` | `SimpleFluidMaterial` | 488 |
| `LightQuarks` | `light_quarks` | `SimpleFluidMaterial` | 877 |
| `LinoleicAcid` | `linoleic_acid` | `SimpleFluidMaterial` | 572 |
| `LiquidCrystalDetector` | `liquid_crystal_detector` | `SimpleFluidMaterial` | 862 |
| `LiquidEnrichedHelium` | `liquid_enriched_helium` | `SimpleFluidMaterial` | 900 |
| `LiquidFluorine` | `liquid_fluorine` | `SimpleFluidMaterial` | 999 |
| `LiquidHelium` | `liquid_helium` | `SimpleFluidMaterial` | 589 |
| `LiquidHelium3` | `liquid_helium_3` | `SimpleFluidMaterial` | 899 |
| `LiquidNitrogen` | `liquid_nitrogen` | `SimpleFluidMaterial` | 901 |
| `LiquidXenon` | `liquid_xenon` | `SimpleFluidMaterial` | 1000 |
| `LiquidZBLAN` | `molten_zblan` | `SimpleFluidMaterial` | 848 |
| `LithiumCarbonateSolution` | `lithium_carbonate_solution` | `SimpleFluidMaterial` | 643 |
| `LithiumChlorideSolution` | `lithium_chloride_solution` | `SimpleFluidMaterial` | 644 |
| `LithiumCyclopentadienide` | `lithiumcyclopentadienide` | `SimpleFluidMaterial` | 946 |
| `LithiumHydroxideSolution` | `lithium_hydroxide_solution` | `SimpleFluidMaterial` | 681 |
| `LithiumPeroxideSolution` | `lithium_peroxide` | `SimpleFluidMaterial` | 682 |
| `LubricantClaySlurry` | `lubricant_clay_slurry` | `SimpleFluidMaterial` | 933 |
| `LuTmYChlorideSolution` | `lutmy_chloride_solution` | `SimpleFluidMaterial` | 833 |
| `MagnesiumContainingBrine` | `magnesium_containing_brine` | `SimpleFluidMaterial` | 714 |
| `MaleicAnhydride` | `maleic_anhydride` | `SimpleFluidMaterial` | 842 |
| `MBBA` | `mbba` | `SimpleFluidMaterial` | 861 |
| `MediumENaquadah` | `medium_e_naquadah` | `SimpleFluidMaterial` | 504 |
| `MediumNaquadah` | `medium_naquadah` | `SimpleFluidMaterial` | 489 |
| `Mercaptophenol` | `mercaptophenol` | `SimpleFluidMaterial` | 700 |
| `MercuryNitrate` | `mercury_nitrate` | `SimpleFluidMaterial` | 834 |
| `MesitylOxide` | `mesityl_oxide` | `SimpleFluidMaterial` | 654 |
| `MetalHydroxideMix` | `metal_hydroxide_mix` | `SimpleFluidMaterial` | 666 |
| `MetalRichSlagSlurry` | `metal_slag_slurry` | `SimpleFluidMaterial` | 663 |
| `Methoxybenzaldehyde` | `methoxybenzaldehyde` | `SimpleFluidMaterial` | 859 |
| `Methylamine` | `methylamine` | `SimpleFluidMaterial` | 621 |
| `Methylethanolamine` | `methylethanolamine` | `SimpleFluidMaterial` | 902 |
| `MethylFormate` | `methyl_formate` | `SimpleFluidMaterial` | 689 |
| `Methylguanidine` | `methylguanidine` | `SimpleFluidMaterial` | 903 |
| `MethylIsobutylKetone` | `methyl_isobutyl_ketone` | `SimpleFluidMaterial` | 655 |
| `Methylnitronitrosoguanidine` | `methylnitronitrosoguanidine` | `SimpleFluidMaterial` | 904 |
| `MicrocrystallizingHydrogen` | `microcrystallizinghydrogen` | `SimpleFluidMaterial` | 955 |
| `MoltenCalciumSalts` | `molten_calcium_salts` | `SimpleFluidMaterial` | 974 |
| `MolybdenumFlue` | `molybdenum_flue_gas` | `SimpleFluidMaterial` | 584 |
| `MycGene` | `myc_gene` | `SimpleFluidMaterial` | 562 |
| `Naphthaldehyde` | `napthaldehyde` | `SimpleFluidMaterial` | 605 |
| `Naphthylamine` | `naphthylamine` | `SimpleFluidMaterial` | 837 |
| `NaquadahDifluoride` | `naquadah_difluoride` | `SimpleFluidMaterial` | 461 |
| `NaquadahGas` | `naquadah_gas` | `SimpleFluidMaterial` | 487 |
| `NaquadahSolution` | `naquadah_solution` | `SimpleFluidMaterial` | 481 |
| `NaquadahSulfate` | `naquadah_sulfate` | `SimpleFluidMaterial` | 480 |
| `NaquadriaCesiumfluoride` | `naquadria_cesiumfluoride` | `SimpleFluidMaterial` | 474 |
| `NaquadriaCesiumXenonNonfluoride` | `naquadria_cesium_xenon_nonfluoride` | `SimpleFluidMaterial` | 471 |
| `NaquadriaDifluoride` | `naquadria_difluoride` | `SimpleFluidMaterial` | 463 |
| `NaquadriaHexafluoride` | `naquadria_hexafluoride` | `SimpleFluidMaterial` | 464 |
| `NaquadriaticSolution` | `naquadriatic_solution` | `SimpleFluidMaterial` | 455 |
| `NaquadricSolution` | `naquadric_solution` | `SimpleFluidMaterial` | 453 |
| `NbTaFluorideMix` | `nbta_fluoride_mix` | `SimpleFluidMaterial` | 768 |
| `NbTaSeparationMixture` | `nbta_separation_mixture` | `SimpleFluidMaterial` | 765 |
| `NDifluorophenylpyrrole` | `n_difluorophenylpyrrole` | `SimpleFluidMaterial` | 888 |
| `NeutralisedRedMud` | `neutralised_red_mud` | `SimpleFluidMaterial` | 757 |
| `NeutroniumDopedNanotubes` | `neutronium_doped_nanotubes` | `SimpleFluidMaterial` | 910 |
| `NeutronPlasma` | `neutron_plasma` | `SimpleFluidMaterial` | 726 |
| `Nickel56` | `nickel56` | `SimpleFluidMaterial` | 742 |
| `NitratedTriniiteSolution` | `nitrated_triniite_solution` | `SimpleFluidMaterial` | 971 |
| `NitroBenzene` | `nitro_benzene` | `SimpleFluidMaterial` | 550 |
| `Nitrogen14` | `nitrogen_14` | `SimpleFluidMaterial` | 732 |
| `NItrogen15` | `nitrogen_15` | `SimpleFluidMaterial` | 733 |
| `NitrogenPentoxide` | `nitrogen_pentoxide` | `SimpleFluidMaterial` | 684 |
| `NitrosoniumOctafluoroxenate` | `nitrosonium_octafluoroxenate` | `SimpleFluidMaterial` | 473 |
| `Nitrotoluene` | `nitrotoluene` | `SimpleFluidMaterial` | 836 |
| `NitrousAcid` | `nitrous_acid` | `SimpleFluidMaterial` | 959 |
| `NitrylFluoride` | `nitryl_fluoride` | `SimpleFluidMaterial` | 472 |
| `NobleGases` | `noble_gases_mixture` | `SimpleFluidMaterial` | 894 |
| `NonMetals` | `non_metals` | `SimpleFluidMaterial` | 895 |
| `Oct1ene` | `1_octene` | `SimpleFluidMaterial` | 702 |
| `Oct4Gene` | `oct_4_gene` | `SimpleFluidMaterial` | 563 |
| `Octanol` | `octanol` | `SimpleFluidMaterial` | 906 |
| `OrthoXylene` | `ortho_xylene` | `SimpleFluidMaterial` | 597 |
| `OrthoXyleneZeoliteMixture` | `ortho_xylene_zeolite` | `SimpleFluidMaterial` | 598 |
| `OxalicAcid` | `oxalic_acid` | `SimpleFluidMaterial` | 795 |
| `OxidisedNitrogenMix` | `oxidised_nitrogen_mix` | `SimpleFluidMaterial` | 638 |
| `OxidizedResidualSolution` | `oxidized_residual_solution` | `SimpleFluidMaterial` | 986 |
| `Oxydianiline` | `oxydianiline` | `SimpleFluidMaterial` | 941 |
| `OxypentafluoroNiobate` | `oxypentafluoroniobate` | `SimpleFluidMaterial` | 769 |
| `Ozone` | `ozone` | `SimpleFluidMaterial` | 683 |
| `ParaXylene` | `para_xylene` | `SimpleFluidMaterial` | 599 |
| `PCBA` | `pcba` | `SimpleFluidMaterial` | 614 |
| `PCBS` | `pcbs` | `SimpleFluidMaterial` | 615 |
| `Perbromothiophene` | `perbromothiophene` | `SimpleFluidMaterial` | 791 |
| `Perfluorobenzene` | `perfluorobenzene` | `SimpleFluidMaterial` | 970 |
| `PhenylenedioxydiaceticAcid` | `phenylenedioxydiacetic_acid` | `SimpleFluidMaterial` | 926 |
| `Phenylpentanoicacid` | `phenylpentanoicacid` | `SimpleFluidMaterial` | 610 |
| `Phenylsodium` | `phenylsodium` | `SimpleFluidMaterial` | 885 |
| `Phosgene` | `phosgene` | `SimpleFluidMaterial` | 622 |
| `PhosphorousArsenicSolution` | `phosphorous_arsenic_solution` | `SimpleFluidMaterial` | 829 |
| `PhosphorusTrichloride` | `phosphorus_trichloride` | `SimpleFluidMaterial` | 575 |
| `PhosphorylChloride` | `phosphoryl_chloride` | `SimpleFluidMaterial` | 576 |
| `PhotopolymerSolution` | `photopolymer_solution` | `SimpleFluidMaterial` | 889 |
| `PiranhaSolution` | `piranha_solution` | `SimpleFluidMaterial` | 556 |
| `PlasmaChromium48` | `chromium48_plasma` | `SimpleFluidMaterial` | 745 |
| `PlasmaHeliumCNO` | `helium_rich_cno_plasma` | `SimpleFluidMaterial` | 749 |
| `PlasmaIron52` | `iron52_plasma` | `SimpleFluidMaterial` | 746 |
| `PlasmaNickel56` | `nickel56_plasma` | `SimpleFluidMaterial` | 747 |
| `PlasmaTitanium44` | `titanium44_plasma` | `SimpleFluidMaterial` | 748 |
| `PolyacrylonitrileSolution` | `polyacrylonitrile_solution` | `SimpleFluidMaterial` | 688 |
| `PolyamicAcid` | `polyamic_acid` | `SimpleFluidMaterial` | 942 |
| `PolyphenolMix` | `polyphenol_mix` | `SimpleFluidMaterial` | 674 |
| `PotassiumEthoxide` | `potassium_ethoxide` | `SimpleFluidMaterial` | 863 |
| `PotassiumFreeBrine` | `potassium_free_brine` | `SimpleFluidMaterial` | 710 |
| `Propadiene` | `propadiene` | `SimpleFluidMaterial` | 924 |
| `PropargylAlcohol` | `propargyl_alcohol` | `SimpleFluidMaterial` | 534 |
| `PropargylChloride` | `propargyl_chloride` | `SimpleFluidMaterial` | 535 |
| `PrYHoNitrateSolution` | `pryho_nitrate_solution` | `SimpleFluidMaterial` | 828 |
| `PureAluminiumHydroxideSolution` | `pure_aloh3_soution` | `SimpleFluidMaterial` | 755 |
| `PurifiedIronCarbonyl` | `purified_iron_carbonyl` | `SimpleFluidMaterial` | 807 |
| `PurifiedNitrogenMix` | `purified_nitrogen_mix` | `SimpleFluidMaterial` | 639 |
| `Pyridine` | `pyridine` | `SimpleFluidMaterial` | 609 |
| `QuantumDots` | `quantumdots` | `SimpleFluidMaterial` | 952 |
| `QuarkGluonPlasma` | `quark_gluon_plasma` | `SimpleFluidMaterial` | 875 |
| `Quinizarin` | `quinizarin` | `SimpleFluidMaterial` | 839 |
| `RadonDifluoride` | `radon_difluoride` | `SimpleFluidMaterial` | 465 |
| `RadonNaquadriaoctafluoride` | `radon_naquadriaoctafluoride` | `SimpleFluidMaterial` | 466 |
| `RadonRadiumMix` | `radon_radium_mix` | `SimpleFluidMaterial` | 884 |
| `RadonTrioxide` | `radon_trioxide` | `SimpleFluidMaterial` | 470 |
| `RapidlyReplicatingAnimalCells` | `rapidly_replicating_animal_cells` | `SimpleFluidMaterial` | 561 |
| `RareEarthChloridesSolution` | `rare_earth_chlorides_solution` | `SimpleFluidMaterial` | 525 |
| `RareEarthHydroxidesSolution` | `rare_earth_hydroxides_solution` | `SimpleFluidMaterial` | 524 |
| `RareEarthNitrateSolution` | `rare_earth_nitrate_solution` | `SimpleFluidMaterial` | 772 |
| `RedMud` | `red_mud` | `SimpleFluidMaterial` | 756 |
| `RedOil` | `red_oil` | `SimpleFluidMaterial` | 579 |
| `RedSlurry` | `red_slurry` | `SimpleFluidMaterial` | 759 |
| `REEThUSulfateSolution` | `reethu_sulfate_solution` | `SimpleFluidMaterial` | 771 |
| `ResidualTriniiteSolution` | `residual_triniite_solution` | `SimpleFluidMaterial` | 972 |
| `Resin` | `resin` | `SimpleFluidMaterial` | 571 |
| `Resorcinol` | `resorcinol` | `SimpleFluidMaterial` | 603 |
| `RheniumScrubbedSolution` | `rhenium_scrubbed_solution` | `SimpleFluidMaterial` | 909 |
| `RheniumSeparationMixture` | `rhenium_separation_mixture` | `SimpleFluidMaterial` | 908 |
| `RheniumSulfuricSolution` | `rhenium_sulfuric_solution` | `SimpleFluidMaterial` | 585 |
| `RichNitrogenMix` | `rich_nitrogen_mix` | `SimpleFluidMaterial` | 637 |
| `RnCrackedHeavyENaquadah` | `rn_cracked_heavy_e_naquadah` | `SimpleFluidMaterial` | 508 |
| `RnCrackedLightNaquadah` | `rn_cracked_light_e_naquadah` | `SimpleFluidMaterial` | 506 |
| `RnCrackedMediumENaquadah` | `rn_cracked_medium_e_naquadah` | `SimpleFluidMaterial` | 507 |
| `ScandiumTitanium50Mix` | `scandium_titanium50_mix` | `SimpleFluidMaterial` | 883 |
| `SeaborgiumDopedNanotubes` | `seaborgium_doped_nanotubes` | `SimpleFluidMaterial` | 844 |
| `SeaWater` | `sea_water` | `SimpleFluidMaterial` | 706 |
| `SeleniteSolution` | `selenite_solution` | `SimpleFluidMaterial` | 751 |
| `SeleniteTelluriteMix` | `selenite_tellurite_mixture` | `SimpleFluidMaterial` | 750 |
| `SelenousAcid` | `selenous_acid` | `SimpleFluidMaterial` | 961 |
| `SemisolidHydrogen` | `semisolidhydrogen` | `SimpleFluidMaterial` | 954 |
| `SeparatedMetalSlurry` | `separated_metal_slurry` | `SimpleFluidMaterial` | 665 |
| `SilicaGelBase` | `silica_gel_base` | `SimpleFluidMaterial` | 554 |
| `SiliconFluoride` | `silicon_fluoride` | `SimpleFluidMaterial` | 573 |
| `Silvertetrafluoroborate` | `silvertetrafluoroborate` | `SimpleFluidMaterial` | 613 |
| `SmGdOxidesSolution` | `sm_gd_oxides_solution` | `SimpleFluidMaterial` | 527 |
| `Soap` | `soap` | `SimpleFluidMaterial` | 948 |
| `SodiumAcetate` | `sodium_acetate` | `SimpleFluidMaterial` | 619 |
| `SodiumAlginateSolution` | `sodium_alginate_solution` | `SimpleFluidMaterial` | 929 |
| `SodiumCarbonateSolution` | `sodium_carbonate_solution` | `SimpleFluidMaterial` | 633 |
| `SodiumChromateSolution` | `sodium_chromate_solution` | `SimpleFluidMaterial` | 635 |
| `SodiumCyanide` | `sodium_cyanide` | `SimpleFluidMaterial` | 581 |
| `SodiumDichromateSolution` | `sodium_dichromate_solution` | `SimpleFluidMaterial` | 636 |
| `SodiumFreeBrine` | `sodium_free_brine` | `SimpleFluidMaterial` | 709 |
| `SodiumHexafluoroaluminate` | `sodium_hexafluoroaluminate` | `SimpleFluidMaterial` | 632 |
| `SodiumHydroxideBauxite` | `sodium_hydroxide_bauxite` | `SimpleFluidMaterial` | 753 |
| `SodiumHydroxideSolution` | `sodium_hydroxide_solution` | `SimpleFluidMaterial` | 680 |
| `SodiumLithiumSolution` | `sodium_lithium_solution` | `SimpleFluidMaterial` | 712 |
| `SodiumNitrateSolution` | `sodium_nitrate_solution` | `SimpleFluidMaterial` | 595 |
| `SodiumSulfateSolution` | `sodium_sulfate_solution` | `SimpleFluidMaterial` | 634 |
| `SodiumThiocyanate` | `sodium_thiocyanate` | `SimpleFluidMaterial` | 687 |
| `SOX2Gene` | `sox_2_gene` | `SimpleFluidMaterial` | 564 |
| `StearicAcid` | `stearicacid` | `SimpleFluidMaterial` | 950 |
| `Succinaldehyde` | `succinaldehyde` | `SimpleFluidMaterial` | 887 |
| `Sulfanilamide` | `sulfanilamide` | `SimpleFluidMaterial` | 553 |
| `SulfuricBromineSolution` | `sulfuric_bromine_solution` | `SimpleFluidMaterial` | 518 |
| `SupercooledCryotheum` | `supercooled_cryotheum` | `SimpleFluidMaterial` | 530 |
| `SupercriticalCO2` | `supercritcal_co2` | `SimpleFluidMaterial` | 893 |
| `SupercriticalDeuterium` | `supercritical_deuterium` | `SimpleFluidMaterial` | 912 |
| `SupercriticalFLiBe` | `supercritical_flibe` | `SimpleFluidMaterial` | 916 |
| `SupercriticalFLiNaK` | `supercritical_flinak` | `SimpleFluidMaterial` | 915 |
| `SupercriticalLeadBismuthEutectic` | `supercritical_lead_bismuth_eutectic` | `SimpleFluidMaterial` | 917 |
| `SupercriticalSodium` | `supercritical_sodium` | `SimpleFluidMaterial` | 914 |
| `SupercriticalSodiumPotassiumAlloy` | `supercritical_sodium_potassium_alloy` | `SimpleFluidMaterial` | 913 |
| `SupercriticalSteam` | `supercritical_steam` | `SimpleFluidMaterial` | 911 |
| `SuperfluidHelium` | `superfluid_helium` | `SimpleFluidMaterial` | 898 |
| `SuperheavyMix` | `superheavy_mix` | `SimpleFluidMaterial` | 725 |
| `TannicAcid` | `tannic_acid` | `SimpleFluidMaterial` | 677 |
| `TaraniumEnrichedLHelium3` | `taranium_enriched_liquid_helium3` | `SimpleFluidMaterial` | 992 |
| `TaraniumPoorLiquidHelium` | `taranium_poor_liquid_helium` | `SimpleFluidMaterial` | 998 |
| `TaraniumPoorLiquidHeliumMix` | `taranium_poor_liquid_helium_mix` | `SimpleFluidMaterial` | 1001 |
| `TaraniumRichDustyHeliumPlasma` | `taranium_rich_dusty_helium_plasma` | `SimpleFluidMaterial` | 995 |
| `TaraniumRichHelium4` | `taranium_rich_helium_4` | `SimpleFluidMaterial` | 997 |
| `TbHoOxidesSolution` | `tb_ho_oxides_solution` | `SimpleFluidMaterial` | 528 |
| `Tertbutanol` | `tertbutanol` | `SimpleFluidMaterial` | 867 |
| `TertButylAzidoformate` | `tertbuthylcarbonylazide` | `SimpleFluidMaterial` | 869 |
| `TetraethylammoniumBromide` | `tetraethylammonium_bromide` | `SimpleFluidMaterial` | 864 |
| `TetrafluoroboricAcid` | `tetrafluoroboric_acid` | `SimpleFluidMaterial` | 967 |
| `Tetrahydrofuran` | `tetrahydrofuran` | `SimpleFluidMaterial` | 965 |
| `ThalliumSulfateSolution` | `thallium_sulfate` | `SimpleFluidMaterial` | 673 |
| `TheonylTrifluoroacetate` | `theonyl_trifluoroacetate` | `SimpleFluidMaterial` | 977 |
| `ThiocyanicAcid` | `thiocyanic_acid` | `SimpleFluidMaterial` | 656 |
| `ThionylChloride` | `thionyl_chloride` | `SimpleFluidMaterial` | 607 |
| `Titanium44` | `titanium44` | `SimpleFluidMaterial` | 743 |
| `Titanium50Tetrafluoride` | `titanium50_tetrafluoride` | `SimpleFluidMaterial` | 729 |
| `TitaniumIsopropoxide` | `titanium_isopropoxide` | `SimpleFluidMaterial` | 804 |
| `TitaniumTetrafluoride` | `titanium_tetrafluoride` | `SimpleFluidMaterial` | 728 |
| `TitanylSulfate` | `titanyl_sulfate` | `SimpleFluidMaterial` | 760 |
| `TolueneDiisocyanate` | `toluene_diisocyanate` | `SimpleFluidMaterial` | 787 |
| `Toluenesulfonate` | `toluenesulfonate` | `SimpleFluidMaterial` | 840 |
| `Toluidine` | `toluidine` | `SimpleFluidMaterial` | 956 |
| `Triaminoethaneamine` | `triaminoethaneamine` | `SimpleFluidMaterial` | 868 |
| `Tributylamine` | `tributylamine` | `SimpleFluidMaterial` | 819 |
| `TributylPhosphate` | `tributyl_phosphate` | `SimpleFluidMaterial` | 577 |
| `Trichloroferane` | `trichloroferane` | `SimpleFluidMaterial` | 890 |
| `Triethylamine` | `triethylamine` | `SimpleFluidMaterial` | 966 |
| `Trimethylamine` | `trimetylamine` | `SimpleFluidMaterial` | 699 |
| `Trimethylchlorosilane` | `trimethylchlorosilane` | `SimpleFluidMaterial` | 796 |
| `Trimethylsilane` | `trimethylsilane` | `SimpleFluidMaterial` | 873 |
| `TrimethyltinChloride` | `trimethyltin_chloride` | `SimpleFluidMaterial` | 721 |
| `Trioctylamine` | `trioctylamine` | `SimpleFluidMaterial` | 907 |
| `Trioctylphosphine` | `trioctylphosphine` | `SimpleFluidMaterial` | 951 |
| `TritiumHydride` | `tritium_hydride` | `SimpleFluidMaterial` | 987 |
| `Turpentine` | `turpentine` | `SimpleFluidMaterial` | 531 |
| `UltraacidicResidueSolution` | `ultraacidic_residue_solution` | `SimpleFluidMaterial` | 989 |
| `UnprocessedNdYAGSolution` | `unprocessed_ndyag_solution` | `SimpleFluidMaterial` | 822 |
| `UsedDrillingMud` | `used_drilling_mud` | `SimpleFluidMaterial` | 786 |
| `VanadiumWasteSolution` | `vanadium_waste_solution` | `SimpleFluidMaterial` | 624 |
| `ViscoelasticPolyurethane` | `viscoelastic_polyurethane` | `SimpleFluidMaterial` | 780 |
| `ViscoelasticPolyurethaneFoam` | `viscoelastic_polyurethane_foam` | `SimpleFluidMaterial` | 781 |
| `VitaminA` | `vitamin_a` | `SimpleFluidMaterial` | 538 |
| `WaterAgarMix` | `water_agar_mix` | `SimpleFluidMaterial` | 557 |
| `WetEthyleneOxide` | `wet_etylene_oxide` | `SimpleFluidMaterial` | 774 |
| `WetFormamide` | `wet_formamide` | `SimpleFluidMaterial` | 690 |
| `XenicAcid` | `xenic_acid` | `SimpleFluidMaterial` | 990 |
| `XenoauricFluoroantimonicAcid` | `xenoauric_fluoroantimonic_acid` | `SimpleFluidMaterial` | 479 |
| `XenonTrioxide` | `xenon_trioxide` | `SimpleFluidMaterial` | 467 |
| `Ytterbium178` | `ytterbium_178` | `SimpleFluidMaterial` | 738 |
| `ZincAmalgam` | `zinc_amalgam` | `SimpleFluidMaterial` | 671 |
| `ZincExhaustMixture` | `zinc_exhaust_mixture` | `SimpleFluidMaterial` | 661 |
| `ZincPoorMix` | `zinc_poor_mix` | `SimpleFluidMaterial` | 667 |
| `ZincSlagSlurry` | `zinc_slag_slurry` | `SimpleFluidMaterial` | 662 |
| `ZirconChlorinatingResidue` | `zircon_chlorinating_residue` | `SimpleFluidMaterial` | 660 |
| `ZrHfChloride` | `zrhf_chloride` | `SimpleFluidMaterial` | 658 |
| `ZrHfOxyChloride` | `zrhf_oxychloride` | `SimpleFluidMaterial` | 659 |
| `ZrHfSeparationMix` | `zrhf_separation_mix` | `SimpleFluidMaterial` | 657 |

### Simple Dust Materials (417)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `Acetamide` | `acetamide` | `SimpleDustMaterial` | 1409 |
| `Acetonitrile` | `acetonitrile` | `SimpleDustMaterial` | 1410 |
| `AcidicLeachedPyrochlore` | `acidic_leached_pyrochlore` | `SimpleDustMaterial` | 1194 |
| `AcrylicFibers` | `acrylic_fibers` | `SimpleDustMaterial` | 1143 |
| `ActiniumHydride` | `actinium_hydride` | `SimpleDustMaterial` | 1216 |
| `ActiniumNitrate` | `actinium_nitrate` | `SimpleDustMaterial` | 1435 |
| `ActiniumOxalate` | `actinium_oxalate` | `SimpleDustMaterial` | 1215 |
| `ActiniumTriniumHydroxides` | `actinium_trinium_hydroxides` | `SimpleDustMaterial` | 1433 |
| `Actinoids` | `actinoids` | `SimpleDustMaterial` | 1358 |
| `AdipicAcid` | `adipic_acid` | `SimpleDustMaterial` | 1341 |
| `AlizarineCyanineGreen` | `alizarine_cyanine_green` | `SimpleDustMaterial` | 1301 |
| `Alkalis` | `alkalis` | `SimpleDustMaterial` | 1359 |
| `Alumina` | `alumina` | `SimpleDustMaterial` | 1054 |
| `AluminiumChloride` | `aluminium_chloride` | `SimpleDustMaterial` | 1088 |
| `AluminiumComplex` | `aluminium_complex` | `SimpleDustMaterial` | 1208 |
| `AluminiumHydride` | `aluminium_hydride` | `SimpleDustMaterial` | 1154 |
| `AluminiumHydroxide` | `aluminium_hydroxide` | `SimpleDustMaterial` | 1113 |
| `AluminiumNitrate` | `aluminium_nitrate` | `SimpleDustMaterial` | 1263 |
| `AluminiumSulfate` | `aluminium_sulfate` | `SimpleDustMaterial` | 1124 |
| `AluminiumTrifluoride` | `aluminium_trifluoride` | `SimpleDustMaterial` | 1068 |
| `Aminoanthraquinone` | `aminoanthraquinone` | `SimpleDustMaterial` | 1302 |
| `AmmoniumAcetate` | `ammonium_acetate` | `SimpleDustMaterial` | 1408 |
| `AmmoniumBifluoride` | `ammonium_bifluoride` | `SimpleDustMaterial` | 1271 |
| `AmmoniumCarbonate` | `ammonium_carbonate` | `SimpleDustMaterial` | 1276 |
| `AmmoniumManganesePhosphate` | `ammonium_manganese_phosphate` | `SimpleDustMaterial` | 1291 |
| `AmmoniumVanadate` | `ammonium_vanadate` | `SimpleDustMaterial` | 1100 |
| `AnodicSlime` | `anodic_slime` | `SimpleDustMaterial` | 1180 |
| `Anthraquinone` | `anthraquinone` | `SimpleDustMaterial` | 1314 |
| `AntimonyTrichloride` | `antimony_trichloride` | `SimpleDustMaterial` | 1371 |
| `ApatiteSolidResidue` | `apatite_solid_residue` | `SimpleDustMaterial` | 1270 |
| `AuPdCCatalyst` | `aupdc_catalyst` | `SimpleDustMaterial` | 1092 |
| `AuricFluoride` | `auric_fluoride` | `SimpleDustMaterial` | 1016 |
| `BariumAluminate` | `barium_aluminate` | `SimpleDustMaterial` | 1105 |
| `BariumCarbonate` | `barium_carbonate` | `SimpleDustMaterial` | 1103 |
| `BariumChloride` | `barium_chloride` | `SimpleDustMaterial` | 1251 |
| `BariumDifluoride` | `barium_difluoride` | `SimpleDustMaterial` | 1066 |
| `BariumHydroxide` | `barium_hydroxide` | `SimpleDustMaterial` | 1125 |
| `BariumNitrate` | `barium_nitrate` | `SimpleDustMaterial` | 1212 |
| `BariumOxide` | `barium_oxide` | `SimpleDustMaterial` | 1104 |
| `BariumPeroxide` | `barium_peroxide` | `SimpleDustMaterial` | 1197 |
| `BariumStrontiumTitanate` | `barium_strontium_titanate` | `SimpleDustMaterial` | 1249 |
| `BariumTriflate` | `barium_triflate` | `SimpleDustMaterial` | 1232 |
| `BCEPellet` | `bce_pellet` | `SimpleDustMaterial` | 1204 |
| `Benzophenanthrenylacetonitrile` | `benzophenanthrenylacetonitrile` | `SimpleDustMaterial` | 1076 |
| `BetaPinene` | `beta_pinene` | `SimpleDustMaterial` | 1035 |
| `BETS` | `bets` | `SimpleDustMaterial` | 1256 |
| `BETSPerrhenate` | `bets_perrhenate` | `SimpleDustMaterial` | 1384 |
| `BifidobacteriumBreve` | `bifidobacterium_breve` | `SimpleDustMaterial` | 1053 |
| `Bipyridine` | `bipyridine` | `SimpleDustMaterial` | 1169 |
| `BismuthChloride` | `bismuth_chloride` | `SimpleDustMaterial` | 1063 |
| `BismuthGermanate` | `bismuth_germanate` | `SimpleDustMaterial` | 1345 |
| `BismuthVanadate` | `bismuth_vanadate` | `SimpleDustMaterial` | 1282 |
| `BlueHalideMix` | `blue_halide_mix` | `SimpleDustMaterial` | 1228 |
| `BorocarbideDust` | `borocarbide_dust` | `SimpleDustMaterial` | 1386 |
| `BoronCarbide` | `boron_carbide` | `SimpleDustMaterial` | 1238 |
| `BoronFranciumCarbide` | `boron_francium_carbide` | `SimpleDustMaterial` | 1239 |
| `BoronOxide` | `boron_oxide` | `SimpleDustMaterial` | 1258 |
| `BrevibacteriumFlavium` | `brevibacterium_flavium` | `SimpleDustMaterial` | 1050 |
| `Bromosuccinimide` | `bromo_succinimide` | `SimpleDustMaterial` | 1075 |
| `BrownAlgae` | `brown_algae` | `SimpleDustMaterial` | 1043 |
| `BurnedSienna` | `burned_siena` | `SimpleDustMaterial` | 1281 |
| `CadmiumSulfide` | `cadmium_sulfide` | `SimpleDustMaterial` | 1277 |
| `CadmiumTungstate` | `cadmium_tungstate` | `SimpleDustMaterial` | 1348 |
| `CadmiumZincDust` | `cadmium_zinc_dust` | `SimpleDustMaterial` | 1135 |
| `CaesiumHydroxide` | `caesium_hydroxide` | `SimpleDustMaterial` | 1112 |
| `CaesiumNitrate` | `caesium_nitrate` | `SimpleDustMaterial` | 1431 |
| `CalciumAlginate` | `calcium_alginate` | `SimpleDustMaterial` | 1382 |
| `CalciumCarbide` | `calcium_carbide` | `SimpleDustMaterial` | 1033 |
| `CalciumCyanamide` | `calcium_cyanamide` | `SimpleDustMaterial` | 1365 |
| `CalciumMagnesiumSalts` | `camg_salts` | `SimpleDustMaterial` | 1148 |
| `CalciumSalts` | `calcium_salts` | `SimpleDustMaterial` | 1145 |
| `CarbonylPurifiedIron` | `carbonyl_purified_iron` | `SimpleDustMaterial` | 1231 |
| `CassiteriteCokePellets` | `cassiterite_coke_pellets` | `SimpleDustMaterial` | 1198 |
| `Cellulose` | `cellulose` | `SimpleDustMaterial` | 1140 |
| `CeriumOxide` | `cerium_oxide` | `SimpleDustMaterial` | 1021 |
| `CesiumCarborane` | `cesium_carborane` | `SimpleDustMaterial` | 1429 |
| `CesiumCarboranePrecusor` | `cesium_carborane_precursor` | `SimpleDustMaterial` | 1428 |
| `CesiumIodide` | `cesium_iodide` | `SimpleDustMaterial` | 1346 |
| `ChargedCesiumCeriumCobaltIndium` | `charged_cesium_cerium_cobalt_indium` | `SimpleDustMaterial` | 1369 |
| `ChromeOrange` | `chrome_orange` | `SimpleDustMaterial` | 1294 |
| `ChromeYellow` | `chrome_yellow` | `SimpleDustMaterial` | 1293 |
| `ChromiumIIIOxide` | `chromium_iii_oxide` | `SimpleDustMaterial` | 1353 |
| `CleanInertResidues` | `clean_inert_residues` | `SimpleDustMaterial` | 1453 |
| `CoAcABCatalyst` | `coacab_catalyst` | `SimpleDustMaterial` | 1396 |
| `CobaltAluminate` | `cobalt_aluminate` | `SimpleDustMaterial` | 1288 |
| `CobaltZincOxide` | `cobalt_zinc_oxide` | `SimpleDustMaterial` | 1286 |
| `ColumbiteMinorOxideResidue` | `columbite_minor_oxide_residue` | `SimpleDustMaterial` | 1191 |
| `CopperArsenite` | `copper_arsenite` | `SimpleDustMaterial` | 1283 |
| `CopperChloride` | `copper_chloride` | `SimpleDustMaterial` | 1062 |
| `CopperGalliumIndiumMix` | `copper_gallium_indium_mix` | `SimpleDustMaterial` | 1205 |
| `CopperGalliumIndiumSelenide` | `copper_gallium_indium_selenide` | `SimpleDustMaterial` | 1206 |
| `CopperLeach` | `copper_leach` | `SimpleDustMaterial` | 1399 |
| `CopperNitrate` | `copper_nitrate` | `SimpleDustMaterial` | 1211 |
| `CrudeHexanitroHexaazaisowurtzitane` | `crude_hexanitrohexaazaisowurtzitane` | `SimpleDustMaterial` | 1421 |
| `CupriavidusNecator` | `cupriavidus_necator` | `SimpleDustMaterial` | 1055 |
| `CyanIndigoDye` | `cyan_indigo_dye` | `SimpleDustMaterial` | 1308 |
| `Cyanonaphthalene` | `cyanonaphthalene` | `SimpleDustMaterial` | 1093 |
| `Decaborane` | `decaborane` | `SimpleDustMaterial` | 1427 |
| `DehydratedLignite` | `dehydrated_lignite` | `SimpleDustMaterial` | 1203 |
| `DehydrogenationCatalyst` | `dehydrogenation_catalyst` | `SimpleDustMaterial` | 1156 |
| `DiamagneticResidues` | `diamagnetic_residues` | `SimpleDustMaterial` | 1440 |
| `DiaminostilbenedisulfonicAcid` | `diaminostilbenedisulfonic_acid` | `SimpleDustMaterial` | 1295 |
| `DianilineterephthalicAcid` | `dianilineterephthalic_acid` | `SimpleDustMaterial` | 1298 |
| `DiarylideYellow` | `diarylide_yellow` | `SimpleDustMaterial` | 1300 |
| `DibenzylTetraacetylhexaazaisowurtzitane` | `dibenzyltetraacetylhexaazaisowurtzitane` | `SimpleDustMaterial` | 1414 |
| `Dichlorocycloctadieneplatinium` | `dichlorocyclooctadieneplatinium` | `SimpleDustMaterial` | 1174 |
| `Difluorobenzophenone` | `difluorobenzophenone` | `SimpleDustMaterial` | 1087 |
| `Diiodobiphenyl` | `diiodobiphenyl` | `SimpleDustMaterial` | 1168 |
| `Diketopyrrolopyrrole` | `diketopyrrolopyrrole` | `SimpleDustMaterial` | 1304 |
| `Dimethylaminopyridine` | `dimethylaminopyridine` | `SimpleDustMaterial` | 1080 |
| `DirectBrown` | `direct_brown` | `SimpleDustMaterial` | 1297 |
| `DisodiumPhosphate` | `sodium_diphosphate` | `SimpleDustMaterial` | 1142 |
| `DitertbutylDicarbonate` | `ditertbutyl_dicarbonate` | `SimpleDustMaterial` | 1329 |
| `DryGrapheneGel` | `dry_graphene_gel` | `SimpleDustMaterial` | 1355 |
| `DryRedAlgae` | `dry_red_algae` | `SimpleDustMaterial` | 1045 |
| `Durene` | `durene` | `SimpleDustMaterial` | 1401 |
| `DysprosiumOxide` | `dysprosium_oxide` | `SimpleDustMaterial` | 1026 |
| `EnrichedNaquadahConcentrate` | `enriched_naquadah_concentrate` | `SimpleDustMaterial` | 1014 |
| `ErbiumDopedZBLANDust` | `erbium_doped_zblan_dust` | `SimpleDustMaterial` | 1071 |
| `ErbiumOxide` | `erbium_oxide` | `SimpleDustMaterial` | 1028 |
| `ErbiumTrifluoride` | `erbium_trifluoride` | `SimpleDustMaterial` | 1069 |
| `Erythrosine` | `erythrosine` | `SimpleDustMaterial` | 1310 |
| `EschericiaColi` | `eschericia_coli` | `SimpleDustMaterial` | 1052 |
| `EuropiumOxide` | `europium_oxide` | `SimpleDustMaterial` | 1022 |
| `ExoticHeavyResidues` | `exotic_heavy_residues` | `SimpleDustMaterial` | 1452 |
| `FeCrOCatalyst` | `fecro_catalyst` | `SimpleDustMaterial` | 1117 |
| `FerromagneticResidues` | `ferromagnetic_residues` | `SimpleDustMaterial` | 1444 |
| `FinelyPowderedRutile` | `finely_powdered_rutile` | `SimpleDustMaterial` | 1455 |
| `FineZincSlagDust` | `fine_zinc_slag_dust` | `SimpleDustMaterial` | 1133 |
| `Fluorescein` | `fluorescein` | `SimpleDustMaterial` | 1309 |
| `FluorideBatteryElectrolyte` | `fluoride_battery_electrolyte` | `SimpleDustMaterial` | 1379 |
| `FluoroapatiteSolidResidue` | `fluoroapatite_solid_residue` | `SimpleDustMaterial` | 1269 |
| `Fluorocarborane` | `fluorocarborane` | `SimpleDustMaterial` | 1430 |
| `FranciumCaesiumCadmiumBromide` | `francium_caesium_cadmium_bromide` | `SimpleDustMaterial` | 1324 |
| `FranciumCarbide` | `francium_carbide` | `SimpleDustMaterial` | 1237 |
| `FrozenAgarCrystals` | `frozen_agar_crystals` | `SimpleDustMaterial` | 1048 |
| `Fructose` | `fructose` | `SimpleDustMaterial` | 1162 |
| `Fullerene` | `fullerene` | `SimpleDustMaterial` | 1078 |
| `FullereneSuperconductiveDust` | `fullerene_superconductor_dust` | `SimpleDustMaterial` | 1389 |
| `FusedColumbite` | `fused_columbite` | `SimpleDustMaterial` | 1187 |
| `FusedTantalite` | `fused_tantalite` | `SimpleDustMaterial` | 1189 |
| `GadoliniumOxide` | `gadolinium_oxide` | `SimpleDustMaterial` | 1023 |
| `GalliumChloride` | `gallium_chloride` | `SimpleDustMaterial` | 1377 |
| `GalliumIodide` | `gallium_iodide` | `SimpleDustMaterial` | 1225 |
| `GermaniumOxide` | `germanium_oxide` | `SimpleDustMaterial` | 1141 |
| `GermaniumSulfide` | `germanium_sulfide` | `SimpleDustMaterial` | 1344 |
| `Glucosamine` | `glucosamine` | `SimpleDustMaterial` | 1153 |
| `Glucose` | `glucose` | `SimpleDustMaterial` | 1163 |
| `Glutamine` | `glutamine` | `SimpleDustMaterial` | 1037 |
| `GoldDepleteMolybdenite` | `gold_deplete_molybdenite` | `SimpleDustMaterial` | 1059 |
| `GoldLeach` | `gold_leach` | `SimpleDustMaterial` | 1400 |
| `GrapheneAlignedCNT` | `graphene_aligned_cnt` | `SimpleDustMaterial` | 1176 |
| `GrapheneGelSuspension` | `graphene_gel_suspension` | `SimpleDustMaterial` | 1354 |
| `GrapheneNanotubeMix` | `graphene_nanotube_mix` | `SimpleDustMaterial` | 1175 |
| `GrapheneOxidationResidue` | `graphene_oxidation_residue` | `SimpleDustMaterial` | 1115 |
| `GrapheneOxide` | `graphene_oxide` | `SimpleDustMaterial` | 1351 |
| `GraphiteOxide` | `graphite_oxide` | `SimpleDustMaterial` | 1352 |
| `GreenAlgae` | `green_algae` | `SimpleDustMaterial` | 1042 |
| `GreenHalideMix` | `green_halide_mix` | `SimpleDustMaterial` | 1229 |
| `HafniumCarbide` | `hafnium_carbide` | `SimpleDustMaterial` | 1260 |
| `HafniumChloride` | `hafnium_chloride` | `SimpleDustMaterial` | 1128 |
| `HafniumOxide` | `hafnium_oxide` | `SimpleDustMaterial` | 1126 |
| `Halloysite` | `halloysite` | `SimpleDustMaterial` | 1376 |
| `HanPurple` | `hans_purple` | `SimpleDustMaterial` | 1292 |
| `HassiumChloride` | `hassium_chloride` | `SimpleDustMaterial` | 1390 |
| `HeavyDiamagneticResidues` | `heavy_diamagnetic_residues` | `SimpleDustMaterial` | 1441 |
| `HeavyFerromagneticResidues` | `heavy_ferromagnetic_residues` | `SimpleDustMaterial` | 1445 |
| `HeavyMetallicResidues` | `heavy_metallic_residues` | `SimpleDustMaterial` | 1449 |
| `HeavyOxidizedResidues` | `heavy_oxidized_residues` | `SimpleDustMaterial` | 1451 |
| `HeavyParamagneticResidues` | `heavy_paramagnetic_residues` | `SimpleDustMaterial` | 1443 |
| `Hexabenzylhexaazaisowurtzitane` | `hexabenzylhexaazaisowurtzitane` | `SimpleDustMaterial` | 1412 |
| `Hexamethylenetetramine` | `hexamethylenetetramine` | `SimpleDustMaterial` | 1418 |
| `HexanitroHexaazaisowurtzitane` | `hexanitrohexaazaisowurtzitane` | `SimpleDustMaterial` | 1415 |
| `HolmiumOxide` | `holmium_oxide` | `SimpleDustMaterial` | 1027 |
| `HydroxylammoniumSulfate` | `hydroxylammonium_sulfate` | `SimpleDustMaterial` | 1424 |
| `IBX` | `ibx` | `SimpleDustMaterial` | 1332 |
| `IndanthroneBlue` | `indanthrone_blue` | `SimpleDustMaterial` | 1303 |
| `Indigo` | `indigo` | `SimpleDustMaterial` | 1306 |
| `IndiumHydroxide` | `indium_hydroxide` | `SimpleDustMaterial` | 1134 |
| `IndiumIodide` | `indium_iodide` | `SimpleDustMaterial` | 1224 |
| `IndiumTrifluoride` | `indium_trifluoride` | `SimpleDustMaterial` | 1011 |
| `IndiumTrioxide` | `indium_trioxide` | `SimpleDustMaterial` | 1012 |
| `InertResidues` | `inert_residues` | `SimpleDustMaterial` | 1456 |
| `IridiumCyclooctadienylChlorideDimer` | `iridium_cyclooctadienyl_chloride_dimer` | `SimpleDustMaterial` | 1454 |
| `IridiumTrioxide` | `iridiumtrioxide` | `SimpleDustMaterial` | 1405 |
| `IronIodide` | `iron_iodide` | `SimpleDustMaterial` | 1219 |
| `IronPlatinumCatalyst` | `iron_platinum_catalyst` | `SimpleDustMaterial` | 1209 |
| `IronSulfateDust` | `iron_sulfate_dust` | `SimpleDustMaterial` | 1199 |
| `Lanthanoids` | `lanthanoids` | `SimpleDustMaterial` | 1357 |
| `LanthanumCalciumManganate` | `lanthanum_gallium_manganate` | `SimpleDustMaterial` | 1207 |
| `LanthanumEmbeddedFullerene` | `lanthanum_embedded_fullerene` | `SimpleDustMaterial` | 1218 |
| `LanthanumFullereneMix` | `lanthanum_fullerene_mix` | `SimpleDustMaterial` | 1217 |
| `LanthanumFullereneNanotubes` | `lanthanum_fullerene_nanotubes` | `SimpleDustMaterial` | 1393 |
| `LanthanumNickelOxide` | `lanthanum_nickel_oxide` | `SimpleDustMaterial` | 1380 |
| `LanthanumOxide` | `lanthanum_oxide` | `SimpleDustMaterial` | 1018 |
| `LanthanumTrifluoride` | `lanthanum_trifluoride` | `SimpleDustMaterial` | 1067 |
| `LeachedColumbite` | `leached_columbite` | `SimpleDustMaterial` | 1188 |
| `LeachedPyrochlore` | `leached_pyrochlore` | `SimpleDustMaterial` | 1193 |
| `LeachedTantalite` | `leached_tantalite` | `SimpleDustMaterial` | 1190 |
| `LeadChloride` | `lead_chloride` | `SimpleDustMaterial` | 1064 |
| `LeadNitrateCalciumMixture` | `lead_nitrate_calcium_mixture` | `SimpleDustMaterial` | 1164 |
| `LeadScandiumTantalate` | `lead_scandium_tantalate` | `SimpleDustMaterial` | 1255 |
| `LeadSenenide` | `lead_selenide` | `SimpleDustMaterial` | 1254 |
| `Legendarium` | `legendarium` | `SimpleDustMaterial` | 1392 |
| `LightTranstionMetals` | `light_transition_metals` | `SimpleDustMaterial` | 1361 |
| `LithiumAluminiumFluoride` | `lithium_aluminium_fluoride` | `SimpleDustMaterial` | 1259 |
| `LithiumAluminiumHydride` | `lithium_aluminium_hydride` | `SimpleDustMaterial` | 1150 |
| `LithiumCobaltOxide` | `lithium_cobalt_oxide` | `SimpleDustMaterial` | 1372 |
| `LithiumHydride` | `lithium_hydride` | `SimpleDustMaterial` | 1315 |
| `LithiumHydroxide` | `lithium_hydroxide` | `SimpleDustMaterial` | 1321 |
| `LithiumIodide` | `lithium_iodide` | `SimpleDustMaterial` | 1338 |
| `LithiumNiobateNanoparticles` | `lithium_niobate_nanoparticles` | `SimpleDustMaterial` | 1320 |
| `Lithiumthiinediselenide` | `lithiumthiinediselenide` | `SimpleDustMaterial` | 1121 |
| `LithiumTriflate` | `lithium_triflate` | `SimpleDustMaterial` | 1373 |
| `LutetiumOxide` | `lutetium_oxide` | `SimpleDustMaterial` | 1031 |
| `LuTmYVONanoparticles` | `lutm_yvo_nanoparticles` | `SimpleDustMaterial` | 1274 |
| `LuTmYVOPrecipitate` | `lutm_yvo_precipitate` | `SimpleDustMaterial` | 1279 |
| `MagnesiumFluoride` | `magnesium_fluoride` | `SimpleDustMaterial` | 1318 |
| `MagnesiumSulfate` | `magnesium_sulfate` | `SimpleDustMaterial` | 1158 |
| `MagnetorestrictiveAlloy` | `magnetorestrictive_alloy` | `SimpleDustMaterial` | 1257 |
| `ManganeseFluoride` | `manganese_fluoride` | `SimpleDustMaterial` | 1343 |
| `ManganeseIIIOxide` | `manganese_iii_oxide` | `SimpleDustMaterial` | 1311 |
| `ManganeseSulfate` | `manganese_sulfate` | `SimpleDustMaterial` | 1183 |
| `Mauveine` | `mauveine` | `SimpleDustMaterial` | 1305 |
| `MercuryAcetate` | `mercury_acetate` | `SimpleDustMaterial` | 1364 |
| `MercuryChloride` | `mercury_chloride` | `SimpleDustMaterial` | 1312 |
| `MercuryIodide` | `mercury_iodide` | `SimpleDustMaterial` | 1284 |
| `MetallicResidues` | `metallic_residues` | `SimpleDustMaterial` | 1448 |
| `Methylbenzophenanthrene` | `methylbenzophenanthrene` | `SimpleDustMaterial` | 1096 |
| `MgClBromide` | `mgcl_bromide` | `SimpleDustMaterial` | 1083 |
| `MixedAstatideSalts` | `mixed_astatide_salts` | `SimpleDustMaterial` | 1240 |
| `MolybdenumConcentrate` | `molybdenum_concentrate` | `SimpleDustMaterial` | 1060 |
| `MolybdenumTrioxide` | `molybdenum_trioxide` | `SimpleDustMaterial` | 1061 |
| `NaquadahConcentrate` | `naquadah_concentrate` | `SimpleDustMaterial` | 1013 |
| `NaquadriaConcentrate` | `naquadria_concentrate` | `SimpleDustMaterial` | 1015 |
| `NbTaContainingDust` | `nbta_containing_dust` | `SimpleDustMaterial` | 1185 |
| `NdYAGNanoparticles` | `nd_yag_nanoparticles` | `SimpleDustMaterial` | 1265 |
| `NeodymiumDopedYttrium` | `neodymium_doped_yttrium` | `SimpleDustMaterial` | 1264 |
| `NeodymiumOxide` | `neodymium_oxide` | `SimpleDustMaterial` | 1020 |
| `NHydroxysuccinimide` | `n-hydroxysuccinimide` | `SimpleDustMaterial` | 1411 |
| `NiAlCatalyst` | `nial_catalyst` | `SimpleDustMaterial` | 1177 |
| `NiAlOCatalyst` | `nialo_catalyst` | `SimpleDustMaterial` | 1116 |
| `NickelChloride` | `nickel_chloride` | `SimpleDustMaterial` | 1122 |
| `NickelOxideHydroxide` | `nickel_oxide_hydroxide` | `SimpleDustMaterial` | 1383 |
| `NickelTriphenylPhosphite` | `nickel_triphenyl_phosphite` | `SimpleDustMaterial` | 1173 |
| `Nigrosin` | `nigrosin` | `SimpleDustMaterial` | 1296 |
| `NiobiumChloride` | `niobium_chloride` | `SimpleDustMaterial` | 1316 |
| `NiobiumHydroxide` | `niobium_hydroxide` | `SimpleDustMaterial` | 1317 |
| `NiobiumTantalumOxide` | `niobium_tantalum_oxide` | `SimpleDustMaterial` | 1186 |
| `NitroniumTetrafluoroborate` | `nitronium_tetrafluoroborate` | `SimpleDustMaterial` | 1416 |
| `NitrosoniumTetrafluoroborate` | `nitrosonium_tetrafluoroborate` | `SimpleDustMaterial` | 1417 |
| `OxidizedResidues` | `oxidized_residues` | `SimpleDustMaterial` | 1450 |
| `PalladiumAcetate` | `palladium_acetate` | `SimpleDustMaterial` | 1323 |
| `PalladiumBisDibenzylidieneacetone` | `palladium_bisdibenzylidieneacetone` | `SimpleDustMaterial` | 1171 |
| `PalladiumChloride` | `palladium_chloride` | `SimpleDustMaterial` | 1170 |
| `PalladiumLoadedRutileNanoparticles` | `palladium_loaded_rutile_nanoparticles` | `SimpleDustMaterial` | 1339 |
| `ParamagneticResidues` | `paramagnetic_residues` | `SimpleDustMaterial` | 1442 |
| `PartiallyOxidizedResidues` | `partially_oxidized_residues` | `SimpleDustMaterial` | 1447 |
| `PdCCatalyst` | `pdc_catalyst` | `SimpleDustMaterial` | 1419 |
| `PdFullereneMatrix` | `pd_fullerene_matrix` | `SimpleDustMaterial` | 1089 |
| `PdIrReOCeOS` | `pdirreoceos` | `SimpleDustMaterial` | 1081 |
| `PhosphorousPentasulfide` | `phosphorous_pentasulfide` | `SimpleDustMaterial` | 1437 |
| `PiledTBCC` | `piled_tbcc` | `SimpleDustMaterial` | 1214 |
| `PolycyclicAromaticMix` | `polycyclic_aromatic_mix` | `SimpleDustMaterial` | 1349 |
| `PolystyreneNanoParticles` | `polystryrene_nanoparticles` | `SimpleDustMaterial` | 1157 |
| `PostTransitionMetals` | `post_transition_metals` | `SimpleDustMaterial` | 1363 |
| `PotassiumBisulfite` | `potassium_bisulfite` | `SimpleDustMaterial` | 1422 |
| `PotassiumBromate` | `potassium_bromate` | `SimpleDustMaterial` | 1331 |
| `PotassiumBromide` | `potassium_bromide` | `SimpleDustMaterial` | 1330 |
| `PotassiumHydroxylaminedisulfonate` | `potassium_hydroxylaminedisulfonate` | `SimpleDustMaterial` | 1406 |
| `PotassiumMagnesiumSalts` | `kmg_salts` | `SimpleDustMaterial` | 1147 |
| `PotassiumManganate` | `potassium_manganate` | `SimpleDustMaterial` | 1250 |
| `PotassiumNitrite` | `potassium_nitrite` | `SimpleDustMaterial` | 1423 |
| `PotassiumNonahydridorhenate` | `potassium_nonahydridorhenate` | `SimpleDustMaterial` | 1337 |
| `PotassiumNonahydridotechnetate` | `potassium_nonahydridotechnetate` | `SimpleDustMaterial` | 1336 |
| `PotassiumPermanganate` | `potassium_permanganate` | `SimpleDustMaterial` | 1266 |
| `PotassiumPeroxymonosulfate` | `potassium_peroxymonosulfate` | `SimpleDustMaterial` | 1395 |
| `PotassiumPerrhenate` | `potassium_perrhenate` | `SimpleDustMaterial` | 1335 |
| `PotassiumPertechnate` | `potassium_pertechnate` | `SimpleDustMaterial` | 1334 |
| `PotassiumTetrachloroplatinate` | `potassium_tetrachloroplatinate` | `SimpleDustMaterial` | 1172 |
| `PotasssiumFluoroNiobate` | `potassium_fluoroniobate` | `SimpleDustMaterial` | 1195 |
| `PotasssiumFluoroTantalate` | `potassium_fluorotantalate` | `SimpleDustMaterial` | 1196 |
| `PraseodymiumOxide` | `praseodymium_oxide` | `SimpleDustMaterial` | 1019 |
| `PreciousMetals` | `precious_metals` | `SimpleDustMaterial` | 1360 |
| `PreFreezeAgar` | `pre_freeze_agar` | `SimpleDustMaterial` | 1047 |
| `PreZylon` | `pre_zylon` | `SimpleDustMaterial` | 1091 |
| `PrHoYLFNanoparticles` | `prho_ylf_nanoparticles` | `SimpleDustMaterial` | 1273 |
| `PureCrystallineNitricAcid` | `crystalline_nitric_acid` | `SimpleDustMaterial` | 1438 |
| `PureSodiumVanadate` | `pure_sodium_vanadate` | `SimpleDustMaterial` | 1275 |
| `PurifiedColumbite` | `purified_columbite` | `SimpleDustMaterial` | 1367 |
| `PurifiedPyrochlore` | `purified_pyrochlore` | `SimpleDustMaterial` | 1368 |
| `PyromelliticDianhydride` | `pyromellitic_dianhydride` | `SimpleDustMaterial` | 1402 |
| `Quinacridone` | `quinacridone` | `SimpleDustMaterial` | 1299 |
| `RadiumNitrate` | `radium_nitrate` | `SimpleDustMaterial` | 1436 |
| `RawSienna` | `raw_siena` | `SimpleDustMaterial` | 1280 |
| `RedAlgae` | `red_algae` | `SimpleDustMaterial` | 1044 |
| `RedAlgaePowder` | `red_algae_powder` | `SimpleDustMaterial` | 1046 |
| `RedHalideMix` | `red_halide_mix` | `SimpleDustMaterial` | 1230 |
| `RefractoryMetals` | `refractory_metals` | `SimpleDustMaterial` | 1362 |
| `RheniumChloride` | `rhenium_chloride` | `SimpleDustMaterial` | 1370 |
| `RheniumHassiumThalliumIsophtaloylbisdiethylthioureaHexafluorophosphate` | `rhenium_hassium_thallium_isophtaloylbisdiethylthiourea` | `SimpleDustMaterial` | 1391 |
| `RhodamineB` | `rhodamine_b` | `SimpleDustMaterial` | 1326 |
| `RhReNqCatalyst` | `rhrenq_catalyst` | `SimpleDustMaterial` | 1322 |
| `RoastedLepidolite` | `roasted_lepidolite` | `SimpleDustMaterial` | 1119 |
| `RoastedSpodumene` | `roasted_spodumene` | `SimpleDustMaterial` | 1118 |
| `RubidiumIodide` | `rubidium_iodide` | `SimpleDustMaterial` | 1223 |
| `SaccharicAcid` | `saccharic_acid` | `SimpleDustMaterial` | 1340 |
| `SamariumOxide` | `samarium_oxide` | `SimpleDustMaterial` | 1024 |
| `Sarcosine` | `sarcosine` | `SimpleDustMaterial` | 1084 |
| `ScandiumIodide` | `scandium_iodide` | `SimpleDustMaterial` | 1222 |
| `ScandiumOxide` | `scandium_oxide` | `SimpleDustMaterial` | 1032 |
| `ScandiumTriflate` | `scandium_triflate` | `SimpleDustMaterial` | 1233 |
| `ScheelesGreen` | `scheeles_green` | `SimpleDustMaterial` | 1287 |
| `SeaborgiumCarbide` | `seaborgium_carbide` | `SimpleDustMaterial` | 1262 |
| `SelectivelyMutatedCupriavidiusNecator` | `selectively_mutated_cupriavidius_necator` | `SimpleDustMaterial` | 1366 |
| `SeleniumOxide` | `selenium_oxide` | `SimpleDustMaterial` | 1182 |
| `Shewanella` | `shewanella` | `SimpleDustMaterial` | 1056 |
| `SilicaAluminaGel` | `silica_alumina_gel` | `SimpleDustMaterial` | 1039 |
| `SilicaGel` | `silica_gel` | `SimpleDustMaterial` | 1038 |
| `SiliconCarbide` | `silicon_carbide` | `SimpleDustMaterial` | 1058 |
| `SiliconChloride` | `silicon_chloride` | `SimpleDustMaterial` | 1127 |
| `SiliconNanoparticles` | `silicon_nanoparticles` | `SimpleDustMaterial` | 1375 |
| `SilverIodide` | `silver_iodide` | `SimpleDustMaterial` | 1432 |
| `SilverNitrate` | `silver_nitrate` | `SimpleDustMaterial` | 1179 |
| `SilverPerchlorate` | `silver_perchlorate` | `SimpleDustMaterial` | 1397 |
| `SodiumAluminiumHydride` | `sodium_aluminium_hydride` | `SimpleDustMaterial` | 1149 |
| `SodiumArsenate` | `sodium_arsenate` | `SimpleDustMaterial` | 1272 |
| `SodiumAzanide` | `sodium_azanide` | `SimpleDustMaterial` | 1151 |
| `SodiumAzide` | `sodium_azide` | `SimpleDustMaterial` | 1152 |
| `SodiumBorohydride` | `sodium_borohydride` | `SimpleDustMaterial` | 1425 |
| `SodiumBromide` | `sodium_bromide` | `SimpleDustMaterial` | 1236 |
| `SodiumChlorate` | `sodium_chlorate` | `SimpleDustMaterial` | 1398 |
| `SodiumEthoxide` | `sodium_ethoxide` | `SimpleDustMaterial` | 1082 |
| `SodiumHydride` | `sodium_hydride` | `SimpleDustMaterial` | 1155 |
| `SodiumHypochlorite` | `sodium_hypochlorite` | `SimpleDustMaterial` | 1202 |
| `SodiumIodate` | `sodium_iodate` | `SimpleDustMaterial` | 1242 |
| `SodiumIodide` | `sodium_iodide` | `SimpleDustMaterial` | 1241 |
| `SodiumMetavanadate` | `sodium_metavanadate` | `SimpleDustMaterial` | 1394 |
| `SodiumMolybdate` | `sodium_molybdate` | `SimpleDustMaterial` | 1159 |
| `SodiumOxide` | `sodium_oxide` | `SimpleDustMaterial` | 1350 |
| `SodiumPerchlorate` | `sodium_perchlorate` | `SimpleDustMaterial` | 1356 |
| `SodiumPeriodate` | `sodium_periodate` | `SimpleDustMaterial` | 1243 |
| `SodiumPertechnetate` | `sodium_pertechnetate` | `SimpleDustMaterial` | 1333 |
| `SodiumPhosphomolybdate` | `sodium_phosphomolybdate` | `SimpleDustMaterial` | 1160 |
| `SodiumPhosphotungstate` | `sodium_phosphotungstate` | `SimpleDustMaterial` | 1161 |
| `SodiumSalts` | `sodium_salts` | `SimpleDustMaterial` | 1146 |
| `SodiumSeaborgate` | `sodium_seaborgate` | `SimpleDustMaterial` | 1244 |
| `SodiumSulfanilate` | `sodium_sulfanilate` | `SimpleDustMaterial` | 1313 |
| `SodiumSulfite` | `sodium_sulfite` | `SimpleDustMaterial` | 1139 |
| `SodiumTetrafluoroborate` | `sodium_tetrafluoroborate` | `SimpleDustMaterial` | 1426 |
| `SodiumThiosulfate` | `sodium_thiosulfate` | `SimpleDustMaterial` | 1234 |
| `SodiumVanadate` | `sodium_vanadate` | `SimpleDustMaterial` | 1099 |
| `Sorbose` | `sorbose` | `SimpleDustMaterial` | 1381 |
| `Stilbene` | `stilbene` | `SimpleDustMaterial` | 1327 |
| `StoneResidueDust` | `stone_residue_dust` | `SimpleDustMaterial` | 1439 |
| `StreptococcusPyogenes` | `streptococcus_pyogenes` | `SimpleDustMaterial` | 1051 |
| `StrontiumCarbonate` | `strontium_carbonate` | `SimpleDustMaterial` | 1200 |
| `StrontiumChloride` | `strontium_chloride` | `SimpleDustMaterial` | 1246 |
| `StrontiumEuropiumAluminate` | `strontium_europium_aluminate` | `SimpleDustMaterial` | 1248 |
| `StrontiumOxide` | `strontium_oxide` | `SimpleDustMaterial` | 1166 |
| `StrontiumSuperconductorDust` | `strontium_superconductor_dust` | `SimpleDustMaterial` | 1388 |
| `SuccinicAcid` | `succinic_acid` | `SimpleDustMaterial` | 1073 |
| `SuccinicAnhydride` | `succinic_anhydride` | `SimpleDustMaterial` | 1407 |
| `Succinimide` | `succinimide` | `SimpleDustMaterial` | 1074 |
| `SuccinimidylAcetate` | `succinimidyl_acetate` | `SimpleDustMaterial` | 1413 |
| `SulfurCoatedHalloysite` | `sulfur_coated_halloysite` | `SimpleDustMaterial` | 1378 |
| `TantaliteMinorOxideResidue` | `tantalite_minor_oxide_residue` | `SimpleDustMaterial` | 1192 |
| `TantalumOxide` | `tantalum_oxide` | `SimpleDustMaterial` | 1252 |
| `TBCCODust` | `tbcco_dust` | `SimpleDustMaterial` | 1385 |
| `TelluriumOxide` | `tellurium_oxide` | `SimpleDustMaterial` | 1181 |
| `TerbiumOxide` | `terbium_oxide` | `SimpleDustMaterial` | 1025 |
| `Terephthalaldehyde` | `terephthalaldehyde` | `SimpleDustMaterial` | 1090 |
| `Tetraacetyldinitrosohexaazaisowurtzitane` | `tetraacetyldinitrosohexaazaisowurtzitane` | `SimpleDustMaterial` | 1420 |
| `Tetrabromoindigo` | `tetrabromoindigo` | `SimpleDustMaterial` | 1307 |
| `Tetracene` | `tetracene` | `SimpleDustMaterial` | 1328 |
| `TetraethylammoniumNonahydridides` | `tetraethylammonium_nonahydrides` | `SimpleDustMaterial` | 1342 |
| `ThalliumChloride` | `thallium_chloride` | `SimpleDustMaterial` | 1137 |
| `ThalliumIodide` | `thallium_iodide` | `SimpleDustMaterial` | 1221 |
| `ThalliumResidue` | `thallium_residue` | `SimpleDustMaterial` | 1136 |
| `ThuliumOxide` | `thulium_oxide` | `SimpleDustMaterial` | 1029 |
| `TiAlChloride` | `tial_chloride` | `SimpleDustMaterial` | 1079 |
| `TinChloride` | `tin_chloride` | `SimpleDustMaterial` | 1094 |
| `TinSlag` | `tin_slag` | `SimpleDustMaterial` | 1184 |
| `TitaniumCyclopentadienyl` | `titanium_cyclopentadienyl` | `SimpleDustMaterial` | 1235 |
| `TitaniumNitrate` | `titanium_nitrate` | `SimpleDustMaterial` | 1178 |
| `TitaniumYellow` | `titanium_yellow` | `SimpleDustMaterial` | 1285 |
| `TlTmCesiumIodide` | `tl_tm_cesium_iodide` | `SimpleDustMaterial` | 1347 |
| `TriniumTetrafluoride` | `trinium_tetrafluoride` | `SimpleDustMaterial` | 1434 |
| `Triphenylphosphine` | `triphenylphosphine` | `SimpleDustMaterial` | 1095 |
| `UncommonResidues` | `uncommon_residues` | `SimpleDustMaterial` | 1446 |
| `UnfoldedFullerene` | `unfolded_fullerene` | `SimpleDustMaterial` | 1077 |
| `UnprocessedNdYAGDust` | `unprocessed_ndyag_dust` | `SimpleDustMaterial` | 1278 |
| `Urea` | `urea` | `SimpleDustMaterial` | 1267 |
| `UVAHalideMix` | `uva_halide_mix` | `SimpleDustMaterial` | 1226 |
| `VanadiumOxide` | `vanadium_oxide` | `SimpleDustMaterial` | 1101 |
| `VanadiumSlag` | `vanadium_slag` | `SimpleDustMaterial` | 1097 |
| `VanadiumSlagDust` | `vanadium_slag_dust` | `SimpleDustMaterial` | 1098 |
| `WellMixedYBCOxides` | `well_mixed_ybc_oxides` | `SimpleDustMaterial` | 1213 |
| `WetZeoliteSievingPellets` | `wet_zeolite_sieving_pellets` | `SimpleDustMaterial` | 1041 |
| `WhiteHalideMix` | `white_halide_mix` | `SimpleDustMaterial` | 1227 |
| `Xylose` | `xylose` | `SimpleDustMaterial` | 1374 |
| `Yeast` | `yeast` | `SimpleDustMaterial` | 1036 |
| `YtterbiumOxide` | `ytterbium_oxide` | `SimpleDustMaterial` | 1030 |
| `YttriumEuropiumVanadate` | `yttrium_europium_vanadate` | `SimpleDustMaterial` | 1247 |
| `YttriumNitrate` | `yttrium_nitrate` | `SimpleDustMaterial` | 1210 |
| `ZBLANDust` | `zblan_dust` | `SimpleDustMaterial` | 1070 |
| `ZeoliteSievingPellets` | `zeolite_sieving_pellets` | `SimpleDustMaterial` | 1040 |
| `ZincChloride` | `zinc_chloride` | `SimpleDustMaterial` | 1138 |
| `ZincCokePellets` | `zinc_coke_pellets` | `SimpleDustMaterial` | 1129 |
| `ZincFlueDust` | `zinc_flue_dust` | `SimpleDustMaterial` | 1131 |
| `ZincLeachingResidue` | `zinc_leaching_residue` | `SimpleDustMaterial` | 1132 |
| `ZincResidualSlag` | `zinc_residual_slag` | `SimpleDustMaterial` | 1130 |
| `ZincSelenide` | `zinc_selenide` | `SimpleDustMaterial` | 1325 |
| `ZirconiumTetrachloride` | `zirconium_tetrachloride` | `SimpleDustMaterial` | 1057 |
| `ZirconiumTetrafluoride` | `zirconium_tetrafluoride` | `SimpleDustMaterial` | 1065 |
| `ZirconylChloride` | `zirconyl_chloride` | `SimpleDustMaterial` | 1253 |
| `ZnFeAlClCatalyst` | `znfealcl_catalyst` | `SimpleDustMaterial` | 1086 |

### Markers (5)

| GCY Field | GCY Id | Type | Source Line |
|---|---|---|---:|
| `MAX` | `MAX` | `MarkerMaterial` | 1478 |
| `UEV` | `UEV` | `MarkerMaterial` | 1474 |
| `UIV` | `UIV` | `MarkerMaterial` | 1475 |
| `UMV` -> `UXV` | `UMV` -> `uxv` | `MarkerMaterial` | 1476 |
| `UXV` -> `OpV` | `UXV` -> `opv` | `MarkerMaterial` | 1477 |

## Deferred Nuclear-Like

| GCY Field | GCY Id | Type | Original Section | Source Line |
|---|---|---|---|---:|
| `DenseHydrazineFuelMixture` | `dense_hydrazine_fuel_mixture` | `FluidMaterial` | `FLUID MATERIALS` | 216 |
| `RocketFuelCN3H7O3` | `rocket_fuel_b` | `FluidMaterial` | `FLUID MATERIALS` | 217 |
| `RocketFuelH8N4C2O4` | `rocket_fuel_a` | `FluidMaterial` | `FLUID MATERIALS` | 201 |
| `RP1RocketFuel` | `rocket_fuel_c` | `FluidMaterial` | `FLUID MATERIALS` | 218 |
| `ThoriumDopedTungsten` | `thoria_doped_tungsten` | `IngotMaterial` | `INGOT MATERIALS` | 424 |
| `Berkelium` | `berkelium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 84 |
| `Californium` | `californium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 85 |
| `Curium` | `curium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 83 |
| `Einsteinium` | `einsteinium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 86 |
| `Fermium` | `fermium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 87 |
| `Mendelevium` | `mendelevium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 88 |
| `MetastableFlerovium` | `metastable_flerovium` | `IngotMaterial` | `NEW ELEMENTS` | 71 |
| `MetastableHassium` | `metastable_hassium` | `IngotMaterial` | `NEW ELEMENTS` | 72 |
| `MetastableOganesson` | `metastable_oganesson` | `IngotMaterial` | `NEW ELEMENTS` | 70 |
| `Neptunium` | `neptunium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 81 |
| `PlutoniumRadioactive` | `plutonium_radioactive` | `RadioactiveMaterial` | `NEW ELEMENTS` | 82 |
| `Protactinium` | `protactinium` | `RadioactiveMaterial` | `NEW ELEMENTS` | 79 |
| `UraniumRadioactive` | `uranium_radioactive` | `RadioactiveMaterial` | `NEW ELEMENTS` | 80 |
| `Americium241` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 146 |
| `Americium243` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 147 |
| `Americium245` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 148 |
| `AmericiumRadioactive` | `` | `RadioactiveMaterial` | `NUCLEAR MATERIALS` | 145 |
| `Berkelium247` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 158 |
| `Berkelium249` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 159 |
| `Berkelium251` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 160 |
| `Californium251` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 163 |
| `Californium252` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 164 |
| `Californium253` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 165 |
| `Californium256` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 166 |
| `Californium257` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 167 |
| `Curium245` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 151 |
| `Curium246` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 152 |
| `Curium247` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 153 |
| `Curium250` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 154 |
| `Curium251` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 155 |
| `Einsteinium253` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 170 |
| `Einsteinium255` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 171 |
| `Einsteinium257` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 172 |
| `Fermium257` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 175 |
| `Fermium258` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 176 |
| `Fermium259` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 177 |
| `Fermium262` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 178 |
| `Fermium263` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 179 |
| `Mendelevium259` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 182 |
| `Mendelevium261` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 183 |
| `Mendelevium263` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 184 |
| `Neptunium235` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 133 |
| `Neptunium237` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 134 |
| `Neptunium239` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 135 |
| `Plutonium239` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 138 |
| `Plutonium240` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 139 |
| `Plutonium241Isotope` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 140 |
| `Plutonium244Isotope` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 141 |
| `Plutonium245` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 142 |
| `Protactinium233` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 123 |
| `Thorium232Isotope` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 119 |
| `Thorium233` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 120 |
| `ThoriumRadioactive` | `` | `RadioactiveMaterial` | `NUCLEAR MATERIALS` | 118 |
| `Uranium233` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 127 |
| `Uranium234` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 128 |
| `Uranium235Isotope` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 129 |
| `Uranium238Isotope` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 126 |
| `Uranium239` | `` | `IsotopeMaterial` | `NUCLEAR MATERIALS` | 130 |
| `ActiniumSuperhydride` | `actinium_superhydride` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1387 |
| `CaliforniumTrichloride` | `californiumtrichloride` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1404 |
| `CaliforniumTrioxide` | `californiumtrioxide` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1403 |
| `PotassiumUranylTricarbonate` | `potassium_uranyl_carbonate` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1106 |
| `ThUSludge` | `thorium_uranium_sludge` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1017 |
| `UraniumOxideThoriumNitrate` | `uranium_oxide_thorium_nitrate` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1111 |
| `UraniumPeroxideThoriumOxide` | `uranium_peroxide_thorium_oxide` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1107 |
| `UraniumThoriumOxide` | `uranium_thorium_oxide` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1108 |
| `UranylNitrate` | `uranyl_nitrate` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1144 |
| `UranylThoriumNitrate` | `uranium_thorium_nitrate` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1110 |
| `UranylThoriumSulfate` | `uranium_thorium_sulfate` | `SimpleDustMaterial` | `SIMPLE DUSTS` | 1109 |
| `ActiniumSuperhydridePlasma` | `actinium_superhydride_plasma` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 813 |
| `CaliforniumCyclopentadienide` | `californiumcyclopentadienide` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 947 |
| `DepletedGrowthMedium` | `depleted_growth_medium` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 559 |
| `HeavyENaquadahFuel` | `heavy_e_naquadah_fuel` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 511 |
| `HeavyNaquadahFuel` | `heavy_naquadah_fuel` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 496 |
| `HotMetastableOganesson` | `hot_oganesson` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 727 |
| `HyperFuelI` | `hyper_fluid_i` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 513 |
| `HyperFuelII` | `hyper_fluid_ii` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 514 |
| `HyperFuelIII` | `hyper_fluid_iii` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 515 |
| `HyperFuelIV` | `hyper_fluid_iv` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 516 |
| `LightENaquadahFuel` | `light_e_naquadah_fuel` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 509 |
| `LightNaquadahFuel` | `light_naquadah_fuel` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 494 |
| `MediumENaquadahFuel` | `medium_e_naquadah_fuel` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 510 |
| `MediumNaquadahFuel` | `medium_naquadah_fuel` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 495 |
| `OgannesonBreedingBase` | `og_breeding_base` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 736 |
| `PureUranylNitrateSolution` | `pure_uranyl_nitrate` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 695 |
| `PurifiedUranylNitrate` | `purified_uranyl_nitrate_solution` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 628 |
| `QuassifissioningPlasma` | `quasifissioning_plasma` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 737 |
| `TaraniumDepletedHeliumPlasma` | `taranium_depleted_helium_plasma` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 996 |
| `TaraniumDepletedLHelium3` | `taranium_depleted_liquid_helium3` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 994 |
| `TaraniumSemidepletedLHelium3` | `taranium_semidepleted_liquid_helium3` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 993 |
| `ThoriumNitrateSolution` | `thorium_nitrate_solution` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 631 |
| `UraniumDiuranate` | `uranium_diuranate` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 629 |
| `UraniumRefinementWasteSolution` | `uranium_refinement_waste_solution` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 630 |
| `UraniumSulfateWasteSolution` | `uranium_sulfate_waste_solution` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 627 |
| `UranylChlorideSolution` | `uranyl_chloride_solution` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 625 |
| `UranylNitrateSolution` | `uranyl_nitrate_solution` | `SimpleFluidMaterial` | `SIMPLE FLUID MATERIALS` | 626 |

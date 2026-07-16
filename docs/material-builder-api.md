# GTCEu Modern Material Builder API Notes

Status: working notes for migration agents. This is based on local
`gtceu-1.20.1-7.5.3-sources.jar` inspection and should be rechecked when the
GTCEu dependency version changes.

This document covers `Material.Builder`, not recipe builders.

## Registration Flow

Create the addon material registry during `MaterialRegistryEvent`:

```scala
GTCEuAPI.materialManager.createRegistry(MOD_ID)
```

Register new materials during `MaterialEvent`:

```scala
TantalumHafniumCarbide = new Material.Builder(ResourceLocation(MOD_ID, "tantalum_hafnium_carbide"))
  .ingot(4)
  .fluid()
  .color(0x6f6b55)
  .iconSet(METALLIC)
  .appendFlags(STD_METAL, GENERATE_PLATE, GENERATE_ROD)
  .components(Tantalum, 1, Hafnium, 1, Carbon, 1)
  .blast(b => b
    .temp(3900, GasTier.HIGH)
    .blastStats(VA(GTValues.EV), 1000)
  )
  .buildAndRegister()
```

Modify GTCEu-provided materials during `PostMaterialEvent`. Do not re-register
materials whose canonical id already exists in GTCEu Modern.

## Builder Entry Points

- `new Material.Builder(ResourceLocation)`: create a material builder. The id
  path must not end with `_`.
- `buildAndRegister()`: build, verify, and register the material.
- `register()`: delegates to `buildAndRegister()`.
- `langValue(name)`: override the generated English display name.

## Property Verification and EMPTY

`MaterialProperties.verify()` is a property-induction fixpoint, not a
single pass:

1. snapshot the current property values;
2. call `verifyProperty(this)` on every value in that snapshot;
3. repeat while the property-map size changes.

Property implementations use `ensureSet` during verification, so newly induced
properties are verified on a later pass. `ensureSet(key, true)` also triggers
verification immediately. This is why builder calls such as polymer, blast,
ore, and ingot can safely induce additional base properties without codegen
emitting duplicate setters.

The base-property set is FLUID, DUST, INGOT, GEM, and EMPTY. After the fixpoint:

- an entirely property-less material receives `PropertyKey.EMPTY`;
- a material with only non-base properties fails verification;
- setting a real property removes EMPTY;
- removing the final real property restores EMPTY.

EMPTY is an internal no-op placeholder declared by `PropertyKey`; there is no
builder call to author it. A material ADT with zero authored properties is
therefore valid, and the renderer should emit no synthetic property call.

## Base Material Properties

- `dust()`
- `dust(harvestLevel)`
- `dust(harvestLevel, burnTime)`
- `wood()`
- `wood(harvestLevel)`
- `wood(harvestLevel, burnTime)`
- `ingot()`
- `ingot(harvestLevel)`
- `ingot(harvestLevel, burnTime)`
- `gem()`
- `gem(harvestLevel)`
- `gem(harvestLevel, burnTime)`
- `polymer()`
- `polymer(harvestLevel)`
- `polymer(harvestLevel, burnTime)`
- `burnTime(ticks)`

`ingot`, `gem`, and `polymer` ensure a dust property. `polymer` also ensures an
ingot property and adds `FLAMMABLE`, `NO_SMASHING`, and
`DISABLE_DECOMPOSITION` during property verification. `gem` and `ingot` are
mutually exclusive. `burnTime` also ensures a dust property if one does not
exist.

The old Gregicality flag `SMELT_INTO_FLUID` does not exist in GTCEu 7.5.3's
`MaterialFlags`; do not invent a modern ref for it.

## Fluid Properties

- `fluid()`: add a default liquid.
- `fluid(key, state)`: add a fluid with a specific `FluidStorageKey` and
  `FluidState`.
- `fluid(key, FluidBuilder)`: add a fluid with a custom builder.
- `liquid()`
- `liquid(temp)`
- `liquid(FluidBuilder)`
- `gas()`
- `gas(temp)`
- `gas(FluidBuilder)`
- `plasma()`
- `plasma(temp)`
- `plasma(FluidBuilder)`

Important storage keys:

- `FluidStorageKeys.LIQUID`
- `FluidStorageKeys.GAS`
- `FluidStorageKeys.PLASMA`
- `FluidStorageKeys.MOLTEN`

Liquid and molten are different storage keys. Do not use molten unless the
migration explicitly wants a molten fluid.

Useful `FluidBuilder` calls:

- `temperature(kelvin)`
- `color(rgbOrArgb)`
- `disableColor()`
- `density(value)`
- `luminosity(0..15)`
- `viscosity(value)`
- `attribute(attribute)`
- `attributes(attributes...)`
- `customStill()`
- `textures(hasCustomStill)`
- `textures(hasCustomStill, hasCustomFlowing)`
- `block()`
- `disableBucket()`
- `name(name)`
- `translation(keyOrValue)`

## Visuals, Formula, and Composition

- `color(rgb)`: set primary color and enable fluid coloring.
- `color(rgb, hasFluidColor)`: set primary color and fluid color behavior.
- `secondaryColor(rgb)`: set secondary layer color.
- `colorAverage()`: compute color from material components.
- `iconSet(iconSet)`: set material icon set.
- `components(material, amount, ...)`: define component pairs.
- `componentStacks(stacks...)`: define components with `MaterialStack`.
- `formula(formula)`: override formula and auto-format numbers.
- `formula(formula, withFormatting)`: override formula with explicit formatting
  control.
- `element(element)`: bind an element to this material.

Common icon sets:

- `DULL`
- `METALLIC`
- `MAGNETIC`
- `SHINY`
- `BRIGHT`
- `DIAMOND`
- `EMERALD`
- `GEM_HORIZONTAL`
- `GEM_VERTICAL`
- `RUBY`
- `OPAL`
- `GLASS`
- `NETHERSTAR`
- `FINE`
- `SAND`
- `WOOD`
- `ROUGH`
- `FLINT`
- `LIGNITE`
- `QUARTZ`
- `CERTUS`
- `LAPIS`
- `FLUID`
- `RADIOACTIVE`

Only use `element(element)` for the one canonical material representing that
element. For GCY migration, existing GTCEu element materials should usually be
modified in `PostMaterialEvent` instead of re-registered.

## Flags and Generated Forms

- `flags(flags...)`: add material flags.
- `appendFlags(collection, flags...)`: add a flag collection such as `STD_METAL`
  plus extra flags.

Common generation flags:

- `STD_METAL`
- `GENERATE_PLATE`
- `GENERATE_DENSE`
- `GENERATE_ROD`
- `GENERATE_BOLT_SCREW`
- `GENERATE_FRAME`
- `GENERATE_GEAR`
- `GENERATE_LONG_ROD`
- `GENERATE_FOIL`
- `GENERATE_RING`
- `GENERATE_SPRING`

Common behavior/control flags:

- `DISABLE_MATERIAL_RECIPES`
- `DECOMPOSITION_BY_ELECTROLYZING`
- `DECOMPOSITION_BY_CENTRIFUGING`
- `DISABLE_DECOMPOSITION`
- `EXPLOSIVE`
- `FLAMMABLE`
- `STICKY`
- `PHOSPHORESCENT`
- `FIRE_RESISTANT`
- `MORTAR_GRINDABLE`
- `NO_WORKING`
- `NO_SMASHING`
- `NO_SMELTING`
- `NO_ORE_SMELTING`
- `NO_ORE_PROCESSING_TAB`
- `BLAST_FURNACE_CALCITE_DOUBLE`
- `BLAST_FURNACE_CALCITE_TRIPLE`
- `SOLDER_MATERIAL`
- `SOLDER_MATERIAL_BAD`
- `SOLDER_MATERIAL_GOOD`
- `DISABLE_ALLOY_BLAST`
- `DISABLE_ALLOY_PROPERTY`

## Ore Processing

- `ore()`
- `ore(emissive)`
- `ore(oreMultiplier, byproductMultiplier)`
- `ore(oreMultiplier, byproductMultiplier, emissive)`
- `washedIn(material)`
- `washedIn(material, washedAmount)`
- `separatedInto(materials...)`
- `oreSmeltInto(material)`
- `addOreByproducts(materials...)`

`ore` ensures a dust property. Use these methods only for actual ore materials
or materials that should participate in generated ore-processing chains.

## Metallurgy and Automatic Transformations

- `blast(temp)`
- `blast(temp, gasTier)`
- `blastTemp(temp)`
- `blastTemp(temp, gasTier)`
- `blastTemp(temp, gasTier, eutOverride)`
- `blastTemp(temp, gasTier, eutOverride, durationOverride)`
- `blast(b => b...)`
- `polarizesInto(material)`
- `arcSmeltInto(material)`
- `macerateInto(material)`
- `ingotSmeltInto(material)`

`blast` adds EBF behavior. Temperatures above 1750 K generate hot ingot and
vacuum freezer handling. Temperatures below 1000 K also generate primitive
blast furnace behavior.

`BlastProperty.GasTier` values:

- `LOW`: nitrogen
- `MID`: helium
- `HIGH`: argon
- `HIGHER`: neon
- `HIGHEST`: krypton

`BlastProperty.Builder` calls used inside `blast(b => b...)`:

- `temp(temperature)`
- `temp(temperature, gasTier)`
- `blastStats(eutOverride)`
- `blastStats(eutOverride, durationOverride)`
- `vacuumStats(eutOverride)`
- `vacuumStats(eutOverride, durationOverride)`

## Tools, Armor, Rotors, Wires, and Pipes

- `toolStats(toolProperty)`
- `armorStats(armorProperty)`
- `rotorStats(power, efficiency, damage, durability)`
- `cableProperties(voltage, amperage, loss)`
- `cableProperties(voltage, amperage, loss, isSuperCon)`
- `cableProperties(voltage, amperage, loss, isSuperCon, criticalTemperature)`
- `fluidPipeProperties(maxTemp, throughput, gasProof)`
- `fluidPipeProperties(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof)`
- `itemPipeProperties(priority, stacksPerSec)`
- `addDefaultEnchant(enchant, level)`

`addDefaultEnchant` is deprecated and requires tool stats. Avoid new uses.

For superconductors, use `cableProperties(..., true)` so generated wire behavior
matches superconductor semantics. Follow GTCEu Modern voltage names, not old GCY
`UMV`/`UXV` names.

## Hazards

- `radioactiveHazard(multiplier)`
- `hazard(trigger, condition)`
- `hazard(trigger, condition, progressionMultiplier)`
- `hazard(trigger, condition, progressionMultiplier, applyToDerivatives)`
- `hazard(trigger, condition, applyToDerivatives)`
- `removeHazard()`

Materials can inherit hazards from components. Use `removeHazard()` when a
component hazard should not apply to the derived material.

## Tags and Prefix Suppression

- `ignoredTagPrefixes(prefixes...)`: suppress generated items for specific
  `TagPrefix` entries.
- `customTags(tagKey)`: add an item tag to all generated items for the material.

Use `ignoredTagPrefixes` when a material has a broad property but must not
generate a particular form.

## Post-Registration Patches

Post-registration changes operate on already-built runtime materials. They do
not rebuild or re-register the material:

```scala
val ore = Iron.getProperty(PropertyKey.ORE)
ore.setOreByProducts(Nickel, Tin, Gold)
ore.setWashedIn(SodiumPersulfate)
```

GTCEu's `MaterialFlagAddition.register()` uses this pattern extensively.
Addon-owned changes belong in `PostMaterialEvent`, after all registrations are
available.

Important setter semantics from the property sources:

- `OreProperty.setOreByProducts(...)` clears the current list, then adds the
  supplied values.
- `OreProperty.addOreByProducts(...)` appends.
- `OreProperty.setSeparatedInto(...)` also appends despite its `set` name.
- `setWashedIn(material)` preserves the existing/default 100 mB amount;
  `setWashedIn(material, amount)` replaces both values.
- `setDirectSmeltResult`, `setMagneticMaterial`, `setArcSmeltingInto`, and
  `FluidProperty.setPrimaryKey` replace one stored target.

`getProperty(key)` may return null. A patch must target a material that already
has the required property; the first material patch ADT deliberately models
typed setter calls, not arbitrary `setProperty` insertion. Builder-time
registration and post-registration mutation are separate lifecycles.

## GCY Migration Rules of Thumb

- Old `SimpleDustMaterial`: usually `.dust().color(...).iconSet(...)`, plus
  `.formula(...)` or `.components(...)` when the source has chemistry.
- Old `SimpleFluidMaterial`: usually `.liquid()` or `.gas()`. Avoid `.fluid()`
  when liquid/gas semantics matter.
- Old `DustMaterial`: `.dust()`, optional `.ore(...)`, optional generated form
  flags.
- Old `IngotMaterial`: `.ingot(level).fluid()`, then `STD_METAL` and explicit
  `GENERATE_*` flags as needed.
- Old polymer-like `IngotMaterial`: prefer `.polymer(level)` plus its authored
  fluid and only the modern flags not already added by PolymerProperty.
- Old `GemMaterial`: `.gem()`, never `.ingot()` unless the material is being
  deliberately reclassified.
- Old marker/tier materials: do not blindly register normal materials. Map to
  GTCEu Modern marker/tier semantics.
- Existing GTCEu materials: modify in `PostMaterialEvent`; do not register a
  duplicate id.

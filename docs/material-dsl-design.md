# Direct GTCEu Material DSL

Status: first implementation contract. This document describes the thin
Scala 3 contextual wrapper over GTCEu 7.5.3 `Material.Builder`. It is not a
replacement for GTCEu's material model or verification.

## Scope

The first version is a direct runtime DSL:

```text
Scala contextual DSL -> GTCEu Material.Builder -> buildAndRegister()
```

GTCEu remains responsible for property induction, flag requirements,
duplicate-property checks, and final registration behavior. The DSL owns
syntax, context boundaries, small domain values, and Java API adaptation.

The first version does not include:

- a whole-block quoted macro;
- a type-state builder;
- an intermediate material ADT;
- generated material source;
- automatic reordering of builder calls.

## Authoring Shape

The outer registration owns one material context:

```scala
material("hyperion"):
  langValue("Hyperion Alloy")
  formula("C16H12N2O4")

  ingot(4)
  visual(rgb"6f2200", METALLIC, secondary = Some(rgb"ffbb33"))

  flags(EXT2_METAL, GENERATE_GEAR, GENERATE_ROTOR)
  components(Tungsten * 2, Titanium * 1, Carbon * 12)

  ore:
    settings(multiplier = 2, byproduct = 3, emissive = true)
    washedIn(SulfuricAcid, 250)
    separatedInto(Tungsten, Titanium)
    byproducts(Carbon, Titanium)

  liquid(2800.K)

  gas:
    temperature := 450.K
    density := 0.8
    name := "hyperion_gas"
    translation := "fluid.gregicality.hyperion_gas"
    disableBucket

  fluid(FluidKind.Molten):
    temperature := 1800.K
    block

  blast:
    temperature := 3900.K
    gasTier := HIGH
    blastStats := VA(EV) * 2000.ticks
    vacuumStats := VA(HV) * 600.ticks

  tool(speed = 9.0, damage = 7.0, durability = 2048, level = 4):
    types := List(PICKAXE, SWORD, WRENCH)
    enchantability := 18
    enchantment(Enchantments.SHARPNESS, 3)
    magnetic

  armor(durability = 55, protection = Armor(4, 8, 7, 4)):
    toughness := 4.0
    knockbackResistance := 0.3
    dyeable

  rotor(power = 140, efficiency = 125, damage = 3.5, durability = 3200)

  ignoredTagPrefixes(TagPrefix.dust, TagPrefix.dustSmall)
  customTags(MyMaterialTags.HYPERION)
  hazard(
    HazardTrigger.INHALATION,
    GTMedicalConditions.SILICOSIS,
    progressionMultiplier = 1.5,
    applyToDerivatives = true
  )
```

The syntax rules are deliberately small:

| Form | Meaning |
| --- | --- |
| `langValue("...")`, `formula("...")` | Direct builder call |
| `ingot(4)`, `oreSmeltInto(Steel)` | Direct parameter call |
| `oreProperty()`, `gasFluid(450.K)`, `blastTemp(3900.K)` | Compact direct property call |
| `visual(...)` | Compact multi-value visual configuration |
| `key:` | Enter a nested DSL context |
| `fluid(FluidKind.Molten):` | Enter a fluid block with an explicit storage kind |
| `tool(...):`, `armor(...):` | Enter a property builder block with required constructor values |
| `feature` | A no-argument boolean feature |
| `name := "..."`, `translation := "..."` | Override fluid builder metadata inside a fluid block |
| `enchantment(enchantment, level)` | Add a default tool enchantment |
| `repairIngredient(supplier)`, `noRepair` | Configure armor repair behavior |
| `hazard(..., progressionMultiplier = ..., applyToDerivatives = ...)` | One named form for GTCEu's hazard overload family |
| `value := x` | Set one value or replace a complete collection |
| `values += x` | Append one collection element |
| `Material * amount` | Construct a pure material/amount value |
| `VA(EV) * 2000.ticks` | Construct recipe EU/t plus duration |

Short collections may use varargs:

```scala
flags(GENERATE_PLATE, GENERATE_ROD)
components(Carbon * 2, Hydrogen * 4)
```

Reusable or conditional collections may use `List` or `Vector`:

```scala
val commonFlags = List(GENERATE_PLATE, GENERATE_ROD)
flags(commonFlags)
```

GTCEu flag presets such as `STD_METAL`, `EXT_METAL`, and `EXT2_METAL` are
Java `Collection[MaterialFlag]` values, not individual flags. They use a
separate overload so presets and additional flags remain distinguishable:

```scala
flags(EXT2_METAL, GENERATE_GEAR, GENERATE_ROTOR)
```

## Context Boundaries

Only configurations with a real nested GTCEu builder use a block:

- `gas:` / `plasma:` for standard full `FluidBuilder` settings;
- `fluid(FluidKind):` when the storage kind must be explicit, including
  full liquid and molten configuration;
- `blast` with `BlastProperty.Builder`;
- `tool(...)` with `ToolProperty.Builder`;
- `armor(...)` with `ArmorProperty.Builder`.

`FluidBuilder` is a top-level GTCEu type; there is no
`FluidProperty.Builder`. `ToolProperty.Builder` and `ArmorProperty.Builder`
must be created through their static `of(...)` factories because their
constructors are private.

Compact liquid registration stays a direct call:

```scala
liquid()
liquid(2800.K)
```

Scala 3 does not reliably resolve one package-level name as both a direct
overload and a `name:` context-function block. The DSL therefore keeps
`liquid(...)` for compact direct calls and uses `fluid(FluidKind.Liquid):`
for a fully configured liquid. Names that already own context blocks use an
explicit suffix for their compact forms:

```scala
oreProperty()
oreProperty(emissive = true)
gasFluid()
gasFluid(450.K)
plasmaFluid()
plasmaFluid(12000.K)
blastTemp(1800.K)
blastTemp(3900.K, HIGH)
```

These direct forms submit the same immutable `OreSpec`, `FluidSpec`, or
`BlastSpec` payload as their block counterparts. Bare no-argument words remain
reserved for section-internal boolean features such as `block`,
`disableBucket`, `disableColor`, and `customStill`.

Fluid `name` and `translation` are scalar fields and therefore use
last-write-wins semantics. Tool enchantments append in authoring order. Armor
`repairIngredient(...)` and `noRepair` are mutually exclusive section features;
the last one authored wins.

`ore:` is a deliberate DSL grouping even though GTCEu exposes ore operations
as methods on `Material.Builder`, not as an `OreProperty.Builder`. Its context
keeps ore settings and follow-up processing calls together without changing
the underlying GTCEu lifecycle.

The following remain direct calls because GTCEu exposes them directly on
`Material.Builder` and named arguments provide enough readability:

- base properties;
- compact `oreProperty`, `gasFluid`, `plasmaFluid`, and `blastTemp` forms;
- `langValue` and `formula`;
- `dust`, `wood`, `ingot`, `gem`, and `polymer` overloads;
- `burnTime` and `colorAverage`;
- `visual`;
- flags and components;
- `oreSmeltInto`, `polarizesInto`, `arcSmeltInto`, `macerateInto`, and
  `ingotSmeltInto`;
- `rotor`, `cable`, `fluidPipe`, and `itemPipe` properties;
- fluid name/translation, tool enchantments, and armor repair settings;
- `ignoredTagPrefixes` and `customTags`;
- `removeHazard`, `radioactiveHazard`, and `hazard`.

## Registration Boundary

Forge event wiring stays in `GCYMaterials`. The material definitions themselves
live in `MaterialRegistration.registerAll`, which receives the GTCEu base
materials needed for composition and runs under a `RegistryContext`.

This split has two purposes:

- the production entry point injects the real `GTMaterials` values only after
  GTCEu's material registry event is ready;
- unit tests can inject null placeholder materials into the same definitions and
  use the recording adapter without initializing Forge or the global GTCEu
  material table.

The first runtime slice contains:

- `polyimide`: a compact real migration slice using polymer, fluid, visual,
  flags, components, formula, and blast settings;
- `hyperion`: a deliberately broad stress material exercising direct calls,
  fluid and blast sections, ore grouping, tool and armor sections, and device
  properties in one authored order.

The stress material is an integration fixture for the DSL surface, not a claim
that its illustrative composition is a completed gameplay balance decision.
It uses a fluid-pipe property and deliberately omits `itemPipe`, because GTCEu
rejects a material that contains both pipe properties during verification.

## Runtime Invariants

- `material` is the only operation that creates and finalizes a material.
- `buildAndRegister()` is called once, after the material body completes.
- If the material body throws, `buildAndRegister()` is not called.
- The raw `Material.Builder` is not ambiently exposed as a `given`.
- Nested contexts cannot call material-terminal operations.
- `components` converts typed material amounts to GTCEu `MaterialStack`
  values and calls the typed `componentStacks(MaterialStack...)` overload.
- Flag presets and individual flags are adapted to their respective GTCEu
  overloads.
- Each direct material DSL call reaches the adapter immediately. Nested
  fluid, blast, and ore blocks collect one immutable section payload and
  submit it when the block returns normally.
- Scalar values inside a collected section are last-write-wins; collection
  values such as fluid attributes and ore byproducts append in authoring
  order.
- `FluidKind` records the standard storage choice without initializing
  GTCEu's global `FluidStorageKeys` during authoring or pure unit tests. The
  real adapter resolves the key and `FluidState` immediately before calling
  `Material.Builder.fluid(...)`.
- `ore:` is collected as one `OreSpec` and submitted at the position of the
  block after the block completes.
- `blast:` combines its separately-authored temperature and gas tier into the
  single `BlastProperty.Builder.temp(int, GasTier)` call required by GTCEu.
- `tool(...)` and `armor(...)` receive the values that GTCEu requires at
  builder construction time, then collect optional settings until block exit.
- `types := List(...)` replaces the tool type collection; `types += value`
  appends after the last replacement. A replacement clears earlier appends.
- `Armor(...)` fixes the protection order to helmet, chestplate, leggings,
  boots before the adapter creates GTCEu's required `int[]`.
- `ignoredTagPrefixes` and `customTags` accept either short varargs or reusable
  Scala collections. Empty collections are no-ops; non-empty values preserve
  authoring order.
- `hazard` represents all GTCEu hazard overloads with one `HazardSpec` shape.
  Its named defaults match GTCEu (`1.0`, `false`), and the adapter always calls
  the four-argument Java overload after narrowing the multiplier to `float`.
- `removeHazard`, `radioactiveHazard`, and `hazard` retain GTCEu's
  last-write-wins hazard-property behavior. The DSL does not reorder or reject
  repeated hazard calls.
- GTCEu call order is preserved as authored; the DSL does not reorder calls.

## Macro Boundary

The first implementation is context-only apart from ordinary value syntax
such as `rgb"..."`. A quoted macro is deferred until a concrete authoring
example shows a clear benefit for literal syntax, structural diagnostics, or
meaningful syntax compression.

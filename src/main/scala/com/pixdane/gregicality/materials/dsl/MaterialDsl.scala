package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.HazardTrigger
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

/** Top-level material authoring DSL.
  *
  * Usage:
  * {{{
  * given RegistryContext = RegistryContext("gregicality")
  * material("hyperion"):
  *   langValue("Hyperion Alloy")
  *   formula("C16H12N2O4")
  *   ingot(4)
  *   flags(GENERATE_PLATE, GENERATE_ROD)
  *   components(Tungsten * 2, Hydrogen * 4)
  *   ore:
  *     settings(multiplier = 2, byproduct = 3, emissive = true)
  *     washedIn(SulfuricAcid, 250)
  * }}}
  *
  * A [[RegistryContext]] must be in `given` scope. `material` builds a
  * namespaced `ResourceLocation` from the context's namespace and `path`, asks
  * the context's factory for a [[MaterialBuilderAdapter]], and runs `body` with
  * a [[MaterialContext]] in `given` scope. If `body` returns normally, the
  * material is finalized and registered exactly once; if `body` throws, no
  * finalization happens and the exception propagates.
  *
  * The DSL methods are top-level package definitions so that authors in the
  * `dsl` package (and tests in the same package) can call `material`,
  * `langValue`, `flags`, etc. without an explicit import.
  */

/** Authoring entry point for one material.
  *
  * @param path
  *   the material's path segment; combined with the current [[RegistryContext]]
  *   namespace to form the `ResourceLocation`
  * @param body
  *   the authoring block, run with a [[MaterialContext]] in `given` scope
  * @return
  *   the registered [[Material]]
  */
def material(path: String)(body: MaterialContext ?=> Unit)(using
    ctx: RegistryContext
): Material =
  val id = new ResourceLocation(ctx.namespace, path)
  val adapter = ctx.factory.create(id)
  val materialCtx = new MaterialContext(adapter)
  given MaterialContext = materialCtx
  body(using materialCtx)
  materialCtx.finalizeRegistration()

/** Sets the localized display name of the current material. */
def langValue(name: String)(using mc: MaterialContext): Unit =
  mc.langValue(name)

/** Sets the chemical formula string of the current material. */
def formula(f: String)(using mc: MaterialContext): Unit = mc.formula(f)

/** Sets the chemical formula with GTCEu's formatting switch. */
def formula(f: String, withFormatting: Boolean)(using
    mc: MaterialContext
): Unit = mc.formula(f, withFormatting)

/** Associates the current material with a GTCEu chemical element. */
def element(value: Element)(using mc: MaterialContext): Unit =
  mc.element(value)

/** Adds a dust property using GTCEu's no-argument overload. */
def dust()(using mc: MaterialContext): Unit = mc.dust()

/** Adds a dust property with a harvest level. */
def dust(level: Int)(using mc: MaterialContext): Unit = mc.dust(level)

/** Adds a dust property with a harvest level and burn time. */
def dust(level: Int, burnTime: Int)(using mc: MaterialContext): Unit =
  mc.dust(level, burnTime)

/** Adds a wood property using GTCEu's no-argument overload. */
def wood()(using mc: MaterialContext): Unit = mc.wood()

/** Adds a wood property with a harvest level. */
def wood(level: Int)(using mc: MaterialContext): Unit = mc.wood(level)

/** Adds a wood property with a harvest level and burn time. */
def wood(level: Int, burnTime: Int)(using mc: MaterialContext): Unit =
  mc.wood(level, burnTime)

/** Marks the current material as an ingot with the given harvest level. */
def ingot(level: Int)(using mc: MaterialContext): Unit = mc.ingot(level)

/** Adds an ingot property using GTCEu's no-argument overload. */
def ingot()(using mc: MaterialContext): Unit = mc.ingot()

/** Adds an ingot property with a harvest level and burn time. */
def ingot(level: Int, burnTime: Int)(using mc: MaterialContext): Unit =
  mc.ingot(level, burnTime)

/** Adds a gem property using GTCEu's no-argument overload. */
def gem()(using mc: MaterialContext): Unit = mc.gem()

/** Adds a gem property with a harvest level. */
def gem(level: Int)(using mc: MaterialContext): Unit = mc.gem(level)

/** Adds a gem property with a harvest level and burn time. */
def gem(level: Int, burnTime: Int)(using mc: MaterialContext): Unit =
  mc.gem(level, burnTime)

/** Adds a polymer property using GTCEu's no-argument overload. */
def polymer()(using mc: MaterialContext): Unit = mc.polymer()

/** Adds a polymer property with a harvest level. */
def polymer(level: Int)(using mc: MaterialContext): Unit = mc.polymer(level)

/** Adds a polymer property with a harvest level and burn time. */
def polymer(level: Int, burnTime: Int)(using mc: MaterialContext): Unit =
  mc.polymer(level, burnTime)

/** Replaces the current material color with GTCEu's component average. */
def colorAverage(using mc: MaterialContext): Unit = mc.colorAverage()

/** Applies color, icon set, and optional secondary color to the current
  * material.
  */
def visual(spec: VisualSpec)(using mc: MaterialContext): Unit = mc.visual(spec)

/** Applies a compact visual configuration with named optional values.
  *
  * `hasFluidColor = false` selects GTCEu's two-argument color overload.
  */
def visual(
    color: HexColor,
    iconSet: MaterialIconSet,
    secondary: Option[HexColor] = None,
    hasFluidColor: Boolean = true
)(using mc: MaterialContext): Unit =
  mc.visual(color, iconSet, secondary, hasFluidColor)

/** Adds flags by varargs to the current material. */
def flags(fs: MaterialFlag*)(using mc: MaterialContext): Unit = mc.flags(fs*)

/** Adds flags from any Scala collection to the current material. */
def flags(fs: Iterable[MaterialFlag])(using mc: MaterialContext): Unit =
  mc.flags(fs)

/** Adds a Java-collection preset plus extra varargs flags to the current
  * material.
  */
def flags(preset: java.util.Collection[MaterialFlag], extras: MaterialFlag*)(
    using mc: MaterialContext
): Unit = mc.flags(preset, extras*)

/** Sets the composition of the current material from typed material/amount
  * pairs by varargs.
  */
def components(amounts: MaterialAmount*)(using mc: MaterialContext): Unit =
  mc.components(amounts*)

/** Sets the composition of the current material from any Scala collection of
  * amounts.
  */
def components(amounts: Iterable[MaterialAmount])(using
    mc: MaterialContext
): Unit =
  mc.components(amounts)

/** Opens an ore configuration block on the current material. */
def ore(body: OreContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.ore(body)

/** Adds an ore property with GTCEu's default multipliers and texture mode. */
def oreProperty()(using mc: MaterialContext): Unit =
  mc.submitOre(OreSpec())

/** Adds an ore property with default multipliers and explicit emissive mode. */
def oreProperty(emissive: Boolean)(using mc: MaterialContext): Unit =
  mc.submitOre(OreSpec(emissive = emissive))

/** Opens a fluid configuration block for an explicit standard storage kind.
  *
  * This form covers full liquid, gas, plasma, and molten configuration without
  * colliding with compact direct calls such as `liquid(2800.K)`.
  */
def fluid(kind: FluidKind)(body: FluidContext ?=> Unit)(using
    mc: MaterialContext
): Unit =
  mc.configureFluid(kind, body)

/** Adds a liquid with inferred GTCEu settings. */
def liquid()(using mc: MaterialContext): Unit = mc.liquid()

/** Adds a liquid with an explicit temperature. */
def liquid(temperature: Kelvin)(using mc: MaterialContext): Unit =
  mc.liquid(temperature)

/** Opens a gas configuration block. */
def gas(body: FluidContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.configureFluid(FluidKind.Gas, body)

/** Adds a gas with inferred GTCEu settings without opening a fluid block. */
def gasFluid()(using mc: MaterialContext): Unit =
  mc.addGas()

/** Adds a gas with an explicit temperature without opening a fluid block. */
def gasFluid(temperature: Kelvin)(using mc: MaterialContext): Unit =
  mc.addGas(temperature)

/** Opens a plasma configuration block. */
def plasma(body: FluidContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.configureFluid(FluidKind.Plasma, body)

/** Adds a plasma with inferred GTCEu settings without opening a fluid block. */
def plasmaFluid()(using mc: MaterialContext): Unit =
  mc.addPlasma()

/** Adds a plasma with an explicit temperature without opening a fluid block. */
def plasmaFluid(temperature: Kelvin)(using mc: MaterialContext): Unit =
  mc.addPlasma(temperature)

/** Opens a blast-property configuration block. */
def blast(body: BlastContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.blast(body = body)

/** Adds a blast property with an explicit temperature without opening a block.
  */
def blastTemp(temperature: Kelvin)(using mc: MaterialContext): Unit =
  mc.submitBlast(BlastSpec(temperature = Some(temperature)))

/** Adds a blast property with an explicit temperature and gas tier. */
def blastTemp(temperature: Kelvin, gasTier: GasTier)(using
    mc: MaterialContext
): Unit =
  mc.submitBlast(
    BlastSpec(
      temperature = Some(temperature),
      gasTier = Some(gasTier)
    )
  )

/** Opens a tool-property configuration block.
  *
  * Speeds and damage use `Double` in authoring code so literals such as `9.0`
  * stay natural; the real adapter narrows them to GTCEu's `float` API.
  */
def tool(
    speed: Double,
    damage: Double,
    durability: Int,
    level: Int
)(body: ToolContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.tool(speed, damage, durability, level)(body)

/** Opens an armor-property configuration block. */
def armor(
    durability: Int,
    protection: Armor
)(body: ArmorContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.armor(durability, protection)(body)

/** Sets the ore-smelting target directly on the current material. */
def oreSmeltInto(material: Material)(using mc: MaterialContext): Unit =
  mc.oreSmeltInto(material)

/** Sets the polarizing target directly on the current material. */
def polarizesInto(material: Material)(using mc: MaterialContext): Unit =
  mc.polarizesInto(material)

/** Sets the arc-smelting target directly on the current material. */
def arcSmeltInto(material: Material)(using mc: MaterialContext): Unit =
  mc.arcSmeltInto(material)

/** Sets the macerating target directly on the current material. */
def macerateInto(material: Material)(using mc: MaterialContext): Unit =
  mc.macerateInto(material)

/** Sets the ingot-smelting target directly on the current material. */
def ingotSmeltInto(material: Material)(using mc: MaterialContext): Unit =
  mc.ingotSmeltInto(material)

/** Applies turbine rotor statistics. */
def rotor(
    power: Int,
    efficiency: Int,
    damage: Double,
    durability: Int
)(using mc: MaterialContext): Unit =
  mc.rotor(RotorSpec(power, efficiency, damage, durability))

/** Applies cable properties without a critical-temperature override. */
def cable(
    voltage: NominalVoltage,
    amperage: Int,
    loss: Int,
    superconducting: Boolean = false
)(using mc: MaterialContext): Unit =
  mc.cable(CableSpec(voltage, amperage, loss, superconducting))

/** Applies cable properties with an explicit critical temperature. */
def cable(
    voltage: NominalVoltage,
    amperage: Int,
    loss: Int,
    superconducting: Boolean,
    criticalTemperature: Kelvin
)(using mc: MaterialContext): Unit =
  mc.cable(
    CableSpec(
      voltage,
      amperage,
      loss,
      superconducting,
      Some(criticalTemperature)
    )
  )

/** Applies fluid-pipe properties.
  *
  * Named proof arguments are strongly recommended because the underlying Java
  * method places four booleans next to each other.
  */
def fluidPipe(
    maxTemperature: Kelvin,
    throughput: Int,
    gasProof: Boolean,
    acidProof: Boolean = false,
    cryoProof: Boolean = false,
    plasmaProof: Boolean = false
)(using mc: MaterialContext): Unit =
  mc.fluidPipe(
    FluidPipeSpec(
      maxTemperature,
      throughput,
      gasProof,
      acidProof,
      cryoProof,
      plasmaProof
    )
  )

/** Applies item-pipe properties. */
def itemPipe(priority: Int, stacksPerSecond: Double)(using
    mc: MaterialContext
): Unit =
  mc.itemPipe(ItemPipeSpec(priority, stacksPerSecond))

/** Marks tag prefixes as ignored for the current material by varargs. */
def ignoredTagPrefixes(prefixes: TagPrefix*)(using mc: MaterialContext): Unit =
  mc.ignoredTagPrefixes(prefixes*)

/** Marks tag prefixes as ignored from any Scala collection. */
def ignoredTagPrefixes(prefixes: Iterable[TagPrefix])(using
    mc: MaterialContext
): Unit =
  mc.ignoredTagPrefixes(prefixes)

/** Adds custom item tags to the current material by varargs. */
def customTags(tags: TagKey[Item]*)(using mc: MaterialContext): Unit =
  mc.customTags(tags*)

/** Adds custom item tags from any Scala collection. */
def customTags(tags: Iterable[TagKey[Item]])(using mc: MaterialContext): Unit =
  mc.customTags(tags)

/** Removes any hazard property from the current material. */
def removeHazard(using mc: MaterialContext): Unit =
  mc.removeHazard()

/** Applies GTCEu's standard radioactive hazard. */
def radioactiveHazard(multiplier: Double)(using mc: MaterialContext): Unit =
  mc.radioactiveHazard(multiplier)

/** Applies a complete hazard configuration.
  *
  * The named fields intentionally collapse GTCEu's several hazard overloads
  * into one stable Scala surface.
  */
def hazard(
    trigger: HazardTrigger,
    condition: MedicalCondition,
    progressionMultiplier: Double = 1.0,
    applyToDerivatives: Boolean = false
)(using mc: MaterialContext): Unit =
  mc.hazard(
    HazardSpec(
      trigger,
      condition,
      progressionMultiplier,
      applyToDerivatives
    )
  )

/** Sets ore multipliers and emissive flag. Last call wins. */
def settings(
    multiplier: Int = 1,
    byproduct: Int = 1,
    emissive: Boolean = false
)(using
    oc: OreContext
): Unit = oc.settings(multiplier, byproduct, emissive)

/** Sets the washing fluid and amount for the current ore block. Last call wins.
  */
def washedIn(fluid: Material, amount: Int = 100)(using oc: OreContext): Unit =
  oc.washedIn(fluid, amount)

/** Appends materials to the separatedInto list of the current ore block. */
def separatedInto(ms: Material*)(using oc: OreContext): Unit =
  oc.separatedInto(ms*)

/** Appends materials to the byproducts list of the current ore block. */
def byproducts(ms: Material*)(using oc: OreContext): Unit = oc.byproducts(ms*)

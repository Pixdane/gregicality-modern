package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.data.chemical.material.properties.{
  ArmorProperty,
  ToolProperty
}
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.HazardTrigger
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.fluids.FluidBuilder
import com.gregtechceu.gtceu.api.fluids.FluidState
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

/** Factory for a per-material adapter.
  *
  * The DSL never constructs a
  * [[com.gregtechceu.gtceu.api.data.chemical.material.Material.Builder]]
  * directly; it asks the current [[RegistryContext]] for an adapter bound to a
  * `ResourceLocation`. Tests inject a fake factory that records calls instead
  * of touching the Forge material registry.
  */
private[dsl] trait MaterialBuilderFactory:
  /** Returns an adapter for the material identified by `id`. */
  def create(id: ResourceLocation): MaterialBuilderAdapter

/** Sink for material builder calls.
  *
  * The real implementation forwards each method to the matching
  * `Material.Builder` call and preserves authoring order exactly. The DSL
  * records one `ore` call per `ore:` block (with the fully-assembled
  * [[OreSpec]]) rather than emitting a stream of ore sub-calls.
  */
private[dsl] trait MaterialBuilderAdapter:
  /** Sets the localized display name. */
  def langValue(s: String): Unit

  /** Sets the chemical formula string. */
  def formula(s: String): Unit

  /** Sets the chemical formula with GTCEu's formatting switch. */
  def formula(s: String, withFormatting: Boolean): Unit

  /** Applies a dust property overload. */
  def dust(level: Option[Int], burnTime: Option[Int]): Unit

  /** Applies a wood property overload. */
  def wood(level: Option[Int], burnTime: Option[Int]): Unit

  /** Marks the material as an ingot with the given harvest level. */
  def ingot(level: Int): Unit

  /** Applies an ingot overload not covered by the legacy level-only call. */
  def ingotForm(level: Option[Int], burnTime: Option[Int]): Unit

  /** Applies a gem property overload. */
  def gem(level: Option[Int], burnTime: Option[Int]): Unit

  /** Applies a polymer property overload. */
  def polymer(level: Option[Int], burnTime: Option[Int]): Unit

  /** Sets the material-level burn time. */
  def materialBurnTime(value: Int): Unit

  /** Replaces the material color with GTCEu's averaged color. */
  def colorAverage(): Unit

  /** Applies color, icon set, and optional secondary color in one shot. */
  def visual(spec: VisualSpec): Unit

  /** Adds a sequence of flags. */
  def flags(fs: Seq[MaterialFlag]): Unit

  /** Adds a Java-collection preset plus extra varargs flags. */
  def appendFlags(
      preset: java.util.Collection[MaterialFlag],
      extras: Seq[MaterialFlag]
  ): Unit

  /** Sets the material composition from typed material/amount pairs. */
  def components(amounts: Seq[MaterialAmount]): Unit

  /** Applies a fully-assembled ore configuration. */
  def ore(spec: OreSpec): Unit

  /** Adds one fluid storage entry from an assembled fluid configuration. */
  def fluid(spec: FluidSpec): Unit

  /** Adds a blast property from an assembled blast configuration. */
  def blast(spec: BlastSpec): Unit

  /** Adds a tool property from an assembled tool configuration. */
  def tool(spec: ToolSpec): Unit

  /** Adds an armor property from an assembled armor configuration. */
  def armor(spec: ArmorSpec): Unit

  /** Sets the ore-smelting target. */
  def oreSmeltInto(material: Material): Unit

  /** Sets the polarizing target. */
  def polarizesInto(material: Material): Unit

  /** Sets the arc-smelting target. */
  def arcSmeltInto(material: Material): Unit

  /** Sets the macerating target. */
  def macerateInto(material: Material): Unit

  /** Sets the ingot-smelting target. */
  def ingotSmeltInto(material: Material): Unit

  /** Applies rotor statistics. */
  def rotor(spec: RotorSpec): Unit

  /** Applies cable properties. */
  def cable(spec: CableSpec): Unit

  /** Applies fluid-pipe properties. */
  def fluidPipe(spec: FluidPipeSpec): Unit

  /** Applies item-pipe properties. */
  def itemPipe(spec: ItemPipeSpec): Unit

  /** Marks tag prefixes as ignored for this material. */
  def ignoredTagPrefixes(prefixes: Seq[TagPrefix]): Unit

  /** Adds custom item tags to this material. */
  def customTags(tags: Seq[TagKey[Item]]): Unit

  /** Removes any hazard property from this material. */
  def removeHazard(): Unit

  /** Applies GTCEu's standard radioactive hazard. */
  def radioactiveHazard(multiplier: Double): Unit

  /** Applies a complete hazard configuration. */
  def hazard(spec: HazardSpec): Unit

  /** Finalizes and registers the material; returns the registered `Material`.
    */
  def buildAndRegister(): Material

object MaterialAdapterFactory:

  /** Default factory backed by real GTCEu `Material.Builder` instances. */
  val real: MaterialBuilderFactory = new MaterialBuilderFactory:
    def create(id: ResourceLocation): MaterialBuilderAdapter =
      new GtceuMaterialAdapter(id)

/** Real [[MaterialBuilderAdapter]] over a GTCEu 7.5.3 `Material.Builder`.
  *
  * Each method maps to exactly one `Material.Builder` call. `components` goes
  * through the typed `componentStacks(MaterialStack...)` overload rather than
  * the untyped `components(Object...)` one, so Scala never has to assemble an
  * `Object` array. `ore` collapses an [[OreSpec]] into the documented call
  * sequence: `ore(mult, byproduct, emissive)` first, then optional `washedIn`,
  * non-empty `separatedInto`, and non-empty `addOreByproducts`.
  */
private[dsl] final class GtceuMaterialAdapter(id: ResourceLocation)
    extends MaterialBuilderAdapter:

  private val builder: Material.Builder = new Material.Builder(id)

  def langValue(s: String): Unit =
    builder.langValue(s)

  def formula(s: String): Unit =
    builder.formula(s)

  def formula(s: String, withFormatting: Boolean): Unit =
    builder.formula(s, withFormatting)

  def dust(level: Option[Int], burnTime: Option[Int]): Unit =
    (level, burnTime) match
      case (None, None)              => builder.dust()
      case (Some(harvest), None)     => builder.dust(harvest)
      case (Some(harvest), Some(bt)) => builder.dust(harvest, bt)
      case (None, Some(_))           =>
        throw new IllegalArgumentException(
          "dust burnTime requires harvest level"
        )

  def wood(level: Option[Int], burnTime: Option[Int]): Unit =
    (level, burnTime) match
      case (None, None)              => builder.wood()
      case (Some(harvest), None)     => builder.wood(harvest)
      case (Some(harvest), Some(bt)) => builder.wood(harvest, bt)
      case (None, Some(_))           =>
        throw new IllegalArgumentException(
          "wood burnTime requires harvest level"
        )

  def ingot(level: Int): Unit =
    builder.ingot(level)

  def ingotForm(level: Option[Int], burnTime: Option[Int]): Unit =
    (level, burnTime) match
      case (None, None)              => builder.ingot()
      case (Some(harvest), Some(bt)) => builder.ingot(harvest, bt)
      case (None, Some(_))           =>
        throw new IllegalArgumentException(
          "ingot burnTime requires harvest level"
        )
      case (Some(_), None) =>
        throw new IllegalArgumentException(
          "use ingot(level) for a level-only call"
        )

  def gem(level: Option[Int], burnTime: Option[Int]): Unit =
    (level, burnTime) match
      case (None, None)              => builder.gem()
      case (Some(harvest), None)     => builder.gem(harvest)
      case (Some(harvest), Some(bt)) => builder.gem(harvest, bt)
      case (None, Some(_))           =>
        throw new IllegalArgumentException(
          "gem burnTime requires harvest level"
        )

  def polymer(level: Option[Int], burnTime: Option[Int]): Unit =
    (level, burnTime) match
      case (None, None)              => builder.polymer()
      case (Some(harvest), None)     => builder.polymer(harvest)
      case (Some(harvest), Some(bt)) => builder.polymer(harvest, bt)
      case (None, Some(_))           =>
        throw new IllegalArgumentException(
          "polymer burnTime requires harvest level"
        )

  def materialBurnTime(value: Int): Unit =
    builder.burnTime(value)

  def colorAverage(): Unit =
    builder.colorAverage()

  def visual(spec: VisualSpec): Unit =
    builder.color(spec.color.value)
    builder.iconSet(spec.iconSet)
    spec.secondary.foreach(c => builder.secondaryColor(c.value))

  def flags(fs: Seq[MaterialFlag]): Unit =
    builder.flags(fs*)

  def appendFlags(
      preset: java.util.Collection[MaterialFlag],
      extras: Seq[MaterialFlag]
  ): Unit =
    builder.appendFlags(preset, extras*)

  def components(amounts: Seq[MaterialAmount]): Unit =
    val stacks: Seq[MaterialStack] =
      amounts.map(a => new MaterialStack(a.material, a.amount.toLong))
    builder.componentStacks(stacks*)

  def ore(spec: OreSpec): Unit =
    builder.ore(spec.multiplier, spec.byproductMultiplier, spec.emissive)
    spec.washedIn.foreach(w => builder.washedIn(w.fluid, w.amount))
    if spec.separatedInto.nonEmpty then
      builder.separatedInto(spec.separatedInto*)
    if spec.byproducts.nonEmpty then builder.addOreByproducts(spec.byproducts*)

  def fluid(spec: FluidSpec): Unit =
    val (key, state) = spec.kind match
      case FluidKind.Liquid => FluidStorageKeys.LIQUID -> FluidState.LIQUID
      case FluidKind.Gas    => FluidStorageKeys.GAS -> FluidState.GAS
      case FluidKind.Plasma => FluidStorageKeys.PLASMA -> FluidState.PLASMA
      case FluidKind.Molten => FluidStorageKeys.MOLTEN -> FluidState.LIQUID

    val fluidBuilder = new FluidBuilder().state(state)
    spec.temperature.foreach(k => fluidBuilder.temperature(k.value))
    spec.color.foreach(c => fluidBuilder.color(c.value))
    spec.density.foreach:
      case FluidDensity.Minecraft(value) => fluidBuilder.density(value)
      case FluidDensity.GramsPerCubicCentimeter(value) =>
        fluidBuilder.density(value)
    spec.luminosity.foreach(fluidBuilder.luminosity)
    spec.viscosity.foreach:
      case FluidViscosity.Minecraft(value) => fluidBuilder.viscosity(value)
      case FluidViscosity.Poise(value)     => fluidBuilder.viscosity(value)
    spec.burnTime.foreach(fluidBuilder.burnTime)
    if spec.attributes.nonEmpty then fluidBuilder.attributes(spec.attributes*)
    spec.textures.foreach(t =>
      fluidBuilder.textures(t.customStill, t.customFlowing)
    )
    if spec.hasBlock then fluidBuilder.block()
    if !spec.hasBucket then fluidBuilder.disableBucket()
    if !spec.colorEnabled then fluidBuilder.disableColor()
    builder.fluid(key, fluidBuilder)

  def blast(spec: BlastSpec): Unit =
    builder.blast: blastBuilder =>
      (spec.temperature, spec.gasTier) match
        case (Some(temperature), Some(gasTier)) =>
          blastBuilder.temp(temperature.value, gasTier)
        case (Some(temperature), None) =>
          blastBuilder.temp(temperature.value)
        case (None, Some(_)) =>
          throw new IllegalArgumentException(
            "blast gasTier requires temperature"
          )
        case (None, None) =>
          ()

      spec.blastStats.foreach:
        case RecipeOverride(eut, Some(duration)) =>
          blastBuilder.blastStats(eut.value, duration.value)
        case RecipeOverride(eut, None) =>
          blastBuilder.blastStats(eut.value)

      spec.vacuumStats.foreach:
        case RecipeOverride(eut, Some(duration)) =>
          blastBuilder.vacuumStats(eut.value, duration.value)
        case RecipeOverride(eut, None) =>
          blastBuilder.vacuumStats(eut.value)

      blastBuilder

  def tool(spec: ToolSpec): Unit =
    val toolBuilder = ToolProperty.Builder.of(
      spec.speed.toFloat,
      spec.damage.toFloat,
      spec.durability,
      spec.level
    )
    spec.types.foreach(toolTypes => toolBuilder.types(toolTypes*))
    if spec.additionalTypes.nonEmpty then
      toolBuilder.addTypes(spec.additionalTypes*)
    spec.enchantability.foreach(toolBuilder.enchantability)
    spec.attackSpeed.foreach(speed => toolBuilder.attackSpeed(speed.toFloat))
    spec.durabilityMultiplier.foreach(toolBuilder.durabilityMultiplier)
    if spec.magnetic then toolBuilder.magnetic()
    if spec.unbreakable then toolBuilder.unbreakable()
    if spec.ignoreCraftingTools then toolBuilder.ignoreCraftingTools()
    builder.toolStats(toolBuilder.build())

  def armor(spec: ArmorSpec): Unit =
    val armorBuilder =
      ArmorProperty.Builder.of(spec.durability, spec.protection.toArray)
    spec.enchantability.foreach(armorBuilder.enchantability)
    spec.toughness.foreach(value => armorBuilder.toughness(value.toFloat))
    spec.knockbackResistance.foreach(value =>
      armorBuilder.knockbackResistance(value.toFloat)
    )
    if spec.dyeable then armorBuilder.dyeable(true)
    if spec.unbreakable then armorBuilder.unbreakable()
    builder.armorStats(armorBuilder.build())

  def oreSmeltInto(material: Material): Unit =
    builder.oreSmeltInto(material)

  def polarizesInto(material: Material): Unit =
    builder.polarizesInto(material)

  def arcSmeltInto(material: Material): Unit =
    builder.arcSmeltInto(material)

  def macerateInto(material: Material): Unit =
    builder.macerateInto(material)

  def ingotSmeltInto(material: Material): Unit =
    builder.ingotSmeltInto(material)

  def rotor(spec: RotorSpec): Unit =
    builder.rotorStats(
      spec.power,
      spec.efficiency,
      spec.damage.toFloat,
      spec.durability
    )

  def cable(spec: CableSpec): Unit =
    spec.criticalTemperature match
      case Some(criticalTemperature) =>
        builder.cableProperties(
          spec.voltage.value,
          spec.amperage,
          spec.loss,
          spec.superconducting,
          criticalTemperature.value
        )
      case None if spec.superconducting =>
        builder.cableProperties(
          spec.voltage.value,
          spec.amperage,
          spec.loss,
          true
        )
      case None =>
        builder.cableProperties(spec.voltage.value, spec.amperage, spec.loss)

  def fluidPipe(spec: FluidPipeSpec): Unit =
    val hasAdvancedProof =
      spec.acidProof || spec.cryoProof || spec.plasmaProof
    if hasAdvancedProof then
      builder.fluidPipeProperties(
        spec.maxTemperature.value,
        spec.throughput,
        spec.gasProof,
        spec.acidProof,
        spec.cryoProof,
        spec.plasmaProof
      )
    else
      builder.fluidPipeProperties(
        spec.maxTemperature.value,
        spec.throughput,
        spec.gasProof
      )

  def itemPipe(spec: ItemPipeSpec): Unit =
    builder.itemPipeProperties(spec.priority, spec.stacksPerSecond.toFloat)

  def ignoredTagPrefixes(prefixes: Seq[TagPrefix]): Unit =
    if prefixes.nonEmpty then builder.ignoredTagPrefixes(prefixes*)

  def customTags(tags: Seq[TagKey[Item]]): Unit =
    tags.foreach(tag => builder.customTags(tag))

  def removeHazard(): Unit =
    builder.removeHazard()

  def radioactiveHazard(multiplier: Double): Unit =
    builder.radioactiveHazard(multiplier.toFloat)

  def hazard(spec: HazardSpec): Unit =
    builder.hazard(
      spec.trigger,
      spec.condition,
      spec.progressionMultiplier.toFloat,
      spec.applyToDerivatives
    )

  def buildAndRegister(): Material =
    builder.buildAndRegister()

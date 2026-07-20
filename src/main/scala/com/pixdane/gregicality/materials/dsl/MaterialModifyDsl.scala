package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.chemical.material.properties.{
  FluidPipeProperties,
  HazardProperty,
  IMaterialProperty,
  ItemPipeProperties,
  PropertyKey,
  WireProperties
}
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.HazardTrigger
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.fluids.{FluidBuilder, FluidState}
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import com.gregtechceu.gtceu.common.data.GTMedicalConditions
import net.minecraft.world.item.enchantment.Enchantment

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.GENERATE_FOIL

/** One ordered operation applied to an existing GTCEu ore property. */
enum OreModifyOperation:
  case SetByproducts(materials: List[Material])
  case AddByproducts(materials: List[Material])
  case WashedIn(wash: WashSpec)
  case AddSeparatedInto(materials: List[Material])
  case DirectSmeltResult(material: Material)
  case OreMultiplier(value: Int)
  case ByproductMultiplier(value: Int)
  case Emissive(value: Boolean)

/** Factory for adapters that mutate an already-registered material. */
private[dsl] trait MaterialModifyFactory:
  /** Creates an adapter bound to `material`. */
  def create(material: Material): MaterialModifyAdapter

/** Sink for post-registration material modifications. */
private[dsl] trait MaterialModifyAdapter:
  /** Adds flags and lets GTCEu verify their dependencies. */
  def addFlags(flags: Seq[MaterialFlag]): Unit

  /** Applies ordered operations to the target material's ore property. */
  def ore(operations: Seq[OreModifyOperation]): Unit

  /** Replaces or mutates cable properties. */
  def cable(spec: CableSpec): Unit

  /** Replaces or mutates fluid-pipe properties. */
  def fluidPipe(spec: FluidPipeSpec): Unit

  /** Replaces or mutates item-pipe properties. */
  def itemPipe(spec: ItemPipeSpec): Unit

  /** Sets the target of the ore block's direct smelting operation. */
  def oreSmeltInto(material: Material): Unit

  /** Sets the target of the material's polarizing operation. */
  def polarizesInto(material: Material): Unit

  /** Sets the target of the material's arc-smelting operation. */
  def arcSmeltInto(material: Material): Unit

  /** Sets the target of the material's macerating operation. */
  def macerateInto(material: Material): Unit

  /** Sets the target of the material's direct ingot-smelting operation. */
  def ingotSmeltInto(material: Material): Unit

  /** Changes the existing blast property's temperature. */
  def blastTemperature(temperature: Kelvin): Unit

  /** Suppresses generated forms for the supplied prefixes. */
  def ignoredTagPrefixes(prefixes: Seq[TagPrefix]): Unit

  /** Removes the current hazard property when present. */
  def removeHazard(): Unit

  /** Replaces the current hazard with the standard radioactive hazard. */
  def radioactiveHazard(multiplier: Double): Unit

  /** Replaces the current hazard with a complete hazard specification. */
  def hazard(spec: HazardSpec): Unit

  /** Adds a default enchantment to the target material's tool property. */
  def enchantment(enchantment: Enchantment, level: Int): Unit

  /** Enqueues an additional fluid storage entry before GTCEu registers fluids.
    */
  def addFluid(spec: FluidSpec): Unit

/** Context used while authoring modifications for one existing material. */
private[dsl] final class MaterialModifyContext(
    private val adapter: MaterialModifyAdapter
):
  /** Adds flags to the target material. */
  def addFlags(flags: MaterialFlag*): Unit = adapter.addFlags(flags)

  /** Adds flags from a reusable Scala collection. */
  def addFlags(flags: Iterable[MaterialFlag]): Unit =
    adapter.addFlags(flags.toSeq)

  /** Opens an ordered ore-property patch. */
  def orePatch(body: OreModifyContext ?=> Unit): Unit =
    val ctx = new OreModifyContext
    given OreModifyContext = ctx
    body(using ctx)
    adapter.ore(ctx.operations)

  /** Applies cable properties. */
  def cable(spec: CableSpec): Unit = adapter.cable(spec)

  /** Applies fluid-pipe properties. */
  def fluidPipe(spec: FluidPipeSpec): Unit = adapter.fluidPipe(spec)

  /** Applies item-pipe properties. */
  def itemPipe(spec: ItemPipeSpec): Unit = adapter.itemPipe(spec)

  /** Sets the ore direct-smelting target. */
  def oreSmeltInto(material: Material): Unit = adapter.oreSmeltInto(material)

  /** Sets the polarizing target. */
  def polarizesInto(material: Material): Unit = adapter.polarizesInto(material)

  /** Sets the arc-smelting target. */
  def arcSmeltInto(material: Material): Unit = adapter.arcSmeltInto(material)

  /** Sets the macerating target. */
  def macerateInto(material: Material): Unit = adapter.macerateInto(material)

  /** Sets the ingot-smelting target. */
  def ingotSmeltInto(material: Material): Unit =
    adapter.ingotSmeltInto(material)

  /** Changes the existing blast property's temperature. */
  def blastTemperature(temperature: Kelvin): Unit =
    adapter.blastTemperature(temperature)

  /** Suppresses generated forms for the supplied prefixes. */
  def ignoredTagPrefixes(prefixes: TagPrefix*): Unit =
    adapter.ignoredTagPrefixes(prefixes)

  /** Removes the current hazard. */
  def removeHazard(): Unit = adapter.removeHazard()

  /** Applies the standard radioactive hazard. */
  def radioactiveHazard(multiplier: Double): Unit =
    adapter.radioactiveHazard(multiplier)

  /** Applies a complete hazard specification. */
  def hazard(spec: HazardSpec): Unit = adapter.hazard(spec)

  /** Adds a default enchantment to the target material's tool property. */
  def enchantment(enchantment: Enchantment, level: Int): Unit =
    adapter.enchantment(enchantment, level)

  /** Enqueues an additional fluid storage entry. */
  def addFluid(spec: FluidSpec): Unit = adapter.addFluid(spec)

/** Mutable collector for one `orePatch:` block. */
private[dsl] final class OreModifyContext:
  private val pending =
    scala.collection.mutable.ListBuffer.empty[OreModifyOperation]

  /** The immutable ordered operation snapshot. */
  def operations: Seq[OreModifyOperation] = pending.toList

  /** Replaces existing ore byproducts. */
  def setByproducts(materials: Material*): Unit =
    pending += OreModifyOperation.SetByproducts(materials.toList)

  /** Appends ore byproducts. */
  def addByproducts(materials: Material*): Unit =
    pending += OreModifyOperation.AddByproducts(materials.toList)

  /** Replaces the washing fluid and amount. */
  def washedIn(fluid: Material, amount: Int): Unit =
    pending += OreModifyOperation.WashedIn(WashSpec(fluid, amount))

  /** Appends electromagnetic-separation outputs, matching GTCEu's setter. */
  def separatedInto(materials: Material*): Unit =
    pending += OreModifyOperation.AddSeparatedInto(materials.toList)

  /** Replaces the direct smelting target. */
  def directSmeltResult(material: Material): Unit =
    pending += OreModifyOperation.DirectSmeltResult(material)

  /** Sets the crushed-ore multiplier. */
  def setOreMultiplier(value: Int): Unit =
    pending += OreModifyOperation.OreMultiplier(value)

  /** Sets the byproduct multiplier. */
  def setByproductMultiplier(value: Int): Unit =
    pending += OreModifyOperation.ByproductMultiplier(value)

  /** Sets whether the ore texture is emissive. */
  def setEmissive(value: Boolean): Unit =
    pending += OreModifyOperation.Emissive(value)

/** Registry-level context that supplies the modification adapter factory. */
final class ModificationRegistryContext private[dsl] (
    private[dsl] val factory: MaterialModifyFactory
):
  require(
    factory != null,
    "ModificationRegistryContext factory must not be null"
  )

object ModificationRegistryContext:
  /** Creates a context backed by the real GTCEu material mutation adapter. */
  def real: ModificationRegistryContext =
    new ModificationRegistryContext(MaterialModifyFactoryImpl.real)

  /** Test-only/package-local factory injection entry point. */
  private[dsl] def apply(
      factory: MaterialModifyFactory
  ): ModificationRegistryContext =
    new ModificationRegistryContext(factory)

/** Namespace for the real modification adapter factory. */
private[dsl] object MaterialModifyFactoryImpl:
  val real: MaterialModifyFactory = new MaterialModifyFactory:
    def create(material: Material): MaterialModifyAdapter =
      new GtceuMaterialModifyAdapter(material)

/** GTCEu-backed adapter for post-registration material changes. */
private[dsl] final class GtceuMaterialModifyAdapter(
    private val material: Material
) extends MaterialModifyAdapter:

  private def requireProperty[T <: IMaterialProperty](
      key: PropertyKey[T]
  ): T =
    val property = material.getProperty(key)
    if property == null then
      throw new IllegalStateException(
        s"Material $material has no ${key.toString} property to modify"
      )
    property

  private def replaceHazard(property: HazardProperty): Unit =
    if material.hasProperty(PropertyKey.HAZARD) then
      material.removeProperty(PropertyKey.HAZARD)
    material.setProperty(PropertyKey.HAZARD, property)

  def addFlags(flags: Seq[MaterialFlag]): Unit =
    if flags.nonEmpty then material.addFlags(flags*)

  def ore(operations: Seq[OreModifyOperation]): Unit =
    val property = requireProperty(PropertyKey.ORE)
    operations.foreach:
      case OreModifyOperation.SetByproducts(materials) =>
        property.setOreByProducts(materials*)
      case OreModifyOperation.AddByproducts(materials) =>
        property.addOreByProducts(materials*)
      case OreModifyOperation.WashedIn(wash) =>
        property.setWashedIn(wash.fluid, wash.amount)
      case OreModifyOperation.AddSeparatedInto(materials) =>
        property.setSeparatedInto(materials*)
      case OreModifyOperation.DirectSmeltResult(value) =>
        property.setDirectSmeltResult(value)
      case OreModifyOperation.OreMultiplier(value) =>
        property.setOreMultiplier(value)
      case OreModifyOperation.ByproductMultiplier(value) =>
        property.setByProductMultiplier(value)
      case OreModifyOperation.Emissive(value) =>
        property.setEmissive(value)

  def cable(spec: CableSpec): Unit =
    val property = material.getProperty(PropertyKey.WIRE)
    if property == null then
      material.setProperty(
        PropertyKey.WIRE,
        new WireProperties(
          spec.voltage.value,
          spec.amperage,
          spec.loss,
          spec.superconducting,
          spec.criticalTemperature.fold(0)(_.value)
        )
      )
    else
      property.setVoltage(spec.voltage.value)
      property.setAmperage(spec.amperage)
      property.setSuperconductor(spec.superconducting)
      property.setLossPerBlock(if spec.superconducting then 0 else spec.loss)
      property.setSuperconductorCriticalTemperature(
        spec.criticalTemperature.fold(0)(_.value)
      )
    if !spec.superconducting &&
      spec.voltage.value >= GTValues.V(GTValues.IV) &&
      !material.hasFlag(GENERATE_FOIL)
    then material.addFlags(GENERATE_FOIL)

  def fluidPipe(spec: FluidPipeSpec): Unit =
    val property = material.getProperty(PropertyKey.FLUID_PIPE)
    if property == null then
      material.setProperty(
        PropertyKey.FLUID_PIPE,
        new FluidPipeProperties(
          spec.maxTemperature.value,
          spec.throughput,
          spec.gasProof,
          spec.acidProof,
          spec.cryoProof,
          spec.plasmaProof
        )
      )
    else
      property.setMaxFluidTemperature(spec.maxTemperature.value)
      property.setThroughput(spec.throughput)
      property.setGasProof(spec.gasProof)
      property.setCryoProof(spec.cryoProof)
      property.setPlasmaProof(spec.plasmaProof)
      property.setCanContain(FluidAttributes.ACID, spec.acidProof)

  def itemPipe(spec: ItemPipeSpec): Unit =
    val property = material.getProperty(PropertyKey.ITEM_PIPE)
    if property == null then
      material.setProperty(
        PropertyKey.ITEM_PIPE,
        new ItemPipeProperties(spec.priority, spec.stacksPerSecond.toFloat)
      )
    else
      property.setPriority(spec.priority)
      property.setTransferRate(spec.stacksPerSecond.toFloat)

  def oreSmeltInto(value: Material): Unit =
    requireProperty(PropertyKey.ORE).setDirectSmeltResult(value)

  def polarizesInto(value: Material): Unit =
    requireProperty(PropertyKey.INGOT).setMagneticMaterial(value)

  def arcSmeltInto(value: Material): Unit =
    requireProperty(PropertyKey.INGOT).setArcSmeltingInto(value)

  def macerateInto(value: Material): Unit =
    requireProperty(PropertyKey.INGOT).setMacerateInto(value)

  def ingotSmeltInto(value: Material): Unit =
    requireProperty(PropertyKey.INGOT).setSmeltingInto(value)

  def blastTemperature(value: Kelvin): Unit =
    requireProperty(PropertyKey.BLAST).setBlastTemperature(value.value)

  def ignoredTagPrefixes(prefixes: Seq[TagPrefix]): Unit =
    prefixes.foreach(_.setIgnored(material))

  def removeHazard(): Unit =
    if material.hasProperty(PropertyKey.HAZARD) then
      material.removeProperty(PropertyKey.HAZARD)

  def radioactiveHazard(multiplier: Double): Unit =
    replaceHazard(
      new HazardProperty(
        HazardTrigger.ANY,
        GTMedicalConditions.CARCINOGEN,
        multiplier.toFloat,
        true
      )
    )

  def hazard(spec: HazardSpec): Unit =
    replaceHazard(
      new HazardProperty(
        spec.trigger,
        spec.condition,
        spec.progressionMultiplier.toFloat,
        spec.applyToDerivatives
      )
    )

  def enchantment(value: Enchantment, level: Int): Unit =
    requireProperty(PropertyKey.TOOL).addEnchantmentForTools(value, level)

  def addFluid(spec: FluidSpec): Unit =
    val property = requireProperty(PropertyKey.FLUID)
    val (key, state) = spec.kind match
      case FluidKind.Liquid => FluidStorageKeys.LIQUID -> FluidState.LIQUID
      case FluidKind.Gas    => FluidStorageKeys.GAS -> FluidState.GAS
      case FluidKind.Plasma => FluidStorageKeys.PLASMA -> FluidState.PLASMA
      case FluidKind.Molten => FluidStorageKeys.MOLTEN -> FluidState.LIQUID
    val fluidBuilder = new FluidBuilder().state(state)
    spec.temperature.foreach(value => fluidBuilder.temperature(value.value))
    spec.color.foreach(value => fluidBuilder.color(value.value))
    spec.density.foreach:
      case FluidDensity.Minecraft(value) => fluidBuilder.density(value)
      case FluidDensity.GramsPerCubicCentimeter(value) =>
        fluidBuilder.density(value)
    spec.luminosity.foreach(fluidBuilder.luminosity)
    spec.viscosity.foreach:
      case FluidViscosity.Minecraft(value) => fluidBuilder.viscosity(value)
      case FluidViscosity.Poise(value)     => fluidBuilder.viscosity(value)
    spec.burnTime.foreach(fluidBuilder.burnTime)
    spec.name.foreach(fluidBuilder.name)
    spec.translation.foreach(fluidBuilder.translation)
    spec.attributes.foreach(fluidBuilder.attribute)
    spec.textures.foreach(value =>
      fluidBuilder.textures(value.customStill, value.customFlowing)
    )
    if spec.hasBlock then fluidBuilder.block()
    if !spec.hasBucket then fluidBuilder.disableBucket()
    if !spec.colorEnabled then fluidBuilder.disableColor()
    property.enqueueRegistration(key, fluidBuilder)

/** Opens a modification block for an existing material. */
def modify(target: Material)(body: MaterialModifyContext ?=> Unit)(using
    context: ModificationRegistryContext
): Unit =
  val adapter = context.factory.create(target)
  val modifyContext = new MaterialModifyContext(adapter)
  given MaterialModifyContext = modifyContext
  body(using modifyContext)

/** Adds flags in a modification block. */
def addFlags(flags: MaterialFlag*)(using mc: MaterialModifyContext): Unit =
  mc.addFlags(flags*)

/** Adds flags from a reusable collection in a modification block. */
def addFlags(flags: Iterable[MaterialFlag])(using
    mc: MaterialModifyContext
): Unit = mc.addFlags(flags)

/** Opens an ordered ore-property patch. */
def orePatch(body: OreModifyContext ?=> Unit)(using
    mc: MaterialModifyContext
): Unit = mc.orePatch(body)

/** Sets the target's cable property. */
def setCable(spec: CableSpec)(using mc: MaterialModifyContext): Unit =
  mc.cable(spec)

/** Sets the target's cable property from named values. */
def setCable(
    voltage: NominalVoltage,
    amperage: Int,
    loss: Int,
    superconducting: Boolean = false,
    criticalTemperature: Option[Kelvin] = None
)(using mc: MaterialModifyContext): Unit =
  mc.cable(
    CableSpec(
      voltage,
      amperage,
      loss,
      superconducting,
      criticalTemperature
    )
  )

/** Sets the target's fluid-pipe property. */
def setFluidPipe(spec: FluidPipeSpec)(using mc: MaterialModifyContext): Unit =
  mc.fluidPipe(spec)

/** Sets the target's fluid-pipe property from named values. */
def setFluidPipe(
    maxTemperature: Kelvin,
    throughput: Int,
    gasProof: Boolean,
    acidProof: Boolean = false,
    cryoProof: Boolean = false,
    plasmaProof: Boolean = false
)(using mc: MaterialModifyContext): Unit =
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

/** Sets the target's item-pipe property. */
def setItemPipe(spec: ItemPipeSpec)(using mc: MaterialModifyContext): Unit =
  mc.itemPipe(spec)

/** Sets the target's item-pipe property from named values. */
def setItemPipe(priority: Int, stacksPerSecond: Double)(using
    mc: MaterialModifyContext
): Unit = mc.itemPipe(ItemPipeSpec(priority, stacksPerSecond))

/** Sets the target's ore direct-smelting result. */
def modifyOreSmeltInto(material: Material)(using
    mc: MaterialModifyContext
): Unit =
  mc.oreSmeltInto(material)

/** Sets the target's polarizing result. */
def modifyPolarizesInto(material: Material)(using
    mc: MaterialModifyContext
): Unit =
  mc.polarizesInto(material)

/** Sets the target's arc-smelting result. */
def modifyArcSmeltInto(material: Material)(using
    mc: MaterialModifyContext
): Unit =
  mc.arcSmeltInto(material)

/** Sets the target's macerating result. */
def modifyMacerateInto(material: Material)(using
    mc: MaterialModifyContext
): Unit =
  mc.macerateInto(material)

/** Sets the target's ingot-smelting result. */
def modifyIngotSmeltInto(material: Material)(using
    mc: MaterialModifyContext
): Unit = mc.ingotSmeltInto(material)

/** Changes the target's blast temperature. */
def blastTemperature(temperature: Kelvin)(using
    mc: MaterialModifyContext
): Unit = mc.blastTemperature(temperature)

/** Suppresses generated forms for target prefixes. */
def modifyIgnoredTagPrefixes(prefixes: TagPrefix*)(using
    mc: MaterialModifyContext
): Unit = mc.ignoredTagPrefixes(prefixes*)

/** Removes the target's hazard. */
def modifyRemoveHazard(using mc: MaterialModifyContext): Unit =
  mc.removeHazard()

/** Applies the standard radioactive hazard to the target. */
def modifyRadioactiveHazard(multiplier: Double)(using
    mc: MaterialModifyContext
): Unit = mc.radioactiveHazard(multiplier)

/** Applies a complete hazard specification to the target. */
def modifyHazard(spec: HazardSpec)(using mc: MaterialModifyContext): Unit =
  mc.hazard(spec)

/** Adds a default enchantment to the target's tools. */
def modifyEnchantment(value: Enchantment, level: Int)(using
    mc: MaterialModifyContext
): Unit = mc.enchantment(value, level)

/** Enqueues an additional fluid storage entry for the target. */
def addFluid(spec: FluidSpec)(using mc: MaterialModifyContext): Unit =
  mc.addFluid(spec)

/** Sets ore byproducts, replacing the existing list. */
def setByproducts(materials: Material*)(using
    oc: OreModifyContext
): Unit = oc.setByproducts(materials*)

/** Appends ore byproducts to the existing list. */
def addByproducts(materials: Material*)(using
    oc: OreModifyContext
): Unit = oc.addByproducts(materials*)

/** Sets the washing fluid and amount for an ore patch. */
def modifyWashedIn(fluid: Material, amount: Int)(using
    oc: OreModifyContext
): Unit = oc.washedIn(fluid, amount)

/** Appends electromagnetic-separation outputs for an ore patch. */
def modifySeparatedInto(materials: Material*)(using
    oc: OreModifyContext
): Unit = oc.separatedInto(materials*)

/** Sets the direct smelting result for an ore patch. */
def setDirectSmeltResult(material: Material)(using
    oc: OreModifyContext
): Unit = oc.directSmeltResult(material)

/** Assigns the crushed-ore multiplier in an ore patch. */
def setOreMultiplier(value: Int)(using oc: OreModifyContext): Unit =
  oc.setOreMultiplier(value)

/** Assigns the byproduct multiplier in an ore patch. */
def setByproductMultiplier(value: Int)(using oc: OreModifyContext): Unit =
  oc.setByproductMultiplier(value)

/** Assigns emissive texture behavior in an ore patch. */
def setEmissive(value: Boolean)(using oc: OreModifyContext): Unit =
  oc.setEmissive(value)

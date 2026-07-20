package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.HazardTrigger
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.item.tool.GTToolType
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantment
import munit.FunSuite

import scala.collection.mutable.ListBuffer

import com.pixdane.gregicality.materials.dsl.VoltageTier.*
import com.pixdane.gregicality.common.data.{
  MaterialModification,
  MaterialModificationInputs
}

/** Recorded modification-adapter invocation. */
enum ModifyCall:
  case AddFlags(flags: Seq[MaterialFlag])
  case Ore(operations: Seq[OreModifyOperation])
  case Cable(spec: CableSpec)
  case FluidPipe(spec: FluidPipeSpec)
  case ItemPipe(spec: ItemPipeSpec)
  case OreSmeltInto(material: Material)
  case PolarizesInto(material: Material)
  case ArcSmeltInto(material: Material)
  case MacerateInto(material: Material)
  case IngotSmeltInto(material: Material)
  case BlastTemperature(temperature: Kelvin)
  case IgnoredTagPrefixes(prefixes: Seq[TagPrefix])
  case RemoveHazard
  case RadioactiveHazard(multiplier: Double)
  case Hazard(spec: HazardSpec)
  case DefaultEnchantment(enchantment: Enchantment, level: Int)
  case AddFluid(spec: FluidSpec)

/** In-memory modification adapter used to test the contextual modify DSL. */
final class FakeModifyAdapter(val material: Material)
    extends MaterialModifyAdapter:
  val calls: ListBuffer[ModifyCall] = ListBuffer.empty

  def addFlags(flags: Seq[MaterialFlag]): Unit =
    calls += ModifyCall.AddFlags(flags)
  def ore(operations: Seq[OreModifyOperation]): Unit =
    calls += ModifyCall.Ore(operations)
  def cable(spec: CableSpec): Unit = calls += ModifyCall.Cable(spec)
  def fluidPipe(spec: FluidPipeSpec): Unit =
    calls += ModifyCall.FluidPipe(spec)
  def itemPipe(spec: ItemPipeSpec): Unit = calls += ModifyCall.ItemPipe(spec)
  def oreSmeltInto(value: Material): Unit =
    calls += ModifyCall.OreSmeltInto(value)
  def polarizesInto(value: Material): Unit =
    calls += ModifyCall.PolarizesInto(value)
  def arcSmeltInto(value: Material): Unit =
    calls += ModifyCall.ArcSmeltInto(value)
  def macerateInto(value: Material): Unit =
    calls += ModifyCall.MacerateInto(value)
  def ingotSmeltInto(value: Material): Unit =
    calls += ModifyCall.IngotSmeltInto(value)
  def blastTemperature(value: Kelvin): Unit =
    calls += ModifyCall.BlastTemperature(value)
  def ignoredTagPrefixes(prefixes: Seq[TagPrefix]): Unit =
    calls += ModifyCall.IgnoredTagPrefixes(prefixes)
  def removeHazard(): Unit = calls += ModifyCall.RemoveHazard
  def radioactiveHazard(multiplier: Double): Unit =
    calls += ModifyCall.RadioactiveHazard(multiplier)
  def hazard(spec: HazardSpec): Unit = calls += ModifyCall.Hazard(spec)
  def enchantment(value: Enchantment, level: Int): Unit =
    calls += ModifyCall.DefaultEnchantment(value, level)
  def addFluid(spec: FluidSpec): Unit = calls += ModifyCall.AddFluid(spec)

/** In-memory modification factory. */
final class FakeModifyFactory extends MaterialModifyFactory:
  val adapters: ListBuffer[FakeModifyAdapter] = ListBuffer.empty

  def create(material: Material): MaterialModifyAdapter =
    val adapter = new FakeModifyAdapter(material)
    adapters += adapter
    adapter

class MaterialModifySuite extends FunSuite:
  private val target: Material = null
  private val byproductA: Material = null
  private val byproductB: Material = null
  private val washFluid: Material = null
  private val directSmelt: Material = null
  private val flagA: MaterialFlag = null
  private val flagB: MaterialFlag = null
  private val prefixA: TagPrefix = null
  private val hazardTrigger: HazardTrigger = null
  private val medicalCondition: MedicalCondition = null
  private val enchantmentValue: Enchantment = null

  private def withContext: (FakeModifyFactory, ModificationRegistryContext) =
    val factory = new FakeModifyFactory
    factory -> ModificationRegistryContext(factory)

  test("modification registry context rejects a null factory"):
    intercept[IllegalArgumentException]:
      ModificationRegistryContext(null.asInstanceOf[MaterialModifyFactory])

  test("modify preserves top-level and ore-patch authoring order"):
    val (factory, context) = withContext
    given ModificationRegistryContext = context

    modify(target):
      addFlags(flagA, flagB)
      orePatch:
        setByproducts(byproductA, byproductB)
        addByproducts(byproductA)
        modifyWashedIn(washFluid, 250)
        modifySeparatedInto(byproductB)
        setDirectSmeltResult(directSmelt)
        setOreMultiplier(2)
        setByproductMultiplier(3)
        setEmissive(true)
      setCable(voltage = V(EV), amperage = 4, loss = 2)
      blastTemperature(3900.K)
      modifyIgnoredTagPrefixes(prefixA)

    assertEquals(
      factory.adapters.head.calls.toList,
      List(
        ModifyCall.AddFlags(Seq(flagA, flagB)),
        ModifyCall.Ore(
          Seq(
            OreModifyOperation.SetByproducts(List(byproductA, byproductB)),
            OreModifyOperation.AddByproducts(List(byproductA)),
            OreModifyOperation.WashedIn(WashSpec(washFluid, 250)),
            OreModifyOperation.AddSeparatedInto(List(byproductB)),
            OreModifyOperation.DirectSmeltResult(directSmelt),
            OreModifyOperation.OreMultiplier(2),
            OreModifyOperation.ByproductMultiplier(3),
            OreModifyOperation.Emissive(true)
          )
        ),
        ModifyCall.Cable(CableSpec(V(EV), 4, 2)),
        ModifyCall.BlastTemperature(3900.K),
        ModifyCall.IgnoredTagPrefixes(Seq(prefixA))
      )
    )

  test(
    "modify forwards transformations, pipe properties, hazards, and enchantments"
  ):
    val (factory, context) = withContext
    given ModificationRegistryContext = context

    modify(target):
      setFluidPipe(
        maxTemperature = 2800.K,
        throughput = 100,
        gasProof = true,
        acidProof = true
      )
      setItemPipe(priority = 2, stacksPerSecond = 4.5)
      modifyOreSmeltInto(directSmelt)
      modifyPolarizesInto(byproductA)
      modifyArcSmeltInto(byproductB)
      modifyMacerateInto(byproductA)
      modifyIngotSmeltInto(byproductB)
      modifyRemoveHazard
      modifyRadioactiveHazard(2.0)
      modifyHazard(HazardSpec(hazardTrigger, medicalCondition))
      modifyEnchantment(enchantmentValue, 3)
      addFluid(FluidSpec(FluidKind.Plasma, temperature = Some(12000.K)))

    assertEquals(
      factory.adapters.head.calls.toList,
      List(
        ModifyCall.FluidPipe(
          FluidPipeSpec(
            maxTemperature = 2800.K,
            throughput = 100,
            gasProof = true,
            acidProof = true
          )
        ),
        ModifyCall.ItemPipe(ItemPipeSpec(2, 4.5)),
        ModifyCall.OreSmeltInto(directSmelt),
        ModifyCall.PolarizesInto(byproductA),
        ModifyCall.ArcSmeltInto(byproductB),
        ModifyCall.MacerateInto(byproductA),
        ModifyCall.IngotSmeltInto(byproductB),
        ModifyCall.RemoveHazard,
        ModifyCall.RadioactiveHazard(2.0),
        ModifyCall.Hazard(HazardSpec(hazardTrigger, medicalCondition)),
        ModifyCall.DefaultEnchantment(enchantmentValue, 3),
        ModifyCall.AddFluid(
          FluidSpec(FluidKind.Plasma, temperature = Some(12000.K))
        )
      )
    )

  test("modify propagates body exceptions without swallowing them"):
    val (factory, context) = withContext
    given ModificationRegistryContext = context

    intercept[RuntimeException]:
      modify(target):
        addFlags(flagA)
        throw new RuntimeException("modify failed")

    assertEquals(
      factory.adapters.head.calls.toList,
      List(ModifyCall.AddFlags(Seq(flagA)))
    )

  test(
    "material modification definitions add liquid and plasma noble-gas forms"
  ):
    val factory = new FakeModifyFactory
    given ModificationRegistryContext = ModificationRegistryContext(factory)

    MaterialModification.modifyAll(
      MaterialModificationInputs(
        xenon = target,
        neon = byproductA,
        krypton = byproductB
      )
    )

    assertEquals(factory.adapters.size, 3)
    assertEquals(
      factory.adapters.map(_.calls.toList).toList,
      List(
        List(
          ModifyCall.AddFluid(FluidSpec(FluidKind.Liquid)),
          ModifyCall.AddFluid(FluidSpec(FluidKind.Plasma))
        ),
        List(
          ModifyCall.AddFluid(FluidSpec(FluidKind.Liquid)),
          ModifyCall.AddFluid(FluidSpec(FluidKind.Plasma))
        ),
        List(
          ModifyCall.AddFluid(FluidSpec(FluidKind.Liquid)),
          ModifyCall.AddFluid(FluidSpec(FluidKind.Plasma))
        )
      )
    )

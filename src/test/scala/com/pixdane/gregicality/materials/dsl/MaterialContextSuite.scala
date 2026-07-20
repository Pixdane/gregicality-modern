package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier
import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.HazardTrigger
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute
import com.gregtechceu.gtceu.api.item.tool.GTToolType
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import munit.FunSuite

import scala.collection.mutable.ListBuffer

import com.pixdane.gregicality.materials.dsl.VoltageTier.*

/** Recorded adapter invocation. The suite compares lists of these to assert
  * call order and payload without touching the Forge material registry.
  */
enum Call:
  case LangValue(s: String)
  case Formula(s: String)
  case FormattedFormula(s: String, withFormatting: Boolean)
  case Dust(level: Option[Int], burnTime: Option[Int])
  case Wood(level: Option[Int], burnTime: Option[Int])
  case IngotForm(level: Option[Int], burnTime: Option[Int])
  case Gem(level: Option[Int], burnTime: Option[Int])
  case Polymer(level: Option[Int], burnTime: Option[Int])
  case Visual(spec: VisualSpec)
  case Flags(fs: Seq[MaterialFlag])
  case AppendFlags(
      preset: java.util.Collection[MaterialFlag],
      extras: Seq[MaterialFlag]
  )
  case Components(amounts: Seq[MaterialAmount])
  case Ore(spec: OreSpec)
  case Fluid(spec: FluidSpec)
  case Blast(spec: BlastSpec)
  case Tool(spec: ToolSpec)
  case Armor(spec: ArmorSpec)
  case OreSmeltInto(material: Material)
  case PolarizesInto(material: Material)
  case ArcSmeltInto(material: Material)
  case MacerateInto(material: Material)
  case IngotSmeltInto(material: Material)
  case MaterialBurnTime(value: Int)
  case ColorAverage
  case Rotor(spec: RotorSpec)
  case Cable(spec: CableSpec)
  case FluidPipe(spec: FluidPipeSpec)
  case ItemPipe(spec: ItemPipeSpec)
  case ChemicalElement(value: Element)
  case IgnoredTagPrefixes(prefixes: Seq[TagPrefix])
  case CustomTags(tags: Seq[TagKey[Item]])
  case RemoveHazard
  case RadioactiveHazard(multiplier: Double)
  case Hazard(spec: HazardSpec)
  case BuildAndRegister

/** In-memory [[MaterialBuilderAdapter]] that records every call in order. */
final class FakeAdapter(val id: ResourceLocation)
    extends MaterialBuilderAdapter:
  val calls: ListBuffer[Call] = ListBuffer.empty

  def langValue(s: String): Unit = calls += Call.LangValue(s)
  def formula(s: String): Unit = calls += Call.Formula(s)
  def formula(s: String, withFormatting: Boolean): Unit =
    calls += Call.FormattedFormula(s, withFormatting)
  def dust(level: Option[Int], burnTime: Option[Int]): Unit =
    calls += Call.Dust(level, burnTime)
  def wood(level: Option[Int], burnTime: Option[Int]): Unit =
    calls += Call.Wood(level, burnTime)
  def ingotForm(level: Option[Int], burnTime: Option[Int]): Unit =
    calls += Call.IngotForm(level, burnTime)
  def gem(level: Option[Int], burnTime: Option[Int]): Unit =
    calls += Call.Gem(level, burnTime)
  def polymer(level: Option[Int], burnTime: Option[Int]): Unit =
    calls += Call.Polymer(level, burnTime)
  def visual(spec: VisualSpec): Unit = calls += Call.Visual(spec)
  def flags(fs: Seq[MaterialFlag]): Unit = calls += Call.Flags(fs)
  def appendFlags(
      preset: java.util.Collection[MaterialFlag],
      extras: Seq[MaterialFlag]
  ): Unit = calls += Call.AppendFlags(preset, extras)
  def components(amounts: Seq[MaterialAmount]): Unit =
    calls += Call.Components(amounts)
  def ore(spec: OreSpec): Unit = calls += Call.Ore(spec)
  def fluid(spec: FluidSpec): Unit = calls += Call.Fluid(spec)
  def blast(spec: BlastSpec): Unit = calls += Call.Blast(spec)
  def tool(spec: ToolSpec): Unit = calls += Call.Tool(spec)
  def armor(spec: ArmorSpec): Unit = calls += Call.Armor(spec)
  def oreSmeltInto(material: Material): Unit =
    calls += Call.OreSmeltInto(material)
  def polarizesInto(material: Material): Unit =
    calls += Call.PolarizesInto(material)
  def arcSmeltInto(material: Material): Unit =
    calls += Call.ArcSmeltInto(material)
  def macerateInto(material: Material): Unit =
    calls += Call.MacerateInto(material)
  def ingotSmeltInto(material: Material): Unit =
    calls += Call.IngotSmeltInto(material)
  def materialBurnTime(value: Int): Unit =
    calls += Call.MaterialBurnTime(value)
  def colorAverage(): Unit = calls += Call.ColorAverage
  def rotor(spec: RotorSpec): Unit = calls += Call.Rotor(spec)
  def cable(spec: CableSpec): Unit = calls += Call.Cable(spec)
  def fluidPipe(spec: FluidPipeSpec): Unit = calls += Call.FluidPipe(spec)
  def itemPipe(spec: ItemPipeSpec): Unit = calls += Call.ItemPipe(spec)
  def element(value: Element): Unit = calls += Call.ChemicalElement(value)
  def ignoredTagPrefixes(prefixes: Seq[TagPrefix]): Unit =
    calls += Call.IgnoredTagPrefixes(prefixes)
  def customTags(tags: Seq[TagKey[Item]]): Unit =
    calls += Call.CustomTags(tags)
  def removeHazard(): Unit = calls += Call.RemoveHazard
  def radioactiveHazard(multiplier: Double): Unit =
    calls += Call.RadioactiveHazard(multiplier)
  def hazard(spec: HazardSpec): Unit = calls += Call.Hazard(spec)
  def buildAndRegister(): Material =
    calls += Call.BuildAndRegister
    null

/** In-memory [[MaterialBuilderFactory]] that hands out [[FakeAdapter]] and
  * remembers the id it was asked to create with.
  */
final class FakeFactory extends MaterialBuilderFactory:
  var createdId: Option[ResourceLocation] = None
  var lastAdapter: Option[FakeAdapter] = None
  val adapters: ListBuffer[FakeAdapter] = ListBuffer.empty

  def create(id: ResourceLocation): MaterialBuilderAdapter =
    createdId = Some(id)
    val adapter = new FakeAdapter(id)
    lastAdapter = Some(adapter)
    adapters += adapter
    adapter

/** Test-first contract for the runtime material DSL.
  *
  * These tests assume a production API consisting of:
  *
  *   - `private[dsl] trait MaterialBuilderFactory` with
  *     `def create(id: ResourceLocation): MaterialBuilderAdapter`
  *   - `private[dsl] trait MaterialBuilderAdapter` with `langValue`, `formula`,
  *     `ingot`, `visual`, `flags`, `appendFlags`, `components`, `ore`, and
  *     `buildAndRegister`
  *   - `final case class RegistryContext(namespace: String, factory:
  *     MaterialBuilderFactory)`
  *   - Top-level contextual DSL functions `material`, `langValue`, `formula`,
  *     `ingot`, `visual`, `flags` (three overloads), `components` (two
  *     overloads), and `ore`
  *   - Ore-internal contextual functions `settings`, `washedIn`,
  *     `separatedInto`, and `byproducts`
  *
  * A fake factory and adapter record calls in memory, so the suite never
  * touches the Forge material registry. `null` materials and flags are used as
  * recording placeholders only.
  */
class MaterialContextSuite extends FunSuite:

  private def withContext: (FakeFactory, RegistryContext) =
    val factory = new FakeFactory
    factory -> RegistryContext("gregicality", factory)

  // Null placeholders are safe here: the fake adapter only records references.
  private val tungsten: Material = null
  private val titanium: Material = null
  private val carbon: Material = null
  private val sulfuricAcid: Material = null
  private val flagA: MaterialFlag = null
  private val flagB: MaterialFlag = null
  private val flagC: MaterialFlag = null
  private val attributeA: FluidAttribute = null
  private val attributeB: FluidAttribute = null
  private val toolTypeA: GTToolType = null
  private val toolTypeB: GTToolType = null
  private val toolTypeC: GTToolType = null
  private val prefixA: TagPrefix = null
  private val prefixB: TagPrefix = null
  private val itemTagA: TagKey[Item] = null
  private val itemTagB: TagKey[Item] = null
  private val hazardTrigger: HazardTrigger = null
  private val medicalCondition: MedicalCondition = null
  private val elementValue: Element = null

  test("material creates adapter with namespaced id"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("hyperion"):
      langValue("Hyperion Alloy")
    assertEquals(
      factory.createdId,
      Some(new ResourceLocation("gregicality", "hyperion"))
    )

  test("material body preserves call order and finalizes exactly once"):
    val (factory, context) = withContext
    given RegistryContext = context
    val spec =
      VisualSpec(rgb"6f2200", MaterialIconSet.METALLIC, Some(rgb"ffbb33"))
    material("hyperion"):
      langValue("Hyperion Alloy")
      formula("C16H12N2O4")
      ingot(4)
      visual(spec)
      flags(flagA, flagB)
      components(tungsten * 2, titanium * 1)
      ore:
        settings(multiplier = 2, byproduct = 3, emissive = true)
        washedIn(sulfuricAcid, 250)
        separatedInto(tungsten, titanium)
        byproducts(carbon, titanium)

    val expected = List(
      Call.LangValue("Hyperion Alloy"),
      Call.Formula("C16H12N2O4"),
      Call.IngotForm(Some(4), None),
      Call.Visual(spec),
      Call.Flags(Seq(flagA, flagB)),
      Call.Components(
        Seq(MaterialAmount(tungsten, 2), MaterialAmount(titanium, 1))
      ),
      Call.Ore(
        OreSpec(
          multiplier = 2,
          byproductMultiplier = 3,
          emissive = true,
          washedIn = Some(WashSpec(sulfuricAcid, 250)),
          separatedInto = List(tungsten, titanium),
          byproducts = List(carbon, titanium)
        )
      ),
      Call.BuildAndRegister
    )
    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(calls, expected)
    assertEquals(calls.count(_ == Call.BuildAndRegister), 1)

  test("body exception suppresses buildAndRegister"):
    val (factory, context) = withContext
    given RegistryContext = context
    intercept[RuntimeException]:
      material("boom"):
        langValue("Boom")
        throw new RuntimeException("boom")

    val calls = factory.lastAdapter.get.calls.toList
    assert(!calls.contains(Call.BuildAndRegister))

  test("visual forwards the full VisualSpec payload"):
    val (factory, context) = withContext
    given RegistryContext = context
    val spec =
      VisualSpec(rgb"6f2200", MaterialIconSet.METALLIC, Some(rgb"ffbb33"))
    material("v"):
      visual(spec)
    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(calls, List(Call.Visual(spec), Call.BuildAndRegister))

  test("flags varargs and Scala collection route to the single-flags overload"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("f"):
      flags(flagA, flagB)
      flags(List(flagA, flagB))
    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Flags(Seq(flagA, flagB)),
        Call.Flags(Seq(flagA, flagB)),
        Call.BuildAndRegister
      )
    )

  test("flags Java preset plus extras routes to appendFlags"):
    val (factory, context) = withContext
    given RegistryContext = context
    val preset = new java.util.ArrayList[MaterialFlag]()
    preset.add(flagA)
    preset.add(flagB)
    material("fp"):
      flags(preset, flagC)
    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.AppendFlags(preset, Seq(flagC)),
        Call.BuildAndRegister
      )
    )

  test("components varargs and collection produce the same adapter call"):
    val (factory, context) = withContext
    given RegistryContext = context
    val amounts = List(tungsten * 2, titanium * 1)
    material("c"):
      components(tungsten * 2, titanium * 1)
      components(amounts)
    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Components(
          Seq(MaterialAmount(tungsten, 2), MaterialAmount(titanium, 1))
        ),
        Call.Components(
          Seq(MaterialAmount(tungsten, 2), MaterialAmount(titanium, 1))
        ),
        Call.BuildAndRegister
      )
    )

  test("ore block collects one OreSpec at its position in the call order"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("o"):
      langValue("Ore Material")
      ore:
        settings(multiplier = 2, byproduct = 3, emissive = true)
        washedIn(sulfuricAcid, 250)
        separatedInto(tungsten, titanium)
        byproducts(carbon, titanium)
      formula("X2Y")

    val calls = factory.lastAdapter.get.calls.toList
    val expectedOre = Call.Ore(
      OreSpec(
        multiplier = 2,
        byproductMultiplier = 3,
        emissive = true,
        washedIn = Some(WashSpec(sulfuricAcid, 250)),
        separatedInto = List(tungsten, titanium),
        byproducts = List(carbon, titanium)
      )
    )
    assertEquals(
      calls,
      List(
        Call.LangValue("Ore Material"),
        expectedOre,
        Call.Formula("X2Y"),
        Call.BuildAndRegister
      )
    )

  test("fluid blocks collect settings and submit once in authored order"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("fluids"):
      langValue("Fluid Material")
      gas:
        temperature := 450.K
        color := rgb"7f8fa0"
        density := 0.8
        luminosity := 7
        viscosity := 3500
        burnTime := 200
        block
        disableBucket
        disableColor
        customStill
        attributes(attributeA, attributeB)
      plasma:
        textures(customStill = false, customFlowing = true)
        attributes(List(attributeB))
      fluid(FluidKind.Molten):
        temperature := 1800.K
      formula("F")

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.LangValue("Fluid Material"),
        Call.Fluid(
          FluidSpec(
            kind = FluidKind.Gas,
            temperature = Some(450.K),
            color = Some(rgb"7f8fa0"),
            density = Some(FluidDensity.GramsPerCubicCentimeter(0.8)),
            luminosity = Some(7),
            viscosity = Some(FluidViscosity.Minecraft(3500)),
            burnTime = Some(200),
            attributes = List(attributeA, attributeB),
            textures = Some(FluidTextures(customStill = true)),
            hasBlock = true,
            hasBucket = false,
            colorEnabled = false
          )
        ),
        Call.Fluid(
          FluidSpec(
            kind = FluidKind.Plasma,
            attributes = List(attributeB),
            textures = Some(FluidTextures(customFlowing = true)),
            colorEnabled = false
          )
        ),
        Call.Fluid(
          FluidSpec(
            kind = FluidKind.Molten,
            temperature = Some(1800.K)
          )
        ),
        Call.Formula("F"),
        Call.BuildAndRegister
      )
    )

  test("fluid direct forms preserve standard storage keys and temperatures"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("direct_fluids"):
      liquid()
      liquid(2800.K)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Fluid(FluidSpec(FluidKind.Liquid)),
        Call.Fluid(
          FluidSpec(
            FluidKind.Liquid,
            temperature = Some(2800.K)
          )
        ),
        Call.BuildAndRegister
      )
    )

  test("gasFluid and plasmaFluid preserve storage kinds and temperatures"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("direct_gas_and_plasma"):
      gasFluid()
      gasFluid(450.K)
      plasmaFluid()
      plasmaFluid(12000.K)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Fluid(FluidSpec(FluidKind.Gas)),
        Call.Fluid(
          FluidSpec(
            FluidKind.Gas,
            temperature = Some(450.K)
          )
        ),
        Call.Fluid(FluidSpec(FluidKind.Plasma)),
        Call.Fluid(
          FluidSpec(
            FluidKind.Plasma,
            temperature = Some(12000.K)
          )
        ),
        Call.BuildAndRegister
      )
    )

  test("oreProperty preserves default multipliers"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("direct_ores"):
      oreProperty()
      oreProperty(emissive = true)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Ore(OreSpec()),
        Call.Ore(OreSpec(emissive = true)),
        Call.BuildAndRegister
      )
    )

  test("blast block combines characteristic values into one section payload"):
    val (factory, context) = withContext
    given RegistryContext = context
    val ebf = VA(EV) * 2000.ticks
    val vacuum = VA(HV) * 600.ticks
    material("blast_material"):
      formula("B")
      blast:
        temperature := 3900.K
        gasTier := GasTier.HIGH
        blastStats := ebf
        vacuumStats := vacuum
      langValue("Blast Material")

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Formula("B"),
        Call.Blast(
          BlastSpec(
            temperature = Some(3900.K),
            gasTier = Some(GasTier.HIGH),
            blastStats = Some(RecipeOverride(ebf.eut, Some(ebf.duration))),
            vacuumStats =
              Some(RecipeOverride(vacuum.eut, Some(vacuum.duration)))
          )
        ),
        Call.LangValue("Blast Material"),
        Call.BuildAndRegister
      )
    )

  test("blast recipe overrides accept EUt without a duration"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("blast_eut_only"):
      blast:
        temperature := 1800.K
        blastStats := VA(HV)
        vacuumStats := VA(MV)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Blast(
          BlastSpec(
            temperature = Some(1800.K),
            blastStats = Some(RecipeOverride(VA(HV), None)),
            vacuumStats = Some(RecipeOverride(VA(MV), None))
          )
        ),
        Call.BuildAndRegister
      )
    )

  test("blastTemp preserves temperature and optional gas tier"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("direct_blast"):
      blastTemp(1800.K)
      blastTemp(3900.K, GasTier.HIGH)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Blast(BlastSpec(temperature = Some(1800.K))),
        Call.Blast(
          BlastSpec(
            temperature = Some(3900.K),
            gasTier = Some(GasTier.HIGH)
          )
        ),
        Call.BuildAndRegister
      )
    )

  test("tool block replaces and appends types before submitting once"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("tool_material"):
      formula("T")
      tool(speed = 9.0, damage = 7.0, durability = 2048, level = 4):
        types += toolTypeA
        types := List(toolTypeB, toolTypeC)
        types += toolTypeA
        enchantability := 18
        attackSpeed := 1.2
        durabilityMultiplier := 3
        magnetic
        unbreakable
        ignoreCraftingTools
      langValue("Tool Material")

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Formula("T"),
        Call.Tool(
          ToolSpec(
            speed = 9.0,
            damage = 7.0,
            durability = 2048,
            level = 4,
            types = Some(List(toolTypeB, toolTypeC)),
            additionalTypes = List(toolTypeA),
            enchantability = Some(18),
            attackSpeed = Some(1.2),
            durabilityMultiplier = Some(3),
            magnetic = true,
            unbreakable = true,
            ignoreCraftingTools = true
          )
        ),
        Call.LangValue("Tool Material"),
        Call.BuildAndRegister
      )
    )

  test("armor block collects protection and feature values once"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("armor_material"):
      langValue("Armor Material")
      armor(durability = 55, protection = Armor(4, 8, 7, 4)):
        toughness := 4.0
        knockbackResistance := 0.3
        enchantability := 18
        dyeable
        unbreakable
      formula("A")

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.LangValue("Armor Material"),
        Call.Armor(
          ArmorSpec(
            durability = 55,
            protection = Armor(4, 8, 7, 4),
            toughness = Some(4.0),
            knockbackResistance = Some(0.3),
            enchantability = Some(18),
            dyeable = true,
            unbreakable = true
          )
        ),
        Call.Formula("A"),
        Call.BuildAndRegister
      )
    )

  test("base property overloads and transformations forward in authored order"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("direct_calls"):
      dust()
      dust(3)
      dust(4, 100)
      wood()
      wood(2)
      wood(3, 200)
      ingot()
      ingot(4)
      ingot(5, 300)
      gem()
      gem(3)
      gem(4, 400)
      polymer()
      polymer(2)
      polymer(3, 500)
      formula("X2Y", withFormatting = true)
      oreSmeltInto(tungsten)
      polarizesInto(titanium)
      arcSmeltInto(carbon)
      macerateInto(tungsten)
      ingotSmeltInto(titanium)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Dust(None, None),
        Call.Dust(Some(3), None),
        Call.Dust(Some(4), Some(100)),
        Call.Wood(None, None),
        Call.Wood(Some(2), None),
        Call.Wood(Some(3), Some(200)),
        Call.IngotForm(None, None),
        Call.IngotForm(Some(4), None),
        Call.IngotForm(Some(5), Some(300)),
        Call.Gem(None, None),
        Call.Gem(Some(3), None),
        Call.Gem(Some(4), Some(400)),
        Call.Polymer(None, None),
        Call.Polymer(Some(2), None),
        Call.Polymer(Some(3), Some(500)),
        Call.FormattedFormula("X2Y", withFormatting = true),
        Call.OreSmeltInto(tungsten),
        Call.PolarizesInto(titanium),
        Call.ArcSmeltInto(carbon),
        Call.MacerateInto(tungsten),
        Call.IngotSmeltInto(titanium),
        Call.BuildAndRegister
      )
    )

  test("device property calls preserve named values and domain units"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("device_properties"):
      burnTime(600)
      colorAverage
      rotor(power = 140, efficiency = 125, damage = 3.5, durability = 3200)
      cable(voltage = V(EV), amperage = 4, loss = 2)
      cable(
        voltage = V(HV),
        amperage = 8,
        loss = 0,
        superconducting = true,
        criticalTemperature = 90.K
      )
      fluidPipe(
        maxTemperature = 2800.K,
        throughput = 100,
        gasProof = true
      )
      fluidPipe(
        maxTemperature = 12000.K,
        throughput = 400,
        gasProof = true,
        acidProof = true,
        cryoProof = true,
        plasmaProof = true
      )
      itemPipe(priority = 2, stacksPerSecond = 4.5)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.MaterialBurnTime(600),
        Call.ColorAverage,
        Call.Rotor(RotorSpec(140, 125, 3.5, 3200)),
        Call.Cable(CableSpec(V(EV), 4, 2)),
        Call.Cable(
          CableSpec(
            voltage = V(HV),
            amperage = 8,
            loss = 0,
            superconducting = true,
            criticalTemperature = Some(90.K)
          )
        ),
        Call.FluidPipe(
          FluidPipeSpec(
            maxTemperature = 2800.K,
            throughput = 100,
            gasProof = true
          )
        ),
        Call.FluidPipe(
          FluidPipeSpec(
            maxTemperature = 12000.K,
            throughput = 400,
            gasProof = true,
            acidProof = true,
            cryoProof = true,
            plasmaProof = true
          )
        ),
        Call.ItemPipe(ItemPipeSpec(priority = 2, stacksPerSecond = 4.5)),
        Call.BuildAndRegister
      )
    )

  test("tag and hazard calls keep collections and named defaults"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("hazardous"):
      ignoredTagPrefixes(prefixA, prefixB)
      ignoredTagPrefixes(List(prefixB))
      customTags(itemTagA, itemTagB)
      customTags(List(itemTagB))
      removeHazard
      radioactiveHazard(2.5)
      hazard(hazardTrigger, medicalCondition)
      hazard(
        hazardTrigger,
        medicalCondition,
        progressionMultiplier = 1.5,
        applyToDerivatives = true
      )

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.IgnoredTagPrefixes(Seq(prefixA, prefixB)),
        Call.IgnoredTagPrefixes(Seq(prefixB)),
        Call.CustomTags(Seq(itemTagA, itemTagB)),
        Call.CustomTags(Seq(itemTagB)),
        Call.RemoveHazard,
        Call.RadioactiveHazard(2.5),
        Call.Hazard(
          HazardSpec(
            trigger = hazardTrigger,
            condition = medicalCondition
          )
        ),
        Call.Hazard(
          HazardSpec(
            trigger = hazardTrigger,
            condition = medicalCondition,
            progressionMultiplier = 1.5,
            applyToDerivatives = true
          )
        ),
        Call.BuildAndRegister
      )
    )

  test("element and multi-argument visual calls preserve builder metadata"):
    val (factory, context) = withContext
    given RegistryContext = context
    material("metadata"):
      visual(
        rgb"123456",
        MaterialIconSet.DULL,
        secondary = Some(rgb"abcdef"),
        hasFluidColor = false
      )
      element(elementValue)

    val calls = factory.lastAdapter.get.calls.toList
    assertEquals(
      calls,
      List(
        Call.Visual(
          VisualSpec(
            color = rgb"123456",
            iconSet = MaterialIconSet.DULL,
            secondary = Some(rgb"abcdef"),
            hasFluidColor = Some(false)
          )
        ),
        Call.ChemicalElement(elementValue),
        Call.BuildAndRegister
      )
    )

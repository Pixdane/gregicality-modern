package com.pixdane.gregicality.codegen.core.materials

import cats.data.NonEmptyVector
import com.pixdane.gregicality.core.refs.*
import org.junit.jupiter.api.Assertions.{
  assertEquals,
  assertFalse,
  assertNotEquals,
  assertTrue
}
import org.junit.jupiter.api.Test

class MaterialContentTest:

  @Test
  def newMaterialDefaultsRepresentAuthoredOmission(): Unit =
    val spec = NewMaterialSpec(
      id = valid(RegistryPath.from("polyimide")),
      field = valid(ScalaIdent.from("Polyimide"))
    )

    assertEquals(MaterialIdentity(), spec.identity)
    assertEquals(VisualSpec(), spec.visuals)
    assertEquals(CompositionSpec(), spec.composition)
    assertEquals(MaterialProperties(), spec.properties)
    assertEquals(MaterialFlagSpec(), spec.flags)
    assertEquals(MaterialTagConfig(), spec.tags)
    assertTrue(spec.properties.productIterator.forall(_ == None))

  @Test
  def solidPropertySpecsKeepOnlyAuthoredContent(): Unit =
    val dust = DustPropertySpec()
    val ingot = IngotPropertySpec()

    assertTrue(dust.harvestLevel.isEmpty)
    assertTrue(dust.burnTime.isEmpty)
    assertTrue(ingot.smeltingInto.isEmpty)
    assertTrue(ingot.arcSmeltingInto.isEmpty)
    assertTrue(ingot.macerateInto.isEmpty)
    assertTrue(ingot.magneticMaterial.isEmpty)
    assertEquals(GemPropertySpec(), GemPropertySpec())
    assertEquals(WoodPropertySpec(), WoodPropertySpec())
    assertEquals(PolymerPropertySpec(), PolymerPropertySpec())

  @Test
  def fluidSpecsKeepBuilderDefaultsUnmaterialized(): Unit =
    val entry = FluidEntry(liquidKey)
    val property = FluidPropertySpec(NonEmptyVector.one(entry))

    assertEquals(FluidBuilderSpec(), entry.builder)
    assertTrue(property.primaryKey.isEmpty)
    assertTrue(entry.builder.temperature.isEmpty)
    assertTrue(entry.builder.state.isEmpty)
    assertEquals(FluidColor.Inferred, entry.builder.color)
    assertTrue(entry.builder.density.isEmpty)
    assertTrue(entry.builder.luminosity.isEmpty)
    assertTrue(entry.builder.viscosity.isEmpty)
    assertTrue(entry.builder.attributes.isEmpty)
    assertEquals(FluidTextures.Inferred, entry.builder.textures)
    assertFalse(entry.builder.createBlock)
    assertFalse(entry.builder.disableBucket)
    assertTrue(entry.builder.burnTime.isEmpty)
    assertTrue(entry.builder.name.isEmpty)
    assertTrue(entry.builder.translation.isEmpty)

    val rgb = valid(HexRgb.from(0x123456))
    assertNotEquals(FluidColor.Inferred, FluidColor.Explicit(rgb))
    assertNotEquals(FluidColor.Explicit(rgb), FluidColor.Disabled)
    assertNotEquals(FluidState.Liquid, FluidState.Gas)
    assertNotEquals(
      FluidDensity.GramsPerCubicCentimeter(1.0),
      FluidDensity.Minecraft(1000)
    )
    assertNotEquals(
      FluidViscosity.Poise(1.0),
      FluidViscosity.Minecraft(10000)
    )
    assertNotEquals(
      FluidTextures.CustomStill,
      FluidTextures.CustomStillAndFlowing
    )

  @Test
  def oreDefaultsSelectBuilderOverloadsWithoutStoringNumbers(): Unit =
    val ore = OrePropertySpec()
    val wash = OreWashSpec(iron)
    val multipliers = OreMultipliers(
      ore = valid(PositiveInt.from(2)),
      byproduct = valid(PositiveInt.from(3))
    )

    assertTrue(ore.multipliers.isEmpty)
    assertFalse(ore.emissive)
    assertTrue(ore.directSmeltResult.isEmpty)
    assertTrue(ore.washedIn.isEmpty)
    assertTrue(ore.separatedInto.isEmpty)
    assertTrue(ore.byproducts.isEmpty)
    assertTrue(wash.amount.isEmpty)
    assertEquals(2, multipliers.ore.value)
    assertEquals(3, multipliers.byproduct.value)

  @Test
  def blastAndPolymerDoNotMaterializeInducedProperties(): Unit =
    val blast = BlastPropertySpec(valid(Kelvin.from(3900)))
    val properties = MaterialProperties(
      polymer = Some(PolymerPropertySpec()),
      blast = Some(blast)
    )

    assertTrue(properties.dust.isEmpty)
    assertTrue(properties.ingot.isEmpty)
    assertTrue(blast.gasTier.isEmpty)
    assertTrue(blast.eutOverride.isEmpty)
    assertTrue(blast.durationOverride.isEmpty)
    assertTrue(blast.vacuumEutOverride.isEmpty)
    assertTrue(blast.vacuumDurationOverride.isEmpty)

  @Test
  def compositionVisualsFlagsAndTagsPreserveInputs(): Unit =
    val amount = valid(PositiveInt.from(2))
    val rgb = valid(HexRgb.from(0xabcdef))
    val component = ComponentSpec(iron, amount)
    val formula = FormulaOverride("Fe2", formatSubscripts = true)
    val visuals = VisualSpec(
      primaryColor = ColorSpec.AverageComponents,
      secondaryColor = Some(rgb),
      iconSet = Some(icon),
      fluidColor = FluidColorPolicy.Disabled
    )
    val flags = MaterialFlagSpec(
      presets = Vector(flagPreset),
      flags = Set(flag)
    )
    val tags = MaterialTagConfig(
      ignoredTagPrefixes = Vector(tagPrefix),
      customItemTags = Vector(itemTag)
    )

    assertEquals(
      Vector(component),
      CompositionSpec(Vector(component), Some(formula)).components
    )
    assertEquals(ColorSpec.AverageComponents, visuals.primaryColor)
    assertEquals(Some(rgb), visuals.secondaryColor)
    assertEquals(flags, MaterialFlagSpec(Vector(flagPreset), Set(flag)))
    assertEquals(tags, MaterialTagConfig(Vector(tagPrefix), Vector(itemTag)))

  private val symbol = ScalaSymbolPath.fromFqcn("com.example.Symbol")
  private val iron = MaterialRef(ResourceId("gtceu", "iron"), symbol)
  private val liquidKey = FluidStorageKeyRef(symbol)
  private val icon = MaterialIconRef(symbol)
  private val flagPreset = MaterialFlagPresetRef(symbol)
  private val flag = MaterialFlagRef(symbol)
  private val tagPrefix = TagPrefixRef(symbol)
  private val itemTag = ItemTagRef(symbol)

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      errors => throw new AssertionError(errors.toString),
      identity
    )

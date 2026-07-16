package com.pixdane.gregicality.codegen.core.materials

import cats.data.{NonEmptyVector, Validated}
import com.pixdane.gregicality.core.refs.*
import com.pixdane.gregicality.core.refs.gtceu.MaterialFlagsRef
import org.junit.jupiter.api.Assertions.{
  assertEquals,
  assertSame,
  assertTrue,
  fail
}
import org.junit.jupiter.api.Test

class MaterialValidatorTest:

  @Test
  def validSpecsReturnSameInstanceWithoutExpandingAuthoredContent(): Unit =
    val inputs = Vector(
      material(properties = MaterialProperties()),
      material(
        properties = MaterialProperties(
          dust = Some(DustPropertySpec()),
          ingot = Some(IngotPropertySpec())
        )
      ),
      material(
        properties = MaterialProperties(
          blast = Some(BlastPropertySpec(valid(Kelvin.from(3900))))
        )
      ),
      material(
        properties = MaterialProperties(polymer = Some(PolymerPropertySpec()))
      )
    )

    inputs.foreach { input =>
      val output =
        validResult(MaterialValidator.validateSpec(input, emptySymbols))
      assertSame(input, output)
    }
    assertTrue(inputs(2).properties.ingot.isEmpty)
    assertTrue(inputs(2).properties.dust.isEmpty)
    assertTrue(inputs(3).properties.ingot.isEmpty)
    assertTrue(inputs(3).properties.dust.isEmpty)

  @Test
  def effectiveIngotConflictsWithAuthoredGem(): Unit =
    val conflictingProperties = Vector(
      MaterialProperties(
        ingot = Some(IngotPropertySpec()),
        gem = Some(GemPropertySpec())
      ),
      MaterialProperties(
        blast = Some(BlastPropertySpec(valid(Kelvin.from(3900)))),
        gem = Some(GemPropertySpec())
      ),
      MaterialProperties(
        polymer = Some(PolymerPropertySpec()),
        gem = Some(GemPropertySpec())
      )
    )

    conflictingProperties.foreach { properties =>
      assertTrue(
        issues(
          MaterialValidator.validateSpec(
            material(properties = properties),
            emptySymbols
          )
        )
          .exists(_.isInstanceOf[ValidationIssue.IngotGemConflict])
      )
    }

  @Test
  def fluidKeysAndPrimaryKeyAreCheckedWithoutChoosingDefaults(): Unit =
    val duplicate = material(
      properties = MaterialProperties(
        fluid = Some(
          FluidPropertySpec(
            NonEmptyVector.of(FluidEntry(liquidKey), FluidEntry(liquidKey))
          )
        )
      )
    )
    val missingPrimary = material(
      properties = MaterialProperties(
        fluid = Some(
          FluidPropertySpec(
            fluids = NonEmptyVector.one(FluidEntry(liquidKey)),
            primaryKey = Some(gasKey)
          )
        )
      )
    )
    val validPrimary = material(
      properties = MaterialProperties(
        fluid = Some(
          FluidPropertySpec(
            fluids = NonEmptyVector.of(
              FluidEntry(liquidKey),
              FluidEntry(gasKey)
            ),
            primaryKey = Some(gasKey)
          )
        )
      )
    )

    assertTrue(
      issues(MaterialValidator.validateSpec(duplicate, emptySymbols))
        .exists(_.isInstanceOf[ValidationIssue.DuplicateFluidKey])
    )
    assertTrue(
      issues(MaterialValidator.validateSpec(missingPrimary, emptySymbols))
        .exists(_.isInstanceOf[ValidationIssue.PrimaryFluidKeyMissing])
    )
    assertSame(
      validPrimary,
      validResult(MaterialValidator.validateSpec(validPrimary, emptySymbols))
    )

  @Test
  def validateSetAccumulatesIdentityAndCanonicalPathIssues(): Unit =
    val first = material(id = "iron_", field = "Repeated")
    val second = material(id = "iron_", field = "Repeated")
    val marker = MarkerMaterialSpec(
      id = valid(RegistryPath.from("marker_")),
      field = valid(ScalaIdent.from("Marker"))
    )
    val set = MaterialSet(
      NonEmptyVector.of(
        MaterialDeclaration.NewMaterial(first),
        MaterialDeclaration.NewMaterial(second),
        MaterialDeclaration.MarkerMaterial(marker)
      )
    )
    val symbols =
      MaterialValidationSymbols.fromMaps(canonicalMaterialPaths = Set("iron_"))
    val found = issues(MaterialValidator.validateSet(set, symbols))

    assertTrue(
      found.exists(_.isInstanceOf[ValidationIssue.DuplicateMaterialId])
    )
    assertTrue(
      found.exists(_.isInstanceOf[ValidationIssue.DuplicateMaterialField])
    )
    assertTrue(
      found.exists(_.isInstanceOf[ValidationIssue.MaterialIdTrailingUnderscore])
    )
    assertTrue(
      found.exists(
        _.isInstanceOf[ValidationIssue.CanonicalMaterialIdCollision]
      )
    )

  @Test
  def validMaterialSetReturnsTheOriginalInstance(): Unit =
    val set = MaterialSet(
      NonEmptyVector.of(
        MaterialDeclaration.NewMaterial(material()),
        MaterialDeclaration.MarkerMaterial(
          MarkerMaterialSpec(
            id = valid(RegistryPath.from("marker")),
            field = valid(ScalaIdent.from("Marker"))
          )
        )
      )
    )

    assertSame(
      set,
      validResult(MaterialValidator.validateSet(set, emptySymbols))
    )

  @Test
  def requiredPropertiesUseTheEffectivePropertyView(): Unit =
    val symbols = flagSymbols
    val plateWithIngot = material(
      properties = MaterialProperties(ingot = Some(IngotPropertySpec())),
      flags = MaterialFlagSpec(flags = Set(generatePlate))
    )
    val foilWithBlast = material(
      properties = MaterialProperties(
        blast = Some(BlastPropertySpec(valid(Kelvin.from(3900))))
      ),
      flags = MaterialFlagSpec(flags = Set(generateFoil, generatePlate))
    )
    val plateWithoutDust = material(
      flags = MaterialFlagSpec(flags = Set(generatePlate))
    )
    val foilWithDustOnly = material(
      properties = MaterialProperties(dust = Some(DustPropertySpec())),
      flags = MaterialFlagSpec(flags = Set(generateFoil, generatePlate))
    )

    assertSame(
      plateWithIngot,
      validResult(MaterialValidator.validateSpec(plateWithIngot, symbols))
    )
    assertSame(
      foilWithBlast,
      validResult(MaterialValidator.validateSpec(foilWithBlast, symbols))
    )
    assertTrue(
      issues(MaterialValidator.validateSpec(plateWithoutDust, symbols))
        .exists {
          case ValidationIssue.MissingRequiredProperty(_, _, required) =>
            required == dustKey
          case _ => false
        }
    )
    assertTrue(
      issues(MaterialValidator.validateSpec(foilWithDustOnly, symbols))
        .exists {
          case ValidationIssue.MissingRequiredProperty(_, _, required) =>
            required == ingotKey
          case _ => false
        }
    )

  @Test
  def requiredFlagClosureIsAuthoredRatherThanAdded(): Unit =
    val gear = material(
      flags = MaterialFlagSpec(flags = Set(generateGear))
    )
    val fineWire = material(
      flags = MaterialFlagSpec(flags = Set(generateFineWire))
    )

    val gearMissing =
      issues(MaterialValidator.validateSpec(gear, flagSymbols)).collect {
        case ValidationIssue.MissingRequiredFlag(_, _, required) => required
      }.toSet
    val fineWireMissing =
      issues(MaterialValidator.validateSpec(fineWire, flagSymbols)).collect {
        case ValidationIssue.MissingRequiredFlag(_, _, required) => required
      }.toSet

    assertEquals(Set(generatePlate, generateRod), gearMissing)
    assertEquals(Set(generateFoil, generatePlate), fineWireMissing)
    assertEquals(Set(generateGear), gear.flags.flags)
    assertEquals(Set(generateFineWire), fineWire.flags.flags)

  @Test
  def presetsExpandOnlyInsideValidation(): Unit =
    val input = material(
      properties = MaterialProperties(ingot = Some(IngotPropertySpec())),
      flags = MaterialFlagSpec(presets = Vector(ext2Metal))
    )

    assertSame(
      input,
      validResult(MaterialValidator.validateSpec(input, flagSymbols))
    )
    assertEquals(Vector(ext2Metal), input.flags.presets)
    assertTrue(input.flags.flags.isEmpty)

  @Test
  def unknownFlagsAndPresetsFailClosed(): Unit =
    val input = material(
      flags = MaterialFlagSpec(
        presets = Vector(preset("UNKNOWN_PRESET")),
        flags = Set(flag("UNKNOWN_FLAG"))
      )
    )
    val found = issues(MaterialValidator.validateSpec(input, emptySymbols))

    assertTrue(
      found.exists(_.isInstanceOf[ValidationIssue.UnknownMaterialFlag])
    )
    assertTrue(
      found.exists(_.isInstanceOf[ValidationIssue.UnknownMaterialFlagPreset])
    )

  @Test
  def multiPropertyFlagRequiresEveryProperty(): Unit =
    val symbols = flagSymbols
    val validInput = material(
      properties = MaterialProperties(
        gem = Some(GemPropertySpec()),
        ore = Some(OrePropertySpec())
      ),
      flags = MaterialFlagSpec(flags = Set(highSifterOutput))
    )
    val missingOre = material(
      properties = MaterialProperties(gem = Some(GemPropertySpec())),
      flags = MaterialFlagSpec(flags = Set(highSifterOutput))
    )

    assertSame(
      validInput,
      validResult(MaterialValidator.validateSpec(validInput, symbols))
    )
    assertTrue(
      issues(MaterialValidator.validateSpec(missingOre, symbols)).exists {
        case ValidationIssue.MissingRequiredProperty(_, _, required) =>
          required == oreKey
        case _ => false
      }
    )

  @Test
  def defaultSymbolsUseGeneratedFlagRequirements(): Unit =
    val input = material(
      properties = MaterialProperties(ingot = Some(IngotPropertySpec())),
      flags = MaterialFlagSpec(flags = Set(MaterialFlagsRef.GENERATE_PLATE))
    )

    assertSame(
      input,
      validResult(MaterialValidator.validateSpec(input))
    )

  @Test
  def defaultSymbolsDetectCanonicalGtceuMaterialPaths(): Unit =
    val iron = material(id = "iron", field = "LocalIron")
    val set = MaterialSet(
      NonEmptyVector.one(MaterialDeclaration.NewMaterial(iron))
    )

    assertTrue(
      issues(MaterialValidator.validateSet(set))
        .exists(_.isInstanceOf[ValidationIssue.CanonicalMaterialIdCollision])
    )

  @Test
  def targetStatesThatWouldMaterializeBuilderDefaultsAreRejected(): Unit =
    val disabledDefaultColor = material(
      properties = MaterialProperties(),
      visuals = VisualSpec(fluidColor = FluidColorPolicy.Disabled)
    )
    val disabledAverageColor = material(
      properties = MaterialProperties(),
      visuals = VisualSpec(
        primaryColor = ColorSpec.AverageComponents,
        fluidColor = FluidColorPolicy.Disabled
      )
    )
    val blastDurationWithoutEut = material(
      properties = MaterialProperties(
        blast = Some(
          BlastPropertySpec(
            temperature = valid(Kelvin.from(3900)),
            durationOverride = Some(valid(DurationTicks.from(1000)))
          )
        )
      )
    )
    val vacuumDurationWithoutEut = material(
      properties = MaterialProperties(
        blast = Some(
          BlastPropertySpec(
            temperature = valid(Kelvin.from(3900)),
            vacuumDurationOverride = Some(valid(DurationTicks.from(300)))
          )
        )
      )
    )

    assertTrue(
      issues(MaterialValidator.validateSpec(disabledDefaultColor, emptySymbols))
        .exists(
          _.isInstanceOf[ValidationIssue.FluidColorPolicyRequiresExplicitColor]
        )
    )
    assertTrue(
      issues(MaterialValidator.validateSpec(disabledAverageColor, emptySymbols))
        .exists(
          _.isInstanceOf[ValidationIssue.FluidColorPolicyRequiresExplicitColor]
        )
    )
    assertTrue(
      issues(
        MaterialValidator.validateSpec(blastDurationWithoutEut, emptySymbols)
      ).exists(_.isInstanceOf[ValidationIssue.BlastDurationRequiresEut])
    )
    assertTrue(
      issues(
        MaterialValidator.validateSpec(vacuumDurationWithoutEut, emptySymbols)
      ).exists(_.isInstanceOf[ValidationIssue.VacuumDurationRequiresEut])
    )

  private val generatePlate = flag("GENERATE_PLATE")
  private val generateRod = flag("GENERATE_ROD")
  private val generateGear = flag("GENERATE_GEAR")
  private val generateFoil = flag("GENERATE_FOIL")
  private val generateFineWire = flag("GENERATE_FINE_WIRE")
  private val generateLongRod = flag("GENERATE_LONG_ROD")
  private val generateBoltScrew = flag("GENERATE_BOLT_SCREW")
  private val highSifterOutput = flag("HIGH_SIFTER_OUTPUT")
  private val ext2Metal = preset("EXT2_METAL")
  private val dustKey = propertyKey("DUST")
  private val ingotKey = propertyKey("INGOT")
  private val gemKey = propertyKey("GEM")
  private val oreKey = propertyKey("ORE")

  private val flagSymbols = MaterialValidationSymbols.fromMaps(
    flagRequirements = Map(
      generatePlate -> MaterialFlagRequirements(
        requiredFlags = Vector.empty,
        requiredProperties = Vector(dustKey)
      ),
      generateRod -> MaterialFlagRequirements(
        requiredFlags = Vector.empty,
        requiredProperties = Vector(dustKey)
      ),
      generateGear -> MaterialFlagRequirements(
        requiredFlags = Vector(generatePlate, generateRod),
        requiredProperties = Vector(dustKey)
      ),
      generateFoil -> MaterialFlagRequirements(
        requiredFlags = Vector(generatePlate),
        requiredProperties = Vector(ingotKey)
      ),
      generateFineWire -> MaterialFlagRequirements(
        requiredFlags = Vector(generateFoil),
        requiredProperties = Vector(ingotKey)
      ),
      generateLongRod -> MaterialFlagRequirements(
        requiredFlags = Vector(generateRod),
        requiredProperties = Vector(dustKey)
      ),
      generateBoltScrew -> MaterialFlagRequirements(
        requiredFlags = Vector(generateRod),
        requiredProperties = Vector(dustKey)
      ),
      highSifterOutput -> MaterialFlagRequirements(
        requiredFlags = Vector.empty,
        requiredProperties = Vector(gemKey, oreKey)
      )
    ),
    presetMembers = Map(
      ext2Metal -> Vector(
        generatePlate,
        generateRod,
        generateLongRod,
        generateBoltScrew
      )
    )
  )

  private val emptySymbols = MaterialValidationSymbols.fromMaps()

  private val liquidKey =
    FluidStorageKeyRef(ScalaSymbolPath.fromFqcn("example.FluidKeys.LIQUID"))
  private val gasKey =
    FluidStorageKeyRef(ScalaSymbolPath.fromFqcn("example.FluidKeys.GAS"))

  private def material(
      id: String = "test_material",
      field: String = "TestMaterial",
      properties: MaterialProperties = MaterialProperties(),
      flags: MaterialFlagSpec = MaterialFlagSpec(),
      visuals: VisualSpec = VisualSpec()
  ): NewMaterialSpec =
    NewMaterialSpec(
      id = valid(RegistryPath.from(id)),
      field = valid(ScalaIdent.from(field)),
      properties = properties,
      flags = flags,
      visuals = visuals
    )

  private def flag(name: String): MaterialFlagRef =
    MaterialFlagRef(
      ScalaSymbolPath.member("com.example.MaterialFlags", name)
    )

  private def preset(name: String): MaterialFlagPresetRef =
    MaterialFlagPresetRef(
      ScalaSymbolPath.member("com.example.GTMaterials", name)
    )

  private def propertyKey(name: String): MaterialPropertyKeyRef =
    MaterialPropertyKeyRef(
      ScalaSymbolPath.member("com.example.PropertyKey", name)
    )

  private def issues[A](result: ValidationResult[A]): Vector[ValidationIssue] =
    result match
      case Validated.Invalid(errors) => errors.toChain.toVector
      case Validated.Valid(_)        => fail("expected validation failure")

  private def validResult[A](result: ValidationResult[A]): A =
    result.fold(
      errors => throw new AssertionError(errors.toString),
      identity
    )

  private def valid[A](result: ValidationResult[A]): A =
    validResult(result)

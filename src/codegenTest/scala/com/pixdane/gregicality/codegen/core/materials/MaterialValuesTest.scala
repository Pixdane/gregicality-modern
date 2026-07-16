package com.pixdane.gregicality.codegen.core.materials

import cats.data.Validated
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue, fail}
import org.junit.jupiter.api.Test

class MaterialValuesTest:

  @Test
  def registryPathValidatesMinecraftPathSyntax(): Unit =
    val path = valid(RegistryPath.from("chemistry/polyimide_"))
    assertEquals("chemistry/polyimide_", path.value)

    assertInvalidScalar(RegistryPath.from(""), "RegistryPath")
    assertInvalidScalar(RegistryPath.from("Gregicality"), "RegistryPath")
    assertInvalidScalar(
      RegistryPath.from("gregicality:polyimide"),
      "RegistryPath"
    )
    assertInvalidScalar(RegistryPath.from("has space"), "RegistryPath")

  @Test
  def scalaIdentRejectsInvalidIdentifiersAndKeywords(): Unit =
    val ident = valid(ScalaIdent.from("Polyimide"))
    assertEquals("Polyimide", ident.value)

    assertInvalidScalar(ScalaIdent.from("3Bad"), "ScalaIdent")
    assertInvalidScalar(ScalaIdent.from("has.dot"), "ScalaIdent")
    assertInvalidScalar(ScalaIdent.from("class"), "ScalaIdent")

  @Test
  def hexRgbAcceptsOnlyTwentyFourBitColors(): Unit =
    assertEquals(0, valid(HexRgb.from(0)).value)
    assertEquals(0xffffff, valid(HexRgb.from(0xffffff)).value)
    assertInvalidScalar(HexRgb.from(-1), "HexRgb")
    assertInvalidScalar(HexRgb.from(0x1000000), "HexRgb")

  @Test
  def integerWrappersEnforceTheirBounds(): Unit =
    assertEquals(1, valid(PositiveInt.from(1)).value)
    assertInvalidScalar(PositiveInt.from(0), "PositiveInt")
    assertInvalidScalar(PositiveInt.from(-1), "PositiveInt")

    assertEquals(0, valid(NonNegativeInt.from(0)).value)
    assertInvalidScalar(NonNegativeInt.from(-1), "NonNegativeInt")

    assertEquals(1, valid(Kelvin.from(1)).value)
    assertInvalidScalar(Kelvin.from(0), "Kelvin")

    assertEquals(1, valid(DurationTicks.from(1)).value)
    assertInvalidScalar(DurationTicks.from(0), "DurationTicks")

    assertEquals(0, valid(HarvestLevel.from(0)).value)
    assertInvalidScalar(HarvestLevel.from(-1), "HarvestLevel")

    assertEquals(0, valid(BurnTimeTicks.from(0)).value)
    assertInvalidScalar(BurnTimeTicks.from(-1), "BurnTimeTicks")

    assertEquals(0, valid(FluidTemperature.from(0)).value)
    assertInvalidScalar(FluidTemperature.from(-1), "FluidTemperature")

  @Test
  def voltageMustBePositive(): Unit =
    assertEquals(1L, valid(Voltage.from(1L)).value)
    assertInvalidScalar(Voltage.from(0L), "Voltage")
    assertInvalidScalar(Voltage.from(-1L), "Voltage")

  @Test
  def voltageExpressionsPreserveAuthoredForms(): Unit =
    val ev = valid(ScalaIdent.from("EV"))
    val voltage = valid(Voltage.from(2048L))

    assertEquals(ev, VoltageExpr.Tier(ev).name)
    assertEquals(ev, VoltageExpr.VA(ev).name)
    assertEquals(voltage, VoltageExpr.Literal(voltage).value)

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      errors => throw new AssertionError(errors.toString),
      identity
    )

  private def assertInvalidScalar(
      result: ValidationResult[?],
      expectedName: String
  ): Unit =
    result match
      case Validated.Invalid(errors) =>
        errors.head match
          case ValidationIssue.InvalidScalar(name, _, requirement) =>
            assertEquals(expectedName, name)
            assertTrue(requirement.nonEmpty)
      case Validated.Valid(_) =>
        fail(s"Expected invalid $expectedName")

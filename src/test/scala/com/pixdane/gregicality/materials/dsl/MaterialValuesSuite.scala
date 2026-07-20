package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.pixdane.gregicality.materials.dsl.VoltageTier.*
import munit.FunSuite

class MaterialValuesSuite extends FunSuite:
  test("constructs validated domain values"):
    assertEquals(3900.K.value, 3900)
    assertEquals(2000.ticks.value, 2000)
    assertEquals(rgb"6f2200".value, 0x6f2200)

  test("validates temperature and duration boundaries"):
    assertEquals(Kelvin(1).value, 1)
    assertEquals(Ticks(0).value, 0)

    intercept[IllegalArgumentException]:
      Kelvin(0)
    intercept[IllegalArgumentException]:
      Kelvin(-1)
    intercept[IllegalArgumentException]:
      Ticks(-1)

  test("validates packed and parsed RGB boundaries"):
    assertEquals(HexColor(0).value, 0)
    assertEquals(HexColor(0xffffff).value, 0xffffff)
    assertEquals(HexColor.fromHex("#ABCDEF").value, 0xabcdef)

    intercept[IllegalArgumentException]:
      HexColor(-1)
    intercept[IllegalArgumentException]:
      HexColor(0x1000000)
    intercept[IllegalArgumentException]:
      HexColor.fromHex("")
    intercept[IllegalArgumentException]:
      HexColor.fromHex("fff")
    intercept[IllegalArgumentException]:
      HexColor.fromHex("xyzxyz")

  test("maps voltage tiers to GTCEu voltage tables"):
    assertEquals(V(EV).value, 2048L)
    assertEquals(VA(EV).value, 1920)

  test("constructs material amounts and recipe statistics"):
    val testMaterial: Material = null
    val amount = testMaterial * 12
    val stats = VA(EV) * 2000.ticks

    assertEquals(amount.amount, 12)
    assertEquals(stats.eut.value, 1920)
    assertEquals(stats.duration.value, 2000)

  test("requires material amounts to be strictly positive"):
    val testMaterial: Material = null

    assertEquals(MaterialAmount(testMaterial, 1).amount, 1)
    intercept[IllegalArgumentException]:
      MaterialAmount(testMaterial, 0)
    intercept[IllegalArgumentException]:
      MaterialAmount(testMaterial, -1)

package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.pixdane.gregicality.materials.dsl.VoltageTier.*
import munit.FunSuite

class MaterialValuesSuite extends FunSuite:
  test("constructs validated domain values"):
    assertEquals(3900.K.value, 3900)
    assertEquals(2000.ticks.value, 2000)
    assertEquals(rgb"6f2200".value, 0x6f2200)

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

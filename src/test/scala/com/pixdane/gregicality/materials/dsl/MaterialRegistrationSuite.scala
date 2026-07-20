package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.pixdane.gregicality.common.data.MaterialRegistration
import com.pixdane.gregicality.common.data.MaterialRegistrationInputs
import munit.FunSuite

import com.pixdane.gregicality.materials.dsl.VoltageTier.*

/** Verifies the real material-definition module through the recording adapter.
  *
  * The definitions are the same functions used by the Forge event callback, but
  * the test injects null base-material references so no GTCEu global material
  * table or Forge registry is initialized.
  */
class MaterialRegistrationSuite extends FunSuite:

  test("registerAll authors the compact and stress materials in order"):
    val factory = new FakeFactory
    given RegistryContext = RegistryContext("gregicality", factory)
    val inputs = MaterialRegistrationInputs(
      carbon = null,
      hydrogen = null,
      nitrogen = null,
      oxygen = null,
      sulfuricAcid = null
    )

    val registered = MaterialRegistration.registerAll(inputs)

    assertEquals(
      factory.adapters.map(_.id).toList,
      List(
        new net.minecraft.resources.ResourceLocation(
          "gregicality",
          "polyimide"
        ),
        new net.minecraft.resources.ResourceLocation("gregicality", "hyperion")
      )
    )
    assertEquals(registered.polyimide, null)
    assertEquals(registered.hyperion, null)

    val polyimideCalls = factory.adapters.head.calls.toList
    assertEquals(
      polyimideCalls,
      List(
        Call.LangValue("Polyimide"),
        Call.Polymer(Some(4), None),
        Call.MaterialBurnTime(200),
        Call.Visual(
          VisualSpec(
            color = rgb"2d2d2d",
            iconSet = MaterialIconSet.METALLIC,
            secondary = Some(rgb"111111"),
            hasFluidColor = Some(false)
          )
        ),
        Call.Flags(
          Seq(MaterialFlags.GENERATE_FOIL, MaterialFlags.GENERATE_PLATE)
        ),
        Call.Components(
          Seq(
            MaterialAmount(null, 22),
            MaterialAmount(null, 10),
            MaterialAmount(null, 2),
            MaterialAmount(null, 5)
          )
        ),
        Call.FormattedFormula("(C22H10N2O5)n", withFormatting = true),
        Call.Fluid(
          FluidSpec(
            kind = FluidKind.Liquid,
            temperature = Some(700.K),
            burnTime = Some(400),
            textures = Some(FluidTextures(customStill = true)),
            hasBlock = true,
            hasBucket = false,
            colorEnabled = false
          )
        ),
        Call.Blast(
          BlastSpec(
            temperature = Some(3900.K),
            gasTier = Some(
              com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier.HIGH
            ),
            blastStats = Some(RecipeOverride(VA(EV), Some(1000.ticks))),
            vacuumStats = Some(RecipeOverride(VA(HV), Some(300.ticks)))
          )
        ),
        Call.BuildAndRegister
      )
    )

    val hyperionCalls = factory.adapters(1).calls.toList
    assertEquals(hyperionCalls.last, Call.BuildAndRegister)
    assertEquals(
      hyperionCalls.collect { case Call.LangValue(value) => value },
      List("Hyperion Alloy")
    )
    assertEquals(
      hyperionCalls.collect { case Call.Ore(spec) => spec },
      List(
        OreSpec(
          multiplier = 2,
          byproductMultiplier = 3,
          emissive = true,
          washedIn = Some(WashSpec(null, 250)),
          separatedInto = List(null, null),
          byproducts = List(null)
        )
      )
    )
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.Fluid]), 2)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.Blast]), 1)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.Tool]), 1)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.Armor]), 1)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.Rotor]), 1)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.Cable]), 1)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.FluidPipe]), 1)
    assertEquals(hyperionCalls.count(_.isInstanceOf[Call.ItemPipe]), 0)

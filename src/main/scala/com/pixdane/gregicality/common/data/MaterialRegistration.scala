package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier
import com.pixdane.gregicality.materials.dsl.*

import com.pixdane.gregicality.materials.dsl.VoltageTier.*

/** Existing GTCEu materials referenced by this registration slice.
  *
  * Keeping these dependencies explicit lets the production Forge callback
  * inject `GTMaterials` only after the material registry is ready, while unit
  * tests can use null recording placeholders without initializing GTCEu's
  * global material table.
  *
  * @param carbon
  *   GTCEu carbon material used in compositions
  * @param hydrogen
  *   GTCEu hydrogen material used in compositions
  * @param nitrogen
  *   GTCEu nitrogen material used in compositions and byproducts
  * @param oxygen
  *   GTCEu oxygen material used in compositions and ore separation
  * @param sulfuricAcid
  *   GTCEu sulfuric-acid material used for Hyperion ore washing
  */
final case class MaterialRegistrationInputs(
    carbon: Material,
    hydrogen: Material,
    nitrogen: Material,
    oxygen: Material,
    sulfuricAcid: Material
)

/** Materials created by one invocation of [[MaterialRegistration.registerAll]].
  *
  * @param polyimide
  *   the compact polymer migration slice
  * @param hyperion
  *   the broad DSL integration fixture
  */
final case class RegisteredMaterials(
    polyimide: Material,
    hyperion: Material
)

/** Runtime material definitions authored through the contextual DSL. */
object MaterialRegistration:

  /** Registers the initial compact migration slice and the DSL stress material.
    *
    * Definitions run in source order and use the caller's [[RegistryContext]],
    * so the same method can target either the real GTCEu adapter or a recording
    * test adapter.
    */
  def registerAll(inputs: MaterialRegistrationInputs)(using
      RegistryContext
  ): RegisteredMaterials =
    val polyimide = material("polyimide"):
      langValue("Polyimide")
      polymer(4)
      burnTime(200)
      visual(
        rgb"2d2d2d",
        MaterialIconSet.METALLIC,
        secondary = Some(rgb"111111"),
        hasFluidColor = false
      )
      flags(MaterialFlags.GENERATE_FOIL, MaterialFlags.GENERATE_PLATE)
      components(
        inputs.carbon * 22,
        inputs.hydrogen * 10,
        inputs.nitrogen * 2,
        inputs.oxygen * 5
      )
      formula("(C22H10N2O5)n", withFormatting = true)
      fluid(FluidKind.Liquid):
        temperature := 700.K
        burnTime := 400
        customStill
        block
        disableBucket
        disableColor
      blast:
        temperature := 3900.K
        gasTier := GasTier.HIGH
        blastStats := VA(EV) * 1000.ticks
        vacuumStats := VA(HV) * 300.ticks

    val hyperion = material("hyperion"):
      langValue("Hyperion Alloy")
      formula("C16H12N2O4")
      ingot(4)
      visual(
        rgb"6f2200",
        MaterialIconSet.METALLIC,
        secondary = Some(rgb"ffbb33")
      )
      flags(MaterialFlags.GENERATE_PLATE, MaterialFlags.GENERATE_ROD)
      components(polyimide * 1, inputs.carbon * 2, inputs.oxygen * 1)
      ore:
        settings(multiplier = 2, byproduct = 3, emissive = true)
        washedIn(inputs.sulfuricAcid, 250)
        separatedInto(inputs.carbon, inputs.oxygen)
        byproducts(inputs.nitrogen)
      liquid(2800.K)
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
      blast:
        temperature := 3900.K
        gasTier := GasTier.HIGH
        blastStats := VA(EV) * 2000.ticks
        vacuumStats := VA(HV) * 600.ticks
      tool(speed = 9.0, damage = 7.0, durability = 2048, level = 4):
        enchantability := 18
        attackSpeed := 1.2
        durabilityMultiplier := 3
        magnetic
        unbreakable
        ignoreCraftingTools
      armor(durability = 55, protection = Armor(4, 8, 7, 4)):
        toughness := 4.0
        knockbackResistance := 0.3
        enchantability := 18
        dyeable
        unbreakable
      rotor(power = 140, efficiency = 125, damage = 3.5, durability = 3200)
      cable(voltage = V(EV), amperage = 4, loss = 2)
      fluidPipe(maxTemperature = 2800.K, throughput = 100, gasProof = true)

    RegisteredMaterials(polyimide, hyperion)

package com.pixdane.gregicality.common.machine.multiblock

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.GTValues.{UEV, UHV, UV, VLVH, VLVT}
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.{
  IMachineBlockEntity,
  MultiblockMachineDefinition
}
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine
import com.gregtechceu.gtceu.api.machine.multiblock.{
  PartAbility,
  WorkableElectricMultiblockMachine
}
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder
import com.gregtechceu.gtceu.common.data.GTBlocks
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMultis
import com.gregtechceu.gtceu.common.machine.multiblock.electric.BedrockOreMinerMachine
import com.pixdane.gregicality.Gregicality
import com.pixdane.gregicality.dsl.machine.MachineBuilderDsl.{abilities => _, *}
import com.pixdane.gregicality.dsl.machine.MultiblockMachineBuilderDsl.*
import com.pixdane.gregicality.Gregicality.REGISTRATE
import com.pixdane.gregicality.dsl.api.ComponentDsl.*
import com.pixdane.gregicality.dsl.api.TraceabilityPredicateDsl.*
import com.pixdane.gregicality.dsl.pattern.FactoryBlockPatternDsl.*
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

// TODO placeholder: VoidMinerMachine has no real mining logic yet.
// It uses DUMMY_RECIPES and reuses BedrockOreMiner structure/casing to validate
// the multiblock registration DSL. Real void-mining RecipeLogic and ore
// production are not implemented.
class VoidMinerMachine(holder: IMachineBlockEntity, tier: Int)
    extends WorkableElectricMultiblockMachine(holder)
    with ITieredMachine

object VoidMinerMachine:

  def register(): Array[MultiblockMachineDefinition] =
    registerTieredMultis(
      REGISTRATE,
      "void_miner",
      VoidMinerMachine(_, _),
      build(_)(using _),
      UV,
      UHV,
      UEV
    )

  private def build(tier: Integer)(using
      builder: MultiblockMachineBuilder[_, _]
  ): MultiblockMachineDefinition =
    multiblock:
      rotationState(RotationState.NON_Y_AXIS)

      langValue(s"${VLVH(tier)} Bedrock Ore Miner ${VLVT(tier)}")

      recipeType(DUMMY_RECIPES)

      tooltips:
        literal("text")
        translatable("gregicality.machine.void_miner.description")
      appearanceBlock(() => getCasingState(tier))

      pattern("""
          | +X<-o----> +Y
          |     |
          |    -Z
          """):

        structure := """
          |XXX|#F#|#F#|#F#|###|###|###
          |XXX|FCF|FCF|FCF|#F#|#F#|#F#
          |XSX|#F#|#F#|#F#|###|###|###
          """

        where:
          val definition: MultiblockMachineDefinition =
            summon[MultiblockMachineDefinition]

          'S' := controller(definition.get)
          'X' :=
            blocks(VoidMinerMachine.getCasingState(tier)):
              setMinGlobalLimited(3)
            | abilities(PartAbility.INPUT_ENERGY):
              setMinGlobalLimited(1)
              setMaxGlobalLimited(2)
            | abilities(PartAbility.EXPORT_ITEMS):
              setMaxGlobalLimited(1)

          'C' := blocks(BedrockOreMinerMachine.getCasingState(tier))
          'F' := blocks(BedrockOreMinerMachine.getFrameState(tier))
          '#' := any

      workableCasingModel(
        VoidMinerMachine.getBaseTexture(tier),
        GTCEu.id("block/multiblock/bedrock_ore_miner")
      )

  private def getCasingState(tier: Integer): Block =
    tier match
      case UV  => GTBlocks.CASING_STEEL_SOLID.get
      case UHV => GTBlocks.CASING_TITANIUM_STABLE.get
      case UEV => GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get
      case _   => GTBlocks.CASING_STEEL_SOLID.get

  private def getBaseTexture(tier: Integer): ResourceLocation =
    tier match
      case UV =>
        GTCEu.id("block/casings/solid/machine_casing_solid_steel")
      case UHV =>
        GTCEu.id("block/casings/solid/machine_casing_stable_titanium")
      case UEV =>
        GTCEu.id("block/casings/solid/machine_casing_robust_tungstensteel")
      case _ =>
        GTCEu.id("block/casings/solid/machine_casing_solid_steel")

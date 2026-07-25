package com.pixdane.gregicality.common.machine.multiblock

import com.gregtechceu.gtceu.GTCEu
import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.GTValues.{UEV, UHV, UV, VLVH, VLVT}
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.data.chemical.material.Material
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
import com.gregtechceu.gtceu.common.data.{GTBlocks, GTMaterials}
import com.gregtechceu.gtceu.common.data.GTRecipeTypes.DUMMY_RECIPES
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMultis
import com.pixdane.gregicality.Gregicality
import com.pixdane.gregicality.dsl.machine.MachineBuilderDsl.{abilities => _, *}
import com.pixdane.gregicality.dsl.machine.MultiblockMachineBuilderDsl.*
import com.pixdane.gregicality.Gregicality.REGISTRATE
import com.pixdane.gregicality.dsl.api.ComponentDsl.*
import com.pixdane.gregicality.dsl.api.TraceabilityPredicateDsl.*
import com.pixdane.gregicality.dsl.api.TraceabilityPredicateDsl.Predicates.*
import com.pixdane.gregicality.dsl.pattern.FactoryBlockPatternDsl.*
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

// TODO: VoidMinerMachine has the real 9x10x10 structure from TJFork but still
// uses DUMMY_RECIPES and placeholder casings/frames. Real void-mining
// RecipeLogic, ore production, and the temperature/fluid system are not
// implemented. Materials (Seaborgium/Bohrium) and fluids (Pyrotheum/
// Cryotheum/DrillingMud) use GT built-in placeholders.
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
      rotationState := RotationState.NON_Y_AXIS

      langValue := s"${VLVH(tier)} Void Ore Miner ${VLVT(tier)}"

      recipeType(DUMMY_RECIPES)

      tooltips:
        literal("text")
        translatable("gregicality.machine.void_miner.description")

      appearanceBlock := (() => getCasingState(tier))

      pattern("""
          | +X<-o----> +Y
          |     |
          |    -Z
          """):

        structure := """
            |CCCCCCCCC|CCCCCCCCC|C       C|C       C|C       C|CCCCCCCCC|CFFFFFFFC|CFFFFFFFC|C       C|C       C
            |C       C|C       C|         |         |         |C   D   C|F  DDD  F|F  DDD  F|   DDD   |         
            |C       C|C       C|         |    D    |   DDD   |C  DDD  C|F DD DD F|F D   D F|  D   D  |         
            |C   D   C|C   D   C|   DDD   |   D D   |  DD DD  |C D   D C|FDD   DDF|FD     DF| D     D |         
            |C  D D  C|C  D D  C|   D D   |  D   D  |  D   D  |CDD   DDC|FD     DF|FD     DF| D     D |         
            |C   D   C|C   D   C|   DDD   |   D D   |  DD DD  |C D   D C|FDD   DDF|FD     DF| D     D |         
            |C       C|C       C|         |    D    |   DDD   |C  DDD  C|F DD DD F|F D   D F|  D   D  |         
            |C       C|C       C|         |         |         |C   D   C|F  DDD  F|F  DDD  F|   DDD   |         
            |CCCCCCCCC|CCCCSCCCC|C       C|C       C|C       C|CCCCCCCCC|CFFFFFFFC|CFFFFFFFC|C       C|C       C
            """

        where:
          val definition: MultiblockMachineDefinition =
            summon[MultiblockMachineDefinition]

          'S' := controller(definition.get)

          'C' := blocks(getCasingState(tier))
            | abilities(PartAbility.EXPORT_ITEMS, PartAbility.EXPORT_FLUIDS)
            | abilities(PartAbility.IMPORT_FLUIDS) {
              minGlobalLimited := 1
            }
            | abilities(PartAbility.INPUT_ENERGY) {
              minGlobalLimited := 1
            }
            | abilities(PartAbility.MAINTENANCE)

          'D' := blocks(getSecondaryCasingState(tier))

          'F' := frames(getFrameMaterial(tier))

      workableCasingModel(
        baseCasing = VoidMinerMachine.getBaseTexture(tier),
        workableModel = GTCEu.id("block/multiblock/bedrock_ore_miner")
      )

  // Placeholder primary casing 'C' -- TJFork uses Hastelloy-N/Tritanium/Quantum
  private def getCasingState(tier: Integer): Block =
    tier match
      case UV  => GTBlocks.CASING_STEEL_SOLID.get
      case UHV => GTBlocks.CASING_TITANIUM_STABLE.get
      case UEV => GTBlocks.CASING_TUNGSTENSTEEL_ROBUST.get
      case _   => GTBlocks.CASING_STEEL_SOLID.get

  // Placeholder secondary casing 'D' -- TJFork uses Staballoy/Incoloy-813/Hastelloy-X78
  private def getSecondaryCasingState(tier: Integer): Block =
    tier match
      case UV  => GTBlocks.CASING_STAINLESS_CLEAN.get
      case UHV => GTBlocks.CASING_PTFE_INERT.get
      case UEV => GTBlocks.CASING_HSSE_STURDY.get
      case _   => GTBlocks.CASING_STAINLESS_CLEAN.get

  // Placeholder frame 'F' -- TJFork uses TungstenSteel/Seaborgium/Bohrium
  private def getFrameMaterial(tier: Integer): Material =
    tier match
      case UV  => GTMaterials.TungstenSteel
      case UHV => GTMaterials.NaquadahAlloy
      case UEV => GTMaterials.HastelloyX
      case _   => GTMaterials.TungstenSteel

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

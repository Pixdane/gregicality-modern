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
import com.tterrag.registrate.providers.RegistrateLangProvider
import com.pixdane.gregicality.dsl.machine.MachineBuilderDsl.{abilities as _, *}
import com.pixdane.gregicality.dsl.machine.MultiblockMachineBuilderDsl.*
import com.pixdane.gregicality.Gregicality.REGISTRATE
import com.pixdane.gregicality.dsl.api.ComponentDsl.*
import com.pixdane.gregicality.dsl.api.FactoryBlockPatternDsl.*
import com.pixdane.gregicality.dsl.api.LangDsl.{machineTooltip, translations}
import com.pixdane.gregicality.dsl.api.TraceabilityPredicateDsl.*
import com.pixdane.gregicality.dsl.api.TraceabilityPredicateDsl.Predicates.*
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block

// TODO: VoidMiner migration status (skeleton only; runtime logic missing).
//
// [x] Structure pattern (9x10x10, matches TJFork)
// [x] Abilities + maintenance hatch declared
// [x] Lang/tooltips + generated assets
// [x] Config (maxTemp, fluids, ore blacklist/whitelist, oreVariants, oreProcStep)
// [ ] Consume config
// [ ] Custom materials — Seaborgium, Bohrium (IngotMaterial + GENERATE_FRAME).
// [ ] Custom fluids — Pyrotheum, Cryotheum, DrillingMud, UsedDrillingMud.
// [ ] Custom casings — MetalCasing1 (Hastelloy-N/Incoloy-813/Hastelloy-X78),
//     MetalCasing2 (Staballoy/Tritanium/Quantum); replace GT placeholders.
// [ ] Custom frames — Seaborgium/Bohrium frames; replace BedrockOreMiner borrow.
// [ ] VoidMinerHandler — ore collection via OrePrefix, ORES/ORES_2/ORES_3 lists.
// [ ] updateFormedValid — temperature system (Pyrotheum up / Cryotheum down),
//     overheat, fluid consumption (DrillingMud -> UsedDrillingMud), ore output.
// [ ] NBT persistence — temperature, overheat flag, drilling fluid state.
// [ ] RecipeType — decide: keep DUMMY_RECIPES (TJFork has no recipe map) or
//     model temp/fluid as a GTRecipeType.
// [ ] Assembly-line recipes — 3 tiers (Mk I needs Large Miner prerequisites).
// [ ] JEI shapeInfo — concrete-block preview layout (pattern uses predicates,
//     shapeInfo uses specific blocks/tier hatches; chars can differ).
// [ ] Large Miner prerequisites — Basic/Large/Advanced miners (recipe inputs).
class VoidMiner(holder: IMachineBlockEntity, tier: Int)
    extends WorkableElectricMultiblockMachine(holder)
    with ITieredMachine

object VoidMiner:

  private inline val ID = "void_miner"

  def register(): Array[MultiblockMachineDefinition] =
    registerTieredMultis(
      REGISTRATE,
      ID,
      VoidMiner(_, _),
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
        multiTranslatable(s"gregicality.machine.$ID.tooltip", 6)

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

          'C' := blocks(getCasingState(tier)) {
            minGlobalLimited := 3
          } | abilities(PartAbility.EXPORT_ITEMS) {
            exactLimit := 1
            previewCount := 1
          } | abilities(PartAbility.EXPORT_FLUIDS) {
            exactLimit := 1
            previewCount := 1
          } | abilities(PartAbility.IMPORT_FLUIDS) {
            exactLimit := 1
            previewCount := 1
          } | abilities(PartAbility.INPUT_ENERGY) {
            minGlobalLimited := 1
            maxGlobalLimited := 2
            previewCount := 1
          } | abilities(PartAbility.MAINTENANCE) {
            exactLimit := 1
            previewCount := 1
          }

          'D' := blocks(getSecondaryCasingState(tier))

          'F' := frames(getFrameMaterial(tier))

      workableCasingModel(
        baseCasing = VoidMiner.getBaseTexture(tier),
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

  def langEntries(provider: RegistrateLangProvider): Unit =
    given RegistrateLangProvider = provider
    translations:
      machineTooltip(
        ID,
        "The Void Ore Miner will produce tons of ores.",
        "It will consume Pyrotheum and Cryotheum alternating each second.",
        "Temperature will increase if there is only Pyrotheum and will decrease if there is only Cryotheum.",
        "The First step will be to only feed with Pyrotheum and when the temperature is at the perfect spot, slowly add Cryotheum.",
        "If temperature is above the max temperature the Void Ore Miner will stop working and slowly cool down.",
        "Consumes Drilling Mud and outputs Used Drilling Mud every second at the same rate as Pyrotheum."
      )

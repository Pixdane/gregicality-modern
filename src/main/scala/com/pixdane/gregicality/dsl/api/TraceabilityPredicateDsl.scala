package com.pixdane.gregicality.dsl.api

import com.gregtechceu.gtceu.api.block.IMachineBlock
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.{
  MultiblockState,
  Predicates as GtPredicates,
  TraceabilityPredicate
}
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.common.block.LampBlock
import com.lowdragmc.lowdraglib.utils.BlockInfo
import net.minecraft.tags.TagKey
import net.minecraft.world.item.DyeColor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import com.tterrag.registrate.util.entry.BlockEntry

import scala.annotation.targetName
import scala.jdk.FunctionConverters.*

object TraceabilityPredicateDsl:

  /** Public predicate constructors. Each factory mirrors a static method on
    * GTCEu's [[com.gregtechceu.gtceu.api.pattern.Predicates]] and is offered in
    * two forms: a plain form returning the predicate, and a context-function
    * form that gives callers a [[TraceabilityPredicate]] in scope so modifiers
    * like [[setMinGlobalLimited]] can be chained inline.
    */
  object Predicates:

    @targetName("blocksFromBlocks")
    def blocks(blocks: Block*): TraceabilityPredicate =
      GtPredicates.blocks(blocks*)

    @targetName("blocksFromBlocks")
    def blocks(blocks: Block*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.blocks(blocks*)
      proc
      predicate

    @targetName("blocksFromMachineBlocks")
    def blocks(blocks: IMachineBlock*): TraceabilityPredicate =
      GtPredicates.blocks(blocks*)

    @targetName("blocksFromMachineBlocks")
    def blocks(blocks: IMachineBlock*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.blocks(blocks*)
      proc
      predicate

    def states(states: BlockState*): TraceabilityPredicate =
      GtPredicates.states(states*)

    def states(states: BlockState*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.states(states*)
      proc
      predicate

    def machines(definitions: MachineDefinition*): TraceabilityPredicate =
      GtPredicates.machines(definitions*)

    def machines(definitions: MachineDefinition*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.machines(definitions*)
      proc
      predicate

    def blockTag(tag: TagKey[Block]): TraceabilityPredicate =
      GtPredicates.blockTag(tag)

    def blockTag(tag: TagKey[Block])(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.blockTag(tag)
      proc
      predicate

    def fluids(fluids: Fluid*): TraceabilityPredicate =
      GtPredicates.fluids(fluids*)

    def fluids(fluids: Fluid*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.fluids(fluids*)
      proc
      predicate

    def fluidTag(tag: TagKey[Fluid]): TraceabilityPredicate =
      GtPredicates.fluidTag(tag)

    def fluidTag(tag: TagKey[Fluid])(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.fluidTag(tag)
      proc
      predicate

    def controller(block: IMachineBlock): TraceabilityPredicate =
      GtPredicates.controller(GtPredicates.blocks(block))

    def controller(block: IMachineBlock)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.controller(GtPredicates.blocks(block))
      proc
      predicate

    def air: TraceabilityPredicate = GtPredicates.air()

    def any: TraceabilityPredicate = GtPredicates.any()

    def lamps(lamps: BlockEntry[LampBlock]*): TraceabilityPredicate =
      GtPredicates.lamps(lamps*)

    def lamps(lamps: BlockEntry[LampBlock]*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.lamps(lamps*)
      proc
      predicate

    def anyLamp: TraceabilityPredicate = GtPredicates.anyLamp()

    def anyLamp(proc: TraceabilityPredicate ?=> Unit): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.anyLamp()
      proc
      predicate

    def lampsByColor(color: DyeColor): TraceabilityPredicate =
      GtPredicates.lampsByColor(color)

    def heatingCoils: TraceabilityPredicate = GtPredicates.heatingCoils()

    def heatingCoils(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.heatingCoils()
      proc
      predicate

    def cleanroomFilters: TraceabilityPredicate =
      GtPredicates.cleanroomFilters()

    def cleanroomFilters(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate = GtPredicates.cleanroomFilters()
      proc
      predicate

    def powerSubstationBatteries: TraceabilityPredicate =
      GtPredicates.powerSubstationBatteries()

    def powerSubstationBatteries(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.powerSubstationBatteries()
      proc
      predicate

    def ability(ability: PartAbility, tiers: Int*): TraceabilityPredicate =
      GtPredicates.ability(ability, tiers*)

    def ability(ability: PartAbility, tiers: Int*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.ability(ability, tiers*)
      proc
      predicate

    def abilities(abilities: PartAbility*): TraceabilityPredicate =
      GtPredicates.abilities(abilities*)

    def abilities(abilities: PartAbility*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.abilities(abilities*)
      proc
      predicate

    def autoAbilities(recipeType: GTRecipeType*): TraceabilityPredicate =
      GtPredicates.autoAbilities(recipeType*)

    def autoAbilities(recipeType: GTRecipeType*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.autoAbilities(recipeType*)
      proc
      predicate

    def autoAbilities(
        recipeType: Array[GTRecipeType],
        checkEnergyIn: Boolean,
        checkEnergyOut: Boolean,
        checkItemIn: Boolean,
        checkItemOut: Boolean,
        checkFluidIn: Boolean,
        checkFluidOut: Boolean
    ): TraceabilityPredicate =
      GtPredicates.autoAbilities(
        recipeType,
        checkEnergyIn,
        checkEnergyOut,
        checkItemIn,
        checkItemOut,
        checkFluidIn,
        checkFluidOut
      )

    def autoAbilities(
        recipeType: Array[GTRecipeType],
        checkEnergyIn: Boolean,
        checkEnergyOut: Boolean,
        checkItemIn: Boolean,
        checkItemOut: Boolean,
        checkFluidIn: Boolean,
        checkFluidOut: Boolean
    )(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.autoAbilities(
          recipeType,
          checkEnergyIn,
          checkEnergyOut,
          checkItemIn,
          checkItemOut,
          checkFluidIn,
          checkFluidOut
        )
      proc
      predicate

    def autoAbilities(
        checkMaintenance: Boolean,
        checkMuffler: Boolean,
        checkParallel: Boolean
    ): TraceabilityPredicate =
      GtPredicates.autoAbilities(checkMaintenance, checkMuffler, checkParallel)

    def autoAbilities(
        checkMaintenance: Boolean,
        checkMuffler: Boolean,
        checkParallel: Boolean
    )(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.autoAbilities(
          checkMaintenance,
          checkMuffler,
          checkParallel
        )
      proc
      predicate

    def dataHatchPredicate(
        defaultPredicate: TraceabilityPredicate
    ): TraceabilityPredicate =
      GtPredicates.dataHatchPredicate(defaultPredicate)

    def frames(frameMaterials: Material*): TraceabilityPredicate =
      GtPredicates.frames(frameMaterials*)

    def frames(frameMaterials: Material*)(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.frames(frameMaterials*)
      proc
      predicate

    def custom(
        predicate: MultiblockState => Boolean,
        candidates: () => Array[BlockInfo]
    ): TraceabilityPredicate =
      GtPredicates.custom(predicate.asJava, candidates.asJava)

    def custom(
        _predicate: MultiblockState => Boolean,
        candidates: () => Array[BlockInfo]
    )(
        proc: TraceabilityPredicate ?=> Unit
    ): TraceabilityPredicate =
      given predicate: TraceabilityPredicate =
        GtPredicates.custom(_predicate.asJava, candidates.asJava)
      proc
      predicate
  end Predicates

  extension (self: TraceabilityPredicate)
    def |(other: TraceabilityPredicate): TraceabilityPredicate =
      self.or(other)

  def setMinGlobalLimited(min: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinGlobalLimited(min)

  def setMinGlobalLimited(min: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinGlobalLimited(min, previewCount)

  def setMaxGlobalLimited(max: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxGlobalLimited(max)

  def setMaxGlobalLimited(max: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxGlobalLimited(max, previewCount)

  def setMinLayerLimited(min: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinLayerLimited(min)

  def setMinLayerLimited(min: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinLayerLimited(min, previewCount)

  def setMaxLayerLimited(max: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxLayerLimited(max)

  def setMaxLayerLimited(max: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxLayerLimited(max, previewCount)

  def setExactLimit(limit: Int)(using predicate: TraceabilityPredicate): Unit =
    predicate.setExactLimit(limit)

  def setPreviewCount(count: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setPreviewCount(count)

  def disableRenderFormed()(using predicate: TraceabilityPredicate): Unit =
    predicate.disableRenderFormed()

  def setIO(io: IO)(using predicate: TraceabilityPredicate): Unit =
    predicate.setIO(io)

  def setNBTParser(nbtParser: String)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setNBTParser(nbtParser)

  def setSlotName(slotName: String)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setSlotName(slotName)

  /** Attribute `:=` value syntax for the `set*` modifiers above. Each attribute
    * is a singleton object whose `:=` method delegates to the corresponding
    * setter, resolving the target [[TraceabilityPredicate]] from context
    * (supplied by a predicate's `proc` block). The two-Int attributes accept
    * either a single limit or a `(limit, previewCount)` tuple.
    */
  object minGlobalLimited:
    def :=(value: Int)(using predicate: TraceabilityPredicate): Unit =
      predicate.setMinGlobalLimited(value)
    def :=(value: (Int, Int))(using predicate: TraceabilityPredicate): Unit =
      predicate.setMinGlobalLimited(value._1, value._2)

  object maxGlobalLimited:
    def :=(value: Int)(using predicate: TraceabilityPredicate): Unit =
      predicate.setMaxGlobalLimited(value)
    def :=(value: (Int, Int))(using predicate: TraceabilityPredicate): Unit =
      predicate.setMaxGlobalLimited(value._1, value._2)

  object minLayerLimited:
    def :=(value: Int)(using predicate: TraceabilityPredicate): Unit =
      predicate.setMinLayerLimited(value)
    def :=(value: (Int, Int))(using predicate: TraceabilityPredicate): Unit =
      predicate.setMinLayerLimited(value._1, value._2)

  object maxLayerLimited:
    def :=(value: Int)(using predicate: TraceabilityPredicate): Unit =
      predicate.setMaxLayerLimited(value)
    def :=(value: (Int, Int))(using predicate: TraceabilityPredicate): Unit =
      predicate.setMaxLayerLimited(value._1, value._2)

  object exactLimit:
    def :=(value: Int)(using predicate: TraceabilityPredicate): Unit =
      predicate.setExactLimit(value)

  object previewCount:
    def :=(value: Int)(using predicate: TraceabilityPredicate): Unit =
      predicate.setPreviewCount(value)

  object io:
    def :=(value: IO)(using predicate: TraceabilityPredicate): Unit =
      predicate.setIO(value)

  object nbtParser:
    def :=(value: String)(using predicate: TraceabilityPredicate): Unit =
      predicate.setNBTParser(value)

  object slotName:
    def :=(value: String)(using predicate: TraceabilityPredicate): Unit =
      predicate.setSlotName(value)

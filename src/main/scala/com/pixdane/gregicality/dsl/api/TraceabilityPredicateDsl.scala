package com.pixdane.gregicality.dsl.api

import com.gregtechceu.gtceu.api.block.IMachineBlock
import com.gregtechceu.gtceu.api.capability.recipe.IO
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.pattern.{
  MultiblockState,
  Predicates,
  TraceabilityPredicate
}
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.lowdragmc.lowdraglib.utils.BlockInfo
import net.minecraft.world.level.block.Block

import scala.annotation.targetName
import scala.jdk.FunctionConverters.*

object TraceabilityPredicateDsl:

  @targetName("blocksFromBlocks")
  def blocks(blocks: Block*): TraceabilityPredicate =
    Predicates.blocks(blocks*)

  @targetName("blocksFromBlocks")
  def blocks(blocks: Block*)(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.blocks(blocks*)
    proc
    predicate

  @targetName("blocksFromMachineBlocks")
  def blocks(blocks: IMachineBlock*): TraceabilityPredicate =
    Predicates.blocks(blocks*)

  @targetName("blocksFromMachineBlocks")
  def blocks(blocks: IMachineBlock*)(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.blocks(blocks*)
    proc
    predicate

  def controller(block: IMachineBlock): TraceabilityPredicate =
    Predicates.controller(Predicates.blocks(block))

  def controller(block: IMachineBlock)(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate =
      Predicates.controller(Predicates.blocks(block))
    proc
    predicate

  def air: TraceabilityPredicate = Predicates.air()

  def air(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.air()
    proc
    predicate

  def any: TraceabilityPredicate = Predicates.any()

  def any(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.any()
    proc
    predicate

  def heatingCoils: TraceabilityPredicate = Predicates.heatingCoils()

  def heatingCoils(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.heatingCoils()
    proc
    predicate

  def ability(ability: PartAbility, tiers: Int*): TraceabilityPredicate =
    Predicates.ability(ability, tiers*)

  def ability(ability: PartAbility, tiers: Int*)(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.ability(ability, tiers*)
    proc
    predicate

  def abilities(abilities: PartAbility*): TraceabilityPredicate =
    Predicates.abilities(abilities*)

  def abilities(abilities: PartAbility*)(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate = Predicates.abilities(abilities*)
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
    Predicates.autoAbilities(
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
      Predicates.autoAbilities(
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

  def custom(
      predicate: MultiblockState => Boolean,
      candidates: () => Array[BlockInfo]
  ): TraceabilityPredicate =
    Predicates.custom(predicate.asJava, candidates.asJava)

  def custom(
      _predicate: MultiblockState => Boolean,
      candidates: () => Array[BlockInfo]
  )(
      proc: TraceabilityPredicate ?=> Unit
  ): TraceabilityPredicate =
    given predicate: TraceabilityPredicate =
      Predicates.custom(_predicate.asJava, candidates.asJava)
    proc
    predicate

  // ---- or 链运算符 ----

  /** 追加一个分支到 or 链。返回新实例，不修改原 predicate。 */
  extension (self: TraceabilityPredicate)
    def |(other: TraceabilityPredicate): TraceabilityPredicate =
      self.or(other)

  // ---- 限量与配置方法 (given TraceabilityPredicate) ----

  /** 全局最少数量 */
  def setMinGlobalLimited(min: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinGlobalLimited(min)

  /** 全局最少数量 + JEI 预览数 */
  def setMinGlobalLimited(min: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinGlobalLimited(min, previewCount)

  /** 全局最多数量 */
  def setMaxGlobalLimited(max: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxGlobalLimited(max)

  /** 全局最多数量 + JEI 预览数 */
  def setMaxGlobalLimited(max: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxGlobalLimited(max, previewCount)

  /** 每层最少数量 */
  def setMinLayerLimited(min: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinLayerLimited(min)

  /** 每层最少数量 + JEI 预览数 */
  def setMinLayerLimited(min: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMinLayerLimited(min, previewCount)

  /** 每层最多数量 */
  def setMaxLayerLimited(max: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxLayerLimited(max)

  /** 每层最多数量 + JEI 预览数 */
  def setMaxLayerLimited(max: Int, previewCount: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setMaxLayerLimited(max, previewCount)

  /** 最少与最多都等于 limit */
  def setExactLimit(limit: Int)(using predicate: TraceabilityPredicate): Unit =
    predicate.setExactLimit(limit)

  /** JEI 预览中出现的数量 */
  def setPreviewCount(count: Int)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setPreviewCount(count)

  /** 禁止渲染成型结构 */
  def disableRenderFormed()(using predicate: TraceabilityPredicate): Unit =
    predicate.disableRenderFormed()

  /** 设置 IO 方向 */
  def setIO(io: IO)(using predicate: TraceabilityPredicate): Unit =
    predicate.setIO(io)

  /** 设置 NBT 解析器 */
  def setNBTParser(nbtParser: String)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setNBTParser(nbtParser)

  /** 设置槽位名 */
  def setSlotName(slotName: String)(using
      predicate: TraceabilityPredicate
  ): Unit =
    predicate.setSlotName(slotName)

package com.pixdane.gregicality.dsl.machine

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI
import com.gregtechceu.gtceu.api.item.MetaMachineItem
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity
import com.gregtechceu.gtceu.api.machine.MetaMachine
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility
import com.gregtechceu.gtceu.api.recipe.GTRecipe
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder
import com.gregtechceu.gtceu.api.registry.registrate.provider.GTBlockstateProvider
import it.unimi.dsi.fastutil.objects.Reference2IntMap
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.shapes.VoxelShape
import com.tterrag.registrate.builders.BlockBuilder
import com.tterrag.registrate.builders.ItemBuilder
import com.tterrag.registrate.providers.DataGenContext

import java.util.List as JList
import scala.collection.mutable.ArrayBuffer
import scala.jdk.FunctionConverters.*
import scala.jdk.CollectionConverters.*

object MachineBuilderDsl:

  def machine(machine: IMachineBlockEntity => MetaMachine)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.machine(machine.asJava)

  def blockModel(
      blockModel: (
          DataGenContext[Block, ? <: Block],
          GTBlockstateProvider
      ) => Unit
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.blockModel(
      (ctx: DataGenContext[Block, ? <: Block], prov: GTBlockstateProvider) =>
        blockModel(ctx, prov)
    )

  def shape(shape: VoxelShape)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.shape(shape)

  def rotationState(rotationState: RotationState)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.rotationState(rotationState)

  def allowExtendedFacing(allowExtendedFacing: Boolean)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.allowExtendedFacing(allowExtendedFacing)

  def hasBER(hasBER: Boolean)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.hasBER(hasBER)

  def renderMultiblockWorldPreview(
      renderMultiblockWorldPreview: Boolean
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.renderMultiblockWorldPreview(renderMultiblockWorldPreview)

  def renderMultiblockXEIPreview(
      renderMultiblockXEIPreview: Boolean
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.renderMultiblockXEIPreview(renderMultiblockXEIPreview)

  def blockProp(
      blockProp: BlockBehaviour.Properties => BlockBehaviour.Properties
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.blockProp((t: BlockBehaviour.Properties) => blockProp(t))

  def itemProp(itemProp: Item.Properties => Item.Properties)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.itemProp((t: Item.Properties) => itemProp(t))

  def blockBuilder(
      blockBuilder: BlockBuilder[? <: Block, ?] => Unit
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.blockBuilder(blockBuilder.asJava)

  def itemBuilder(
      itemBuilder: ItemBuilder[? <: MetaMachineItem, ?] => Unit
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.itemBuilder(itemBuilder.asJava)

  def onBlockEntityRegister(
      onBlockEntityRegister: BlockEntityType[BlockEntity] => Unit
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.onBlockEntityRegister((t: BlockEntityType[BlockEntity]) =>
      onBlockEntityRegister(t)
    )

  def tier(tier: Int)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.tier(tier)

  def recipeOutputLimits(
      recipeOutputLimits: Reference2IntMap[RecipeCapability[?]]
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeOutputLimits(recipeOutputLimits)

  def paintingColor(paintingColor: Int)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.paintingColor(paintingColor)

  def itemColor(itemColor: (ItemStack, Int) => Int)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.itemColor((stack, tint) => itemColor(stack, tint))

  def tooltipBuilder(
      tooltipBuilder: (ItemStack, JList[Component]) => Unit
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.tooltipBuilder(tooltipBuilder.asJava)

  def alwaysTryModifyRecipe(alwaysTryModifyRecipe: Boolean)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.alwaysTryModifyRecipe(alwaysTryModifyRecipe)

  def beforeWorking(beforeWorking: (IRecipeLogicMachine, GTRecipe) => Boolean)(
      using builder: MachineBuilder[_, _]
  ): Unit =
    builder.beforeWorking(beforeWorking.asJava)

  def onWorking(onWorking: IRecipeLogicMachine => Boolean)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.onWorking(onWorking.asJava)

  def onWaiting(onWaiting: IRecipeLogicMachine => Unit)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.onWaiting(onWaiting.asJava)

  def afterWorking(afterWorking: IRecipeLogicMachine => Unit)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.afterWorking(afterWorking.asJava)

  def regressWhenWaiting(regressWhenWaiting: Boolean)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.regressWhenWaiting(regressWhenWaiting)

  def allowCoverOnFront(allowCoverOnFront: Boolean)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.allowCoverOnFront(allowCoverOnFront)

  def appearance(appearance: () => BlockState)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.appearance(appearance.asJava)

  def editableUI(editableUI: EditableMachineUI)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.editableUI(editableUI)

  def langValue(langValue: String)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.langValue(langValue)

  def recipeType(recipeType: GTRecipeType)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeType(recipeType)

  def recipeTypes(recipeTypes: GTRecipeType*)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeTypes(recipeTypes*)

  def model(model: MachineBuilder.ModelInitializer)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.model(model)

  def simpleModel(modelName: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.simpleModel(modelName)

  def defaultModel()(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.defaultModel()
  def tieredHullModel(model: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.tieredHullModel(model)

  def overlayTieredHullModel(name: String)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.overlayTieredHullModel(name)

  def overlayTieredHullModel(overlayModel: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.overlayTieredHullModel(overlayModel)

  def colorOverlayTieredHullModel(overlay: String)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlayTieredHullModel(overlay)

  def colorOverlayTieredHullModel(
      overlay: String,
      pipeOverlay: String,
      emissiveOverlay: String
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay)

  def colorOverlayTieredHullModel(overlay: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlayTieredHullModel(overlay)

  def colorOverlayTieredHullModel(
      overlay: ResourceLocation,
      pipeOverlay: ResourceLocation,
      emissiveOverlay: ResourceLocation
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlayTieredHullModel(overlay, pipeOverlay, emissiveOverlay)

  def overlaySteamHullModel(name: String)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.overlaySteamHullModel(name)

  def overlaySteamHullModel(overlayModel: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.overlaySteamHullModel(overlayModel)

  def colorOverlaySteamHullModel(overlay: String)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlaySteamHullModel(overlay)

  def colorOverlaySteamHullModel(
      overlay: String,
      pipeOverlay: String,
      emissiveOverlay: String
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay)

  def colorOverlaySteamHullModel(
      overlay: String,
      pipeOverlay: ResourceLocation,
      emissiveOverlay: String
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay)

  def colorOverlaySteamHullModel(overlay: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlaySteamHullModel(overlay)

  def colorOverlaySteamHullModel(
      overlay: ResourceLocation,
      pipeOverlay: ResourceLocation,
      emissiveOverlay: ResourceLocation
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.colorOverlaySteamHullModel(overlay, pipeOverlay, emissiveOverlay)

  def workableTieredHullModel(workableModel: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.workableTieredHullModel(workableModel)

  def simpleGeneratorModel(workableModel: ResourceLocation)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.simpleGeneratorModel(workableModel)

  def workableSteamHullModel(
      isHighPressure: Boolean,
      workableModel: ResourceLocation
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.workableSteamHullModel(isHighPressure, workableModel)

  def workableCasingModel(
      baseCasing: ResourceLocation,
      workableModel: ResourceLocation
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.workableCasingModel(baseCasing, workableModel)

  def sidedOverlayCasingModel(
      baseCasing: ResourceLocation,
      workableModel: ResourceLocation
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.sidedOverlayCasingModel(baseCasing, workableModel)

  def sidedWorkableCasingModel(
      baseCasing: ResourceLocation,
      workableModel: ResourceLocation
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.sidedWorkableCasingModel(baseCasing, workableModel)

  def appearanceBlock(block: () => ? <: Block)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.appearanceBlock(block.asJava)

  def tooltips(child: ArrayBuffer[Component] ?=> Unit)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    given components: ArrayBuffer[Component] = ArrayBuffer()
    child
    builder.tooltips(components.asJava)

  def conditionalTooltip(
      component: Component,
      condition: () => Boolean
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.conditionalTooltip(component, condition.asJava)

  def conditionalTooltip(
      component: Component,
      condition: Boolean
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.conditionalTooltip(component, condition)

  def abilities(abilities: PartAbility*)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.abilities(abilities*)

  def modelProperty(property: Property[?])(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.modelProperty(property)

  def modelProperty[T <: Comparable[T]](
      property: Property[T],
      defaultValue: T
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.modelProperty(property, defaultValue)

  def modelProperties(properties: Property[?]*)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.modelProperties(properties*)

  def modelProperties(properties: Iterable[Property[?]])(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.modelProperties(properties.asJavaCollection)

  def modelProperties(
      properties: Map[Property[?], ? <: Comparable[?]]
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.modelProperties(properties.asJava)

  def removeModelProperty(property: Property[?])(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.removeModelProperty(property)

  def clearModelProperties()(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.clearModelProperties()
  def recipeModifier(recipeModifier: RecipeModifier)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeModifier(recipeModifier)

  def recipeModifier(
      recipeModifier: RecipeModifier,
      alwaysTryModifyRecipe: Boolean
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeModifier(recipeModifier, alwaysTryModifyRecipe)

  def recipeModifiers(recipeModifiers: RecipeModifier*)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeModifiers(recipeModifiers*)

  def recipeModifiers(
      alwaysTryModifyRecipe: Boolean,
      recipeModifiers: RecipeModifier*
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.recipeModifiers(alwaysTryModifyRecipe, recipeModifiers*)

  def noRecipeModifier()(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.noRecipeModifier()
  def addOutputLimit(capability: RecipeCapability[?], limit: Int)(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.addOutputLimit(capability, limit)

  def multiblockPreviewRenderer(
      multiBlockWorldPreview: Boolean,
      multiBlockXEIPreview: Boolean
  )(using
      builder: MachineBuilder[_, _]
  ): Unit =
    builder.multiblockPreviewRenderer(
      multiBlockWorldPreview,
      multiBlockXEIPreview
    )

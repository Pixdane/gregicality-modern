package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.event.{
  MaterialEvent,
  MaterialRegistryEvent,
  PostMaterialEvent
}
import com.pixdane.gregicality.Gregicality
import com.pixdane.gregicality.common.data.materials.GCYMaterialsGeneratedIndex
import net.minecraftforge.eventbus.api.{IEventBus, SubscribeEvent}
import org.apache.logging.log4j.Logger

private class GCYMaterials(private val logger: Logger):
  @SubscribeEvent
  def addMaterialRegistries(event: MaterialRegistryEvent): Unit =
    GTCEuAPI.materialManager.createRegistry(Gregicality.MOD_ID)

  @SubscribeEvent
  def addMaterials(event: MaterialEvent): Unit =
    GCYMaterials.registerMaterials(event)

  @SubscribeEvent
  def modifyMaterials(event: PostMaterialEvent): Unit =
    GCYMaterialsGeneratedIndex.patchAll()

object GCYMaterials:
  def init(logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.register(new GCYMaterials(logger))

  private def registerMaterials(event: MaterialEvent): Unit =
    GCYMaterialsGeneratedIndex.registerAll()

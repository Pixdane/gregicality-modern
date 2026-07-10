package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.event.{MaterialEvent, MaterialRegistryEvent, PostMaterialEvent}
import com.pixdane.gregicality.Gregicality.MOD_ID
import net.minecraftforge.eventbus.api.{IEventBus, SubscribeEvent}
import org.apache.logging.log4j.Logger

private class GCYMaterials(private val logger: Logger):
  @SubscribeEvent
  def addMaterialRegistries(event: MaterialRegistryEvent): Unit =
    logger.info("HELLO from material registry event")
    GTCEuAPI.materialManager.createRegistry(MOD_ID)

  @SubscribeEvent
  def addMaterials(event: MaterialEvent): Unit =
    logger.info("HELLO from material event")

  @SubscribeEvent
  def modifyMaterials(event: PostMaterialEvent): Unit =
    logger.info("HELLO from post material event")

object GCYMaterials:
  def init(logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.register(new GCYMaterials(logger))

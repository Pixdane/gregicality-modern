package com.pixdane.gregicality.client

import net.minecraft.client.Minecraft
import net.minecraftforge.eventbus.api.{IEventBus, SubscribeEvent}
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Logger

class Client(private val logger: Logger):
  @SubscribeEvent
  def onClientSetup(event: FMLClientSetupEvent): Unit =
    logger.info("HELLO FROM CLIENT SETUP")
    logger.info("MINECRAFT NAME >> {}", Minecraft.getInstance.getUser.getName)

object Client:
  def init(logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.register(new Client(logger))

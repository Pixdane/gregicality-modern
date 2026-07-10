package com.pixdane.gregicality.client

import net.minecraftforge.eventbus.api.{IEventBus, SubscribeEvent}
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Logger

private class GregicalityClient(private val logger: Logger):
  @SubscribeEvent
  def onClientSetup(event: FMLClientSetupEvent): Unit =
    logger.info("HELLO from client setup")

object GregicalityClient:
  def init(logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.register(new GregicalityClient(logger))

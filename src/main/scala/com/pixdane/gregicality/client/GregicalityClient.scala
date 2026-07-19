package com.pixdane.gregicality.client

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.apache.logging.log4j.Logger

object GregicalityClient:
  def init()(using logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.addListener(onClientSetup)

  private def onClientSetup(event: FMLClientSetupEvent)(using
      logger: Logger
  ): Unit =
    logger.info("HELLO from client setup")

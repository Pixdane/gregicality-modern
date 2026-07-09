package com.pixdane.gregicality

import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.LogManager

@Mod(Gregicality.MOD_ID)
object Gregicality:
  final val MOD_ID = "gregicality"

  private val LOGGER = LogManager.getLogger

  private def commonSetup(event: FMLCommonSetupEvent): Unit =
    LOGGER.info("HELLO from common setup")

  {
    val modEventBus = FMLJavaModLoadingContext.get().getModEventBus
    modEventBus.addListener(this.commonSetup)
  }

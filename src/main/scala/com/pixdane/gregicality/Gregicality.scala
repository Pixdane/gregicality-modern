package com.pixdane.gregicality

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.GTCEuAPI.RegisterEvent
import com.gregtechceu.gtceu.api.machine.MachineDefinition
import com.gregtechceu.gtceu.api.recipe.GTRecipeType
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate
import com.gregtechceu.gtceu.api.sound.SoundEntry
import com.pixdane.gregicality.client.GregicalityClient
import com.pixdane.gregicality.common.data.GCYMaterials
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.apache.logging.log4j.{LogManager, Logger}

@Mod(Gregicality.MOD_ID)
object Gregicality:
  inline val MOD_ID = "gregicality"

  val REGISTRATE: GTRegistrate = GTRegistrate.create(MOD_ID)

  init()

  private def init(): Unit =

    given Logger = LogManager.getLogger(MOD_ID)
    given modEventBus: IEventBus = FMLJavaModLoadingContext.get().getModEventBus

    modEventBus.addListener(onCommonSetup)

    GCYMaterials.init()

    modEventBus.addGenericListener(classOf[GTRecipeType], registerRecipeTypes)
    modEventBus.addGenericListener(classOf[MachineDefinition], registerMachines)
    modEventBus.addGenericListener(classOf[SoundEntry], registerSounds)

    REGISTRATE.registerRegistrate()

    DistExecutor.unsafeRunWhenOn(
      Dist.CLIENT,
      () => () => GregicalityClient.init()
    )

  def id(path: String) = ResourceLocation(MOD_ID, path)

  private def onCommonSetup(
      event: FMLCommonSetupEvent
  )(using logger: Logger): Unit = {
    event.enqueueWork[Unit](() => logger.info("HELLO from common setup"))
  }

  private def registerRecipeTypes(
      event: RegisterEvent[ResourceLocation, GTRecipeType]
  ): Unit = ()

  private def registerMachines(
      event: RegisterEvent[ResourceLocation, MachineDefinition]
  ): Unit = ()

  private def registerSounds(
      event: RegisterEvent[ResourceLocation, SoundEntry]
  ): Unit = ()

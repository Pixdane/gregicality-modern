package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.event.{
  MaterialEvent,
  MaterialRegistryEvent,
  PostMaterialEvent
}
import com.pixdane.gregicality.Gregicality
import net.minecraftforge.eventbus.api.IEventBus
import org.apache.logging.log4j.Logger

object GCYMaterials:
  def init()(using logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.addListener(addMaterialRegistries)
    modEventBus.addListener(addMaterials)
    modEventBus.addListener(modifyMaterials)
    modEventBus.addListener(registerMaterials)

  private def addMaterialRegistries(event: MaterialRegistryEvent): Unit =
    GTCEuAPI.materialManager.createRegistry(Gregicality.MOD_ID)

  private def addMaterials(event: MaterialEvent): Unit =
    registerMaterials(event)

  private def modifyMaterials(event: PostMaterialEvent): Unit =
    ()

  private def registerMaterials(event: MaterialEvent): Unit =
    ()

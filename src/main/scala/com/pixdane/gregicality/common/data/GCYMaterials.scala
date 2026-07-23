package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.GTCEuAPI
import com.gregtechceu.gtceu.api.data.chemical.material.event.{
  MaterialEvent,
  MaterialRegistryEvent,
  PostMaterialEvent
}
import com.gregtechceu.gtceu.common.data.GTMaterials
import com.pixdane.gregicality.Gregicality
import com.pixdane.gregicality.materials.dsl.{
  ModificationRegistryContext,
  RegistryContext
}
import net.minecraftforge.eventbus.api.IEventBus
import org.apache.logging.log4j.Logger

/** Forge event bridge for the Gregicality material registry and definitions. */
object GCYMaterials:

  /** Installs the material registry, registration, and post-registration event
    * listeners on the mod event bus.
    */
  def init()(using logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.addListener(addMaterialRegistries)
    modEventBus.addListener(addMaterials)
    modEventBus.addListener(modifyMaterials)

  private def addMaterialRegistries(event: MaterialRegistryEvent): Unit =
    GTCEuAPI.materialManager.createRegistry(Gregicality.MOD_ID)

  private def addMaterials(event: MaterialEvent): Unit =
    registerMaterials()

  private def modifyMaterials(event: PostMaterialEvent): Unit =
    given ModificationRegistryContext = ModificationRegistryContext.real
    MaterialModification.modifyAll(
      MaterialModificationInputs(
        xenon = GTMaterials.Xenon,
        neon = GTMaterials.Neon,
        krypton = GTMaterials.Krypton
      )
    )

  private def registerMaterials(): Unit =
    given RegistryContext = RegistryContext(Gregicality.MOD_ID)
    MaterialRegistration.registerAll(
      MaterialRegistrationInputs(
        carbon = GTMaterials.Carbon,
        hydrogen = GTMaterials.Hydrogen,
        nitrogen = GTMaterials.Nitrogen,
        oxygen = GTMaterials.Oxygen,
        sulfuricAcid = GTMaterials.SulfuricAcid
      )
    )

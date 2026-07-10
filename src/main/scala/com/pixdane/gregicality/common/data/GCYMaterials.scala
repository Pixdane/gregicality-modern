package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.{GTCEuAPI, GTValues}
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.event.{MaterialEvent, MaterialRegistryEvent, PostMaterialEvent}
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet.*
import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier
import com.gregtechceu.gtceu.common.data.GTMaterials.*
import com.pixdane.gregicality.Gregicality
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
    ()

object GCYMaterials:
  var TestMaterial: Material = _

  def init(logger: Logger, modEventBus: IEventBus): Unit =
    modEventBus.register(new GCYMaterials(logger))

  private def registerMaterials(event: MaterialEvent): Unit =
    TestMaterial = new Material.Builder(Gregicality.id("test_material"))
      .ingot(4)
      .fluid()
      .langValue("Test Material")
      .color(0x6f6b55)
      .iconSet(METALLIC)
      .appendFlags(STD_METAL, GENERATE_PLATE, GENERATE_ROD)
      .components(Tantalum, 1, Hafnium, 1, Carbon, 1)
      .blast(b => b
        .temp(3900, GasTier.HIGH)
        .blastStats(GTValues.VA(GTValues.EV), 1000)
      )
      .buildAndRegister()

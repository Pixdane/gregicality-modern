package com.pixdane.gregicality

import com.gregtechceu.gtceu.api.addon.{GTAddon, IGTAddon}
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate

@GTAddon
final class GregicalityAddon extends IGTAddon:
  override def getRegistrate: GTRegistrate =
    Gregicality.REGISTRATE

  override def initializeAddon(): Unit =
    ()

  override def addonModId(): String =
    Gregicality.MOD_ID

package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.pixdane.gregicality.common.machine.multiblock.VoidMiner

object GregicalityMultiMachines:
  var VOID_MINER: Array[MultiblockMachineDefinition] = _

  def init(): Unit =
    VOID_MINER = VoidMiner.register()

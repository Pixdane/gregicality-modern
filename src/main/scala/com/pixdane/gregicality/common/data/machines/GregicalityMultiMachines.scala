package com.pixdane.gregicality.common.data.machines

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.pixdane.gregicality.common.machine.multiblock.VoidMinerMachine

object GregicalityMultiMachines:
  var VOID_MINER: Array[MultiblockMachineDefinition] = _

  def init(): Unit =
    VOID_MINER = VoidMinerMachine.register()

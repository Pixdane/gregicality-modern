package com.pixdane.gregicality.common.machine.multiblock

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.{
  IMachineBlockEntity,
  MultiblockMachineDefinition
}
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.registerTieredMultis
import com.pixdane.gregicality.dsl.machine.multiblock.MachineBuilderDsl.*
import com.pixdane.gregicality.Gregicality.REGISTRATE

class VoidMinerMachine(holder: IMachineBlockEntity, tier: Int)
    extends WorkableElectricMultiblockMachine(holder)
    with ITieredMachine

object VoidMinerMachine:
  def register(): Array[MultiblockMachineDefinition] =
    registerTieredMultis(
      REGISTRATE,
      "void_miner",
      VoidMinerMachine(_, _),
      build(_)(using _),
      GTValues.UV,
      GTValues.UHV,
      GTValues.UEV
    )

  private def build(tier: Integer)(using
      builder: MultiblockMachineBuilder[_, _]
  ): MultiblockMachineDefinition =
    multiblock:
      rotationState(RotationState.NON_Y_AXIS)

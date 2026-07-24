package com.pixdane.gregicality.dsl.machine.multiblock

import com.gregtechceu.gtceu.api.data.RotationState
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder

object MachineBuilderDsl:
  def multiblock(
      proc: MultiblockMachineBuilder[_, _] ?=> Unit
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): MultiblockMachineDefinition =
    proc
    builder.register()

  def rotationState(rotationState: RotationState)(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.rotationState(rotationState)
    ()

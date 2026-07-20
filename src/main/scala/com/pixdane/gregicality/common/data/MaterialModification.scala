package com.pixdane.gregicality.common.data

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.pixdane.gregicality.materials.dsl.*

/** Existing GTCEu materials patched by the Gregicality post-material phase. */
final case class MaterialModificationInputs(
    xenon: Material,
    neon: Material,
    krypton: Material
)

/** Post-registration material changes owned by Gregicality. */
object MaterialModification:

  /** Applies addon-owned modifications after all material builders have run.
    *
    * Noble-gas storage additions follow the migration decision that keeps the
    * GTCEu gas storage as the primary form while adding liquid and plasma
    * entries for recipe migration. No material is registered from this phase.
    */
  def modifyAll(inputs: MaterialModificationInputs)(using
      ModificationRegistryContext
  ): Unit =
    addNobleGasForms(inputs.xenon)
    addNobleGasForms(inputs.neon)
    addNobleGasForms(inputs.krypton)

  private def addNobleGasForms(material: Material)(using
      ModificationRegistryContext
  ): Unit =
    modify(material):
      addFluid(FluidSpec(FluidKind.Liquid))
      addFluid(FluidSpec(FluidKind.Plasma))

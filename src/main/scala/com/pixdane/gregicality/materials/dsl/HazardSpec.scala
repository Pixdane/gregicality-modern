package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.properties.HazardProperty.HazardTrigger
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition

/** Complete hazard configuration forwarded to GTCEu's hazard property.
  *
  * The DSL deliberately combines GTCEu's four hazard overloads into one
  * immutable value. `progressionMultiplier` and `applyToDerivatives` therefore
  * remain explicit and named at the authoring site while the adapter always
  * selects GTCEu's most general overload.
  *
  * @param trigger
  *   the exposure trigger, such as inhalation, skin contact, or any exposure
  * @param condition
  *   the medical condition applied by GTCEu
  * @param progressionMultiplier
  *   multiplier applied to hazard progression; defaults to GTCEu's `1`
  * @param applyToDerivatives
  *   whether the hazard is inherited by derived materials
  */
final case class HazardSpec(
    trigger: HazardTrigger,
    condition: MedicalCondition,
    progressionMultiplier: Double = 1.0,
    applyToDerivatives: Boolean = false
)

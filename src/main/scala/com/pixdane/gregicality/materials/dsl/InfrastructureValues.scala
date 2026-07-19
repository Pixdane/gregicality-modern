package com.pixdane.gregicality.materials.dsl

/** Rotor statistics forwarded to `Material.Builder.rotorStats`.
  *
  * `damage` is kept as a `Double` in the DSL so decimal literals remain
  * readable; the adapter narrows it to GTCEu's `float` parameter.
  */
final case class RotorSpec(
    power: Int,
    efficiency: Int,
    damage: Double,
    durability: Int
)

/** Cable-property settings forwarded to one GTCEu cable overload.
  *
  * When `criticalTemperature` is absent, the adapter selects the three- or
  * four-argument GTCEu overload based on `superconducting`. When present, it
  * selects the five-argument overload.
  *
  * @param voltage
  *   nominal voltage represented by the existing [[NominalVoltage]] wrapper
  * @param amperage
  *   cable amperage
  * @param loss
  *   EU loss per block
  * @param superconducting
  *   whether the cable is superconducting
  * @param criticalTemperature
  *   optional critical temperature for the five-argument overload
  */
final case class CableSpec(
    voltage: NominalVoltage,
    amperage: Int,
    loss: Int,
    superconducting: Boolean = false,
    criticalTemperature: Option[Kelvin] = None
)

/** Fluid-pipe settings forwarded to a `fluidPipeProperties` overload.
  *
  * The four proof booleans are named to keep the Java API's positional boolean
  * list readable at the call site.
  */
final case class FluidPipeSpec(
    maxTemperature: Kelvin,
    throughput: Int,
    gasProof: Boolean = false,
    acidProof: Boolean = false,
    cryoProof: Boolean = false,
    plasmaProof: Boolean = false
)

/** Item-pipe settings forwarded to `Material.Builder.itemPipeProperties`. */
final case class ItemPipeSpec(
    priority: Int,
    stacksPerSecond: Double
)

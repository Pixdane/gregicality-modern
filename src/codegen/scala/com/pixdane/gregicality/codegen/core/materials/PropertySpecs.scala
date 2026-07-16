package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.{GasTierRef, MaterialRef}

/** Optional authored settings for the dust property slot. */
final case class DustPropertySpec(
    harvestLevel: Option[HarvestLevel] = None,
    burnTime: Option[BurnTimeTicks] = None
)

/** Authored ingot transformation targets. None leaves GTCEu's runtime self or
  * null default untouched.
  */
final case class IngotPropertySpec(
    smeltingInto: Option[MaterialRef] = None,
    arcSmeltingInto: Option[MaterialRef] = None,
    macerateInto: Option[MaterialRef] = None,
    magneticMaterial: Option[MaterialRef] = None
)

/** Presence marker for the gem property slot. */
final case class GemPropertySpec()

/** Presence marker for the wood property slot. */
final case class WoodPropertySpec()

/** Presence marker for the polymer property slot. */
final case class PolymerPropertySpec()

/** Authored ore registration and follow-up calls. Absent multipliers select the
  * builder overload that owns GTCEu's multiplier defaults.
  */
final case class OrePropertySpec(
    multipliers: Option[OreMultipliers] = None,
    emissive: Boolean = false,
    directSmeltResult: Option[MaterialRef] = None,
    washedIn: Option[OreWashSpec] = None,
    separatedInto: Vector[MaterialRef] = Vector.empty,
    byproducts: Vector[MaterialRef] = Vector.empty
)

/** Explicit ore and byproduct multipliers for an ore builder overload. */
final case class OreMultipliers(ore: PositiveInt, byproduct: PositiveInt)

/** Authored wash material. None amount selects GTCEu's one-argument builder
  * overload and keeps its 100 mB default inside GTCEu.
  */
final case class OreWashSpec(
    material: MaterialRef,
    amount: Option[PositiveInt] = None
)

/** Authored blast property and optional recipe-stat overrides. */
final case class BlastPropertySpec(
    temperature: Kelvin,
    gasTier: Option[GasTierRef] = None,
    eutOverride: Option[VoltageExpr] = None,
    durationOverride: Option[DurationTicks] = None,
    vacuumEutOverride: Option[VoltageExpr] = None,
    vacuumDurationOverride: Option[DurationTicks] = None
)

package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.GTValues
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet

/** Temperature in kelvin. Must be strictly positive. */
final case class Kelvin private (value: Int):
  override def toString: String = s"$value K"

object Kelvin:
  /** Constructs a positive absolute temperature.
    *
    * @throws IllegalArgumentException
    *   when `value` is not positive
    */
  def apply(value: Int): Kelvin =
    require(value > 0, s"Kelvin must be > 0, got $value")
    new Kelvin(value)

/** A duration in game ticks. Must be non-negative. */
final case class Ticks private (value: Int):
  override def toString: String = s"$value ticks"

object Ticks:
  /** Constructs a non-negative game duration.
    *
    * @throws IllegalArgumentException
    *   when `value` is negative
    */
  def apply(value: Int): Ticks =
    require(value >= 0, s"Ticks must be >= 0, got $value")
    new Ticks(value)

/** An RGB color packed into a single int, range 0x000000..0xFFFFFF. */
final case class HexColor private (value: Int):
  override def toString: String = f"#$value%06X"

object HexColor:
  /** Constructs a packed 24-bit RGB color.
    *
    * @throws IllegalArgumentException
    *   when `value` is outside `0x000000..0xFFFFFF`
    */
  def apply(value: Int): HexColor =
    require(
      value >= 0x000000 && value <= 0xffffff,
      f"RGB must be in 0x000000..0xFFFFFF, got 0x$value%06X"
    )
    new HexColor(value)

  /** Parse a hex color from a bare hex string such as "6f2200". */
  def fromHex(hex: String): HexColor =
    require(hex != null && hex.nonEmpty, "hex color string must be non-empty")
    val cleaned = hex.trim.toLowerCase.stripPrefix("#").stripPrefix("0x")
    require(
      cleaned.matches("[0-9a-f]{6}"),
      s"hex color must be 6 hex digits, got '$hex'"
    )
    HexColor(Integer.parseInt(cleaned, 16))

/** GTCEu voltage-tier indices in the same order as `GTValues.V` and
  * `GTValues.VA`.
  */
enum VoltageTier:
  case ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV, UEV, UIV, UXV, OpV, MAX

/** Nominal machine/cable voltage for a tier. */
final case class NominalVoltage(value: Long):
  override def toString: String = s"$value V"

/** Recipe-side EU/t for a tier (cable-loss adjusted). */
final case class RecipeEUt(value: Int):
  override def toString: String = s"$value EU/t"

object V:
  /** Returns the nominal machine or cable voltage for `tier`. */
  def apply(tier: VoltageTier): NominalVoltage =
    NominalVoltage(GTValues.V(tier.ordinal))

object VA:
  /** Returns the cable-loss-adjusted recipe EU/t for `tier`. */
  def apply(tier: VoltageTier): RecipeEUt =
    RecipeEUt(GTValues.VA(tier.ordinal))

/** A GTCEu material paired with an integer amount. */
final case class MaterialAmount(material: Material, amount: Int):
  require(amount > 0, s"MaterialAmount must be > 0, got $amount")
  override def toString: String = s"$material x$amount"

/** Recipe energy and duration: a RecipeEUt paired with a Ticks value. */
final case class RecipeStats(eut: RecipeEUt, duration: Ticks):
  override def toString: String = s"$eut for $duration"

/** Compact visual configuration for one material.
  *
  * `hasFluidColor` is optional because the one-argument GTCEu color overload
  * keeps its native default when the author does not mention fluid coloring.
  * Supplying `Some(false)` selects GTCEu's `color(int, false)` overload.
  */
final case class VisualSpec(
    color: HexColor,
    iconSet: MaterialIconSet,
    secondary: Option[HexColor] = None,
    hasFluidColor: Option[Boolean] = None
)

/** A washing step: a washing fluid and an amount. */
final case class WashSpec(fluid: Material, amount: Int):
  require(amount > 0, s"WashSpec amount must be > 0, got $amount")

/** Ore configuration grouping, collected before calling GTCEu ore methods. */
final case class OreSpec(
    multiplier: Int = 1,
    byproductMultiplier: Int = 1,
    emissive: Boolean = false,
    washedIn: Option[WashSpec] = None,
    separatedInto: List[Material] = List.empty,
    byproducts: List[Material] = List.empty
):
  require(multiplier > 0, s"ore multiplier must be > 0, got $multiplier")
  require(
    byproductMultiplier > 0,
    s"ore byproduct multiplier must be > 0, got $byproductMultiplier"
  )

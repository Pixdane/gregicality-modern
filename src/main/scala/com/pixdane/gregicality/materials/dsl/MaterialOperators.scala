package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material

import scala.annotation.targetName

// Operator-style syntax for building DSL domain values.
//
// These extensions construct pure values only; they never touch a GTCEu
// builder. The intent is to keep authoring terse:
//
//   3900.K                  -> Kelvin(3900)
//   2000.ticks              -> Ticks(2000)
//   rgb"6f2200"             -> HexColor.fromHex("6f2200")
//   Netherite * 3           -> MaterialAmount(Netherite, 3)
//   VA(EV) * 2000.ticks     -> RecipeStats(VA(EV), 2000.ticks)
//
// `*` is deliberately overloaded between "material x amount" and
// "EU/t x duration". The receiver types disambiguate at compile time.

extension (sc: StringContext)
  /** Constructs an RGB color from a literal such as `rgb"6f2200"`.
    *
    * Interpolated arguments are deliberately rejected. A quoted macro may
    * replace this runtime validation later if compile-time diagnostics prove
    * useful.
    */
  def rgb(args: Any*): HexColor =
    require(args.isEmpty, "rgb interpolation does not accept arguments")
    HexColor.fromHex(sc.parts.mkString)

extension (value: Int)
  /** Build a [[Kelvin]] temperature: `3900.K`. */
  def K: Kelvin = Kelvin(value)

  /** Build a [[Ticks]] duration: `2000.ticks`. */
  def ticks: Ticks = Ticks(value)

extension (eut: RecipeEUt)
  /** Pair a recipe EU/t with a duration: `VA(EV) * 2000.ticks`. */
  @targetName("recipeEUt_times_ticks")
  infix def *(duration: Ticks): RecipeStats =
    RecipeStats(eut, duration)

extension (material: Material)
  /** Pair a GTCEu material with an amount: `Tungsten * 2`. */
  @targetName("materialTimesAmount")
  infix def *(amount: Int): MaterialAmount =
    MaterialAmount(material, amount)

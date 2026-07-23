package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier

/** A blast or vacuum recipe override expressed as an EU/t and an optional
  * duration.
  *
  * The DSL lets authors write either a full `VA(EV) * 2000.ticks`
  * ([[RecipeStats]], EU/t plus duration) or a bare `VA(HV)` ([[RecipeEUt]],
  * EU/t alone). Both forms reduce to this value so the adapter can call
  * `BlastProperty.Builder.blastStats` / `vacuumStats` with the matching
  * overload:
  *
  *   - `RecipeOverride(eut, Some(duration))` -> `blastStats(eut, duration)`
  *   - `RecipeOverride(eut, None)` -> `blastStats(eut)`
  *
  * `duration` is `Option[Ticks]` rather than a sentinel `int` so the "not
  * specified" state is total and cannot be confused with a real zero-tick
  * duration.
  *
  * This is a pure value: it performs no GTCEu range validation. GTCEu remains
  * authoritative for any recipe-stat constraints at build time.
  *
  * @param eut
  *   the recipe EU/t to override with
  * @param duration
  *   the optional recipe duration; `None` leaves duration to GTCEu's default
  */
final case class RecipeOverride(eut: RecipeEUt, duration: Option[Ticks]):
  override def toString: String = duration match
    case Some(d) => s"$eut for $d"
    case None    => eut.toString

object RecipeOverride:
  /** Builds an override from a fully-specified [[RecipeStats]] pair. */
  def apply(stats: RecipeStats): RecipeOverride =
    RecipeOverride(stats.eut, Some(stats.duration))

  /** Builds an override from an EU/t alone, leaving duration to GTCEu. */
  def apply(eut: RecipeEUt): RecipeOverride =
    RecipeOverride(eut, None)

/** Fully-assembled blast-property configuration.
  *
  * Collected by [[BlastContext]] inside a `blast:` block and committed to the
  * adapter as a single immutable payload when the block returns normally. Each
  * field is optional because every blast setting is independently optional in
  * the DSL; the adapter decides how to translate the combination into
  * `BlastProperty.Builder` calls.
  *
  * Invariant enforced by the adapter (not here): a `gasTier` without a
  * `temperature` is invalid, because GTCEu only exposes gas tier through
  * `BlastProperty.Builder.temp(int, GasTier)`. Keeping that check in the
  * adapter avoids duplicating GTCEu's own validation surface in the value type.
  *
  * @param temperature
  *   the blast temperature, if set
  * @param gasTier
  *   the EBF gas tier, if set; requires `temperature` to be set as well
  * @param blastStats
  *   the EBF recipe override (EU/t and optional duration), if set
  * @param vacuumStats
  *   the vacuum recipe override (EU/t and optional duration), if set
  */
final case class BlastSpec(
    temperature: Option[Kelvin] = None,
    gasTier: Option[GasTier] = None,
    blastStats: Option[RecipeOverride] = None,
    vacuumStats: Option[RecipeOverride] = None
):
  override def toString: String =
    val parts = List(
      temperature.map(t => s"temp=$t"),
      gasTier.map(g => s"gasTier=$g"),
      blastStats.map(s => s"blast=$s"),
      vacuumStats.map(s => s"vacuum=$s")
    ).flatten
    if parts.isEmpty then "BlastSpec(empty)"
    else parts.mkString("BlastSpec(", ", ", ")")

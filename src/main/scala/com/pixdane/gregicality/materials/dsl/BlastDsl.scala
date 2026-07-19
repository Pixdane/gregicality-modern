package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier

/** Accumulator for a `blast:` block.
  *
  * Created package-private by [[MaterialContexts.MaterialContext#blast]] and
  * placed in `given` scope for the block body. The block body never touches a
  * `BlastProperty.Builder` directly; it writes scalar settings onto this
  * accumulator via the `temperature`, `gasTier`, `blastStats`, and
  * `vacuumStats` symbols. When the block returns normally, [[toSpec]] freezes
  * the accumulated state into one immutable [[BlastSpec]] that the adapter maps
  * onto `Material.Builder.blast(UnaryOperator)`.
  *
  * Scalar settings take the last value written, matching [[OreContext]]'s
  * scalar semantics. No GTCEu range or compatibility validation happens here;
  * in particular, the "gasTier requires temperature" rule is enforced by the
  * adapter, where the GTCEu `BlastProperty.Builder.temp(int, GasTier)`
  * constraint actually lives.
  *
  * The fields are mutable `var`s rather than a `ListBuffer` of pending updates
  * because blast settings are inherently scalar and last-wins; a buffer would
  * only be needed to preserve every authored call, which the suite does not
  * require.
  */
private[dsl] final class BlastContext extends TemperatureTarget:

  /** The blast temperature, or `None` if not yet set. Last write wins. */
  private var temperature: Option[Kelvin] = None

  /** The EBF gas tier, or `None` if not yet set. Last write wins. */
  private var gasTier: Option[GasTier] = None

  /** The EBF recipe override, or `None` if not yet set. Last write wins. */
  private var blastStats: Option[RecipeOverride] = None

  /** The vacuum recipe override, or `None` if not yet set. Last write wins. */
  private var vacuumStats: Option[RecipeOverride] = None

  /** Records the blast temperature. Last write wins. */
  def setTemperature(value: Kelvin): Unit =
    temperature = Some(value)

  /** Records the EBF gas tier. Last write wins. */
  def setGasTier(value: GasTier): Unit =
    gasTier = Some(value)

  /** Records the EBF recipe override from a full EU/t + duration pair. */
  def setBlastStats(stats: RecipeStats): Unit =
    blastStats = Some(RecipeOverride(stats))

  /** Records the EBF recipe override from an EU/t alone. */
  def setBlastStats(eut: RecipeEUt): Unit =
    blastStats = Some(RecipeOverride(eut))

  /** Records the vacuum recipe override from a full EU/t + duration pair. */
  def setVacuumStats(stats: RecipeStats): Unit =
    vacuumStats = Some(RecipeOverride(stats))

  /** Records the vacuum recipe override from an EU/t alone. */
  def setVacuumStats(eut: RecipeEUt): Unit =
    vacuumStats = Some(RecipeOverride(eut))

  /** Freezes the accumulated settings into an immutable [[BlastSpec]]. */
  def toSpec: BlastSpec =
    BlastSpec(
      temperature = temperature,
      gasTier = gasTier,
      blastStats = blastStats,
      vacuumStats = vacuumStats
    )

/** EBF gas-tier assignment target: `gasTier := GasTier.HIGH`. */
object gasTier

extension (token: gasTier.type)
  /** Sets the EBF gas tier. Last write wins. */
  infix def :=(value: GasTier)(using bc: BlastContext): Unit =
    bc.setGasTier(value)

/** EBF recipe-override assignment target: `blastStats := VA(EV) * 2000.ticks`
  * or `blastStats := VA(HV)`.
  */
object blastStats

extension (token: blastStats.type)
  /** Sets the EBF recipe override from a full EU/t + duration pair. */
  infix def :=(stats: RecipeStats)(using bc: BlastContext): Unit =
    bc.setBlastStats(stats)

  /** Sets the EBF recipe override from an EU/t alone, leaving duration to
    * GTCEu's default.
    */
  infix def :=(eut: RecipeEUt)(using bc: BlastContext): Unit =
    bc.setBlastStats(eut)

/** Vacuum recipe-override assignment target:
  * `vacuumStats := VA(HV) * 600.ticks` or `vacuumStats := VA(MV)`.
  */
object vacuumStats

extension (token: vacuumStats.type)
  /** Sets the vacuum recipe override from a full EU/t + duration pair. */
  infix def :=(stats: RecipeStats)(using bc: BlastContext): Unit =
    bc.setVacuumStats(stats)

  /** Sets the vacuum recipe override from an EU/t alone, leaving duration to
    * GTCEu's default.
    */
  infix def :=(eut: RecipeEUt)(using bc: BlastContext): Unit =
    bc.setVacuumStats(eut)

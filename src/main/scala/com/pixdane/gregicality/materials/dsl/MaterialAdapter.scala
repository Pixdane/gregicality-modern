package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialStack
import com.gregtechceu.gtceu.api.fluids.FluidBuilder
import com.gregtechceu.gtceu.api.fluids.FluidState
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys
import net.minecraft.resources.ResourceLocation

/** Factory for a per-material adapter.
  *
  * The DSL never constructs a
  * [[com.gregtechceu.gtceu.api.data.chemical.material.Material.Builder]]
  * directly; it asks the current [[RegistryContext]] for an adapter bound to a
  * `ResourceLocation`. Tests inject a fake factory that records calls instead
  * of touching the Forge material registry.
  */
private[dsl] trait MaterialBuilderFactory:
  /** Returns an adapter for the material identified by `id`. */
  def create(id: ResourceLocation): MaterialBuilderAdapter

/** Sink for material builder calls.
  *
  * The real implementation forwards each method to the matching
  * `Material.Builder` call and preserves authoring order exactly. The DSL
  * records one `ore` call per `ore:` block (with the fully-assembled
  * [[OreSpec]]) rather than emitting a stream of ore sub-calls.
  */
private[dsl] trait MaterialBuilderAdapter:
  /** Sets the localized display name. */
  def langValue(s: String): Unit

  /** Sets the chemical formula string. */
  def formula(s: String): Unit

  /** Marks the material as an ingot with the given harvest level. */
  def ingot(level: Int): Unit

  /** Applies color, icon set, and optional secondary color in one shot. */
  def visual(spec: VisualSpec): Unit

  /** Adds a sequence of flags. */
  def flags(fs: Seq[MaterialFlag]): Unit

  /** Adds a Java-collection preset plus extra varargs flags. */
  def appendFlags(
      preset: java.util.Collection[MaterialFlag],
      extras: Seq[MaterialFlag]
  ): Unit

  /** Sets the material composition from typed material/amount pairs. */
  def components(amounts: Seq[MaterialAmount]): Unit

  /** Applies a fully-assembled ore configuration. */
  def ore(spec: OreSpec): Unit

  /** Adds one fluid storage entry from an assembled fluid configuration. */
  def fluid(spec: FluidSpec): Unit

  /** Adds a blast property from an assembled blast configuration. */
  def blast(spec: BlastSpec): Unit

  /** Finalizes and registers the material; returns the registered `Material`.
    */
  def buildAndRegister(): Material

object MaterialAdapterFactory:

  /** Default factory backed by real GTCEu `Material.Builder` instances. */
  val real: MaterialBuilderFactory = new MaterialBuilderFactory:
    def create(id: ResourceLocation): MaterialBuilderAdapter =
      new GtceuMaterialAdapter(id)

/** Real [[MaterialBuilderAdapter]] over a GTCEu 7.5.3 `Material.Builder`.
  *
  * Each method maps to exactly one `Material.Builder` call. `components` goes
  * through the typed `componentStacks(MaterialStack...)` overload rather than
  * the untyped `components(Object...)` one, so Scala never has to assemble an
  * `Object` array. `ore` collapses an [[OreSpec]] into the documented call
  * sequence: `ore(mult, byproduct, emissive)` first, then optional `washedIn`,
  * non-empty `separatedInto`, and non-empty `addOreByproducts`.
  */
private[dsl] final class GtceuMaterialAdapter(id: ResourceLocation)
    extends MaterialBuilderAdapter:

  private val builder: Material.Builder = new Material.Builder(id)

  def langValue(s: String): Unit =
    builder.langValue(s)

  def formula(s: String): Unit =
    builder.formula(s)

  def ingot(level: Int): Unit =
    builder.ingot(level)

  def visual(spec: VisualSpec): Unit =
    builder.color(spec.color.value)
    builder.iconSet(spec.iconSet)
    spec.secondary.foreach(c => builder.secondaryColor(c.value))

  def flags(fs: Seq[MaterialFlag]): Unit =
    builder.flags(fs*)

  def appendFlags(
      preset: java.util.Collection[MaterialFlag],
      extras: Seq[MaterialFlag]
  ): Unit =
    builder.appendFlags(preset, extras*)

  def components(amounts: Seq[MaterialAmount]): Unit =
    val stacks: Seq[MaterialStack] =
      amounts.map(a => new MaterialStack(a.material, a.amount.toLong))
    builder.componentStacks(stacks*)

  def ore(spec: OreSpec): Unit =
    builder.ore(spec.multiplier, spec.byproductMultiplier, spec.emissive)
    spec.washedIn.foreach(w => builder.washedIn(w.fluid, w.amount))
    if spec.separatedInto.nonEmpty then
      builder.separatedInto(spec.separatedInto*)
    if spec.byproducts.nonEmpty then builder.addOreByproducts(spec.byproducts*)

  def fluid(spec: FluidSpec): Unit =
    val (key, state) = spec.kind match
      case FluidKind.Liquid => FluidStorageKeys.LIQUID -> FluidState.LIQUID
      case FluidKind.Gas    => FluidStorageKeys.GAS -> FluidState.GAS
      case FluidKind.Plasma => FluidStorageKeys.PLASMA -> FluidState.PLASMA
      case FluidKind.Molten => FluidStorageKeys.MOLTEN -> FluidState.LIQUID

    val fluidBuilder = new FluidBuilder().state(state)
    spec.temperature.foreach(k => fluidBuilder.temperature(k.value))
    spec.color.foreach(c => fluidBuilder.color(c.value))
    spec.density.foreach:
      case FluidDensity.Minecraft(value) => fluidBuilder.density(value)
      case FluidDensity.GramsPerCubicCentimeter(value) =>
        fluidBuilder.density(value)
    spec.luminosity.foreach(fluidBuilder.luminosity)
    spec.viscosity.foreach:
      case FluidViscosity.Minecraft(value) => fluidBuilder.viscosity(value)
      case FluidViscosity.Poise(value)     => fluidBuilder.viscosity(value)
    spec.burnTime.foreach(fluidBuilder.burnTime)
    if spec.attributes.nonEmpty then fluidBuilder.attributes(spec.attributes*)
    spec.textures.foreach(t =>
      fluidBuilder.textures(t.customStill, t.customFlowing)
    )
    if spec.hasBlock then fluidBuilder.block()
    if !spec.hasBucket then fluidBuilder.disableBucket()
    if !spec.colorEnabled then fluidBuilder.disableColor()
    builder.fluid(key, fluidBuilder)

  def blast(spec: BlastSpec): Unit =
    builder.blast: blastBuilder =>
      (spec.temperature, spec.gasTier) match
        case (Some(temperature), Some(gasTier)) =>
          blastBuilder.temp(temperature.value, gasTier)
        case (Some(temperature), None) =>
          blastBuilder.temp(temperature.value)
        case (None, Some(_)) =>
          throw new IllegalArgumentException(
            "blast gasTier requires temperature"
          )
        case (None, None) =>
          ()

      spec.blastStats.foreach:
        case RecipeOverride(eut, Some(duration)) =>
          blastBuilder.blastStats(eut.value, duration.value)
        case RecipeOverride(eut, None) =>
          blastBuilder.blastStats(eut.value)

      spec.vacuumStats.foreach:
        case RecipeOverride(eut, Some(duration)) =>
          blastBuilder.vacuumStats(eut.value, duration.value)
        case RecipeOverride(eut, None) =>
          blastBuilder.vacuumStats(eut.value)

      blastBuilder

  def buildAndRegister(): Material =
    builder.buildAndRegister()

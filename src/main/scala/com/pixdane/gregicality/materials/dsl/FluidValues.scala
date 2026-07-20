package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute

/** Standard GTCEu fluid storage kinds supported by the material DSL.
  *
  * The DSL records this stable enum instead of touching `FluidStorageKeys`
  * while authoring. The real adapter resolves the selected kind to GTCEu's
  * static storage key and state only when it forwards the material builder
  * call. This keeps contextual tests independent of Forge bootstrap state.
  */
enum FluidKind:
  /** The standard liquid storage key and liquid state. */
  case Liquid

  /** The standard gas storage key and gas state. */
  case Gas

  /** The standard plasma storage key and plasma state. */
  case Plasma

  /** The molten storage key with liquid state. */
  case Molten

/** Fluid density in either Minecraft units or grams per cubic centimetre.
  *
  * GTCEu's `FluidBuilder` accepts density both as a raw Minecraft integer and
  * as a `double` grams-per-cubic-centimetre value that it converts via its GT6
  * formula. The DSL preserves which form the author wrote so the adapter can
  * call the matching overload without re-deriving the value.
  *
  * Either variant is allowed to carry the sentinel `-1` (GTCEu's
  * `INFER_DENSITY`) so an author can explicitly request inference; the DSL does
  * not replicate GTCEu's density range checks.
  */
enum FluidDensity:
  /** Raw Minecraft density as passed through to `FluidBuilder.density(int)`. */
  case Minecraft(value: Int)

  /** `g/cm^3` value passed through to `FluidBuilder.density(double)`. */
  case GramsPerCubicCentimeter(value: Double)

/** Fluid viscosity in either Minecraft units or poise.
  *
  * Mirrors [[FluidDensity]]: GTCEu accepts both a raw integer viscosity and a
  * `double` poise value (which it multiplies by 10000). The DSL records which
  * form the author wrote.
  */
enum FluidViscosity:
  /** Raw Minecraft viscosity as passed to `FluidBuilder.viscosity(int)`. */
  case Minecraft(value: Int)

  /** Poise value passed to `FluidBuilder.viscosity(double)`. */
  case Poise(value: Double)

/** Custom still/flowing texture flags for a fluid.
  *
  * Mirrors GTCEu's `FluidBuilder.textures(boolean, boolean)`. Both fields
  * default to `false` (no custom textures). `customStill = true` is what the
  * bare `customStill` DSL command and `FluidBuilder.customStill()` produce.
  */
final case class FluidTextures(
    customStill: Boolean = false,
    customFlowing: Boolean = false
)

/** Immutable snapshot of one fluid section's accumulated configuration.
  *
  * Produced by [[FluidContext.toSpec]] at the end of a `fluid(FluidKind):`,
  * `gas:`, or `plasma:` block and forwarded to the material adapter as a single
  * `fluid` call. Every field except `kind` is optional: `None` means "leave
  * GTCEu's inference default in place".
  *
  * `textures` and the bare commands `customStill`, `block`, `disableBucket`,
  * and `disableColor` are folded into this case class instead of being separate
  * adapter calls, because GTCEu collapses them onto a single `FluidBuilder`.
  *
  * `colorEnabled = false` records that the author asked `customStill`,
  * `textures(...)`, or `disableColor` to suppress fluid color. The adapter uses
  * it to decide between `color(int)` and `disableColor()` on the underlying
  * `FluidBuilder`. The DSL itself does no color-range validation.
  *
  * @param kind
  *   the standard storage kind selected by the material entry point
  * @param temperature
  *   explicit kelvin temperature, or `None` to let GTCEu infer
  * @param color
  *   explicit RGB color, or `None` to let GTCEu infer
  * @param density
  *   explicit density in either unit, or `None` to let GTCEu infer
  * @param luminosity
  *   explicit luminosity in `[0, 16)`, or `None` to let GTCEu infer
  * @param viscosity
  *   explicit viscosity in either unit, or `None` to let GTCEu infer
  * @param burnTime
  *   explicit burn time in ticks, or `None` to let GTCEu infer (default `-1`)
  * @param name
  *   optional explicit fluid registration name passed to `FluidBuilder.name`
  * @param translation
  *   optional explicit translation key or value passed to
  *   `FluidBuilder.translation`
  * @param attributes
  *   fluid attributes appended in authoring order; empty by default
  * @param textures
  *   explicit custom texture flags, or `None` if the author did not set them
  * @param hasBlock
  *   whether the bare `block` command was issued
  * @param hasBucket
  *   whether the fluid keeps its bucket; `false` after `disableBucket`
  * @param colorEnabled
  *   whether color is still authoritative; `false` once `customStill`,
  *   `textures(...)`, or `disableColor` is issued
  */
final case class FluidSpec(
    kind: FluidKind,
    temperature: Option[Kelvin] = None,
    color: Option[HexColor] = None,
    density: Option[FluidDensity] = None,
    luminosity: Option[Int] = None,
    viscosity: Option[FluidViscosity] = None,
    burnTime: Option[Int] = None,
    name: Option[String] = None,
    translation: Option[String] = None,
    attributes: List[FluidAttribute] = List.empty,
    textures: Option[FluidTextures] = None,
    hasBlock: Boolean = false,
    hasBucket: Boolean = true,
    colorEnabled: Boolean = true
)

package com.pixdane.gregicality.codegen.core.materials

import cats.data.NonEmptyVector
import com.pixdane.gregicality.core.refs.{FluidAttributeRef, FluidStorageKeyRef}

/** One or more authored fluid registrations plus an optional primary override.
  */
final case class FluidPropertySpec(
    fluids: NonEmptyVector[FluidEntry],
    primaryKey: Option[FluidStorageKeyRef] = None
)

/** One storage key and the authored builder calls for its fluid. */
final case class FluidEntry(
    key: FluidStorageKeyRef,
    builder: FluidBuilderSpec = FluidBuilderSpec()
)

/** Authored FluidBuilder calls. None, false, and Inferred mean no corresponding
  * override call; GTCEu remains responsible for its runtime defaults.
  */
final case class FluidBuilderSpec(
    temperature: Option[FluidTemperature] = None,
    state: Option[FluidState] = None,
    color: FluidColor = FluidColor.Inferred,
    density: Option[FluidDensity] = None,
    luminosity: Option[Int] = None,
    viscosity: Option[FluidViscosity] = None,
    attributes: Vector[FluidAttributeRef] = Vector.empty,
    textures: FluidTextures = FluidTextures.Inferred,
    createBlock: Boolean = false,
    disableBucket: Boolean = false,
    burnTime: Option[BurnTimeTicks] = None,
    name: Option[RegistryPath] = None,
    translation: Option[String] = None
)

/** Authored fluid color policy. */
enum FluidColor:
  case Inferred
  case Explicit(rgb: HexRgb)
  case Disabled

/** Optional explicit FluidBuilder state override. */
enum FluidState:
  case Liquid
  case Gas
  case Plasma

/** Density expressed either in physical or Minecraft units. */
enum FluidDensity:
  case GramsPerCubicCentimeter(value: Double)
  case Minecraft(value: Int)

/** Viscosity expressed either in Poise or Minecraft units. */
enum FluidViscosity:
  case Poise(value: Double)
  case Minecraft(value: Int)

/** Authored choice of inferred or custom still/flowing textures. */
enum FluidTextures:
  case Inferred
  case CustomStill
  case CustomStillAndFlowing

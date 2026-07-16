package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.MaterialIconRef

/** Authored material visual calls; defaults mean that no override was written.
  */
final case class VisualSpec(
    primaryColor: ColorSpec = ColorSpec.Default,
    secondaryColor: Option[HexRgb] = None,
    iconSet: Option[MaterialIconRef] = None,
    fluidColor: FluidColorPolicy = FluidColorPolicy.InheritMaterial
)

/** Distinguishes omitted color, explicit RGB, and component averaging. */
enum ColorSpec:
  case Default
  case Explicit(rgb: HexRgb)
  case AverageComponents

/** Whether generated fluids inherit the material color or disable tinting. */
enum FluidColorPolicy:
  case InheritMaterial
  case Disabled

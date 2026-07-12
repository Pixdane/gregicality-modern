package com.pixdane.gregicality.codegen.core.materials

final case class MaterialForms(
    solidForms: Vector[SolidForm] = Vector.empty,
    fluidForms: Vector[FluidForm] = Vector.empty,
    primaryFluid: Option[FluidKind] = None
)

final case class SolidForm(
    solidKind: SolidKind,
    solidParams: SolidParams = SolidParams()
)

final case class FluidForm(
    fluidKind: FluidKind,
    fluidParams: FluidParams = FluidParams()
)

final case class SolidParams(
    harvestLevel: Option[HarvestLevel] = None,
    burnTime: Option[BurnTimeTicks] = None
)

final case class FluidParams(
    temperature: Option[TemperatureKelvin] = None,
    texture: FluidTexture = FluidTexture.Inferred(),
    block: Boolean = false,
    burnTime: Option[BurnTimeTicks] = None,
    attributes: Vector[FluidAttributeRef] = Vector.empty,
    name: Option[RegistryPath] = None,
    translation: Option[TranslationKey] = None
)

enum SolidKind:
  case Dust, Ingot, Gem, Wood, Polymer

enum FluidKind:
  case Liquid, Gas, Plasma

enum FluidTexture:
  case Inferred(color: FluidColor = FluidColor.Inferred)
  case CustomStill

enum FluidColor:
  case Inferred
  case Disabled
  case Colored(color: HexRgb)

opaque type FluidAttributeRef = String

opaque type HarvestLevel = Int
opaque type BurnTimeTicks = Int
opaque type TemperatureKelvin = Int

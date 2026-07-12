package com.pixdane.gregicality.codegen.core.materials

final case class MaterialVisual(
    color: Option[HexRgb] = None,
    secondaryColor: Option[HexRgb] = None,
    materialFluidColor: Boolean = true,
    iconSet: Option[MaterialIconSetRef] = None
)

opaque type MaterialIconSetRef = String

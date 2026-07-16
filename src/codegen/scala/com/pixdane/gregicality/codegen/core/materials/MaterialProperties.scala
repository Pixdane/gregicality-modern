package com.pixdane.gregicality.codegen.core.materials

/** Unique authored property slots in the first implementation slice. None means
  * the author did not request that property; induced runtime properties are not
  * written back here.
  */
final case class MaterialProperties(
    dust: Option[DustPropertySpec] = None,
    ingot: Option[IngotPropertySpec] = None,
    gem: Option[GemPropertySpec] = None,
    wood: Option[WoodPropertySpec] = None,
    polymer: Option[PolymerPropertySpec] = None,
    fluid: Option[FluidPropertySpec] = None,
    ore: Option[OrePropertySpec] = None,
    blast: Option[BlastPropertySpec] = None
)

package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.{
  ElementRef,
  ItemTagRef,
  MaterialFlagPresetRef,
  MaterialFlagRef,
  TagPrefixRef
}

/** Complete authored content for one new Material.Builder registration. */
final case class NewMaterialSpec(
    id: RegistryPath,
    field: ScalaIdent,
    identity: MaterialIdentity = MaterialIdentity(),
    visuals: VisualSpec = VisualSpec(),
    composition: CompositionSpec = CompositionSpec(),
    properties: MaterialProperties = MaterialProperties(),
    flags: MaterialFlagSpec = MaterialFlagSpec(),
    tags: MaterialTagConfig = MaterialTagConfig()
)

/** Optional identity metadata that is not a material property. */
final case class MaterialIdentity(
    displayName: Option[String] = None,
    element: Option[ElementRef] = None
)

/** Authored flag presets and individual flags, without runtime flag closure. */
final case class MaterialFlagSpec(
    presets: Vector[MaterialFlagPresetRef] = Vector.empty,
    flags: Set[MaterialFlagRef] = Set.empty
)

/** Authored tag calls attached to a material registration. */
final case class MaterialTagConfig(
    ignoredTagPrefixes: Vector[TagPrefixRef] = Vector.empty,
    customItemTags: Vector[ItemTagRef] = Vector.empty
)

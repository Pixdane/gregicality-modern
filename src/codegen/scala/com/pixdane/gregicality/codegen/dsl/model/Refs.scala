package com.pixdane.gregicality.codegen.dsl.model

final case class ScalaSymbolPath(parts: Vector[String])

final case class ResourceId(namespace: String, path: String)

final case class MaterialRef(id: ResourceId, path: ScalaSymbolPath)

final case class ElementRef(path: ScalaSymbolPath)

final case class MaterialIconRef(path: ScalaSymbolPath)

final case class MaterialFlagRef(path: ScalaSymbolPath)

final case class FluidAttributeRef(path: ScalaSymbolPath)

final case class TagPrefixRef(path: ScalaSymbolPath)

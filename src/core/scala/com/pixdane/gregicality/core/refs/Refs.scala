package com.pixdane.gregicality.core.refs

final case class ScalaSymbolPath(parts: Vector[String]):
  def append(part: String): ScalaSymbolPath =
    ScalaSymbolPath(parts :+ part)

object ScalaSymbolPath:
  def fromFqcn(fqcn: String): ScalaSymbolPath =
    ScalaSymbolPath(fqcn.split('.').toVector)

  def member(ownerFqcn: String, memberName: String): ScalaSymbolPath =
    fromFqcn(ownerFqcn).append(memberName)

final case class ResourceId(namespace: String, path: String)

final case class MaterialRef(id: ResourceId, path: ScalaSymbolPath)

final case class ElementRef(path: ScalaSymbolPath)

final case class MaterialIconRef(path: ScalaSymbolPath)

final case class MaterialFlagRef(path: ScalaSymbolPath)

final case class FluidAttributeRef(path: ScalaSymbolPath)

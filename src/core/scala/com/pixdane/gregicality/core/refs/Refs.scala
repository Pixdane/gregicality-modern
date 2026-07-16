package com.pixdane.gregicality.core.refs

/**
 * Dotted Scala symbol path split into ordered segments. Pure data; the segments
 * name a symbol but never hold a runtime GTCEu object.
 */
final case class ScalaSymbolPath(parts: Vector[String]):
  def append(part: String): ScalaSymbolPath =
    ScalaSymbolPath(parts :+ part)

object ScalaSymbolPath:
  def fromFqcn(fqcn: String): ScalaSymbolPath =
    ScalaSymbolPath(fqcn.split('.').toVector)

  def member(ownerFqcn: String, memberName: String): ScalaSymbolPath =
    fromFqcn(ownerFqcn).append(memberName)

/**
 * Minecraft resource identifier (namespace + path). Pure data; does not resolve
 * to a runtime registry object.
 */
final case class ResourceId(namespace: String, path: String)

/** Path-only reference to a material. Pure data; no runtime GTCEu object. */
final case class MaterialRef(id: ResourceId, path: ScalaSymbolPath)

/** Path-only reference to a generic element. Pure data; no runtime GTCEu object. */
final case class ElementRef(path: ScalaSymbolPath)

/** Path-only reference to a material icon. Pure data; no runtime GTCEu object. */
final case class MaterialIconRef(path: ScalaSymbolPath)

/** Path-only reference to a material flag. Pure data; no runtime GTCEu object. */
final case class MaterialFlagRef(path: ScalaSymbolPath)

/** Path-only reference to a fluid attribute. Pure data; no runtime GTCEu object. */
final case class FluidAttributeRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a material flag preset.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class MaterialFlagPresetRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a fluid storage key.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class FluidStorageKeyRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a gas tier.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class GasTierRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a tag prefix.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class TagPrefixRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a hazard trigger.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class HazardTriggerRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a medical condition.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class MedicalConditionRef(path: ScalaSymbolPath)

/**
 * Path-only reference to a tool type.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class ToolTypeRef(path: ScalaSymbolPath)

/**
 * Path-only reference to an item tag.
 * Pure data; must not hold a runtime GTCEu object.
 */
final case class ItemTagRef(path: ScalaSymbolPath)

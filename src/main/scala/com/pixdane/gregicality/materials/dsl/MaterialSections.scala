package com.pixdane.gregicality.materials.dsl

/** A named, distinguishable section of the material authoring DSL. */
trait SectionMarker:
  /** Stable display name for this DSL section. */
  def name: String

object SectionMarker:
  /** All sections defined by this batch, in declaration order. */
  val all: List[SectionMarker] =
    List(MaterialSection, FluidSection, BlastSection, ToolSection, ArmorSection, OreSection)

/** Top-level material authoring context. */
object MaterialSection extends SectionMarker:
  val name: String = "Material"

/** Nested fluid/liquid/gas/plasma builder context. */
object FluidSection extends SectionMarker:
  val name: String = "Fluid"

/** Nested blast property builder context. */
object BlastSection extends SectionMarker:
  val name: String = "Blast"

/** Nested tool property builder context. */
object ToolSection extends SectionMarker:
  val name: String = "Tool"

/** Nested armor property builder context. */
object ArmorSection extends SectionMarker:
  val name: String = "Armor"

/** Nested ore grouping context (DSL-only; GTCEu has no OreProperty.Builder). */
object OreSection extends SectionMarker:
  val name: String = "Ore"

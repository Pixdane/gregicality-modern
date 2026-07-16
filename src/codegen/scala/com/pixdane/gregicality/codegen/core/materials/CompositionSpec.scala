package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.MaterialRef

/** Ordered authored components and an optional post-build formula override. */
final case class CompositionSpec(
    components: Vector[ComponentSpec] = Vector.empty,
    formulaOverride: Option[FormulaOverride] = None
)

/** One material component and its positive authored amount. */
final case class ComponentSpec(material: MaterialRef, amount: PositiveInt)

/** Explicit formula text and the GTCEu subscript-formatting choice. */
final case class FormulaOverride(text: String, formatSubscripts: Boolean)

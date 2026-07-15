package com.pixdane.gregicality.symbolgen.render

import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.framework.{
  GeneratedScalaFile,
  RefOutputSpec,
  ScannedMaterialRef,
  ScannedPathRef
}

/** Renders scanned symbols into the Scala object described by a
  * [[RefOutputSpec]].
  *
  * Each generated object exposes one accessor per scanned ref and, for
  * materials, a `resolve` index keyed by registry id. Material files are
  * chunked into private `byIdEntriesN` helpers so that no single method exceeds
  * a practical size for very large material sets.
  */
object RefOutputRenderer:
  private val IndexChunkSize = 200

  def generateMaterialFile(
      target: RefOutputSpec,
      refs: Vector[ScannedMaterialRef]
  ): GeneratedScalaFile =
    val sorted = refs.sortBy(_.name)
    generateRefFile(
      target = target,
      entries = sorted.map(renderMaterialRef(target)),
      suffix = renderLookupIndex(target, sorted)
    )

  def generatePathFile(
      target: RefOutputSpec,
      refs: Vector[ScannedPathRef]
  ): GeneratedScalaFile =
    val sorted = refs.sortBy(_.name)
    generateRefFile(
      target = target,
      entries = sorted.map(renderPathRef(target)),
      suffix = ScalaCode.empty
    )

  private def generateRefFile(
      target: RefOutputSpec,
      entries: Vector[ScalaCode],
      suffix: ScalaCode
  ): GeneratedScalaFile =
    val prefix = ScalaCode.lines(
      s"package ${target.outputPackage}",
      "",
      "import com.pixdane.gregicality.core.refs.*",
      "",
      s"object ${target.outputObject}:"
    )
    val separator = ScalaCode.line("")
    val code = prefix ++ ScalaCode.joinWith(separator)(entries) ++ suffix

    GeneratedScalaFile(
      relativePath = target.outputPackage.replace('.', '/') + "/" +
        target.outputObject + ".scala",
      content = code.render
    )

  private def renderMaterialRef(target: RefOutputSpec)(
      ref: ScannedMaterialRef
  ): ScalaCode =
    ScalaCode.lines(
      s"  def ${ref.name}: ${target.valueType} =",
      s"    ${target.valueType}(",
      s"      ResourceId(${quote(ref.id.namespace)}, ${quote(ref.id.path)}),",
      s"      ${renderScalaPath(ref.path)}",
      s"    )"
    )

  private def renderPathRef(target: RefOutputSpec)(
      ref: ScannedPathRef
  ): ScalaCode =
    ScalaCode.lines(
      s"  def ${ref.name}: ${target.valueType} =",
      s"    ${target.valueType}(${renderScalaPath(ref.path)})"
    )

  private def renderLookupIndex(
      target: RefOutputSpec,
      refs: Vector[ScannedMaterialRef]
  ): ScalaCode =
    val chunks =
      refs
        .grouped(IndexChunkSize)
        .toVector
    val entriesExpression =
      if chunks.isEmpty then "Vector.empty"
      else chunks.indices.map(index => s"byIdEntries$index").mkString(" ++ ")
    val chunkLines = chunks.zipWithIndex.flatMap { case (chunk, index) =>
      Vector(
        "",
        s"  private def byIdEntries$index: Vector[${target.valueType}] =",
        chunk
          .map(ref => ref.name)
          .mkString("    Vector(", ", ", ")")
      )
    }

    ScalaCode.lines(
      (Vector(
        "",
        s"  def resolve(id: ResourceId): Option[${target.valueType}] =",
        "    byIdIndex.get(id)",
        "",
        s"  private lazy val byIdIndex: Map[ResourceId, ${target.valueType}] =",
        "    byIdEntries.iterator.map(ref => ref.id -> ref).toMap",
        "",
        s"  private def byIdEntries: Vector[${target.valueType}] =",
        s"    $entriesExpression"
      ) ++ chunkLines)*
    )

  private def renderScalaPath(path: ScalaSymbolPath): String =
    s"ScalaSymbolPath(Vector(${path.parts.map(quote).mkString(", ")}))"

  private def quote(value: String): String =
    "\"" + value.flatMap {
      case '\\' => "\\\\"
      case '"'  => "\\\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case char => char.toString
    } + "\""

package com.pixdane.gregicality.symbolgen.render

import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialRef,
  ScannedPathRef,
  ScannedRegisteredMaterialRef
}

object RefObjectRenderer:
  private val IndexChunkSize = 200

  def generateMaterialFile(
      target: RefObjectTarget,
      refs: Vector[ScannedMaterialRef]
  ): GeneratedScalaFile =
    val sorted = refs.sortBy(_.name)
    generateRefFile(
      target = target,
      entries = sorted.map(renderMaterialRef(target)),
      suffix = renderLookupIndex(target, sorted)
    )

  def generatePathFile(
      target: RefObjectTarget,
      refs: Vector[ScannedPathRef]
  ): GeneratedScalaFile =
    val sorted = refs.sortBy(_.name)
    generateRefFile(
      target = target,
      entries = sorted.map(renderPathRef(target)),
      suffix = ScalaCode.empty
    )

  private def generateRefFile(
      target: RefObjectTarget,
      entries: Vector[ScalaCode],
      suffix: ScalaCode
  ): GeneratedScalaFile =
    val code = refObjectLayout(target, suffix).apply(entries)

    GeneratedScalaFile(
      relativePath = target.outputPackage.replace('.', '/') + "/" +
        target.outputObject + ".scala",
      content = code.render
    )

  private def renderMaterialRef(target: RefObjectTarget)(
      ref: ScannedMaterialRef
  ): ScalaCode =
    ScalaCode.lines(
      s"  def ${ref.name}: ${target.valueType} =",
      s"    ${target.valueType}(",
      s"      ResourceId(${quote(ref.id.namespace)}, ${quote(ref.id.path)}),",
      s"      ${renderScalaPath(ref.path)}",
      s"    )"
    )

  private def renderPathRef(target: RefObjectTarget)(
      ref: ScannedPathRef
  ): ScalaCode =
    ScalaCode.lines(
      s"  def ${ref.name}: ${target.valueType} =",
      s"    ${target.valueType}(${renderScalaPath(ref.path)})"
    )

  private def refObjectLayout(
      target: RefObjectTarget,
      suffix: ScalaCode
  ): CodeLayout =
    CodeLayout(
      prefix = ScalaCode.lines(
        s"package ${target.outputPackage}",
        "",
        "import com.pixdane.gregicality.core.refs.*",
        "",
        s"object ${target.outputObject}:"
      ),
      separator = ScalaCode.line(""),
      suffix = suffix
    )

  private def renderLookupIndex(
      target: RefObjectTarget,
      refs: Vector[ScannedMaterialRef]
  ): ScalaCode =
    val chunks =
      refs
        .collect { case ref: ScannedRegisteredMaterialRef => ref }
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

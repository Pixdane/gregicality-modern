package com.pixdane.gregicality.symbolgen

object RefObjectRenderer:
  private val AllChunkSize = 100

  def generateFile(job: RefJob, archive: SourceArchive): GeneratedScalaFile =
    val refs = job.source(archive).sortBy(_.name)
    val entries = refs.map(renderRef(job.target))
    val code = refObjectLayout(job.target, refs).apply(entries)

    GeneratedScalaFile(
      relativePath =
        job.target.outputPackage.replace('.', '/') + "/" +
          job.target.outputObject + ".scala",
      content = code.render
    )

  def renderRef(target: RefObjectTarget)(ref: ScannedRef): ScalaCode =
    target.renderKind match
      case RefRenderKind.WithId =>
        val id =
          ref.id.getOrElse {
            throw new IllegalArgumentException(
              s"${target.outputObject}.${ref.name} requires a ResourceId"
            )
          }

        ScalaCode.lines(
          s"  def ${ref.name}: ${target.valueType} =",
          s"    ${target.valueType}(",
          s"      ResourceId(${quote(id.namespace)}, ${quote(id.path)}),",
          s"      ${renderScalaPath(ref.path)}",
          s"    )"
        )

      case RefRenderKind.PathOnly =>
        ScalaCode.lines(
          s"  def ${ref.name}: ${target.valueType} =",
          s"    ${target.valueType}(${renderScalaPath(ref.path)})"
        )

  private def refObjectLayout(
      target: RefObjectTarget,
      refs: Vector[ScannedRef]
  ): CodeLayout =
    CodeLayout(
      prefix = ScalaCode.lines(
        s"package ${target.outputPackage}",
        "",
        "import com.pixdane.gregicality.codegen.dsl.ref.*",
        "",
        s"object ${target.outputObject}:"
      ),
      separator = ScalaCode.line(""),
      suffix = ScalaCode.line("") ++ renderAllVector(target, refs)
    )

  private def renderAllVector(target: RefObjectTarget, refs: Vector[ScannedRef]): ScalaCode =
    if refs.isEmpty then
      ScalaCode.lines(
        s"  def all: Vector[${target.valueType}] =",
        "    Vector.empty"
      )
    else
      val chunks = refs.grouped(AllChunkSize).toVector
      val allLines =
        if chunks.size == 1 then
          Vector(
            s"  def all: Vector[${target.valueType}] =",
            s"    all0"
          )
        else
          Vector(
            s"  def all: Vector[${target.valueType}] =",
            chunks.indices.map(index => s"all$index").mkString("    ", " ++ ", "")
          )

      val chunkLines =
        chunks.zipWithIndex.flatMap { case (chunk, index) =>
          Vector(
            "",
            s"  private def all$index: Vector[${target.valueType}] =",
            chunk.map(_.name).mkString("    Vector(", ", ", ")")
          )
        }

      ScalaCode.lines((allLines ++ chunkLines)*)

  private def renderScalaPath(path: ScalaPath): String =
    s"ScalaPath(Vector(${path.parts.map(quote).mkString(", ")}))"

  private def quote(value: String): String =
    "\"" + value.flatMap {
      case '\\' => "\\\\"
      case '"' => "\\\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case char => char.toString
    } + "\""

object RefSupportRenderer:
  val PackageName = "com.pixdane.gregicality.codegen.dsl.ref"

  def generateFile(): GeneratedScalaFile =
    GeneratedScalaFile(
      relativePath = PackageName.replace('.', '/') + "/Refs.scala",
      content = ScalaCode.lines(
        s"package $PackageName",
        "",
        "final case class ScalaPath(parts: Vector[String])",
        "",
        "final case class ResourceId(namespace: String, path: String)",
        "",
        "final case class MaterialRef(id: ResourceId, path: ScalaPath)",
        "",
        "final case class ElementRef(path: ScalaPath)",
        "",
        "final case class MaterialIconRef(path: ScalaPath)",
        "",
        "final case class MaterialFlagRef(path: ScalaPath)",
        "",
        "final case class FluidAttributeRef(path: ScalaPath)",
        "",
        "final case class TagPrefixRef(path: ScalaPath)"
      ).render
    )

object RefAggregateRenderer:
  def generateFile(outputPackage: String, outputObject: String, exports: Vector[String]): GeneratedScalaFile =
    val exportLines =
      exports.sorted.map { name =>
        s"  export $name.{all as ${allAlias(name)}, *}"
      }
    val lines =
      Vector(
        s"package $outputPackage",
        "",
        s"object $outputObject:"
      ) ++ exportLines

    GeneratedScalaFile(
      relativePath = outputPackage.replace('.', '/') + "/" + outputObject + ".scala",
      content = ScalaCode
        .lines(lines*)
        .render
    )

  private def allAlias(outputObject: String): String =
    "all" + outputObject.stripSuffix("Ref")

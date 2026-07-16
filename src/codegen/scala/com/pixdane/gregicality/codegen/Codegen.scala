package com.pixdane.gregicality.codegen

import java.nio.file.Path

import com.pixdane.gregicality.codegen.core.materials.*
import com.pixdane.gregicality.core.refs.ScalaSymbolPath
import com.pixdane.gregicality.generator.{
  GeneratedScalaFile,
  GeneratedSourceWriter
}

/** Command-line arguments for material source generation. */
final case class CodegenArgs(out: Path)

object CodegenArgs:

  /** Parses the Gradle-facing `--out <directory>` argument. */
  def parse(arguments: Vector[String]): CodegenArgs =
    arguments match
      case Vector("--out", outputDir) if outputDir.nonEmpty =>
        CodegenArgs(Path.of(outputDir))
      case _ =>
        throw new IllegalArgumentException(
          "usage: material codegen --out <generated-source-directory>"
        )

/** Validates authored material packages and renders their owned Scala sources.
  */
object Codegen:
  private val RuntimePackage =
    "com.pixdane.gregicality.common.data.materials"
  private val RuntimePath = RuntimePackage.replace('.', '/')
  private val IndexObject = "GCYMaterialsGeneratedIndex"
  private val IdFactory = ScalaSymbolPath.member(
    "com.pixdane.gregicality.Gregicality",
    "id"
  )

  private final case class MaterialPackage(
      set: MaterialSet,
      output: MaterialOutputSpec
  )

  private val Packages = Vector(
    MaterialPackage(
      set = GCYMaterialSets.chemistryPolymers,
      output = MaterialOutputSpec(
        packageName = RuntimePackage,
        objectName = valid(
          ScalaIdent.from("GCYMaterialsChemistryPolymers")
        ),
        idFactory = IdFactory
      )
    )
  )

  /** Produces the complete deterministic file set owned by material codegen. */
  def generateFiles(): Vector[GeneratedScalaFile] =
    val materialFiles = Packages.map { materialPackage =>
      val validated = valid(
        MaterialValidator.validateSet(materialPackage.set)
      )
      val content = MaterialRenderer
        .render(MaterialPlanner.plan(validated, materialPackage.output))
        .render

      GeneratedScalaFile(
        relativePath = sourcePath(materialPackage.output.objectName.value),
        content = content
      )
    }
    val materialObjects = Packages.map(_.output.objectName.value)

    materialFiles :+ GeneratedScalaFile(
      relativePath = sourcePath(IndexObject),
      content = renderIndex(materialObjects)
    )

  /** Atomically synchronizes the generated-material source directory. */
  def run(args: CodegenArgs): Unit =
    GeneratedSourceWriter.sync(args.out, generateFiles())

  private def sourcePath(objectName: String): String =
    s"$RuntimePath/$objectName.scala"

  private def renderIndex(materialObjects: Vector[String]): String =
    val registerCalls =
      materialObjects.map(name => s"    $name.register()")
    val patchCalls =
      materialObjects.map(name => s"    $name.patch()")

    ScalaCode(
      Vector(
        s"package $RuntimePackage",
        "",
        s"object $IndexObject:",
        "  def registerAll(): Unit ="
      ) ++ registerCalls ++
        Vector(
          "",
          "  def patchAll(): Unit ="
        ) ++ patchCalls
    ).render

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      issues =>
        throw new IllegalArgumentException(
          "material code generation failed:\n" +
            issues.iterator.map(_.message).mkString("\n")
        ),
      identity
    )
end Codegen

@main def main(arguments: String*): Unit =
  Codegen.run(CodegenArgs.parse(arguments.toVector))

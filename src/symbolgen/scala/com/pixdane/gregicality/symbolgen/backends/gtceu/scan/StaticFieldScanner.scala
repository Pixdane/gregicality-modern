package com.pixdane.gregicality.symbolgen.backends.gtceu.scan

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.pixdane.gregicality.core.refs.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.framework.{
  ScannedPathRef,
  SourceArchive
}

import scala.jdk.CollectionConverters.*

/** Scans a single GTCEu static-field source file for public static final fields
  * of a given type and emits a [[ScannedPathRef]] per field, skipping
  * deprecated fields.
  */
object StaticFieldScanner:
  def scan(input: StaticFieldScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[Vector[ScannedPathRef]] =
    GtceuScanDiagnostic.fromArchive(
      archive
        .parse(input.sourcePath)
        .map(unit => collectRefs(unit, input))
    )

  private def collectRefs(
      unit: CompilationUnit,
      input: StaticFieldScanSpec
  ): Vector[ScannedPathRef] =
    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(field =>
        field.isPublic &&
          field.isStatic &&
          field.isFinal &&
          !isDeprecated(field) &&
          field.getElementType.asString == input.memberTypeSimpleName
      )
      .flatMap(_.getVariables.asScala.toVector)
      .map { variable =>
        val name = variable.getNameAsString

        ScannedPathRef(
          name = name,
          path = ScalaSymbolPath.member(input.ownerFqcn, name)
        )
      }

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

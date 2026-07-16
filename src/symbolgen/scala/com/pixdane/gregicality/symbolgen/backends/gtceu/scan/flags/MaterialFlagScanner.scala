package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.flags

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.{
  Expression,
  FieldAccessExpr,
  MethodCallExpr,
  NameExpr
}
import com.pixdane.gregicality.core.refs.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult,
  MaterialFlagScanSpec
}
import com.pixdane.gregicality.symbolgen.framework.{
  ScannedMaterialFlagRef,
  SourceArchive
}

import scala.jdk.CollectionConverters.*

/** Scans MaterialFlag builder declarations and their source-level dependencies.
  */
object MaterialFlagScanner:

  def scan(input: MaterialFlagScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[Vector[ScannedMaterialFlagRef]] =
    GtceuScanDiagnostic.fromArchive(
      archive
        .parse(input.sourcePath)
        .map(unit => collectFlags(unit, input))
    )

  private def collectFlags(
      unit: CompilationUnit,
      input: MaterialFlagScanSpec
  ): Vector[ScannedMaterialFlagRef] =
    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(isMaterialFlagField)
      .flatMap(_.getVariables.asScala.toVector)
      .map { variable =>
        val calls =
          if variable.getInitializer.isPresent then
            variable.getInitializer.get
              .findAll(classOf[MethodCallExpr])
              .asScala
              .toVector
          else Vector.empty
        val requiredFlags = calls
          .filter(_.getNameAsString == "requireFlags")
          .flatMap(_.getArguments.asScala.toVector)
          .flatMap(memberName)
          .map(name => ScalaSymbolPath.member(input.ownerFqcn, name))
          .distinct
        val requiredProperties = calls
          .filter(_.getNameAsString == "requireProps")
          .flatMap(_.getArguments.asScala.toVector)
          .flatMap(memberName)
          .map(name => ScalaSymbolPath.member(input.propertyKeyOwnerFqcn, name))
          .distinct
        val name = variable.getNameAsString

        ScannedMaterialFlagRef(
          name = name,
          path = ScalaSymbolPath.member(input.ownerFqcn, name),
          requiredFlags = requiredFlags,
          requiredProperties = requiredProperties
        )
      }

  private def isMaterialFlagField(field: FieldDeclaration): Boolean =
    field.isPublic &&
      field.isStatic &&
      field.isFinal &&
      field.getElementType.asString == "MaterialFlag" &&
      !isDeprecated(field)

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

  private def memberName(expression: Expression): Option[String] =
    expression match
      case name: NameExpr         => Some(name.getNameAsString)
      case field: FieldAccessExpr => Some(field.getNameAsString)
      case _                      => None

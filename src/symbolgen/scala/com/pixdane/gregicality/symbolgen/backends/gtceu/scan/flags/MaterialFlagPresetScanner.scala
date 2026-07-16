package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.flags

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.{FieldDeclaration, InitializerDeclaration}
import com.github.javaparser.ast.expr.{
  Expression,
  FieldAccessExpr,
  MethodCallExpr,
  NameExpr
}
import com.github.javaparser.ast.stmt.ExpressionStmt
import com.pixdane.gregicality.core.refs.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult,
  MaterialFlagPresetScanSpec
}
import com.pixdane.gregicality.symbolgen.framework.{
  ScannedMaterialFlagPresetRef,
  SourceArchive
}

import scala.collection.mutable
import scala.jdk.CollectionConverters.*

/** Scans ordered GTMaterials flag-preset initialization into flattened members.
  */
object MaterialFlagPresetScanner:

  def scan(input: MaterialFlagPresetScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[Vector[ScannedMaterialFlagPresetRef]] =
    GtceuScanDiagnostic.fromArchive(
      archive
        .parse(input.sourcePath)
        .map(unit => collectPresets(unit, input))
    )

  private def collectPresets(
      unit: CompilationUnit,
      input: MaterialFlagPresetScanSpec
  ): Vector[ScannedMaterialFlagPresetRef] =
    val presetNames = unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(isPresetField)
      .flatMap(_.getVariables.asScala.toVector)
      .map(_.getNameAsString)
    val members = mutable.LinkedHashMap.from(
      presetNames.map(_ -> Vector.empty[ScalaSymbolPath])
    )

    unit
      .findAll(classOf[InitializerDeclaration])
      .asScala
      .toVector
      .filter(_.isStatic)
      .flatMap(_.getBody.getStatements.asScala.toVector)
      .foreach {
        case statement: ExpressionStmt =>
          statement.getExpression match
            case call: MethodCallExpr =>
              applyInitializerCall(call, members, input)
            case _ => ()
        case _ => ()
      }

    presetNames.map { name =>
      ScannedMaterialFlagPresetRef(
        name = name,
        path = ScalaSymbolPath.member(input.ownerFqcn, name),
        members = members(name)
      )
    }

  private def applyInitializerCall(
      call: MethodCallExpr,
      members: mutable.LinkedHashMap[String, Vector[ScalaSymbolPath]],
      input: MaterialFlagPresetScanSpec
  ): Unit =
    val target =
      if call.getScope.isPresent then memberName(call.getScope.get) else None

    target.filter(members.contains).foreach { preset =>
      call.getNameAsString match
        case "add" if call.getArguments.size == 1 =>
          flagPath(call.getArgument(0), input)
            .foreach(path => members.update(preset, members(preset) :+ path))
        case "addAll" if call.getArguments.size == 1 =>
          val appended = addAllMembers(call.getArgument(0), members, input)
          members.update(preset, members(preset) ++ appended)
        case _ => ()
    }

  private def addAllMembers(
      expression: Expression,
      members: mutable.LinkedHashMap[String, Vector[ScalaSymbolPath]],
      input: MaterialFlagPresetScanSpec
  ): Vector[ScalaSymbolPath] =
    expression match
      case call: MethodCallExpr
          if call.getNameAsString == "asList" ||
            call.getNameAsString == "of" =>
        call.getArguments.asScala.toVector.flatMap(flagPath(_, input))
      case _ =>
        memberName(expression)
          .flatMap(members.get)
          .getOrElse(Vector.empty)

  private def flagPath(
      expression: Expression,
      input: MaterialFlagPresetScanSpec
  ): Option[ScalaSymbolPath] =
    memberName(expression)
      .map(name => ScalaSymbolPath.member(input.flagOwnerFqcn, name))

  private def memberName(expression: Expression): Option[String] =
    expression match
      case name: NameExpr         => Some(name.getNameAsString)
      case field: FieldAccessExpr => Some(field.getNameAsString)
      case _                      => None

  private def isPresetField(field: FieldDeclaration): Boolean =
    val elementType = field.getElementType.asString.replace(" ", "")
    val isCollection =
      elementType == "List<MaterialFlag>" ||
        elementType == "Set<MaterialFlag>" ||
        elementType == "Collection<MaterialFlag>"

    field.isPublic &&
    field.isStatic &&
    field.isFinal &&
    isCollection &&
    !isDeprecated(field)

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

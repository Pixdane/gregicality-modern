package com.pixdane.gregicality.symbolgen

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.Expression
import com.github.javaparser.ast.expr.FieldAccessExpr
import com.github.javaparser.ast.expr.MethodCallExpr
import com.github.javaparser.ast.expr.NameExpr
import com.github.javaparser.ast.expr.ObjectCreationExpr
import com.github.javaparser.ast.expr.StringLiteralExpr

import scala.jdk.CollectionConverters.*

final case class StaticMemberSource(
    sourcePath: String,
    ownerFqcn: String,
    memberTypeSimpleName: String
)

final case class GtMaterialsSource(
    declarationPath: String,
    assignmentDir: String,
    ownerFqcn: String,
    namespace: String
)

object GtceuSourceScanners:
  def scanStaticMembers(input: StaticMemberSource)(
      archive: SourceArchive
  ): Vector[ScannedRef] =
    val unit = archive.parse(input.sourcePath)

    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(field =>
        field.isPublic &&
          field.isStatic &&
          field.isFinal &&
          field.getElementType.asString == input.memberTypeSimpleName
      )
      .flatMap(_.getVariables.asScala.toVector)
      .map { variable =>
        val name = variable.getNameAsString

        ScannedRef(
          name = name,
          id = None,
          path = ScalaPath.member(input.ownerFqcn, name)
        )
      }

  def scanGtMaterials(input: GtMaterialsSource)(
      archive: SourceArchive
  ): Vector[ScannedRef] =
    val declarationUnit = archive.parse(input.declarationPath)
    val declaredMaterialNames = scanDeclaredMaterialNames(declarationUnit)

    archive
      .parseUnder(input.assignmentDir)
      .flatMap { case (_, unit) =>
        scanMaterialAssignments(unit, input)
      }
      .filter(ref => declaredMaterialNames.contains(ref.name))
      .sortBy(_.name)

  private def scanDeclaredMaterialNames(unit: CompilationUnit): Set[String] =
    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(field =>
        field.isPublic &&
          field.isStatic &&
          field.getElementType.asString == "Material"
      )
      .flatMap(_.getVariables.asScala.toVector)
      .map(_.getNameAsString)
      .toSet

  private def scanMaterialAssignments(
      unit: CompilationUnit,
      input: GtMaterialsSource
  ): Vector[ScannedRef] =
    unit
      .findAll(classOf[AssignExpr])
      .asScala
      .toVector
      .flatMap { assignment =>
        for
          name <- assignedName(assignment)
          idPath <- extractGtceuMaterialId(assignment.getValue)
        yield ScannedRef(
          name = name,
          id = Some(ResourceId(input.namespace, idPath)),
          path = ScalaPath.member(input.ownerFqcn, name)
        )
      }

  private def assignedName(assignment: AssignExpr): Option[String] =
    assignment.getTarget match
      case name: NameExpr         => Some(name.getNameAsString)
      case field: FieldAccessExpr => Some(field.getNameAsString)
      case _                      => None

  private def extractGtceuMaterialId(expression: Expression): Option[String] =
    val createsMaterialBuilder =
      expression
        .findAll(classOf[ObjectCreationExpr])
        .asScala
        .exists(_.getType.asString == "Material.Builder")

    Option
      .when(createsMaterialBuilder) {
        expression
          .findAll(classOf[MethodCallExpr])
          .asScala
          .toVector
          .collectFirst {
            case call
                if call.getNameAsString == "id" &&
                  call.getScope.isPresent &&
                  call.getScope.get.toString == "GTCEu" &&
                  !call.getArguments.isEmpty =>
              call.getArgument(0)
          }
          .collect { case literal: StringLiteralExpr =>
            literal.asString
          }
      }
      .flatten

package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.model.*

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

final case class StaticFieldScanSpec(
    sourcePath: String,
    ownerFqcn: String,
    memberTypeSimpleName: String
)

final case class GtMaterialsScanSpec(
    declarationPath: String,
    assignmentDir: String,
    ownerFqcn: String,
    namespace: String
)

object GtceuSourceScanners:
  def scanStaticMembers(input: StaticFieldScanSpec)(
      archive: SourceArchive
  ): Vector[ScannedPathRef] =
    val unit = archive.parse(input.sourcePath)

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

  def scanGtMaterials(input: GtMaterialsScanSpec)(
      archive: SourceArchive
  ): Vector[ScannedMaterialRef] =
    val declarationUnit = archive.parse(input.declarationPath)
    val declaredMaterialNames = scanDeclaredMaterialNames(declarationUnit)

    val assignmentUnits = archive.parseUnder(input.assignmentDir).map(_._2)
    val builderAssignments = assignmentUnits
      .flatMap(unit => scanMaterialAssignments(unit, input))
      .filter(ref => declaredMaterialNames.contains(ref.name))
    val aliases = assignmentUnits
      .flatMap(unit => scanMaterialAliases(unit, input))
      .filter { case (name, _) => declaredMaterialNames.contains(name) }

    val duplicateNames = (builderAssignments.map(_.name) ++ aliases.map(_._1))
      .groupBy(identity)
      .collect { case (name, occurrences) if occurrences.sizeIs > 1 => name }
      .toVector
      .sorted

    if duplicateNames.nonEmpty then
      throw new IllegalArgumentException(
        s"duplicate GTCEu material assignments: ${duplicateNames.mkString(", ")}"
      )

    validateDuplicateMaterialIds(builderAssignments)

    val refsByName = scala.collection.mutable.Map.from(
      builderAssignments.map(ref => ref.name -> ref)
    )
    val aliasesByName = aliases.toMap

    def resolve(
        name: String,
        visiting: Set[String] = Set.empty
    ): Option[ScannedMaterialRef] =
      refsByName.get(name).orElse {
        Option
          .when(!visiting.contains(name)) {
            aliasesByName.get(name).flatMap { targetName =>
              resolve(targetName, visiting + name).map { targetRef =>
                val aliasRef = ScannedMaterialAliasRef(
                  name = name,
                  id = targetRef.id,
                  path = ScalaSymbolPath.member(input.ownerFqcn, name)
                )
                refsByName.update(name, aliasRef)
                aliasRef
              }
            }
          }
          .flatten
      }

    val declaredAssignments = declaredMaterialNames.toVector.flatMap(resolve(_))
    val assignedNames = declaredAssignments.iterator.map(_.name).toSet
    val missingNames = (declaredMaterialNames -- assignedNames).toVector.sorted

    if missingNames.nonEmpty then
      throw new IllegalArgumentException(
        s"declared GTCEu materials without a recognized builder or alias assignment: ${missingNames.mkString(", ")}"
      )

    declaredAssignments.sortBy(_.name)

  private def validateDuplicateMaterialIds(
      assignments: Vector[ScannedMaterialRef]
  ): Unit =
    val duplicateIds = assignments
      .map(ref => ref.id -> ref.name)
      .groupBy(_._1)
      .collect {
        case (id, refs) if refs.sizeIs > 1 =>
          id -> refs.map(_._2).sorted
      }
      .toVector
      .sortBy { case (id, _) => (id.namespace, id.path) }

    if duplicateIds.nonEmpty then
      val details = duplicateIds
        .map { case (id, names) =>
          s"${id.namespace}:${id.path} (${names.mkString(", ")})"
        }
        .mkString("; ")

      throw new IllegalArgumentException(
        s"duplicate GTCEu material registry ids: $details"
      )

  private def scanMaterialAliases(
      unit: CompilationUnit,
      input: GtMaterialsScanSpec
  ): Vector[(String, String)] =
    unit
      .findAll(classOf[AssignExpr])
      .asScala
      .toVector
      .flatMap { assignment =>
        for
          name <- assignedName(assignment, input.ownerFqcn)
          targetName <- materialReferenceName(
            assignment.getValue,
            input.ownerFqcn
          )
        yield name -> targetName
      }

  private def scanDeclaredMaterialNames(unit: CompilationUnit): Set[String] =
    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(field =>
        field.isPublic &&
          field.isStatic &&
          !isDeprecated(field)
      )
      .flatMap(_.getVariables.asScala.toVector)
      .filter(_.getType.asString == "Material")
      .map(_.getNameAsString)
      .toSet

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

  private def scanMaterialAssignments(
      unit: CompilationUnit,
      input: GtMaterialsScanSpec
  ): Vector[ScannedMaterialRef] =
    unit
      .findAll(classOf[AssignExpr])
      .asScala
      .toVector
      .flatMap { assignment =>
        for
          name <- assignedName(assignment, input.ownerFqcn)
          idPath <- extractGtceuMaterialId(assignment.getValue)
        yield ScannedRegisteredMaterialRef(
          name = name,
          id = ResourceId(input.namespace, idPath),
          path = ScalaSymbolPath.member(input.ownerFqcn, name)
        )
      }

  private def assignedName(
      assignment: AssignExpr,
      ownerFqcn: String
  ): Option[String] =
    materialReferenceName(assignment.getTarget, ownerFqcn)

  private def materialReferenceName(
      expression: Expression,
      ownerFqcn: String
  ): Option[String] =
    expression match
      case name: NameExpr => Some(name.getNameAsString)
      case field: FieldAccessExpr
          if isMaterialOwner(field.getScope, ownerFqcn) =>
        Some(field.getNameAsString)
      case _ => None

  private def isMaterialOwner(
      expression: Expression,
      ownerFqcn: String
  ): Boolean =
    val owner = expression.toString
    owner == ownerFqcn || owner == ownerFqcn.split('.').last

  private def extractGtceuMaterialId(expression: Expression): Option[String] =
    fluentRoot(expression) match
      case builder: ObjectCreationExpr
          if builder.getType.asString == "Material.Builder" &&
            builder.getArguments.size == 1 =>
        builder.getArgument(0) match
          case call: MethodCallExpr
              if call.getNameAsString == "id" &&
                call.getScope.isPresent &&
                call.getScope.get.toString == "GTCEu" &&
                call.getArguments.size == 1 =>
            call.getArgument(0) match
              case literal: StringLiteralExpr => Some(literal.asString)
              case _                          => None
          case _ => None
      case _ => None

  private def fluentRoot(expression: Expression): Expression =
    expression match
      case call: MethodCallExpr if call.getScope.isPresent =>
        fluentRoot(call.getScope.get)
      case root => root

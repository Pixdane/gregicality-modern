package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.model.*

import com.github.javaparser.ast.Node
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
  private val GtceuOwnerFqcn = "com.gregtechceu.gtceu.GTCEu"

  private final case class SourceSite(path: String, line: Option[Int]):
    def render: String =
      line.fold(path)(value => s"$path:$value")

    def sortKey: (String, Int) =
      path -> line.getOrElse(Int.MaxValue)

  private final case class LocatedMaterialAssignment(
      ref: ScannedRegisteredMaterialRef,
      site: SourceSite
  )

  private final case class LocatedMaterialAlias(
      name: String,
      targetName: String,
      site: SourceSite
  )

  private final case class RejectedMaterialAssignment(
      name: String,
      reason: String,
      site: SourceSite
  )

  private enum MaterialAssignmentTarget:
    case Accepted(name: String)
    case ForeignOwner(name: String)
    case Unrelated

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
    val declarations =
      scanDeclaredMaterials(input.declarationPath, declarationUnit)
    val declaredMaterialNames = declarations.keySet

    val assignmentUnits = archive.parseUnder(input.assignmentDir)
    val builderAssignments = assignmentUnits
      .flatMap { case (sourcePath, unit) =>
        scanMaterialAssignments(sourcePath, unit, input)
      }
      .filter(assignment => declaredMaterialNames.contains(assignment.ref.name))
    val aliases = assignmentUnits
      .flatMap { case (sourcePath, unit) =>
        scanMaterialAliases(sourcePath, unit, input)
      }
      .filter(alias => declaredMaterialNames.contains(alias.name))
    val rejectedAssignments = assignmentUnits.flatMap {
      case (sourcePath, unit) =>
        scanRejectedMaterialAssignments(
          sourcePath,
          unit,
          input,
          declaredMaterialNames
        )
    }

    val refsByName: scala.collection.mutable.Map[String, ScannedMaterialRef] =
      scala.collection.mutable.Map.from(
        builderAssignments.map(assignment =>
          assignment.ref.name -> assignment.ref
        )
      )
    val aliasesByName =
      aliases.map(alias => alias.name -> alias.targetName).toMap

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
    val rejectedNames = rejectedAssignments.iterator.map(_.name).toSet
    val missingNames =
      (declaredMaterialNames -- assignedNames -- rejectedNames).toVector.sorted
    val diagnostics = Vector(
      duplicateAssignmentDiagnostic(builderAssignments, aliases),
      duplicateMaterialIdDiagnostic(builderAssignments),
      rejectedAssignmentDiagnostic(rejectedAssignments),
      missingAssignmentDiagnostic(missingNames, declarations)
    ).flatten

    if diagnostics.nonEmpty then
      throw new IllegalArgumentException(
        "GTCEu material scan failed:\n" +
          diagnostics.map(message => s"- $message").mkString("\n")
      )

    declaredAssignments.sortBy(_.name)

  private def duplicateAssignmentDiagnostic(
      assignments: Vector[LocatedMaterialAssignment],
      aliases: Vector[LocatedMaterialAlias]
  ): Option[String] =
    val occurrences =
      assignments.map(assignment => assignment.ref.name -> assignment.site) ++
        aliases.map(alias => alias.name -> alias.site)
    val duplicates = occurrences
      .groupBy(_._1)
      .collect {
        case (name, values) if values.sizeIs > 1 =>
          name -> values.map(_._2).sortBy(_.sortKey)
      }
      .toVector
      .sortBy(_._1)

    Option.when(duplicates.nonEmpty) {
      val details = duplicates
        .map { case (name, sites) =>
          s"$name (${sites.map(_.render).mkString(", ")})"
        }
        .mkString("; ")

      s"duplicate GTCEu material assignments: $details"
    }

  private def duplicateMaterialIdDiagnostic(
      assignments: Vector[LocatedMaterialAssignment]
  ): Option[String] =
    val duplicateIds = assignments
      .groupBy(_.ref.id)
      .collect {
        case (id, values) if values.sizeIs > 1 =>
          id -> values.sortBy(assignment =>
            assignment.ref.name -> assignment.site.sortKey
          )
      }
      .toVector
      .sortBy { case (id, _) => (id.namespace, id.path) }

    Option.when(duplicateIds.nonEmpty) {
      val details = duplicateIds
        .map { case (id, values) =>
          val refs = values
            .map(assignment =>
              s"${assignment.ref.name} at ${assignment.site.render}"
            )
            .mkString(", ")
          s"${id.namespace}:${id.path} ($refs)"
        }
        .mkString("; ")

      s"duplicate GTCEu material registry ids: $details"
    }

  private def rejectedAssignmentDiagnostic(
      assignments: Vector[RejectedMaterialAssignment]
  ): Option[String] =
    Option.when(assignments.nonEmpty) {
      val details = assignments
        .sortBy(assignment => assignment.name -> assignment.site.sortKey)
        .map(assignment =>
          s"${assignment.name} at ${assignment.site.render} (${assignment.reason})"
        )
        .mkString("; ")

      s"unsupported GTCEu material assignments: $details"
    }

  private def missingAssignmentDiagnostic(
      missingNames: Vector[String],
      declarations: Map[String, SourceSite]
  ): Option[String] =
    Option.when(missingNames.nonEmpty) {
      val details = missingNames
        .map(name => s"$name (declared at ${declarations(name).render})")
        .mkString(", ")

      s"declared GTCEu materials without a recognized builder or alias assignment: $details"
    }

  private def scanMaterialAliases(
      sourcePath: String,
      unit: CompilationUnit,
      input: GtMaterialsScanSpec
  ): Vector[LocatedMaterialAlias] =
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
        yield LocatedMaterialAlias(
          name = name,
          targetName = targetName,
          site = sourceSite(sourcePath, assignment)
        )
      }

  private def scanRejectedMaterialAssignments(
      sourcePath: String,
      unit: CompilationUnit,
      input: GtMaterialsScanSpec,
      declaredMaterialNames: Set[String]
  ): Vector[RejectedMaterialAssignment] =
    unit
      .findAll(classOf[AssignExpr])
      .asScala
      .toVector
      .flatMap { assignment =>
        val target =
          materialAssignmentTarget(assignment.getTarget, input.ownerFqcn)
        val value = assignment.getValue

        target match
          case MaterialAssignmentTarget.Accepted(name)
              if declaredMaterialNames.contains(name) &&
                extractGtceuMaterialId(value).isEmpty &&
                materialReferenceName(value, input.ownerFqcn).isEmpty =>
            Some(
              RejectedMaterialAssignment(
                name = name,
                reason = rejectedValueReason(value, input.ownerFqcn),
                site = sourceSite(sourcePath, assignment)
              )
            )
          case MaterialAssignmentTarget.ForeignOwner(name)
              if declaredMaterialNames.contains(name) &&
                looksLikeMaterialBuilder(value) =>
            Some(
              RejectedMaterialAssignment(
                name = name,
                reason =
                  s"assignment target is not owned by ${input.ownerFqcn}",
                site = sourceSite(sourcePath, assignment)
              )
            )
          case _ =>
            None
      }

  private def scanDeclaredMaterials(
      sourcePath: String,
      unit: CompilationUnit
  ): Map[String, SourceSite] =
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
      .map(variable =>
        variable.getNameAsString -> sourceSite(sourcePath, variable)
      )
      .toMap

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

  private def sourceSite(sourcePath: String, node: Node): SourceSite =
    val begin = node.getBegin
    SourceSite(
      path = sourcePath,
      line = Option.when(begin.isPresent)(begin.get.line)
    )

  private def scanMaterialAssignments(
      sourcePath: String,
      unit: CompilationUnit,
      input: GtMaterialsScanSpec
  ): Vector[LocatedMaterialAssignment] =
    unit
      .findAll(classOf[AssignExpr])
      .asScala
      .toVector
      .flatMap { assignment =>
        for
          name <- assignedName(assignment, input.ownerFqcn)
          idPath <- extractGtceuMaterialId(assignment.getValue)
        yield LocatedMaterialAssignment(
          ref = ScannedRegisteredMaterialRef(
            name = name,
            id = ResourceId(input.namespace, idPath),
            path = ScalaSymbolPath.member(input.ownerFqcn, name)
          ),
          site = sourceSite(sourcePath, assignment)
        )
      }

  private def assignedName(
      assignment: AssignExpr,
      ownerFqcn: String
  ): Option[String] =
    materialAssignmentTarget(assignment.getTarget, ownerFqcn) match
      case MaterialAssignmentTarget.Accepted(name) => Some(name)
      case _                                       => None

  private def materialAssignmentTarget(
      expression: Expression,
      ownerFqcn: String
  ): MaterialAssignmentTarget =
    expression match
      case name: NameExpr =>
        MaterialAssignmentTarget.Accepted(name.getNameAsString)
      case field: FieldAccessExpr
          if isMaterialOwner(field.getScope, ownerFqcn) =>
        MaterialAssignmentTarget.Accepted(field.getNameAsString)
      case field: FieldAccessExpr =>
        MaterialAssignmentTarget.ForeignOwner(field.getNameAsString)
      case _ =>
        MaterialAssignmentTarget.Unrelated

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

  private def rejectedValueReason(
      expression: Expression,
      ownerFqcn: String
  ): String =
    if looksLikeMaterialBuilder(expression) then
      """builder constructor must be new Material.Builder(GTCEu.id("literal"))"""
    else
      expression match
        case _: NameExpr | _: FieldAccessExpr =>
          s"alias target is not a member of $ownerFqcn"
        case _ =>
          "unsupported assignment value"

  private def looksLikeMaterialBuilder(expression: Expression): Boolean =
    fluentRoot(expression) match
      case creation: ObjectCreationExpr =>
        creation.getType.asString == "Material.Builder"
      case _ =>
        false

  private def isMaterialOwner(
      expression: Expression,
      ownerFqcn: String
  ): Boolean =
    isOwnerReference(expression, ownerFqcn)

  private def isOwnerReference(
      expression: Expression,
      ownerFqcn: String
  ): Boolean =
    val ownerParts = ownerFqcn.split('.').toVector
    qualifiedNameParts(expression).exists(parts =>
      parts == ownerParts || parts == Vector(ownerParts.last)
    )

  private def qualifiedNameParts(
      expression: Expression
  ): Option[Vector[String]] =
    expression match
      case name: NameExpr =>
        Some(Vector(name.getNameAsString))
      case field: FieldAccessExpr =>
        qualifiedNameParts(field.getScope).map(_ :+ field.getNameAsString)
      case _ =>
        None

  private def extractGtceuMaterialId(expression: Expression): Option[String] =
    fluentRoot(expression) match
      case builder: ObjectCreationExpr
          if builder.getType.asString == "Material.Builder" &&
            builder.getArguments.size == 1 =>
        builder.getArgument(0) match
          case call: MethodCallExpr
              if call.getNameAsString == "id" &&
                call.getScope.isPresent &&
                isOwnerReference(call.getScope.get, GtceuOwnerFqcn) &&
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

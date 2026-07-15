package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.materials

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.AssignExpr
import com.github.javaparser.ast.expr.{
  Expression,
  FieldAccessExpr,
  MethodCallExpr,
  NameExpr,
  ObjectCreationExpr,
  StringLiteralExpr
}
import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtMaterialsScanSpec,
  SourceSite
}
import com.pixdane.gregicality.symbolgen.framework.ScannedMaterialRef

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*

/** Classification of a material assignment's target expression. */
enum MaterialAssignmentTarget:
  case Accepted(name: String)
  case ForeignOwner(name: String)
  case Unrelated

/** Scans GTCEu material builder assignments.
  *
  * `scanAssignments` collects assignments recognized as belonging to the
  * scanned owner, while `scanRejected` collects declared-material assignments
  * whose value was not a supported builder (or targeted a foreign owner). Both
  * rely on the file-local `MaterialExpressionParsers` helper to inspect
  * JavaParser expressions.
  */
object MaterialAssignmentScanner:
  def scanAssignments(
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
          name <- MaterialExpressionParsers.assignedName(
            assignment,
            input.ownerFqcn
          )
          idPath <- MaterialExpressionParsers.extractGtceuMaterialId(
            assignment.getValue,
            input.idFactoryFqcn
          )
        yield LocatedMaterialAssignment(
          ref = ScannedMaterialRef(
            name = name,
            id = ResourceId(input.namespace, idPath),
            path = ScalaSymbolPath.member(input.ownerFqcn, name)
          ),
          site = SourceSite.fromNode(sourcePath, assignment)
        )
      }

  def scanRejected(
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
          MaterialExpressionParsers.materialAssignmentTarget(
            assignment.getTarget,
            input.ownerFqcn
          )
        val value = assignment.getValue

        target match
          case MaterialAssignmentTarget.Accepted(name)
              if declaredMaterialNames.contains(name) &&
                MaterialExpressionParsers
                  .extractGtceuMaterialId(value, input.idFactoryFqcn)
                  .isEmpty =>
            Some(
              RejectedMaterialAssignment(
                name = name,
                reason = MaterialExpressionParsers
                  .rejectedValueReason(value),
                site = SourceSite.fromNode(sourcePath, assignment)
              )
            )
          case MaterialAssignmentTarget.ForeignOwner(name)
              if declaredMaterialNames.contains(name) &&
                MaterialExpressionParsers.looksLikeMaterialBuilder(value) =>
            Some(
              RejectedMaterialAssignment(
                name = name,
                reason =
                  s"assignment target is not owned by ${input.ownerFqcn}",
                site = SourceSite.fromNode(sourcePath, assignment)
              )
            )
          case _ =>
            None
      }
end MaterialAssignmentScanner

/** File-local helpers that interpret material builder expressions.
  *
  * Kept as a private object so that the scanner's public surface stays narrow
  * while the parsing logic remains grouped and named for its responsibility.
  */
private object MaterialExpressionParsers:

  def assignedName(
      assignment: AssignExpr,
      ownerFqcn: String
  ): Option[String] =
    materialAssignmentTarget(assignment.getTarget, ownerFqcn) match
      case MaterialAssignmentTarget.Accepted(name) => Some(name)
      case _                                       => None

  def materialAssignmentTarget(
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

  def rejectedValueReason(expression: Expression): String =
    if looksLikeMaterialBuilder(expression) then
      """builder constructor must be new Material.Builder(GTCEu.id("literal"))"""
    else "unsupported assignment value"

  def looksLikeMaterialBuilder(expression: Expression): Boolean =
    fluentRoot(expression) match
      case creation: ObjectCreationExpr =>
        creation.getType.asString == "Material.Builder"
      case _ =>
        false

  def extractGtceuMaterialId(
      expression: Expression,
      idFactoryFqcn: String
  ): Option[String] =
    fluentRoot(expression) match
      case builder: ObjectCreationExpr
          if builder.getType.asString == "Material.Builder" &&
            builder.getArguments.size == 1 =>
        builder.getArgument(0) match
          case call: MethodCallExpr
              if call.getNameAsString == "id" &&
                call.getScope.isPresent &&
                isOwnerReference(call.getScope.get, idFactoryFqcn) &&
                call.getArguments.size == 1 =>
            call.getArgument(0) match
              case literal: StringLiteralExpr => Some(literal.asString)
              case _                          => None
          case _ =>
            None
      case _ =>
        None

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

  @tailrec
  private def fluentRoot(expression: Expression): Expression =
    expression match
      case call: MethodCallExpr if call.getScope.isPresent =>
        fluentRoot(call.getScope.get)
      case root => root

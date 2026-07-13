package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.github.javaparser.ast.expr.{
  AssignExpr,
  Expression,
  FieldAccessExpr,
  MethodCallExpr,
  NameExpr,
  ObjectCreationExpr,
  StringLiteralExpr
}

import scala.annotation.tailrec

object MaterialExpressionParsers:

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
          case _ => None
      case _ => None

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

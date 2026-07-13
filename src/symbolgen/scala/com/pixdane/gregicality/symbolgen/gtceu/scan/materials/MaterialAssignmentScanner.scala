package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.AssignExpr
import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.gtceu.GtMaterialsScanSpec
import com.pixdane.gregicality.symbolgen.gtceu.scan.SourceSite
import com.pixdane.gregicality.symbolgen.scan.ScannedRegisteredMaterialRef

import scala.jdk.CollectionConverters.*

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
          ref = ScannedRegisteredMaterialRef(
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
                  .isEmpty &&
                MaterialExpressionParsers
                  .materialReferenceName(value, input.ownerFqcn)
                  .isEmpty =>
            Some(
              RejectedMaterialAssignment(
                name = name,
                reason = MaterialExpressionParsers
                  .rejectedValueReason(value, input.ownerFqcn),
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

package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.expr.AssignExpr
import com.pixdane.gregicality.symbolgen.gtceu.GtMaterialsScanSpec
import com.pixdane.gregicality.symbolgen.gtceu.scan.SourceSite

import scala.jdk.CollectionConverters.*

object MaterialAliasScanner:
  def scan(
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
          name <- MaterialExpressionParsers.assignedName(
            assignment,
            input.ownerFqcn
          )
          targetName <- MaterialExpressionParsers.materialReferenceName(
            assignment.getValue,
            input.ownerFqcn
          )
        yield LocatedMaterialAlias(
          name = name,
          targetName = targetName,
          site = SourceSite.fromNode(sourcePath, assignment)
        )
      }
end MaterialAliasScanner

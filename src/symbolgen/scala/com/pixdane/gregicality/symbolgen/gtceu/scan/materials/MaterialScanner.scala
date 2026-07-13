package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import cats.data.{Ior, NonEmptyChain}
import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.gtceu.GtMaterialsScanSpec
import com.pixdane.gregicality.symbolgen.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult
}
import com.pixdane.gregicality.symbolgen.scan.ScannedMaterialRef

object MaterialScanner:
  def scan(input: GtMaterialsScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[MaterialScanInput] =
    val declarationUnit = archive.parse(input.declarationPath)
    val declarations =
      MaterialDeclarationScanner.scan(input.declarationPath, declarationUnit)
    val declaredMaterialNames = declarations.keySet

    val assignmentUnits = archive.parseUnder(input.assignmentDir)
    val builderAssignments = assignmentUnits
      .flatMap { case (sourcePath, unit) =>
        MaterialAssignmentScanner.scanAssignments(sourcePath, unit, input)
      }
      .filter(assignment => declaredMaterialNames.contains(assignment.ref.name))
    val rejectedAssignments = assignmentUnits.flatMap {
      case (sourcePath, unit) =>
        MaterialAssignmentScanner.scanRejected(
          sourcePath,
          unit,
          input,
          declaredMaterialNames
        )
    }

    Ior.right(
      MaterialScanInput(
        declarations = declarations,
        assignments = builderAssignments,
        rejectedAssignments = rejectedAssignments
      )
    )

  def preprocess(
      input: MaterialScanInput
  ): GtceuScanResult[Vector[ScannedMaterialRef]] =
    val declaredMaterialNames = input.declarations.keySet
    val refsByName =
      input.assignments
        .map(assignment => assignment.ref.name -> assignment.ref)
        .toMap

    val assignedNames = refsByName.keySet
    val rejectedNames = input.rejectedAssignments.iterator.map(_.name).toSet
    val missingNames =
      (declaredMaterialNames -- assignedNames -- rejectedNames).toVector.sorted

    val duplicateDiags =
      MaterialDiagnostics.duplicateAssignments(input.assignments)
    val duplicateIdDiags =
      MaterialDiagnostics.duplicateMaterialIds(input.assignments)
    val rejectedDiags =
      MaterialDiagnostics.rejectedAssignments(input.rejectedAssignments)
    val missingDiags =
      MaterialDiagnostics.missingAssignments(missingNames, input.declarations)
    val declaredAssignments =
      declaredMaterialNames.toVector
        .flatMap(refsByName.get)
        .sortBy(_.name)

    val orderedDiags: Vector[GtceuScanDiagnostic] =
      duplicateDiags ++ duplicateIdDiags ++ rejectedDiags ++ missingDiags
    NonEmptyChain.fromSeq(orderedDiags) match
      case None =>
        Ior.right(declaredAssignments)
      case Some(nec) =>
        Ior.both(nec, declaredAssignments)
end MaterialScanner

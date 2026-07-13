package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.gtceu.GtMaterialsScanSpec
import com.pixdane.gregicality.symbolgen.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult,
  GtceuScanResultOps
}
import com.pixdane.gregicality.symbolgen.scan.ScannedMaterialRef

object MaterialScanner:
  def scan(input: GtMaterialsScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[Vector[ScannedMaterialRef]] =
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
    val aliases = assignmentUnits
      .flatMap { case (sourcePath, unit) =>
        MaterialAliasScanner.scan(sourcePath, unit, input)
      }
      .filter(alias => declaredMaterialNames.contains(alias.name))
    val rejectedAssignments = assignmentUnits.flatMap {
      case (sourcePath, unit) =>
        MaterialAssignmentScanner.scanRejected(
          sourcePath,
          unit,
          input,
          declaredMaterialNames
        )
    }

    val refsByName = builderAssignments
      .map(assignment => assignment.ref.name -> assignment.ref)
      .toMap

    val aliasResolution =
      MaterialAliasResolver.resolve(aliases, refsByName, input.ownerFqcn)

    val resolvedRefByName: Map[String, ScannedMaterialRef] =
      refsByName ++ aliasResolution.refs.map(ref => ref.name -> ref)

    val assignedNames = resolvedRefByName.keySet
    val rejectedNames = rejectedAssignments.iterator.map(_.name).toSet
    val aliasDiagnosticNames =
      aliasResolution.cycles.iterator.flatMap(_.names).toSet ++
        aliasResolution.unresolved.iterator.map(_.name).toSet
    val missingNames =
      (declaredMaterialNames -- assignedNames -- rejectedNames -- aliasDiagnosticNames).toVector.sorted

    val duplicateDiags =
      MaterialDiagnostics.duplicateAssignments(builderAssignments, aliases)
    val duplicateIdDiags =
      MaterialDiagnostics.duplicateMaterialIds(builderAssignments)
    val rejectedDiags =
      MaterialDiagnostics.rejectedAssignments(rejectedAssignments)
    val missingDiags =
      MaterialDiagnostics.missingAssignments(missingNames, declarations)
    val declaredAssignments =
      declaredMaterialNames.toVector
        .flatMap(resolvedRefByName.get)
        .sortBy(_.name)

    GtceuScanDiagnostic.fromCategories(
      duplicates = duplicateDiags,
      duplicateIds = duplicateIdDiags,
      rejected = rejectedDiags,
      missing = missingDiags,
      aliasCycles = aliasResolution.cycles,
      unresolvedAliases = aliasResolution.unresolved
    ) match
      case None =>
        GtceuScanResultOps.clean(declaredAssignments)
      case Some(nec) =>
        GtceuScanResultOps.withDiagnostics(nec, declaredAssignments)
end MaterialScanner

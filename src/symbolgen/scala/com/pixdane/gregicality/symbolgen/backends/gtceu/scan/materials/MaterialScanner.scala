package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.materials

import cats.data.{Ior, NonEmptyChain}
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanDiagnostic,
  GtMaterialsScanSpec,
  GtceuScanResult,
  SourceSite
}
import com.pixdane.gregicality.symbolgen.framework.{
  ScannedMaterialRef,
  SourceArchive
}

/** Input to the material preprocess stage: the declared materials, the
  * recognized builder assignments, and the assignments rejected because their
  * value did not look like a supported builder.
  */
final case class MaterialScanInput(
    declarations: Map[String, SourceSite],
    assignments: Vector[LocatedMaterialAssignment],
    rejectedAssignments: Vector[RejectedMaterialAssignment]
)

/** A builder assignment recognized as belonging to the scanned owner class. */
final case class LocatedMaterialAssignment(
    ref: ScannedMaterialRef,
    site: SourceSite
)

/** A declared-material assignment whose value was not a supported builder, to
  * be reported as a rejection rather than a missing assignment.
  */
final case class RejectedMaterialAssignment(
    name: String,
    reason: String,
    site: SourceSite
)

/** Scans GTCEu material declarations and builder assignments.
  *
  * The scan stage reads the `GTMaterials` declaration file plus every file
  * under the materials assignment directory and collects declarations, accepted
  * assignments, and rejected assignments into a [[MaterialScanInput]]. The
  * preprocess stage then turns that input into the final ordered set of
  * [[ScannedMaterialRef]]s, emitting diagnostics for duplicate assignments,
  * duplicate ids, rejected builders, and declared materials with no assignment.
  */
object MaterialScanner:
  def scan(input: GtMaterialsScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[MaterialScanInput] =
    GtceuScanDiagnostic
      .fromArchive(archive.parse(input.declarationPath))
      .flatMap { declarationUnit =>
        val declarations =
          MaterialDeclarationScanner.scan(
            input.declarationPath,
            declarationUnit
          )
        val declaredMaterialNames = declarations.keySet

        val (assignmentUnits, parseErrors) =
          archive.parseUnder(input.assignmentDir)
        val builderAssignments = assignmentUnits
          .flatMap { case (sourcePath, unit) =>
            MaterialAssignmentScanner.scanAssignments(
              sourcePath,
              unit,
              input
            )
          }
          .filter(assignment =>
            declaredMaterialNames.contains(assignment.ref.name)
          )
        val rejectedAssignments = assignmentUnits.flatMap {
          case (sourcePath, unit) =>
            MaterialAssignmentScanner.scanRejected(
              sourcePath,
              unit,
              input,
              declaredMaterialNames
            )
        }

        GtceuScanDiagnostic.fromParsedUnder(
          MaterialScanInput(
            declarations = declarations,
            assignments = builderAssignments,
            rejectedAssignments = rejectedAssignments
          ),
          parseErrors
        )
      }

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
      MaterialValidation.duplicateAssignments(input.assignments)
    val duplicateIdDiags =
      MaterialValidation.duplicateMaterialIds(input.assignments)
    val rejectedDiags =
      MaterialValidation.rejectedAssignments(input.rejectedAssignments)
    val missingDiags =
      MaterialValidation.missingAssignments(missingNames, input.declarations)
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

package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.pixdane.gregicality.symbolgen.gtceu.scan.{
  GtceuScanDiagnostic,
  MaterialIdOccurrence,
  SourceSite
}

object MaterialDiagnostics:
  def duplicateAssignments(
      assignments: Vector[LocatedMaterialAssignment],
      aliases: Vector[LocatedMaterialAlias]
  ): Vector[GtceuScanDiagnostic.DuplicateAssignment] =
    val occurrences =
      assignments.map(assignment => assignment.ref.name -> assignment.site) ++
        aliases.map(alias => alias.name -> alias.site)
    occurrences
      .groupBy(_._1)
      .collect {
        case (name, values) if values.sizeIs > 1 =>
          name -> values.map(_._2).sortBy(_.sortKey)
      }
      .toVector
      .sortBy(_._1)
      .map { case (name, sites) =>
        GtceuScanDiagnostic.DuplicateAssignment(name = name, sites = sites)
      }

  def duplicateMaterialIds(
      assignments: Vector[LocatedMaterialAssignment]
  ): Vector[GtceuScanDiagnostic.DuplicateMaterialId] =
    assignments
      .groupBy(_.ref.id)
      .collect {
        case (id, values) if values.sizeIs > 1 =>
          id -> values.sortBy(assignment =>
            assignment.ref.name -> assignment.site.sortKey
          )
      }
      .toVector
      .sortBy { case (id, _) => (id.namespace, id.path) }
      .map { case (id, values) =>
        val occurrences = values.map(assignment =>
          MaterialIdOccurrence(
            name = assignment.ref.name,
            site = assignment.site
          )
        )
        GtceuScanDiagnostic.DuplicateMaterialId(
          id = s"${id.namespace}:${id.path}",
          refs = occurrences
        )
      }

  def rejectedAssignments(
      assignments: Vector[RejectedMaterialAssignment]
  ): Vector[GtceuScanDiagnostic.RejectedAssignment] =
    assignments
      .sortBy(assignment => assignment.name -> assignment.site.sortKey)
      .map(assignment =>
        GtceuScanDiagnostic.RejectedAssignment(
          name = assignment.name,
          reason = assignment.reason,
          site = assignment.site
        )
      )

  def missingAssignments(
      missingNames: Vector[String],
      declarations: Map[String, SourceSite]
  ): Vector[GtceuScanDiagnostic.MissingAssignment] =
    missingNames.map(name =>
      GtceuScanDiagnostic.MissingAssignment(
        name = name,
        declarationSite = declarations(name)
      )
    )

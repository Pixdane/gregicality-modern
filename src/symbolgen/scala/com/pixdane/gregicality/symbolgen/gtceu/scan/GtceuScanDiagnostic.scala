package com.pixdane.gregicality.symbolgen.gtceu.scan

import cats.data.IorNec

type GtceuScanResult[A] = IorNec[GtceuScanDiagnostic, A]

final case class MaterialIdOccurrence(name: String, site: SourceSite)

enum GtceuScanDiagnostic:
  case DuplicateAssignment(name: String, sites: Vector[SourceSite])
  case DuplicateMaterialId(id: String, refs: Vector[MaterialIdOccurrence])
  case RejectedAssignment(name: String, reason: String, site: SourceSite)
  case MissingAssignment(name: String, declarationSite: SourceSite)

  def render: String = this match
    case DuplicateAssignment(name, sites) =>
      s"duplicate GTCEu material assignments: $name (${sites.map(_.render).mkString(", ")})"
    case DuplicateMaterialId(id, refs) =>
      val detail = refs
        .map(occ => s"${occ.name} at ${occ.site.render}")
        .mkString(", ")
      s"duplicate GTCEu material registry ids: $id ($detail)"
    case RejectedAssignment(name, reason, site) =>
      s"unsupported GTCEu material assignments: $name at ${site.render} ($reason)"
    case MissingAssignment(name, declarationSite) =>
      s"declared GTCEu materials without a recognized builder assignment: " +
        s"$name (declared at ${declarationSite.render})"

package com.pixdane.gregicality.symbolgen.gtceu.scan

import cats.data.{Ior, IorNec, NonEmptyChain}

type GtceuScanResult[A] = IorNec[GtceuScanDiagnostic, A]

enum GtceuScanDiagnostic:
  case DuplicateAssignment(name: String, sites: Vector[SourceSite])
  case DuplicateMaterialId(id: String, refs: Vector[MaterialIdOccurrence])
  case RejectedAssignment(name: String, reason: String, site: SourceSite)
  case MissingAssignment(name: String, declarationSite: SourceSite)
  case AliasCycle(names: Vector[String])
  case UnresolvedAlias(name: String, targetName: String, site: SourceSite)

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
      s"declared GTCEu materials without a recognized builder or alias assignment: " +
        s"$name (declared at ${declarationSite.render})"
    case AliasCycle(names) =>
      s"circular GTCEu material aliases: ${names.mkString(" -> ")}"
    case UnresolvedAlias(name, targetName, site) =>
      s"unresolved GTCEu material alias: $name -> $targetName at ${site.render}"

object GtceuScanDiagnostic:
  def fromCategories(
      duplicates: Vector[DuplicateAssignment],
      duplicateIds: Vector[DuplicateMaterialId],
      rejected: Vector[RejectedAssignment],
      missing: Vector[MissingAssignment],
      aliasCycles: Vector[AliasCycle],
      unresolvedAliases: Vector[UnresolvedAlias]
  ): Option[NonEmptyChain[GtceuScanDiagnostic]] =
    val ordered: Vector[GtceuScanDiagnostic] =
      duplicates ++ duplicateIds ++ rejected ++ missing ++ aliasCycles ++ unresolvedAliases
    NonEmptyChain.fromSeq(ordered)

object GtceuScanResultOps:
  def clean[A](value: A): GtceuScanResult[A] =
    Ior.right(value)

  def withDiagnostics[A](
      diagnostics: NonEmptyChain[GtceuScanDiagnostic],
      value: A
  ): GtceuScanResult[A] =
    Ior.both(diagnostics, value)

final case class MaterialIdOccurrence(name: String, site: SourceSite)

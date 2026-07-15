package com.pixdane.gregicality.symbolgen.backends.gtceu.scan

import cats.data.{Ior, IorNec, NonEmptyChain}
import com.pixdane.gregicality.symbolgen.framework.{
  Diagnostic,
  SourceArchiveError
}

/** The result type of a GTCEu scan or preprocess stage: a value paired with
  * zero or more diagnostics via `IorNec`.
  */
type GtceuScanResult[A] = IorNec[GtceuScanDiagnostic, A]

/** A material or field referenced from a GTCEu source location. */
final case class IdOccurrence(name: String, site: SourceSite)

/** Diagnostics specific to scanning GTCEu sources.
  *
  * Each variant names a distinct class of problem the GTCEu scanners can
  * detect: duplicate assignments, duplicate material registry ids, rejected
  * builder shapes, declared materials missing an assignment, or archive
  * read/parse failures.
  */
enum GtceuScanDiagnostic extends Diagnostic:
  case DuplicateAssignment(name: String, sites: Vector[SourceSite])
  case DuplicateMaterialId(id: String, refs: Vector[IdOccurrence])
  case RejectedAssignment(name: String, reason: String, site: SourceSite)
  case MissingAssignment(name: String, declarationSite: SourceSite)
  case MissingSource(path: String)
  case SourceParseError(path: String, message: String)

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
    case MissingSource(path) =>
      s"missing GTCEu source file: $path"
    case SourceParseError(path, message) =>
      s"failed to parse GTCEu source: $path ($message)"

object GtceuScanDiagnostic:
  def fromArchiveError(error: SourceArchiveError): GtceuScanDiagnostic =
    error match
      case SourceArchiveError.Missing(path) =>
        GtceuScanDiagnostic.MissingSource(path)
      case SourceArchiveError.ParseFailed(path, message) =>
        GtceuScanDiagnostic.SourceParseError(path, message)

  def fromArchive[A](
      result: Either[SourceArchiveError, A]
  ): GtceuScanResult[A] =
    Ior.fromEither(
      result.left.map(error => NonEmptyChain.one(fromArchiveError(error)))
    )

  def fromParsedUnder[A](
      value: A,
      errors: Vector[SourceArchiveError]
  ): GtceuScanResult[A] =
    NonEmptyChain.fromSeq(errors.map(fromArchiveError)) match
      case None        => Ior.right(value)
      case Some(chain) => Ior.both(chain, value)

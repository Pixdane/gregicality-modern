package com.pixdane.gregicality.symbolgen.cli

import java.nio.file.Path

import cats.data.Ior
import cats.implicits.*

import com.pixdane.gregicality.symbolgen.gtceu.{
  GtceuRefJobs,
  GtceuRefScanner,
  GtceuScannedRefs
}
import com.pixdane.gregicality.symbolgen.gtceu.scan.GtceuScanDiagnostic
import com.pixdane.gregicality.symbolgen.io.GeneratedSourceWriter
import com.pixdane.gregicality.symbolgen.io.SourceArchiveReader
import com.pixdane.gregicality.symbolgen.render.RefAggregateRenderer
import com.pixdane.gregicality.symbolgen.render.RefObjectRenderer

object GenerateGtRefs:
  def main(args: Array[String]): Unit =
    run(Args.parse(args.toVector))

  def run(args: Args): Unit =
    args.kind match
      case "gtceu" =>
        val archive = SourceArchiveReader.readJar(args.sources)
        val scanned =
          GtceuRefJobs.jobs.traverse(GtceuRefScanner.scan(_, archive))
        scanned match
          case Ior.Right(refs) =>
            val refFiles = refs.map {
              case GtceuScannedRefs.Materials(target, refs) =>
                RefObjectRenderer.generateMaterialFile(target, refs)
              case GtceuScannedRefs.Paths(target, refs) =>
                RefObjectRenderer.generatePathFile(target, refs)
            }
            val aggregateFile =
              RefAggregateRenderer.generateFile(
                outputPackage =
                  "com.pixdane.gregicality.codegen.dsl.refs.gtceu",
                outputObject = "GTRefs",
                exports = GtceuRefJobs.jobs.map(_.target.outputObject)
              )

            GeneratedSourceWriter.sync(
              outputDir = args.out,
              files = refFiles :+ aggregateFile
            )
          case Ior.Left(diagnostics) =>
            throw scanFailedException(diagnostics.toChain.toList)
          case Ior.Both(diagnostics, _) =>
            throw scanFailedException(diagnostics.toChain.toList)

      case other =>
        throw new IllegalArgumentException(
          s"unsupported ref generation kind: $other"
        )

  private def scanFailedException(
      diagnostics: List[GtceuScanDiagnostic]
  ): IllegalArgumentException =
    new IllegalArgumentException(
      "GTCEu material scan failed:\n" +
        diagnostics.map(diagnostic => s"- ${diagnostic.render}").mkString("\n")
    )

final case class Args(kind: String, sources: Path, out: Path)

object Args:
  def parse(values: Vector[String]): Args =
    Args(
      kind = required(values, "--kind"),
      sources = Path.of(required(values, "--sources")),
      out = Path.of(required(values, "--out"))
    )

  private def required(values: Vector[String], name: String): String =
    values
      .sliding(2)
      .collectFirst {
        case Vector(key, value) if key == name => value
      }
      .getOrElse {
        throw new IllegalArgumentException(s"missing required argument: $name")
      }

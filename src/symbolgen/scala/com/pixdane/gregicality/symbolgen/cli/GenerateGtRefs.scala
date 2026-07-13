package com.pixdane.gregicality.symbolgen.cli

import java.nio.file.Path

import com.pixdane.gregicality.symbolgen.gtceu.{
  GtceuRefJobs,
  GtceuRefScanner,
  GtceuScannedRefs
}
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
        val refFiles =
          GtceuRefJobs.jobs.map {
            job =>
              GtceuRefScanner.scan(job, archive) match
                case GtceuScannedRefs.Materials(target, refs) =>
                  RefObjectRenderer.generateMaterialFile(target, refs)
                case GtceuScannedRefs.Paths(target, refs) =>
                  RefObjectRenderer.generatePathFile(target, refs)
          }
        val aggregateFile =
          RefAggregateRenderer.generateFile(
            outputPackage = "com.pixdane.gregicality.codegen.dsl.refs.gtceu",
            outputObject = "GTRefs",
            exports = GtceuRefJobs.jobs.map(_.target.outputObject)
          )

        GeneratedSourceWriter.sync(
          outputDir = args.out,
          files = refFiles :+ aggregateFile
        )

      case other =>
        throw new IllegalArgumentException(
          s"unsupported ref generation kind: $other"
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

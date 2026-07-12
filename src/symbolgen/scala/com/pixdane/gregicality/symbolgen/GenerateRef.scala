package com.pixdane.gregicality.symbolgen

import java.nio.file.Path

object GenerateRef:
  def main(args: Array[String]): Unit =
    run(Args.parse(args.toVector))

  def run(args: Args): Unit =
    args.kind match
      case "gtceu" =>
        val archive = SourceArchiveReader.readJar(args.sources)
        val refFiles = RefJobs.all.map(job => RefObjectRenderer.generateFile(job, archive))
        val aggregateFile =
          RefAggregateRenderer.generateFile(
            outputPackage = "com.pixdane.gregicality.codegen.dsl.refs",
            outputObject = "GTRefs",
            exports = RefJobs.all.map(_.target.outputObject)
          )

        GeneratedSourceWriter.sync(
          outputDir = args.out,
          files = RefSupportRenderer.generateFile() +: (refFiles :+ aggregateFile)
        )

      case other =>
        throw new IllegalArgumentException(s"unsupported ref generation kind: $other")

final case class Args(kind: String, sources: Path, out: Path)

object Args:
  def parse(values: Vector[String]): Args =
    Args(
      kind = required(values, "--kind"),
      sources = Path.of(required(values, "--sources")),
      out = Path.of(required(values, "--out"))
    )

  private def required(values: Vector[String], name: String): String =
    values.sliding(2).collectFirst {
      case Vector(key, value) if key == name => value
    }.getOrElse {
      throw new IllegalArgumentException(s"missing required argument: $name")
    }

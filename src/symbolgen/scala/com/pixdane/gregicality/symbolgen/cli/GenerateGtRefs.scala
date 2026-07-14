package com.pixdane.gregicality.symbolgen.cli

import java.nio.file.Path

import cats.data.Ior

import com.pixdane.gregicality.symbolgen.domain.Diagnostic
import com.pixdane.gregicality.symbolgen.domain.SymbolgenDomains
import com.pixdane.gregicality.symbolgen.io.GeneratedSourceWriter
import com.pixdane.gregicality.symbolgen.io.SourceArchiveReader

object GenerateGtRefs:
  def main(args: Array[String]): Unit =
    run(GenerateGtRefsArgs.parse(args.toVector))

  def run(args: GenerateGtRefsArgs): Unit =
    val domain = SymbolgenDomains.find(args.kind).getOrElse {
      throw new IllegalArgumentException(
        s"unsupported ref generation kind: ${args.kind}"
      )
    }

    val archive = SourceArchiveReader.readJar(args.sources)
    domain.generate(archive) match
      case Ior.Right(files) =>
        GeneratedSourceWriter.sync(outputDir = args.out, files = files)
      case Ior.Left(diagnostics) =>
        throw symbolgenFailedException(args.kind, diagnostics.toChain.toList)
      case Ior.Both(diagnostics, _) =>
        throw symbolgenFailedException(args.kind, diagnostics.toChain.toList)

  private def symbolgenFailedException(
      kind: String,
      diagnostics: Iterable[Diagnostic]
  ): IllegalArgumentException =
    new IllegalArgumentException(
      s"symbol generation failed for '$kind':\n" +
        diagnostics.map(diagnostic => s"- ${diagnostic.render}").mkString("\n")
    )

final case class GenerateGtRefsArgs(kind: String, sources: Path, out: Path)

object GenerateGtRefsArgs:
  def parse(values: Vector[String]): GenerateGtRefsArgs =
    GenerateGtRefsArgs(
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

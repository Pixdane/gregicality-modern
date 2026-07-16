package com.pixdane.gregicality.symbolgen

import java.nio.file.Path

import cats.data.Ior

import com.pixdane.gregicality.generator.GeneratedSourceWriter
import com.pixdane.gregicality.symbolgen.backends.SymbolGenerators
import com.pixdane.gregicality.symbolgen.framework.Diagnostic
import com.pixdane.gregicality.symbolgen.io.SourceArchiveReader

/** Command-line entry point for symbol generation.
  *
  * Resolves the requested generator (`--kind`) from the backend registry, reads
  * the sources jar into a `SourceArchive`, runs the generator, and atomically
  * syncs the produced files to the output directory. When the generator returns
  * diagnostics (with or without output), the run fails with an
  * `IllegalArgumentException` listing every diagnostic.
  */
object GenerateRefs:
  def main(args: Array[String]): Unit =
    run(GenerateRefsArgs.parse(args.toVector))

  def run(args: GenerateRefsArgs): Unit =
    val domain = SymbolGenerators.find(args.kind).getOrElse {
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

/** Parsed command-line arguments for [[GenerateRefs]]. */
final case class GenerateRefsArgs(kind: String, sources: Path, out: Path)

object GenerateRefsArgs:
  def parse(values: Vector[String]): GenerateRefsArgs =
    GenerateRefsArgs(
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

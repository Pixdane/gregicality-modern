package com.pixdane.gregicality.symbolgen.framework

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit

import scala.jdk.CollectionConverters.*

/** A problem encountered while reading or parsing a source file out of the
  * archive. Converted into a backend-specific diagnostic by
  * [[com.pixdane.gregicality.symbolgen.backends.gtceu.scan.GtceuScanDiagnostic]].
  */
enum SourceArchiveError:
  case Missing(path: String)
  case ParseFailed(path: String, message: String)

  def render: String = this match
    case Missing(path) =>
      s"missing source file: $path"
    case ParseFailed(path, message) =>
      s"failed to parse $path: $message"

/** An in-memory view of the `.java` files extracted from a sources jar.
  *
  * The archive is the shared input to every backend job: it exposes raw source
  * text by path and can lazily parse individual files or whole subtrees into
  * JavaParser `CompilationUnit`s, collecting parse errors instead of failing
  * fast so that a single unparseable file does not abort the whole pipeline.
  */
final case class SourceArchive(files: Map[String, String]):
  def source(path: String): Option[String] =
    files.get(path)

  def parse(path: String): Either[SourceArchiveError, CompilationUnit] =
    source(path) match
      case None =>
        Left(SourceArchiveError.Missing(path))
      case Some(text) =>
        try Right(StaticJavaParser.parse(text))
        catch
          case e: ParseProblemException =>
            Left(
              SourceArchiveError.ParseFailed(
                path,
                e.getProblems.asScala.map(_.getMessage).mkString("; ")
              )
            )

  def parseUnder(
      prefix: String
  ): (Vector[(String, CompilationUnit)], Vector[SourceArchiveError]) =
    files.toVector
      .collect {
        case (path, _) if path.startsWith(prefix) && path.endsWith(".java") =>
          path
      }
      .sorted
      .foldLeft(
        (
          Vector.empty[(String, CompilationUnit)],
          Vector.empty[SourceArchiveError]
        )
      ) { (acc, path) =>
        parse(path) match
          case Right(unit) => (acc._1 :+ (path -> unit), acc._2)
          case Left(error) => (acc._1, acc._2 :+ error)
      }

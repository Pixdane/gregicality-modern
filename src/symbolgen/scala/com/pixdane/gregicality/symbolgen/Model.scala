package com.pixdane.gregicality.symbolgen

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit

final case class SourceArchive(files: Map[String, String]):
  def source(path: String): String =
    files.getOrElse(
      path,
      throw new IllegalArgumentException(s"missing source file: $path")
    )

  def parse(path: String): CompilationUnit =
    StaticJavaParser.parse(source(path))

  def parseUnder(prefix: String): Vector[(String, CompilationUnit)] =
    files.toVector
      .collect {
        case (path, _) if path.startsWith(prefix) && path.endsWith(".java") =>
          path -> parse(path)
      }
      .sortBy(_._1)

final case class ScannedRef(
    name: String,
    id: Option[ResourceId],
    path: ScalaPath
)

final case class ResourceId(namespace: String, path: String)

final case class ScalaPath(parts: Vector[String])

object ScalaPath:
  def fromFqcn(fqcn: String): ScalaPath =
    ScalaPath(fqcn.split('.').toVector)

  def member(ownerFqcn: String, memberName: String): ScalaPath =
    ScalaPath.fromFqcn(ownerFqcn).append(memberName)

extension (path: ScalaPath)
  def append(part: String): ScalaPath =
    ScalaPath(path.parts :+ part)

enum RefRenderKind:
  case WithId
  case PathOnly

final case class RefObjectTarget(
    outputPackage: String,
    outputObject: String,
    valueType: String,
    renderKind: RefRenderKind
)

final case class RefJob(
    id: String,
    source: SourceArchive => Vector[ScannedRef],
    target: RefObjectTarget
)

final case class GeneratedScalaFile(
    relativePath: String,
    content: String
)

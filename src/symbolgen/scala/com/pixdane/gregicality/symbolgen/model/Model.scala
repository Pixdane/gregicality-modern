package com.pixdane.gregicality.symbolgen.model

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.pixdane.gregicality.codegen.dsl.model.{ResourceId, ScalaSymbolPath}

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

sealed trait ScannedRef:
  def name: String
  def path: ScalaSymbolPath

sealed trait ScannedMaterialRef extends ScannedRef:
  def id: ResourceId

final case class ScannedRegisteredMaterialRef(
    name: String,
    id: ResourceId,
    path: ScalaSymbolPath
) extends ScannedMaterialRef

final case class ScannedMaterialAliasRef(
    name: String,
    id: ResourceId,
    path: ScalaSymbolPath
) extends ScannedMaterialRef

final case class ScannedPathRef(
    name: String,
    path: ScalaSymbolPath
) extends ScannedRef

final case class RefObjectTarget(
    outputPackage: String,
    outputObject: String,
    valueType: String
)

enum RefJob:
  case Materials(
      id: String,
      scan: SourceArchive => Vector[ScannedMaterialRef],
      objectTarget: RefObjectTarget
  )
  case Paths(
      id: String,
      scan: SourceArchive => Vector[ScannedPathRef],
      objectTarget: RefObjectTarget
  )

  def target: RefObjectTarget =
    this match
      case Materials(_, _, target) => target
      case Paths(_, _, target)     => target

final case class GeneratedScalaFile(
    relativePath: String,
    content: String
)

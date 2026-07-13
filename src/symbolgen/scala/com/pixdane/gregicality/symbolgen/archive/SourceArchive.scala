package com.pixdane.gregicality.symbolgen.archive

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

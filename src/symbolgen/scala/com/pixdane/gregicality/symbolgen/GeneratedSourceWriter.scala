package com.pixdane.gregicality.symbolgen

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import scala.jdk.CollectionConverters.*
import scala.util.Using

object GeneratedSourceWriter:
  def sync(outputDir: Path, files: Vector[GeneratedScalaFile]): Unit =
    clearDirectory(outputDir)

    files.foreach { file =>
      val path = outputDir.resolve(file.relativePath)
      Files.createDirectories(path.getParent)
      Files.writeString(path, file.content, StandardCharsets.UTF_8)
    }

  private def clearDirectory(path: Path): Unit =
    if Files.exists(path) then
      val paths =
        Using.resource(Files.walk(path)) { stream =>
          stream.iterator().asScala.toVector.sortBy(_.getNameCount).reverse
        }

      paths.foreach(Files.deleteIfExists)

    Files.createDirectories(path)

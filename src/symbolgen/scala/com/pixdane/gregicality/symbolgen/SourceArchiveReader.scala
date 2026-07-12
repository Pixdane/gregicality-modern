package com.pixdane.gregicality.symbolgen

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipFile

import scala.jdk.CollectionConverters.*
import scala.util.Using

object SourceArchiveReader:
  def readJar(path: Path): SourceArchive =
    Using.resource(ZipFile(path.toFile)) { zip =>
      val files =
        zip.entries().asScala
          .filter(entry => !entry.isDirectory && entry.getName.endsWith(".java"))
          .map { entry =>
            val text =
              Using.resource(zip.getInputStream(entry)) { input =>
                String(input.readAllBytes(), StandardCharsets.UTF_8)
              }

            entry.getName -> text
          }
          .toMap

      SourceArchive(files)
    }

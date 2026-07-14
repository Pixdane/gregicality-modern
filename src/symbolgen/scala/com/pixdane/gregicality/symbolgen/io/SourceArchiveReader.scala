package com.pixdane.gregicality.symbolgen.io

import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.zip.ZipFile

import scala.jdk.CollectionConverters.*
import scala.util.Using

import com.pixdane.gregicality.symbolgen.framework.SourceArchive

/** Produces a [[SourceArchive]] from a Java sources jar.
  *
  * This is the archive's decoupled producer: it knows only how to read a jar
  * and collect `.java` entries, and is intentionally separate from
  * [[SourceArchive]] itself so that archive parsing logic can be tested without
  * touching the filesystem.
  */
object SourceArchiveReader:
  def readJar(path: Path): SourceArchive =
    Using.resource(ZipFile(path.toFile)) { zip =>
      val files =
        zip
          .entries()
          .asScala
          .filter(entry =>
            !entry.isDirectory && entry.getName.endsWith(".java")
          )
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

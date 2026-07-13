package com.pixdane.gregicality.symbolgen.io

import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.util.UUID

import scala.jdk.CollectionConverters.*
import scala.util.Using

import com.pixdane.gregicality.symbolgen.model.GeneratedScalaFile

object GeneratedSourceWriter:
  def sync(outputDir: Path, files: Vector[GeneratedScalaFile]): Unit =
    val normalizedOutputDir = outputDir.toAbsolutePath.normalize
    val parent = Option(normalizedOutputDir.getParent).getOrElse {
      throw new IllegalArgumentException(
        s"generated output directory must have a parent: $outputDir"
      )
    }
    validateFiles(files)
    Files.createDirectories(parent)

    val stagingDir = Files.createTempDirectory(
      parent,
      s".${normalizedOutputDir.getFileName}.staging-"
    )
    val backupDir = parent.resolve(
      s".${normalizedOutputDir.getFileName}.backup-${UUID.randomUUID()}"
    )
    var hasBackup = false

    try
      writeFiles(stagingDir, files)

      if Files.exists(normalizedOutputDir) then
        moveDirectory(normalizedOutputDir, backupDir)
        hasBackup = true

      try moveDirectory(stagingDir, normalizedOutputDir)
      catch
        case installError: Exception =>
          if hasBackup && !Files.exists(normalizedOutputDir) then
            try moveDirectory(backupDir, normalizedOutputDir)
            catch
              case rollbackError: Exception =>
                installError.addSuppressed(rollbackError)
          throw installError
    finally deleteTree(stagingDir)

    if hasBackup then deleteTree(backupDir)

  private def validateFiles(files: Vector[GeneratedScalaFile]): Unit =
    val normalizedPaths = files.map { file =>
      val path = Path.of(file.relativePath).normalize
      if path.isAbsolute || path.getNameCount == 0 || path.startsWith("..") then
        throw new IllegalArgumentException(
          s"generated source path must stay under the output directory: ${file.relativePath}"
        )
      path
    }

    val duplicatePaths = normalizedPaths
      .groupBy(identity)
      .collect { case (path, occurrences) if occurrences.sizeIs > 1 => path }
      .toVector
      .sortBy(_.toString)

    if duplicatePaths.nonEmpty then
      throw new IllegalArgumentException(
        s"duplicate generated source paths: ${duplicatePaths.mkString(", ")}"
      )

  private def writeFiles(
      outputDir: Path,
      files: Vector[GeneratedScalaFile]
  ): Unit =
    files.foreach { file =>
      val path = outputDir.resolve(file.relativePath).normalize
      Files.createDirectories(path.getParent)
      Files.writeString(path, file.content, StandardCharsets.UTF_8)
    }

  private def moveDirectory(source: Path, target: Path): Unit =
    try Files.move(source, target, ATOMIC_MOVE)
    catch case _: AtomicMoveNotSupportedException => Files.move(source, target)

  private def deleteTree(path: Path): Unit =
    if Files.exists(path) then
      val paths =
        Using.resource(Files.walk(path)) { stream =>
          stream.iterator().asScala.toVector.sortBy(_.getNameCount).reverse
        }

      paths.foreach(Files.deleteIfExists)

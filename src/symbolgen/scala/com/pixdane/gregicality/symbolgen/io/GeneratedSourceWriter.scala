package com.pixdane.gregicality.symbolgen.io

import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.util.UUID

import scala.jdk.CollectionConverters.*
import scala.util.Using

import com.pixdane.gregicality.symbolgen.model.GeneratedScalaFile

object GeneratedSourceWriter:
  def sync(outputDir: Path, files: Vector[GeneratedScalaFile]): Unit =
    syncWithMover(outputDir, files)(moveDirectory)

  private[symbolgen] def syncWithMover(
      outputDir: Path,
      files: Vector[GeneratedScalaFile]
  )(move: (Path, Path) => Unit): Unit =
    val normalizedOutputDir = outputDir.toAbsolutePath.normalize
    val parent = Option(normalizedOutputDir.getParent).getOrElse {
      throw new IllegalArgumentException(
        s"generated output directory must have a parent: $outputDir"
      )
    }
    validateFiles(files)
    Files.createDirectories(parent)

    if outputMatches(normalizedOutputDir, files) then return

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
        move(normalizedOutputDir, backupDir)
        hasBackup = true

      try move(stagingDir, normalizedOutputDir)
      catch
        case installError: Exception =>
          if hasBackup && !Files.exists(normalizedOutputDir) then
            try move(backupDir, normalizedOutputDir)
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

  private def outputMatches(
      outputDir: Path,
      files: Vector[GeneratedScalaFile]
  ): Boolean =
    if !Files.isDirectory(outputDir, NOFOLLOW_LINKS) then false
    else
      val expectedFiles = files
        .map(file => Path.of(file.relativePath).normalize -> file.content)
        .toMap
      val expectedDirectories =
        expectedFiles.keySet.flatMap(parentPaths)

      Using.resource(Files.walk(outputDir)) { stream =>
        val entries = stream
          .iterator()
          .asScala
          .filterNot(_ == outputDir)
          .map(path => outputDir.relativize(path) -> path)
          .toVector
        val containsUnsupportedEntry = entries.exists { case (_, path) =>
          !Files.isRegularFile(path, NOFOLLOW_LINKS) &&
          !Files.isDirectory(path, NOFOLLOW_LINKS)
        }
        val actualFiles = entries.collect {
          case (relativePath, path)
              if Files.isRegularFile(path, NOFOLLOW_LINKS) =>
            relativePath
        }.toSet
        val actualDirectories = entries.collect {
          case (relativePath, path)
              if Files.isDirectory(path, NOFOLLOW_LINKS) =>
            relativePath
        }.toSet

        !containsUnsupportedEntry &&
        actualFiles == expectedFiles.keySet &&
        actualDirectories == expectedDirectories &&
        expectedFiles.forall { case (relativePath, expectedContent) =>
          Files
            .readAllBytes(outputDir.resolve(relativePath))
            .sameElements(expectedContent.getBytes(StandardCharsets.UTF_8))
        }
      }

  private def parentPaths(path: Path): Set[Path] =
    Option(path.getParent) match
      case Some(parent) => parentPaths(parent) + parent
      case None         => Set.empty

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

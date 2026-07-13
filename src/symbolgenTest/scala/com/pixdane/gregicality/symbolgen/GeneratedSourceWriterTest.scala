package com.pixdane.gregicality.symbolgen

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime

import scala.jdk.CollectionConverters.*
import scala.util.Using

import com.pixdane.gregicality.symbolgen.io.GeneratedSourceWriter
import com.pixdane.gregicality.symbolgen.model.GeneratedScalaFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class GeneratedSourceWriterTest:
  @TempDir
  var tempDir: Path = _

  @Test
  def syncReplacesTheOwnedDirectoryAndDeletesStaleFiles(): Unit =
    val outputDir = tempDir.resolve("generated")
    val staleFile = outputDir.resolve("stale.scala")
    Files.createDirectories(outputDir)
    Files.writeString(staleFile, "stale", StandardCharsets.UTF_8)

    GeneratedSourceWriter.sync(
      outputDir,
      Vector(GeneratedScalaFile("pkg/Fresh.scala", "fresh\n"))
    )

    assertFalse(Files.exists(staleFile))
    assertEquals(
      "fresh\n",
      Files.readString(
        outputDir.resolve("pkg/Fresh.scala"),
        StandardCharsets.UTF_8
      )
    )

  @Test
  def syncDoesNotRewriteUnchangedOutput(): Unit =
    val outputDir = tempDir.resolve("generated")
    val generatedFile = outputDir.resolve("pkg/Fresh.scala")
    val files = Vector(GeneratedScalaFile("pkg/Fresh.scala", "fresh\n"))

    GeneratedSourceWriter.sync(outputDir, files)

    Files.setLastModifiedTime(generatedFile, FileTime.fromMillis(1_000_000))
    Files.setLastModifiedTime(outputDir, FileTime.fromMillis(2_000_000))
    val generatedFileTime = Files.getLastModifiedTime(generatedFile)
    val outputDirTime = Files.getLastModifiedTime(outputDir)

    GeneratedSourceWriter.sync(outputDir, files)

    assertEquals(generatedFileTime, Files.getLastModifiedTime(generatedFile))
    assertEquals(outputDirTime, Files.getLastModifiedTime(outputDir))

  @Test
  def syncRewritesChangedContent(): Unit =
    val outputDir = tempDir.resolve("generated")
    val generatedFile = outputDir.resolve("pkg/Fresh.scala")
    Files.createDirectories(generatedFile.getParent)
    Files.writeString(generatedFile, "old\n", StandardCharsets.UTF_8)

    GeneratedSourceWriter.sync(
      outputDir,
      Vector(GeneratedScalaFile("pkg/Fresh.scala", "fresh\n"))
    )

    assertEquals(
      "fresh\n",
      Files.readString(generatedFile, StandardCharsets.UTF_8)
    )

  @Test
  def failedInstallRestoresTheExistingOutput(): Unit =
    val outputDir = tempDir.resolve("generated")
    val existingFile = outputDir.resolve("Existing.scala")
    val installError = new IOException("install failed")
    var moveCount = 0
    Files.createDirectories(outputDir)
    Files.writeString(existingFile, "existing\n", StandardCharsets.UTF_8)

    val error = assertThrows(
      classOf[IOException],
      () =>
        GeneratedSourceWriter.syncWithMover(
          outputDir,
          Vector(GeneratedScalaFile("pkg/Fresh.scala", "fresh\n"))
        ) { (source, target) =>
          moveCount += 1
          if moveCount == 2 then throw installError
          Files.move(source, target)
          ()
        }
    )

    assertSame(installError, error)
    assertEquals(3, moveCount)
    assertEquals(
      "existing\n",
      Files.readString(existingFile, StandardCharsets.UTF_8)
    )
    assertFalse(Files.exists(outputDir.resolve("pkg/Fresh.scala")))
    assertTrue(pathsWithPrefix(".generated.staging-").isEmpty)
    assertTrue(pathsWithPrefix(".generated.backup-").isEmpty)

  @Test
  def failedRollbackPreservesTheBackupForRecovery(): Unit =
    val outputDir = tempDir.resolve("generated")
    val existingFile = outputDir.resolve("Existing.scala")
    val installError = new IOException("install failed")
    val rollbackError = new IOException("rollback failed")
    var moveCount = 0
    Files.createDirectories(outputDir)
    Files.writeString(existingFile, "existing\n", StandardCharsets.UTF_8)

    val error = assertThrows(
      classOf[IOException],
      () =>
        GeneratedSourceWriter.syncWithMover(
          outputDir,
          Vector(GeneratedScalaFile("pkg/Fresh.scala", "fresh\n"))
        ) { (source, target) =>
          moveCount += 1
          if moveCount == 2 then throw installError
          if moveCount == 3 then throw rollbackError
          Files.move(source, target)
          ()
        }
    )
    val backupDirs = pathsWithPrefix(".generated.backup-")

    assertSame(installError, error)
    assertEquals(Vector(rollbackError), error.getSuppressed.toVector)
    assertEquals(3, moveCount)
    assertFalse(Files.exists(outputDir))
    assertEquals(1, backupDirs.size)
    assertEquals(
      "existing\n",
      Files.readString(
        backupDirs.head.resolve("Existing.scala"),
        StandardCharsets.UTF_8
      )
    )
    assertTrue(pathsWithPrefix(".generated.staging-").isEmpty)

  @Test
  def invalidInputDoesNotModifyTheExistingOutputDirectory(): Unit =
    val outputDir = tempDir.resolve("generated")
    val existingFile = outputDir.resolve("Existing.scala")
    Files.createDirectories(outputDir)
    Files.writeString(existingFile, "existing\n", StandardCharsets.UTF_8)

    assertThrows(
      classOf[IllegalArgumentException],
      () =>
        GeneratedSourceWriter.sync(
          outputDir,
          Vector(GeneratedScalaFile("../Escaped.scala", "escaped\n"))
        )
    )

    assertTrue(Files.exists(existingFile))
    assertEquals(
      "existing\n",
      Files.readString(existingFile, StandardCharsets.UTF_8)
    )

  private def pathsWithPrefix(prefix: String): Vector[Path] =
    Using.resource(Files.list(tempDir)) { stream =>
      stream
        .iterator()
        .asScala
        .filter(path => path.getFileName.toString.startsWith(prefix))
        .toVector
    }

package com.pixdane.gregicality.symbolgen.framework

import cats.data.{Ior, NonEmptyChain}
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, fail}
import org.junit.jupiter.api.Test

class SymbolJobTest:
  private val archive = SourceArchive(Map.empty)
  private val target = RefOutputSpec(
    outputPackage = "example.refs",
    outputObject = "ExampleRefs",
    valueType = "ExampleRef"
  )

  @Test
  def runComposesScanPreprocessAndRender(): Unit =
    val job = SymbolJob[String, Int, String](
      id = "example",
      target = target,
      scan = _ => Ior.right(2),
      preprocess = value => Ior.right((value * 3).toString),
      render = (target, value) =>
        GeneratedScalaFile(
          relativePath = s"${target.outputObject}.scala",
          content = value
        )
    )

    assertEquals(
      Ior.right(GeneratedScalaFile("ExampleRefs.scala", "6")),
      job.run(archive)
    )

  @Test
  def runAccumulatesScanAndPreprocessDiagnostics(): Unit =
    val job = SymbolJob[String, Int, Int](
      id = "diagnostics",
      target = target,
      scan = _ => Ior.both(NonEmptyChain.one("scan"), 2),
      preprocess =
        value => Ior.both(NonEmptyChain.one("preprocess"), value + 1),
      render =
        (_, value) => GeneratedScalaFile("Diagnostics.scala", value.toString)
    )

    job.run(archive) match
      case Ior.Both(diagnostics, file) =>
        assertEquals(List("scan", "preprocess"), diagnostics.toChain.toList)
        assertEquals(GeneratedScalaFile("Diagnostics.scala", "3"), file)
      case Ior.Left(diagnostics) =>
        fail(
          "expected rendered output with diagnostics, got Left:\n" +
            diagnostics.iterator.mkString("\n")
        )
      case Ior.Right(file) =>
        fail(s"expected diagnostics, got clean output: $file")

  @Test
  def runStopsWhenScanFails(): Unit =
    var preprocessCalled = false
    var renderCalled = false
    val job = SymbolJob[String, Int, Int](
      id = "failure",
      target = target,
      scan = _ => Ior.left(NonEmptyChain.one("scan failed")),
      preprocess = value =>
        preprocessCalled = true
        Ior.right(value)
      ,
      render = (_, value) =>
        renderCalled = true
        GeneratedScalaFile("Failure.scala", value.toString)
    )

    job.run(archive) match
      case Ior.Left(diagnostics) =>
        assertEquals(List("scan failed"), diagnostics.toChain.toList)
      case Ior.Both(diagnostics, file) =>
        fail(
          s"expected Left, got diagnostics ${diagnostics.iterator.mkString(", ")} " +
            s"with output $file"
        )
      case Ior.Right(file) =>
        fail(s"expected Left, got clean output: $file")

    assertFalse(preprocessCalled)
    assertFalse(renderCalled)

  @Test
  def runStopsWhenPreprocessFails(): Unit =
    var renderCalled = false
    val job = SymbolJob[String, Int, Int](
      id = "preprocess-failure",
      target = target,
      scan = _ => Ior.right(2),
      preprocess = _ => Ior.left(NonEmptyChain.one("preprocess failed")),
      render = (_, value) =>
        renderCalled = true
        GeneratedScalaFile("Failure.scala", value.toString)
    )

    job.run(archive) match
      case Ior.Left(diagnostics) =>
        assertEquals(List("preprocess failed"), diagnostics.toChain.toList)
      case Ior.Both(diagnostics, file) =>
        fail(
          s"expected Left, got diagnostics ${diagnostics.iterator.mkString(", ")} " +
            s"with output $file"
        )
      case Ior.Right(file) =>
        fail(s"expected Left, got clean output: $file")

    assertFalse(renderCalled)

package com.pixdane.gregicality.symbolgen.pipeline

import cats.data.{Ior, NonEmptyChain}
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test

class PipelineTest:

  @Test
  def mapTransformsRightValueWithoutDiagnostics(): Unit =
    val lengthOf: Pipeline[String, Nothing, Int] =
      Pipeline("length", (in: String) => Ior.right(in.length))

    val doubled: Pipeline[String, Nothing, Int] =
      lengthOf.map(n => n * 2)

    doubled.run("abc") match
      case Ior.Right(value) =>
        assertEquals(6, value)
      case Ior.Left(diagnostics) =>
        fail(
          "expected Right for clean input, got Left:\n" +
            diagnostics.iterator.mkString("\n")
        )
      case Ior.Both(diagnostics, value) =>
        fail(
          s"expected Right for clean input, got Both with value $value:\n" +
            diagnostics.iterator.mkString("\n")
        )

  @Test
  def mapPreservesDiagnosticsAndTransformsBothValue(): Unit =
    val warning: Pipeline[String, String, Int] =
      Pipeline(
        "warning",
        (_: String) => Ior.both(NonEmptyChain.one("warn"), 5)
      )

    val doubled: Pipeline[String, String, Int] =
      warning.map(n => n * 2)

    doubled.run("anything") match
      case Ior.Both(diagnostics, value) =>
        assertEquals(10, value)
        assertEquals(
          NonEmptyChain.one("warn"),
          diagnostics
        )
      case Ior.Left(diagnostics) =>
        fail(
          "expected Both with mapped value, got Left:\n" +
            diagnostics.iterator.mkString("\n")
        )
      case Ior.Right(value) =>
        fail(
          s"expected Both with diagnostics, got Right with value $value"
        )

  @Test
  def mapLeavesLeftDiagnosticsUntouched(): Unit =
    val failing: Pipeline[String, String, Int] =
      Pipeline(
        "failing",
        (_: String) => Ior.left(NonEmptyChain.one("boom"))
      )

    val mapped: Pipeline[String, String, Int] =
      failing.map(n => n * 2)

    mapped.run("anything") match
      case Ior.Left(diagnostics) =>
        assertEquals(
          NonEmptyChain.one("boom"),
          diagnostics
        )
      case Ior.Both(diagnostics, value) =>
        fail(
          s"expected Left only, got Both with value $value:\n" +
            diagnostics.iterator.mkString("\n")
        )
      case Ior.Right(value) =>
        fail(
          s"expected Left only, got Right with value $value"
        )

  @Test
  def mapChainsAcrossMultipleStages(): Unit =
    val source: Pipeline[Int, Nothing, Int] =
      Pipeline("source", (in: Int) => Ior.right(in))

    val pipeline: Pipeline[Int, Nothing, String] =
      source
        .map(n => n + 1)
        .map(n => n * 10)
        .map(n => s"value=$n")

    pipeline.run(2) match
      case Ior.Right(value) =>
        assertEquals("value=30", value)
      case other =>
        fail(s"expected Right(\"value=30\"), got $other")

end PipelineTest

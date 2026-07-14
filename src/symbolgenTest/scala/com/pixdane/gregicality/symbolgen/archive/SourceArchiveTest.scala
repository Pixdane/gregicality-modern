package com.pixdane.gregicality.symbolgen.archive

import com.pixdane.gregicality.symbolgen.archive.SourceArchiveError.{
  Missing,
  ParseFailed
}
import org.junit.jupiter.api.Assertions.{
  assertEquals,
  assertTrue,
  fail
}
import org.junit.jupiter.api.Test

class SourceArchiveTest:
  private val validJava: String =
    "package example;\npublic class Valid {\n  public static final int VALUE = 1;\n}\n"

  @Test
  def sourceReturnsNoneWhenPathMissing(): Unit =
    val archive = SourceArchive(Map.empty)
    assertEquals(None, archive.source("missing/Path.java"))

  @Test
  def sourceReturnsContentsWhenPathPresent(): Unit =
    val archive = SourceArchive(Map("a/Valid.java" -> validJava))
    assertEquals(Some(validJava), archive.source("a/Valid.java"))

  @Test
  def parseReturnsMissingWhenPathAbsent(): Unit =
    val archive = SourceArchive(Map.empty)
    assertEquals(
      Left(Missing("absent/Path.java")),
      archive.parse("absent/Path.java")
    )

  @Test
  def parseReturnsParseFailedForInvalidJava(): Unit =
    val archive = SourceArchive(Map("bad/Invalid.java" -> "not valid java {{{"))
    archive.parse("bad/Invalid.java") match
      case Left(ParseFailed(path, message)) =>
        assertEquals("bad/Invalid.java", path)
        assertTrue(
          message.nonEmpty,
          s"expected non-empty parse message, got: '$message'"
        )
      case other =>
        fail(s"expected Left(ParseFailed), got: $other")

  @Test
  def parseReturnsUnitForValidJava(): Unit =
    val archive = SourceArchive(Map("ok/Valid.java" -> validJava))
    archive.parse("ok/Valid.java") match
      case Right(unit) =>
        assertEquals(
          "example.Valid",
          unit.getType(0).getFullyQualifiedName.orElseThrow()
        )
      case Left(error) =>
        fail(s"expected Right(unit), got Left($error)")

  @Test
  def parseUnderReturnsEmptyForEmptyPrefixMatch(): Unit =
    val archive =
      SourceArchive(Map("completely/unrelated/File.java" -> validJava))
    val (units, errors) = archive.parseUnder("nope/")
    assertTrue(units.isEmpty, s"expected no units, got: $units")
    assertTrue(errors.isEmpty, s"expected no errors, got: $errors")

  @Test
  def parseUnderKeepsValidFilesAndErrorsAndSortsByPath(): Unit =
    val invalidJava = "broken {{{"
    val archive = SourceArchive(
      Map(
        "dir/c_broken.java" -> invalidJava,
        "dir/a_valid.java"  -> validJava,
        "dir/b_valid.java"  -> validJava
      )
    )
    val (units, errors) = archive.parseUnder("dir/")
    val unitPaths = units.map(_._1)
    assertEquals(
      Vector("dir/a_valid.java", "dir/b_valid.java"),
      unitPaths,
      "valid units must be kept and sorted by path"
    )
    val errorPaths = errors.map {
      case ParseFailed(path, _) => path
      case other               => fail(s"expected ParseFailed, got: $other")
    }
    assertEquals(
      Vector("dir/c_broken.java"),
      errorPaths,
      "broken file must surface as a single error without dropping valid units"
    )

  @Test
  def parseUnderIgnoresNonJavaFilesUnderPrefix(): Unit =
    val archive = SourceArchive(
      Map(
        "dir/notes.md"     -> "# notes",
        "dir/Source.scala" -> "object Source"
      )
    )
    val (units, errors) = archive.parseUnder("dir/")
    assertTrue(units.isEmpty, s"expected no units, got: $units")
    assertTrue(errors.isEmpty, s"expected no errors, got: $errors")

  @Test
  def renderProducesStableText(): Unit =
    assertEquals(
      "missing source file: some/Path.java",
      Missing("some/Path.java").render
    )
    assertEquals(
      "failed to parse some/Path.java: lex error",
      ParseFailed("some/Path.java", "lex error").render
    )

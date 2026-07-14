package com.pixdane.gregicality.symbolgen.gtceu.scan

import cats.data.Ior
import com.pixdane.gregicality.core.refs.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.scan.ScannedPathRef
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, fail}
import org.junit.jupiter.api.Test

class StaticFieldScannerTest:
  @Test
  def scanUsesPublicStaticFinalFieldsOfRequestedType(): Unit =
    val archive = SourceArchive(
      Map(
        "MaterialIconSet.java" ->
          """
            |package com.gregtechceu.gtceu.api.data.chemical.material.info;
            |
            |import java.util.Map;
            |
            |public class MaterialIconSet {
            |  public static final Map<String, MaterialIconSet> ICON_SETS = null;
            |  public static final MaterialIconSet DULL = new MaterialIconSet("dull");
            |  @Deprecated
            |  public static final MaterialIconSet LEGACY = new MaterialIconSet("legacy");
            |  @java.lang.Deprecated
            |  public static final MaterialIconSet QUALIFIED_LEGACY = new MaterialIconSet("qualified_legacy");
            |  public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic");
            |  static final MaterialIconSet HIDDEN = new MaterialIconSet("hidden");
            |}
            |""".stripMargin
      )
    )

    val refs = successfulRefs(
      StaticFieldScanner.scan(
        StaticFieldScanSpec(
          sourcePath = "MaterialIconSet.java",
          ownerFqcn =
            "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet",
          memberTypeSimpleName = "MaterialIconSet"
        )
      )(archive)
    )

    assertEquals(Vector("DULL", "METALLIC"), refs.map(_.name))
    assertEquals(
      ScalaSymbolPath(
        Vector(
          "com",
          "gregtechceu",
          "gtceu",
          "api",
          "data",
          "chemical",
          "material",
          "info",
          "MaterialIconSet",
          "DULL"
        )
      ),
      refs.head.path
    )

  @Test
  def scanReportsMissingSource(): Unit =
    val result = StaticFieldScanner.scan(
      StaticFieldScanSpec(
        sourcePath = "Missing.java",
        ownerFqcn = "example.Missing",
        memberTypeSimpleName = "Missing"
      )
    )(SourceArchive(Map.empty))

    result match
      case Ior.Left(diagnostics) =>
        assertEquals(
          List(GtceuScanDiagnostic.MissingSource("Missing.java")),
          diagnostics.toChain.toList
        )
      case Ior.Both(diagnostics, refs) =>
        fail(
          s"expected missing source failure, got ${diagnostics.toChain.toList} " +
            s"with refs $refs"
        )
      case Ior.Right(refs) =>
        fail(s"expected missing source failure, got refs $refs")

  @Test
  def scanReportsJavaParseFailure(): Unit =
    val result = StaticFieldScanner.scan(
      StaticFieldScanSpec(
        sourcePath = "Broken.java",
        ownerFqcn = "example.Broken",
        memberTypeSimpleName = "Broken"
      )
    )(SourceArchive(Map("Broken.java" -> "public class Broken {")))

    result match
      case Ior.Left(diagnostics) =>
        diagnostics.toChain.toList match
          case List(GtceuScanDiagnostic.SourceParseError(path, message)) =>
            assertEquals("Broken.java", path)
            assertFalse(message.isBlank)
          case other =>
            fail(s"expected one source parse error, got $other")
      case Ior.Both(diagnostics, refs) =>
        fail(
          s"expected parse failure, got ${diagnostics.toChain.toList} " +
            s"with refs $refs"
        )
      case Ior.Right(refs) =>
        fail(s"expected parse failure, got refs $refs")

  private def successfulRefs(
      result: GtceuScanResult[Vector[ScannedPathRef]]
  ): Vector[ScannedPathRef] =
    result match
      case Ior.Right(refs) =>
        refs
      case Ior.Left(diagnostics) =>
        fail(
          "expected successful refs, got diagnostics:\n" +
            diagnostics.iterator.map(_.render).mkString("\n")
        )
        throw new AssertionError("unreachable")
      case Ior.Both(diagnostics, refs) =>
        fail(
          "expected successful refs, got partial result with diagnostics:\n" +
            diagnostics.iterator.map(_.render).mkString("\n") +
            "\npartial refs: " + refs.map(_.name).mkString(", ")
        )
        throw new AssertionError("unreachable")

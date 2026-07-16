package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.flags

import cats.data.Ior
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanResult,
  MaterialFlagScanSpec
}
import com.pixdane.gregicality.symbolgen.framework.{
  ScannedMaterialFlagRef,
  SourceArchive
}
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test

class MaterialFlagScannerTest:

  @Test
  def scanCollectsFlagAndPropertyRequirementsFromBuilderChains(): Unit =
    val archive = SourceArchive(
      Map(
        "MaterialFlags.java" ->
          """
            |public class MaterialFlags {
            |  public static final MaterialFlag GENERATE_PLATE =
            |      new MaterialFlag.Builder("generate_plate")
            |          .requireProps(PropertyKey.DUST)
            |          .build();
            |
            |  public static final MaterialFlag GENERATE_ROD =
            |      new MaterialFlag.Builder("generate_rod")
            |          .requireProps(PropertyKey.DUST)
            |          .build();
            |
            |  public static final MaterialFlag GENERATE_GEAR =
            |      new MaterialFlag.Builder("generate_gear")
            |          .requireFlags(GENERATE_PLATE, GENERATE_ROD)
            |          .requireProps(PropertyKey.DUST)
            |          .build();
            |
            |  public static final MaterialFlag SIMPLE =
            |      new MaterialFlag.Builder("simple").build();
            |
            |  @Deprecated
            |  public static final MaterialFlag LEGACY =
            |      new MaterialFlag.Builder("legacy").build();
            |}
            |""".stripMargin
      )
    )

    val refs = successfulFlags(MaterialFlagScanner.scan(spec)(archive))
    val gear =
      refs.find(_.name == "GENERATE_GEAR").getOrElse(fail("missing gear"))
    val simple = refs.find(_.name == "SIMPLE").getOrElse(fail("missing simple"))

    assertEquals(
      Vector("GENERATE_PLATE", "GENERATE_ROD"),
      gear.requiredFlags.map(_.parts.last)
    )
    assertEquals(Vector("DUST"), gear.requiredProperties.map(_.parts.last))
    assertEquals(
      Vector.fill(2)("MaterialFlags"),
      gear.requiredFlags.map(_.parts.dropRight(1).last)
    )
    assertEquals(
      "PropertyKey",
      gear.requiredProperties.head.parts.dropRight(1).last
    )
    assertEquals(Vector.empty, simple.requiredFlags)
    assertEquals(Vector.empty, simple.requiredProperties)
    assertEquals(
      Vector("GENERATE_GEAR", "GENERATE_PLATE", "GENERATE_ROD", "SIMPLE"),
      refs.map(_.name).sorted
    )

  private val spec = MaterialFlagScanSpec(
    sourcePath = "MaterialFlags.java",
    ownerFqcn = "com.example.MaterialFlags",
    propertyKeyOwnerFqcn = "com.example.PropertyKey"
  )

  private def successfulFlags(
      result: GtceuScanResult[Vector[ScannedMaterialFlagRef]]
  ): Vector[ScannedMaterialFlagRef] =
    result match
      case Ior.Right(refs)       => refs
      case Ior.Left(diagnostics) =>
        fail(diagnostics.iterator.map(_.render).mkString("\n"))
      case Ior.Both(diagnostics, refs) =>
        fail(
          diagnostics.iterator.map(_.render).mkString("\n") +
            s"\npartial refs: ${refs.map(_.name)}"
        )

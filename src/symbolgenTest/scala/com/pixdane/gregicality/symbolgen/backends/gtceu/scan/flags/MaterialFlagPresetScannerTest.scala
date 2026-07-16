package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.flags

import cats.data.Ior
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanResult,
  MaterialFlagPresetScanSpec
}
import com.pixdane.gregicality.symbolgen.framework.{
  ScannedMaterialFlagPresetRef,
  SourceArchive
}
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test

class MaterialFlagPresetScannerTest:

  @Test
  def scanFlattensOrderedPresetInitialization(): Unit =
    val archive = SourceArchive(
      Map(
        "GTMaterials.java" ->
          """
            |public class GTMaterials {
            |  public static final List<MaterialFlag> STD_METAL = new ArrayList<>();
            |  public static final List<MaterialFlag> EXT_METAL = new ArrayList<>();
            |  public static final List<MaterialFlag> EXT2_METAL = new ArrayList<>();
            |
            |  static {
            |    STD_METAL.add(GENERATE_PLATE);
            |    EXT_METAL.addAll(STD_METAL);
            |    EXT_METAL.add(GENERATE_ROD);
            |    EXT2_METAL.addAll(EXT_METAL);
            |    EXT2_METAL.addAll(
            |        Arrays.asList(GENERATE_LONG_ROD, GENERATE_BOLT_SCREW));
            |  }
            |}
            |""".stripMargin
      )
    )

    val refs = successfulPresets(MaterialFlagPresetScanner.scan(spec)(archive))
    val byName = refs.map(ref => ref.name -> ref).toMap

    assertEquals(
      Vector("GENERATE_PLATE"),
      byName("STD_METAL").members.map(_.parts.last)
    )
    assertEquals(
      Vector("GENERATE_PLATE", "GENERATE_ROD"),
      byName("EXT_METAL").members.map(_.parts.last)
    )
    assertEquals(
      Vector(
        "GENERATE_PLATE",
        "GENERATE_ROD",
        "GENERATE_LONG_ROD",
        "GENERATE_BOLT_SCREW"
      ),
      byName("EXT2_METAL").members.map(_.parts.last)
    )

  private val spec = MaterialFlagPresetScanSpec(
    sourcePath = "GTMaterials.java",
    ownerFqcn = "com.example.GTMaterials",
    flagOwnerFqcn = "com.example.MaterialFlags"
  )

  private def successfulPresets(
      result: GtceuScanResult[Vector[ScannedMaterialFlagPresetRef]]
  ): Vector[ScannedMaterialFlagPresetRef] =
    result match
      case Ior.Right(refs)       => refs
      case Ior.Left(diagnostics) =>
        fail(diagnostics.iterator.map(_.render).mkString("\n"))
      case Ior.Both(diagnostics, refs) =>
        fail(
          diagnostics.iterator.map(_.render).mkString("\n") +
            s"\npartial refs: ${refs.map(_.name)}"
        )

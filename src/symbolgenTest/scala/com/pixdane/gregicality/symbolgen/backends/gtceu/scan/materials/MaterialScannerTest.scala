package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.materials

import cats.data.Ior
import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult,
  GtMaterialsScanSpec
}
import com.pixdane.gregicality.symbolgen.framework.{
  SourceArchive,
  ScannedMaterialRef
}
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue, fail}
import org.junit.jupiter.api.Test

class MaterialScannerTest:

  @Test
  def scanGtMaterialsReportsMissingDeclarationSource(): Unit =
    MaterialScanner.scan(materialSource)(SourceArchive(Map.empty)) match
      case Ior.Left(diagnostics) =>
        assertEquals(
          List(
            GtceuScanDiagnostic.MissingSource(
              materialSource.declarationPath
            )
          ),
          diagnostics.toChain.toList
        )
      case Ior.Both(diagnostics, input) =>
        fail(
          s"expected missing source failure, got ${diagnostics.toChain.toList} " +
            s"with input $input"
        )
      case Ior.Right(input) =>
        fail(s"expected missing source failure, got input $input")

  @Test
  def scanGtMaterialsKeepsRefsWhenOneAssignmentSourceCannotParse(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |""".stripMargin
    )
    val brokenPath = s"${materialSource.assignmentDir}Broken.java"
    val archiveWithBrokenSource =
      SourceArchive(archive.files.updated(brokenPath, "public class Broken {"))

    scanMaterials(materialSource)(archiveWithBrokenSource) match
      case Ior.Both(diagnostics, refs) =>
        assertEquals(Vector("Carbon"), refs.map(_.name))
        diagnostics.toChain.toList match
          case List(GtceuScanDiagnostic.SourceParseError(path, message)) =>
            assertEquals(brokenPath, path)
            assertTrue(message.nonEmpty)
          case other =>
            fail(s"expected one source parse error, got $other")
      case Ior.Left(diagnostics) =>
        fail(
          "expected partial refs with diagnostics, got Left:\n" +
            diagnostics.iterator.map(_.render).mkString("\n")
        )
      case Ior.Right(refs) =>
        fail(s"expected parse diagnostics, got clean refs $refs")

  @Test
  def scanGtMaterialsSkipsDeprecatedRegisteredMaterials(): Unit =
    val archive = materialArchive(
      declarations = "Carbon; @Deprecated public static Material LegacyCarbon",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |LegacyCarbon = new Material.Builder(GTCEu.id("legacy_carbon"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    val refs = materialRefs(materialSource)(archive)

    assertEquals(Vector("Carbon"), refs.map(_.name))

  @Test
  def scanGtMaterialsRejectsDeclaredMaterialWithoutRecognizedAssignment()
      : Unit =
    val archive = materialArchive(
      declarations = "Carbon, Hydrogen",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(
      rendered.contains(
        "without a recognized builder assignment"
      )
    )
    assertTrue(rendered.contains("Hydrogen"))
    assertTrue(
      rendered.contains(s"declared at ${materialSource.declarationPath}:")
    )

  @Test
  def scanGtMaterialsRejectsIdNestedOutsideTheBuilderConstructor(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |Carbon = new Material.Builder(otherId())
          |  .dust(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    assertUnsupportedMaterialAssignment(
      archive,
      "Carbon",
      "builder constructor"
    )

  @Test
  def scanGtMaterialsRejectsAssignmentThroughAnotherOwner(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |other.Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    assertUnsupportedMaterialAssignment(
      archive,
      "Carbon",
      s"assignment target is not owned by ${materialSource.ownerFqcn}"
    )

  @Test
  def scanGtMaterialsRejectsNonBuilderAssignmentValue(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = "Carbon = null;"
    )

    assertUnsupportedMaterialAssignment(
      archive,
      "Carbon",
      "unsupported assignment value"
    )

  @Test
  def scanGtMaterialsIgnoresUnrelatedForeignAssignments(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |other.Carbon = localValue;
          |""".stripMargin
    )

    val refs = materialRefs(materialSource)(archive)

    assertEquals(Vector("Carbon"), refs.map(_.name))

  @Test
  def scanGtMaterialsRejectsNonStringGtceuId(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id(CARBON_ID))
          |  .buildAndRegister();
          |""".stripMargin
    )

    assertUnsupportedMaterialAssignment(
      archive,
      "Carbon",
      "builder constructor"
    )

  @Test
  def scanGtMaterialsRejectsWrappedBuilderConstructorArgument(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |Carbon = new Material.Builder(wrap(GTCEu.id("carbon")))
          |  .buildAndRegister();
          |""".stripMargin
    )

    assertUnsupportedMaterialAssignment(
      archive,
      "Carbon",
      "builder constructor"
    )

  @Test
  def scanGtMaterialsRecognizesFullyQualifiedOwners(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |com.gregtechceu.gtceu.common.data.GTMaterials.Carbon =
          |  new Material.Builder(
          |    com.gregtechceu.gtceu.GTCEu.id("carbon")
          |  ).buildAndRegister();
          |""".stripMargin
    )

    val refs = materialRefs(materialSource)(archive)

    assertEquals(Vector("Carbon"), refs.map(_.name))
    assertEquals(ResourceId("gtceu", "carbon"), refs.head.id)

  @Test
  def scanGtMaterialsRejectsDuplicateSymbolAssignments(): Unit =
    val archive = materialArchive(
      declarations = "Carbon",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |Carbon = new Material.Builder(GTCEu.id("carbon_duplicate"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(
      rendered.contains("duplicate GTCEu material assignments")
    )
    assertTrue(rendered.contains("Carbon"))

  @Test
  def scanGtMaterialsRejectsDuplicateRegistryIds(): Unit =
    val archive = materialArchive(
      declarations = "Carbon, Hydrogen",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("shared"))
          |  .buildAndRegister();
          |Hydrogen = new Material.Builder(GTCEu.id("shared"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(
      rendered.contains("duplicate GTCEu material registry ids")
    )
    assertTrue(rendered.contains("gtceu:shared"))
    assertTrue(rendered.contains("Carbon"))
    assertTrue(rendered.contains("Hydrogen"))

  @Test
  def scanGtMaterialsAggregatesDiagnosticsWithSourceLocations(): Unit =
    val archive = materialArchive(
      declarations = "Carbon, Hydrogen, Oxygen",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("shared"))
          |  .buildAndRegister();
          |Carbon = new Material.Builder(GTCEu.id("carbon_duplicate"))
          |  .buildAndRegister();
          |Hydrogen = new Material.Builder(GTCEu.id("shared"))
          |  .buildAndRegister();
          |""".stripMargin
    )

    val (rendered, refs) =
      scanMaterials(materialSource)(archive) match
        case Ior.Both(diagnostics, refs) =>
          diagnostics.iterator.map(_.render).mkString("\n") -> refs
        case Ior.Left(diagnostics) =>
          fail(
            "expected partial refs with diagnostics, got Left:\n" +
              diagnostics.iterator.map(_.render).mkString("\n")
          )
          throw new AssertionError("unreachable")
        case Ior.Right(refs) =>
          fail(
            "expected diagnostics with partial refs, got Right: " +
              refs.map(_.name).mkString(", ")
          )
          throw new AssertionError("unreachable")

    assertEquals(Vector("Carbon", "Hydrogen"), refs.map(_.name))
    assertTrue(rendered.contains("duplicate GTCEu material assignments"))
    assertTrue(rendered.contains("duplicate GTCEu material registry ids"))
    assertTrue(
      rendered.contains(
        "without a recognized builder assignment"
      )
    )
    assertTrue(
      rendered.contains(s"${materialSource.assignmentDir}TestMaterials.java:")
    )
    assertTrue(rendered.contains(s"${materialSource.declarationPath}:"))
    assertTrue(
      rendered.indexOf("duplicate GTCEu material assignments") <
        rendered.indexOf("duplicate GTCEu material registry ids")
    )
    assertTrue(
      rendered.indexOf("duplicate GTCEu material registry ids") <
        rendered.indexOf("without a recognized builder assignment")
    )

  @Test
  def scanGtMaterialsJoinsDeclarationsWithBuilderIds(): Unit =
    val archive = SourceArchive(
      Map(
        "com/gregtechceu/gtceu/common/data/GTMaterials.java" ->
          """
            |package com.gregtechceu.gtceu.common.data;
            |
            |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
            |
            |public class GTMaterials {
            |  public static Material Carbon;
            |  public static Material PolyvinylChloride;
            |  public static Material[] CHEMICAL_DYES;
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/common/data/materials/OrganicChemistryMaterials.java" ->
          """
            |package com.gregtechceu.gtceu.common.data.materials;
            |
            |import com.gregtechceu.gtceu.GTCEu;
            |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
            |
            |import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
            |
            |public class OrganicChemistryMaterials {
            |  public static void register() {
            |    Carbon = new Material.Builder(GTCEu.id("carbon"))
            |      .dust()
            |      .buildAndRegister();
            |
            |    PolyvinylChloride = new Material.Builder(GTCEu.id("polyvinyl_chloride"))
            |      .polymer()
            |      .buildAndRegister();
            |
            |    NotDeclared = new Material.Builder(GTCEu.id("not_declared"))
            |      .buildAndRegister();
            |  }
            |}
            |""".stripMargin
      )
    )

    val refs =
      materialRefs(
        GtMaterialsScanSpec(
          declarationPath =
            "com/gregtechceu/gtceu/common/data/GTMaterials.java",
          assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
          ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
          namespace = "gtceu",
          idFactoryFqcn = "com.gregtechceu.gtceu.GTCEu"
        )
      )(archive)

    assertEquals(Vector("Carbon", "PolyvinylChloride"), refs.map(_.name))
    assertEquals(ResourceId("gtceu", "carbon"), refs.head.id)
    assertEquals(
      ScalaSymbolPath(
        Vector(
          "com",
          "gregtechceu",
          "gtceu",
          "common",
          "data",
          "GTMaterials",
          "Carbon"
        )
      ),
      refs.head.path
    )

  @Test
  def scanGtMaterialsAcceptsCustomIdFactoryOwner(): Unit =
    val customSource = GtMaterialsScanSpec(
      declarationPath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
      assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
      namespace = "gtceu",
      idFactoryFqcn = "example.CustomIds"
    )
    val archive = SourceArchive(
      Map(
        customSource.declarationPath ->
          """
            |package com.gregtechceu.gtceu.common.data;
            |
            |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
            |
            |public class GTMaterials {
            |  public static Material Carbon;
            |}
            |""".stripMargin,
        s"${customSource.assignmentDir}TestMaterials.java" ->
          s"""
             |package com.gregtechceu.gtceu.common.data.materials;
             |
             |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
             |import example.CustomIds;
             |
             |import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
             |
             |public class TestMaterials {
             |  public static void register() {
             |    Carbon = new Material.Builder(CustomIds.id("carbon"))
             |      .buildAndRegister();
             |  }
             |}
             |""".stripMargin
      )
    )

    val refs = materialRefs(customSource)(archive)

    assertEquals(Vector("Carbon"), refs.map(_.name))
    assertEquals(ResourceId("gtceu", "carbon"), refs.head.id)

  @Test
  def scanGtMaterialsRejectsGTCEuIdUnderCustomIdFactoryOwner(): Unit =
    val customSource = GtMaterialsScanSpec(
      declarationPath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
      assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
      namespace = "gtceu",
      idFactoryFqcn = "example.CustomIds"
    )
    val archive = SourceArchive(
      Map(
        customSource.declarationPath ->
          """
            |package com.gregtechceu.gtceu.common.data;
            |
            |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
            |
            |public class GTMaterials {
            |  public static Material Carbon;
            |}
            |""".stripMargin,
        s"${customSource.assignmentDir}TestMaterials.java" ->
          s"""
             |package com.gregtechceu.gtceu.common.data.materials;
             |
             |import com.gregtechceu.gtceu.GTCEu;
             |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
             |
             |import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
             |
             |public class TestMaterials {
             |  public static void register() {
             |    Carbon = new Material.Builder(GTCEu.id("carbon"))
             |      .buildAndRegister();
             |  }
             |}
             |""".stripMargin
      )
    )

    val rendered = materialDiagnostics(customSource)(archive)

    assertTrue(rendered.contains("unsupported GTCEu material assignments"))
    assertTrue(rendered.contains("Carbon"))
    assertTrue(
      rendered.contains(
        s"${customSource.assignmentDir}TestMaterials.java:"
      )
    )

  private def materialRefs(
      spec: GtMaterialsScanSpec
  )(archive: SourceArchive): Vector[ScannedMaterialRef] =
    scanMaterials(spec)(archive) match
      case Ior.Right(refs) =>
        refs
      case Ior.Left(diagnostics) =>
        fail(
          "expected successful material refs, got diagnostics:\n" +
            diagnostics.iterator.map(_.render).mkString("\n")
        )
        throw new AssertionError("unreachable")
      case Ior.Both(diagnostics, refs) =>
        fail(
          "expected successful material refs, got partial result with diagnostics:\n" +
            diagnostics.iterator.map(_.render).mkString("\n") +
            "\npartial refs: " + refs.map(_.name).mkString(", ")
        )
        throw new AssertionError("unreachable")

  private def materialDiagnostics(
      spec: GtMaterialsScanSpec
  )(archive: SourceArchive): String =
    scanMaterials(spec)(archive) match
      case Ior.Left(diagnostics) =>
        diagnostics.iterator.map(_.render).mkString("\n")
      case Ior.Both(diagnostics, _) =>
        diagnostics.iterator.map(_.render).mkString("\n")
      case Ior.Right(_) =>
        fail("expected material diagnostics, got a successful scan result")
        throw new AssertionError("unreachable")

  private def scanMaterials(
      spec: GtMaterialsScanSpec
  )(archive: SourceArchive): GtceuScanResult[Vector[ScannedMaterialRef]] =
    MaterialScanner
      .scan(spec)(archive)
      .flatMap(MaterialScanner.preprocess)

  private def assertUnsupportedMaterialAssignment(
      archive: SourceArchive,
      materialName: String,
      reason: String
  ): Unit =
    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(
      rendered.contains("unsupported GTCEu material assignments")
    )
    assertTrue(rendered.contains(materialName))
    assertTrue(rendered.contains(reason))
    assertTrue(
      rendered.contains(
        s"${materialSource.assignmentDir}TestMaterials.java:"
      )
    )

  private val materialSource = GtMaterialsScanSpec(
    declarationPath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
    assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
    ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
    namespace = "gtceu",
    idFactoryFqcn = "com.gregtechceu.gtceu.GTCEu"
  )

  private def materialArchive(
      declarations: String,
      assignments: String
  ): SourceArchive =
    SourceArchive(
      Map(
        materialSource.declarationPath ->
          s"""
             |package com.gregtechceu.gtceu.common.data;
             |
             |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
             |
             |public class GTMaterials {
             |  public static Material $declarations;
             |}
             |""".stripMargin,
        s"${materialSource.assignmentDir}TestMaterials.java" ->
          s"""
             |package com.gregtechceu.gtceu.common.data.materials;
             |
             |import com.gregtechceu.gtceu.GTCEu;
             |import com.gregtechceu.gtceu.api.data.chemical.material.Material;
             |
             |import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
             |
             |public class TestMaterials {
             |  public static void register() {
             |    $assignments
             |  }
             |}
             |""".stripMargin
      )
    )

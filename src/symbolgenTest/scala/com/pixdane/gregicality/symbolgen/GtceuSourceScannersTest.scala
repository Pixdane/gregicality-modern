package com.pixdane.gregicality.symbolgen

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.gtceu.*
import com.pixdane.gregicality.symbolgen.scan.ScannedMaterialRef
import com.pixdane.gregicality.symbolgen.scan.ScannedPathRef
import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}

import cats.data.Ior
import org.junit.jupiter.api.Assertions.{assertEquals, assertTrue, fail}
import org.junit.jupiter.api.Test

class GtceuSourceScannersTest:

  @Test
  def scanStaticMembersUsesPublicStaticFinalFieldsOfRequestedType(): Unit =
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

    val refs =
      GtceuSourceScanners.scanStaticMembers(
        StaticFieldScanSpec(
          sourcePath = "MaterialIconSet.java",
          ownerFqcn =
            "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet",
          memberTypeSimpleName = "MaterialIconSet"
        )
      )(archive)

    assertEquals(Vector("DULL", "METALLIC"), refs.map(_.name))
    assertTrue(refs.forall(_.isInstanceOf[ScannedPathRef]))
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
  def scanGtMaterialsSkipsDeprecatedAliases(): Unit =
    val archive = materialArchive(
      declarations =
        "Limonite; @java.lang.Deprecated public static Material YellowLimonite",
      assignments = """
          |Limonite = new Material.Builder(GTCEu.id("limonite"))
          |  .buildAndRegister();
          |GTMaterials.YellowLimonite = GTMaterials.Limonite;
          |""".stripMargin
    )

    val refs = materialRefs(materialSource)(archive)

    assertEquals(Vector("Limonite"), refs.map(_.name))

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
        "without a recognized builder or alias assignment"
      )
    )
    assertTrue(rendered.contains("Hydrogen"))

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
      "assignment target is not owned"
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
  def scanGtMaterialsRejectsAliasThroughAnotherOwner(): Unit =
    val archive = materialArchive(
      declarations = "Carbon, Hydrogen",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |Hydrogen = other.Carbon;
          |""".stripMargin
    )

    assertUnsupportedMaterialAssignment(
      archive,
      "Hydrogen",
      "alias target is not a member"
    )

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

    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(rendered.contains("duplicate GTCEu material assignments"))
    assertTrue(rendered.contains("duplicate GTCEu material registry ids"))
    assertTrue(
      rendered.contains(
        "without a recognized builder or alias assignment"
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
        rendered.indexOf("without a recognized builder or alias assignment")
    )

  @Test
  def scanGtMaterialsResolvesMaterialAliasesWithoutTreatingTheirIdAsDuplicate()
      : Unit =
    val archive = materialArchive(
      declarations = "Limonite, YellowLimonite",
      assignments = """
          |Limonite = new Material.Builder(GTCEu.id("limonite"))
          |  .buildAndRegister();
          |GTMaterials.YellowLimonite = com.gregtechceu.gtceu.common.data.GTMaterials.Limonite;
          |""".stripMargin
    )

    val refs = materialRefs(materialSource)(archive)

    assertEquals(Vector("Limonite", "YellowLimonite"), refs.map(_.name))
    assertEquals(refs.head.id, refs.last.id)
    assertEquals("YellowLimonite", refs.last.path.parts.last)

  @Test
  def scanGtMaterialsReportsAliasCycleAsDiagnostic(): Unit =
    val archive = materialArchive(
      declarations = "Alpha, Beta",
      assignments = """
          |Alpha = Beta;
          |Beta = Alpha;
          |""".stripMargin
    )

    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(rendered.contains("Alpha"))
    assertTrue(rendered.contains("Beta"))
    assertTrue(
      rendered.toLowerCase.contains("cycle") ||
        rendered.toLowerCase.contains("circular")
    )

  @Test
  def scanGtMaterialsReportsUnresolvedAliasTargetAsDiagnostic(): Unit =
    val archive = materialArchive(
      declarations = "Carbon, Hydrogen",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |Hydrogen = GTMaterials.Oxygen;
          |""".stripMargin
    )

    val rendered = materialDiagnostics(materialSource)(archive)

    assertTrue(rendered.contains("Hydrogen"))
    assertTrue(
      rendered.toLowerCase.contains("unresolved") ||
        rendered.toLowerCase.contains("cannot resolve") ||
        rendered.toLowerCase.contains("not a member")
    )

  @Test
  def scanGtMaterialsKeepsResolvedPartialRefsWhenDiagnosticsExist(): Unit =
    val archive = materialArchive(
      declarations = "Carbon, Hydrogen, Oxygen",
      assignments = """
          |Carbon = new Material.Builder(GTCEu.id("carbon"))
          |  .buildAndRegister();
          |Hydrogen = GTMaterials.Oxygen;
          |""".stripMargin
    )

    val result =
      GtceuSourceScanners.scanGtMaterials(materialSource)(archive)

    result match
      case Ior.Both(diagnostics, refs) =>
        assertEquals(Vector("Carbon"), refs.map(_.name))
        val rendered = diagnostics.iterator.map(_.render).mkString("\n")
        assertTrue(rendered.contains("Hydrogen"))
        assertTrue(rendered.contains("Oxygen"))
      case Ior.Left(diagnostics) =>
        fail(
          "expected partial refs with diagnostics, got Left:\n" +
            diagnostics.iterator.map(_.render).mkString("\n")
        )
      case Ior.Right(refs) =>
        fail(
          s"expected diagnostics with partial refs, got Right: " +
            s"${refs.map(_.name).mkString(", ")}"
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
    GtceuSourceScanners.scanGtMaterials(spec)(archive) match
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
    GtceuSourceScanners.scanGtMaterials(spec)(archive) match
      case Ior.Left(diagnostics) =>
        diagnostics.iterator.map(_.render).mkString("\n")
      case Ior.Both(diagnostics, _) =>
        diagnostics.iterator.map(_.render).mkString("\n")
      case Ior.Right(_) =>
        fail("expected material diagnostics, got a successful scan result")
        throw new AssertionError("unreachable")

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

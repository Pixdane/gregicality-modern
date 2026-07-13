package com.pixdane.gregicality.symbolgen

import com.pixdane.gregicality.symbolgen.gtceu.*
import com.pixdane.gregicality.symbolgen.model.*
import com.pixdane.gregicality.codegen.dsl.model.{ResourceId, ScalaSymbolPath}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
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

    val refs = GtceuSourceScanners.scanGtMaterials(materialSource)(archive)

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

    val refs = GtceuSourceScanners.scanGtMaterials(materialSource)(archive)

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

    val error = assertThrows(
      classOf[IllegalArgumentException],
      () => GtceuSourceScanners.scanGtMaterials(materialSource)(archive)
    )

    assertTrue(
      error.getMessage.contains(
        "without a recognized builder or alias assignment"
      )
    )
    assertTrue(error.getMessage.contains("Hydrogen"))

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

    val refs = GtceuSourceScanners.scanGtMaterials(materialSource)(archive)

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

    val refs = GtceuSourceScanners.scanGtMaterials(materialSource)(archive)

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

    val error = assertThrows(
      classOf[IllegalArgumentException],
      () => GtceuSourceScanners.scanGtMaterials(materialSource)(archive)
    )

    assertTrue(
      error.getMessage.contains("duplicate GTCEu material assignments")
    )
    assertTrue(error.getMessage.contains("Carbon"))

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

    val error = assertThrows(
      classOf[IllegalArgumentException],
      () => GtceuSourceScanners.scanGtMaterials(materialSource)(archive)
    )

    assertTrue(
      error.getMessage.contains("duplicate GTCEu material registry ids")
    )
    assertTrue(error.getMessage.contains("gtceu:shared"))
    assertTrue(error.getMessage.contains("Carbon"))
    assertTrue(error.getMessage.contains("Hydrogen"))

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

    val error = assertThrows(
      classOf[IllegalArgumentException],
      () => GtceuSourceScanners.scanGtMaterials(materialSource)(archive)
    )
    val message = error.getMessage

    assertTrue(message.contains("duplicate GTCEu material assignments"))
    assertTrue(message.contains("duplicate GTCEu material registry ids"))
    assertTrue(
      message.contains(
        "without a recognized builder or alias assignment"
      )
    )
    assertTrue(
      message.contains(s"${materialSource.assignmentDir}TestMaterials.java:")
    )
    assertTrue(message.contains(s"${materialSource.declarationPath}:"))
    assertTrue(
      message.indexOf("duplicate GTCEu material assignments") <
        message.indexOf("duplicate GTCEu material registry ids")
    )
    assertTrue(
      message.indexOf("duplicate GTCEu material registry ids") <
        message.indexOf("without a recognized builder or alias assignment")
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

    val refs = GtceuSourceScanners.scanGtMaterials(materialSource)(archive)

    assertEquals(Vector("Limonite", "YellowLimonite"), refs.map(_.name))
    assertEquals(refs.head.id, refs.last.id)
    assertEquals("YellowLimonite", refs.last.path.parts.last)

  private def assertUnsupportedMaterialAssignment(
      archive: SourceArchive,
      materialName: String,
      reason: String
  ): Unit =
    val error = assertThrows(
      classOf[IllegalArgumentException],
      () => GtceuSourceScanners.scanGtMaterials(materialSource)(archive)
    )

    assertTrue(
      error.getMessage.contains("unsupported GTCEu material assignments")
    )
    assertTrue(error.getMessage.contains(materialName))
    assertTrue(error.getMessage.contains(reason))
    assertTrue(
      error.getMessage.contains(
        s"${materialSource.assignmentDir}TestMaterials.java:"
      )
    )

  private val materialSource = GtMaterialsScanSpec(
    declarationPath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
    assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
    ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
    namespace = "gtceu"
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
      GtceuSourceScanners.scanGtMaterials(
        GtMaterialsScanSpec(
          declarationPath =
            "com/gregtechceu/gtceu/common/data/GTMaterials.java",
          assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
          ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
          namespace = "gtceu"
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

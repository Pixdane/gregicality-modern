package com.pixdane.gregicality.symbolgen

import org.junit.jupiter.api.Assertions.assertEquals
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
            |  public static final MaterialIconSet METALLIC = new MaterialIconSet("metallic");
            |  static final MaterialIconSet HIDDEN = new MaterialIconSet("hidden");
            |}
            |""".stripMargin
      )
    )

    val refs =
      GtceuSourceScanners.scanStaticMembers(
        StaticMemberSource(
          sourcePath = "MaterialIconSet.java",
          ownerFqcn =
            "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet",
          memberTypeSimpleName = "MaterialIconSet"
        )
      )(archive)

    assertEquals(Vector("DULL", "METALLIC"), refs.map(_.name))
    assertEquals(None, refs.head.id)
    assertEquals(
      ScalaPath(
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
        GtMaterialsSource(
          declarationPath =
            "com/gregtechceu/gtceu/common/data/GTMaterials.java",
          assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
          ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
          namespace = "gtceu"
        )
      )(archive)

    assertEquals(Vector("Carbon", "PolyvinylChloride"), refs.map(_.name))
    assertEquals(Some(ResourceId("gtceu", "carbon")), refs.head.id)
    assertEquals(
      ScalaPath(
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

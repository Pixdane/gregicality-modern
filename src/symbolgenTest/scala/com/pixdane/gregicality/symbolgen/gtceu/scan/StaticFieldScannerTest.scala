package com.pixdane.gregicality.symbolgen.gtceu.scan

import com.pixdane.gregicality.core.refs.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import org.junit.jupiter.api.Assertions.assertEquals
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

    val refs = StaticFieldScanner.scan(
      StaticFieldScanSpec(
        sourcePath = "MaterialIconSet.java",
        ownerFqcn =
          "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet",
        memberTypeSimpleName = "MaterialIconSet"
      )
    )(archive)

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

package com.pixdane.gregicality.symbolgen.backends.gtceu

import cats.data.Ior
import com.pixdane.gregicality.symbolgen.framework.SourceArchive
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GtceuBackendTest:
  @Test
  def generateRunsEveryJobAndAppendsTheAggregateFile(): Unit =
    val archive = SourceArchive(
      Map(
        "com/gregtechceu/gtceu/common/data/GTMaterials.java" ->
          """
            |public class GTMaterials {
            |  public static final List<MaterialFlag> STD_METAL = new ArrayList<>();
            |  public static Material Carbon;
            |  static {
            |    STD_METAL.add(GENERATE_PLATE);
            |  }
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/common/data/materials/TestMaterials.java" ->
          """
            |public class TestMaterials {
            |  void register() {
            |    Carbon = new Material.Builder(GTCEu.id("carbon"))
            |      .buildAndRegister();
            |  }
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/common/data/GTElements.java" ->
          """
            |public class GTElements {
            |  public static final Element HYDROGEN = null;
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialIconSet.java" ->
          """
            |public class MaterialIconSet {
            |  public static final MaterialIconSet DULL = null;
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/api/fluids/attribute/FluidAttributes.java" ->
          """
            |public class FluidAttributes {
            |  public static final FluidAttribute ACID = null;
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/api/fluids/store/FluidStorageKeys.java" ->
          """
            |public class FluidStorageKeys {
            |  public static final FluidStorageKey LIQUID = null;
            |}
            |""".stripMargin,
        "com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialFlags.java" ->
          """
            |public class MaterialFlags {
            |  public static final MaterialFlag GENERATE_PLATE =
            |      new MaterialFlag.Builder("generate_plate")
            |          .requireProps(PropertyKey.DUST)
            |          .build();
            |}
            |""".stripMargin
      )
    )

    GtceuBackend.generate(archive) match
      case Ior.Right(files) =>
        assertEquals(
          Vector(
            "GTMaterialsRef.scala",
            "GTElementsRef.scala",
            "MaterialIconSetsRef.scala",
            "FluidAttributesRef.scala",
            "FluidStorageKeysRef.scala",
            "MaterialFlagsRef.scala",
            "MaterialFlagPresetsRef.scala",
            "GTRefs.scala"
          ),
          files.map(file => file.relativePath.split('/').last)
        )
        val fluidStorageKeys = files
          .find(_.relativePath.endsWith("/FluidStorageKeysRef.scala"))
          .getOrElse(fail("expected FluidStorageKeysRef.scala"))
        assertTrue(
          fluidStorageKeys.content.contains(
            "def LIQUID: FluidStorageKeyRef ="
          )
        )
        assertTrue(
          fluidStorageKeys.content.contains(
            "\"api\", \"fluids\", \"store\", \"FluidStorageKeys\", \"LIQUID\""
          )
        )
        val aggregate = files
          .find(_.relativePath.endsWith("/GTRefs.scala"))
          .getOrElse(fail("expected GTRefs.scala"))
        assertTrue(
          aggregate.content.contains("export FluidStorageKeysRef.*")
        )
      case Ior.Left(diagnostics) =>
        fail(
          "expected generated files, got diagnostics:\n" +
            diagnostics.iterator.map(_.render).mkString("\n")
        )
      case Ior.Both(diagnostics, files) =>
        fail(
          "expected clean generation, got diagnostics:\n" +
            diagnostics.iterator.map(_.render).mkString("\n") +
            "\nfiles: " + files.map(_.relativePath).mkString(", ")
        )

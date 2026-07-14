package com.pixdane.gregicality.symbolgen.backends.gtceu

import cats.data.Ior
import com.pixdane.gregicality.symbolgen.framework.SourceArchive
import org.junit.jupiter.api.Assertions.{assertEquals, fail}
import org.junit.jupiter.api.Test

class GtceuBackendTest:
  @Test
  def generateRunsEveryJobAndAppendsTheAggregateFile(): Unit =
    val archive = SourceArchive(
      Map(
        "com/gregtechceu/gtceu/common/data/GTMaterials.java" ->
          """
            |public class GTMaterials {
            |  public static Material Carbon;
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
        "com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialFlags.java" ->
          """
            |public class MaterialFlags {
            |  public static final MaterialFlag DUST = null;
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
            "MaterialFlagsRef.scala",
            "GTRefs.scala"
          ),
          files.map(file => file.relativePath.split('/').last)
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

package com.pixdane.gregicality.symbolgen

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.job.RefJob
import com.pixdane.gregicality.symbolgen.render.RefObjectTarget
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialAliasRef,
  ScannedPathRef,
  ScannedRegisteredMaterialRef
}
import com.pixdane.gregicality.codegen.dsl.model.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.render.RefObjectRenderer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RefObjectRendererTest:
  @Test
  def renderWithIdRefObject(): Unit =
    val job = RefJob.Materials(
      id = "gt-materials",
      scan = _ =>
        Vector(
          ScannedRegisteredMaterialRef(
            name = "Carbon",
            id = ResourceId("gtceu", "carbon"),
            path = ScalaSymbolPath(
              Vector(
                "com",
                "gregtechceu",
                "gtceu",
                "common",
                "data",
                "GTMaterials",
                "Carbon"
              )
            )
          ),
          ScannedMaterialAliasRef(
            name = "Charcoal",
            id = ResourceId("gtceu", "carbon"),
            path = ScalaSymbolPath(Vector("GTMaterials", "Charcoal"))
          )
        ),
      objectTarget = RefObjectTarget(
        outputPackage = "com.pixdane.gregicality.codegen.dsl.refs.gtceu",
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef"
      )
    )

    val file = RefObjectRenderer.generateFile(job, SourceArchive(Map.empty))

    assertEquals(
      "com/pixdane/gregicality/codegen/dsl/refs/gtceu/GTMaterialsRef.scala",
      file.relativePath
    )
    assertEquals(
      """|package com.pixdane.gregicality.codegen.dsl.refs.gtceu
         |
         |import com.pixdane.gregicality.codegen.dsl.model.*
         |
         |object GTMaterialsRef:
         |  def Carbon: MaterialRef =
         |    MaterialRef(
         |      ResourceId("gtceu", "carbon"),
         |      ScalaSymbolPath(Vector("com", "gregtechceu", "gtceu", "common", "data", "GTMaterials", "Carbon"))
         |    )
         |
         |  def Charcoal: MaterialRef =
         |    MaterialRef(
         |      ResourceId("gtceu", "carbon"),
         |      ScalaSymbolPath(Vector("GTMaterials", "Charcoal"))
         |    )
         |
         |  def resolve(id: ResourceId): Option[MaterialRef] =
         |    byIdIndex.get(id)
         |
         |  private lazy val byIdIndex: Map[ResourceId, MaterialRef] =
         |    byIdEntries.iterator.map(ref => ref.id -> ref).toMap
         |
         |  private def byIdEntries: Vector[MaterialRef] =
         |    byIdEntries0
         |
         |  private def byIdEntries0: Vector[MaterialRef] =
         |    Vector(Carbon)
         |""".stripMargin,
      file.content
    )

  @Test
  def renderMaterialIndexSplitsAfterTwoHundredRegisteredRefs(): Unit =
    val refs = Vector.tabulate(201) { index =>
      val name = f"Material$index%03d"
      ScannedRegisteredMaterialRef(
        name = name,
        id = ResourceId("gtceu", s"material_$index"),
        path = ScalaSymbolPath(Vector("GTMaterials", name))
      )
    }
    val job = RefJob.Materials(
      id = "gt-materials",
      scan = _ => refs,
      objectTarget = RefObjectTarget(
        outputPackage = "com.pixdane.gregicality.codegen.dsl.refs.gtceu",
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef"
      )
    )

    val content =
      RefObjectRenderer.generateFile(job, SourceArchive(Map.empty)).content
    val firstChunk =
      Vector
        .tabulate(200)(index => f"Material$index%03d")
        .mkString("    Vector(", ", ", ")")

    assertEquals(
      201,
      content.linesIterator.count(_.startsWith("  def Material"))
    )
    assertTrue(
      content.contains(
        "  private def byIdEntries: Vector[MaterialRef] =\n" +
          "    byIdEntries0 ++ byIdEntries1"
      )
    )
    assertTrue(content.contains(firstChunk))
    assertTrue(content.contains("    Vector(Material200)"))
    assertTrue(!content.contains("byIdEntries2"))

  @Test
  def renderPathOnlyRefObject(): Unit =
    val job = RefJob.Paths(
      id = "material-icon-sets",
      scan = _ =>
        Vector(
          ScannedPathRef(
            name = "METALLIC",
            path = ScalaSymbolPath(
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
                "METALLIC"
              )
            )
          )
        ),
      objectTarget = RefObjectTarget(
        outputPackage = "com.pixdane.gregicality.codegen.dsl.refs.gtceu",
        outputObject = "MaterialIconSetsRef",
        valueType = "MaterialIconRef"
      )
    )

    val file = RefObjectRenderer.generateFile(job, SourceArchive(Map.empty))

    assertTrue(file.content.contains("object MaterialIconSetsRef:"))
    assertTrue(file.content.contains("def METALLIC: MaterialIconRef ="))
    assertTrue(file.content.contains("MaterialIconRef(ScalaSymbolPath(Vector("))
    assertTrue(!file.content.contains("def all:"))
    assertTrue(!file.content.contains("byIdIndex"))

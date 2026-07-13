package com.pixdane.gregicality.symbolgen

import com.pixdane.gregicality.symbolgen.render.RefObjectTarget
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialRef,
  ScannedPathRef
}
import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import com.pixdane.gregicality.symbolgen.render.RefObjectRenderer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RefObjectRendererTest:
  @Test
  def renderWithIdRefObject(): Unit =
    val target = RefObjectTarget(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "GTMaterialsRef",
      valueType = "MaterialRef"
    )
    val refs = Vector(
      ScannedMaterialRef(
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
      )
    )

    val file = RefObjectRenderer.generateMaterialFile(target, refs)

    assertEquals(
      "com/pixdane/gregicality/core/refs/gtceu/GTMaterialsRef.scala",
      file.relativePath
    )
    assertEquals(
      """|package com.pixdane.gregicality.core.refs.gtceu
         |
         |import com.pixdane.gregicality.core.refs.*
         |
         |object GTMaterialsRef:
         |  def Carbon: MaterialRef =
         |    MaterialRef(
         |      ResourceId("gtceu", "carbon"),
         |      ScalaSymbolPath(Vector("com", "gregtechceu", "gtceu", "common", "data", "GTMaterials", "Carbon"))
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
      ScannedMaterialRef(
        name = name,
        id = ResourceId("gtceu", s"material_$index"),
        path = ScalaSymbolPath(Vector("GTMaterials", name))
      )
    }
    val target = RefObjectTarget(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "GTMaterialsRef",
      valueType = "MaterialRef"
    )

    val content =
      RefObjectRenderer.generateMaterialFile(target, refs).content
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
    val target = RefObjectTarget(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "MaterialIconSetsRef",
      valueType = "MaterialIconRef"
    )
    val refs = Vector(
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
    )

    val file = RefObjectRenderer.generatePathFile(target, refs)

    assertTrue(file.content.contains("object MaterialIconSetsRef:"))
    assertTrue(file.content.contains("def METALLIC: MaterialIconRef ="))
    assertTrue(file.content.contains("MaterialIconRef(ScalaSymbolPath(Vector("))
    assertTrue(!file.content.contains("def all:"))
    assertTrue(!file.content.contains("byIdIndex"))

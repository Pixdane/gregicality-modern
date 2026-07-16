package com.pixdane.gregicality.symbolgen.render

import com.pixdane.gregicality.symbolgen.framework.{
  GeneratedScalaFile,
  RefOutputSpec,
  ScannedMaterialFlagPresetRef,
  ScannedMaterialFlagRef,
  ScannedMaterialRef,
  ScannedPathRef
}
import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RefOutputRendererTest:
  @Test
  def renderWithIdRefObject(): Unit =
    val target = RefOutputSpec(
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

    val file = RefOutputRenderer.generateMaterialFile(target, refs)

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
    val target = RefOutputSpec(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "GTMaterialsRef",
      valueType = "MaterialRef"
    )

    val content =
      RefOutputRenderer.generateMaterialFile(target, refs).content
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
  def renderMaterialFileHandlesEmptyRefs(): Unit =
    val target = RefOutputSpec(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "GTMaterialsRef",
      valueType = "MaterialRef"
    )

    val content =
      RefOutputRenderer.generateMaterialFile(target, Vector.empty).content

    assertTrue(
      content.contains(
        "  private def byIdEntries: Vector[MaterialRef] =\n" +
          "    Vector.empty"
      )
    )
    assertTrue(!content.contains("byIdEntries0"))

  @Test
  def renderPathOnlyRefObject(): Unit =
    val target = RefOutputSpec(
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

    val file = RefOutputRenderer.generatePathFile(target, refs)

    assertTrue(file.content.contains("object MaterialIconSetsRef:"))
    assertTrue(file.content.contains("def METALLIC: MaterialIconRef ="))
    assertTrue(file.content.contains("MaterialIconRef(ScalaSymbolPath(Vector("))
    assertTrue(!file.content.contains("def all:"))
    assertTrue(!file.content.contains("byIdIndex"))

  @Test
  def renderMaterialFlagsKeepsAccessorsAndAddsRequirementsLookup(): Unit =
    val target = RefOutputSpec(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "MaterialFlagsRef",
      valueType = "MaterialFlagRef"
    )
    val owner = Vector("com", "example", "MaterialFlags")
    val propertyOwner = Vector("com", "example", "PropertyKey")
    val refs = Vector(
      ScannedMaterialFlagRef(
        name = "GENERATE_GEAR",
        path = ScalaSymbolPath(owner :+ "GENERATE_GEAR"),
        requiredFlags = Vector(
          ScalaSymbolPath(owner :+ "GENERATE_PLATE"),
          ScalaSymbolPath(owner :+ "GENERATE_ROD")
        ),
        requiredProperties = Vector(
          ScalaSymbolPath(propertyOwner :+ "DUST")
        )
      )
    )

    val content =
      RefOutputRenderer.generateMaterialFlagFile(target, refs).content

    assertTrue(content.contains("def GENERATE_GEAR: MaterialFlagRef ="))
    assertTrue(
      content.contains(
        "def requirements(flag: MaterialFlagRef): Option[MaterialFlagRequirements]"
      )
    )
    assertTrue(content.contains("requiredFlags = Vector("))
    assertTrue(
      content.contains("MaterialPropertyKeyRef(ScalaSymbolPath(Vector(")
    )
  end renderMaterialFlagsKeepsAccessorsAndAddsRequirementsLookup

  @Test
  def renderMaterialFlagPresetsAddsFlattenedMembersLookup(): Unit =
    val target = RefOutputSpec(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "MaterialFlagPresetsRef",
      valueType = "MaterialFlagPresetRef"
    )
    val refs = Vector(
      ScannedMaterialFlagPresetRef(
        name = "STD_METAL",
        path =
          ScalaSymbolPath(Vector("com", "example", "GTMaterials", "STD_METAL")),
        members = Vector(
          ScalaSymbolPath(
            Vector("com", "example", "MaterialFlags", "GENERATE_PLATE")
          )
        )
      )
    )

    val content =
      RefOutputRenderer.generateMaterialFlagPresetFile(target, refs).content

    assertTrue(content.contains("def STD_METAL: MaterialFlagPresetRef ="))
    assertTrue(
      content.contains(
        "def members(preset: MaterialFlagPresetRef): Option[Vector[MaterialFlagRef]]"
      )
    )
    assertTrue(content.contains("MaterialFlagsRef.GENERATE_PLATE"))

package com.pixdane.gregicality.symbolgen

import com.pixdane.gregicality.symbolgen.model.*
import com.pixdane.gregicality.symbolgen.render.RefObjectRenderer
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

    assertTrue(file.relativePath.endsWith("GTMaterialsRef.scala"))
    assertTrue(file.content.contains("object GTMaterialsRef:"))
    assertTrue(file.content.contains("def Carbon: MaterialRef ="))
    assertTrue(file.content.contains("def Charcoal: MaterialRef ="))
    assertTrue(file.content.contains("""ResourceId("gtceu", "carbon")"""))
    assertTrue(
      file.content.contains(
        "def resolve(id: ResourceId): Option[MaterialRef] ="
      )
    )
    assertTrue(file.content.contains("private lazy val byIdIndex:"))
    assertTrue(file.content.contains("Map(Carbon.id -> Carbon)"))
    assertTrue(!file.content.contains("Charcoal.id -> Charcoal"))
    assertTrue(!file.content.contains("def all:"))
    assertTrue(
      file.content.contains(
        """ScalaSymbolPath(Vector("com", "gregtechceu", "gtceu", "common", "data", "GTMaterials", "Carbon"))"""
      )
    )

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

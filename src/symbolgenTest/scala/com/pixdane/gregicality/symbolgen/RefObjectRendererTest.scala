package com.pixdane.gregicality.symbolgen

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RefObjectRendererTest:
  @Test
  def renderWithIdRefObject(): Unit =
    val job = RefJob(
      id = "gt-materials",
      source = _ =>
        Vector(
          ScannedRef(
            name = "Carbon",
            id = Some(ResourceId("gtceu", "carbon")),
            path = ScalaPath(
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
        ),
      target = RefObjectTarget(
        outputPackage = "com.pixdane.gregicality.codegen.dsl.refs",
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef",
        renderKind = RefRenderKind.WithId
      )
    )

    val file = RefObjectRenderer.generateFile(job, SourceArchive(Map.empty))

    assertTrue(file.relativePath.endsWith("GTMaterialsRef.scala"))
    assertTrue(file.content.contains("object GTMaterialsRef:"))
    assertTrue(file.content.contains("def Carbon: MaterialRef ="))
    assertTrue(file.content.contains("""ResourceId("gtceu", "carbon")"""))
    assertTrue(
      file.content.contains(
        """ScalaPath(Vector("com", "gregtechceu", "gtceu", "common", "data", "GTMaterials", "Carbon"))"""
      )
    )

  @Test
  def renderPathOnlyRefObject(): Unit =
    val job = RefJob(
      id = "material-icon-sets",
      source = _ =>
        Vector(
          ScannedRef(
            name = "METALLIC",
            id = None,
            path = ScalaPath(
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
      target = RefObjectTarget(
        outputPackage = "com.pixdane.gregicality.codegen.dsl.refs",
        outputObject = "MaterialIconSetsRef",
        valueType = "MaterialIconRef",
        renderKind = RefRenderKind.PathOnly
      )
    )

    val file = RefObjectRenderer.generateFile(job, SourceArchive(Map.empty))

    assertTrue(file.content.contains("object MaterialIconSetsRef:"))
    assertTrue(file.content.contains("def METALLIC: MaterialIconRef ="))
    assertTrue(file.content.contains("MaterialIconRef(ScalaPath(Vector("))
    assertTrue(file.content.contains("def all: Vector[MaterialIconRef] ="))
    assertTrue(
      file.content.contains("private def all0: Vector[MaterialIconRef] =")
    )

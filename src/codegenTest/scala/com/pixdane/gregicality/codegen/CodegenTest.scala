package com.pixdane.gregicality.codegen

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime

import com.pixdane.gregicality.codegen.core.materials.*
import com.pixdane.gregicality.core.refs.gtceu.{
  FluidStorageKeysRef,
  MaterialFlagsRef
}
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class CodegenTest:
  @TempDir
  var tempDir: Path = _

  @Test
  def chemistryPolymersKeepsOnlyAuthoredPolyimideContent(): Unit =
    val spec = GCYMaterialSets.chemistryPolymers.declarations.head match
      case MaterialDeclaration.NewMaterial(value) => value
      case declaration                            =>
        fail(s"expected a new material, got $declaration")

    assertEquals("polyimide", spec.id.value)
    assertEquals("Polyimide", spec.field.value)
    assertEquals(None, spec.identity.displayName)
    assertEquals(
      Some(1),
      spec.properties.dust.flatMap(_.harvestLevel).map(_.value)
    )
    assertEquals(None, spec.properties.dust.flatMap(_.burnTime))
    assertEquals(Some(PolymerPropertySpec()), spec.properties.polymer)
    assertEquals(None, spec.properties.ingot)
    assertEquals(
      Vector(FluidStorageKeysRef.LIQUID),
      spec.properties.fluid.toVector.flatMap(_.fluids.toVector.map(_.key))
    )
    assertEquals(
      Vector(
        "Carbon" -> 22,
        "Hydrogen" -> 12,
        "Nitrogen" -> 2,
        "Oxygen" -> 6
      ),
      spec.composition.components.map(component =>
        component.material.path.parts.last -> component.amount.value
      )
    )
    assertEquals(None, spec.composition.formulaOverride)
    assertEquals(Set(MaterialFlagsRef.GENERATE_PLATE), spec.flags.flags)
    assertEquals(Vector.empty, spec.flags.presets)
    assertEquals(
      FluidColorPolicy.InheritMaterial,
      spec.visuals.fluidColor
    )

  @Test
  def generateFilesRendersTheMaterialPackageAndIndex(): Unit =
    val files = Codegen.generateFiles()

    assertEquals(
      Vector(
        "com/pixdane/gregicality/common/data/materials/GCYMaterialsChemistryPolymers.scala",
        "com/pixdane/gregicality/common/data/materials/GCYMaterialsGeneratedIndex.scala"
      ),
      files.map(_.relativePath)
    )
    assertEquals(
      golden("chemistry-polymers.scala.golden"),
      files(0).content
    )
    assertEquals(golden("generated-index.scala.golden"), files(1).content)

  @Test
  def runLeavesUnchangedGeneratedSourcesUntouched(): Unit =
    val outputDir = tempDir.resolve("materials")
    val packageDir =
      outputDir.resolve("com/pixdane/gregicality/common/data/materials")
    val materialFile =
      packageDir.resolve("GCYMaterialsChemistryPolymers.scala")
    val indexFile = packageDir.resolve("GCYMaterialsGeneratedIndex.scala")

    Codegen.run(CodegenArgs(outputDir))
    Files.setLastModifiedTime(materialFile, FileTime.fromMillis(1_000_000))
    Files.setLastModifiedTime(indexFile, FileTime.fromMillis(2_000_000))
    Files.setLastModifiedTime(outputDir, FileTime.fromMillis(3_000_000))
    val materialTime = Files.getLastModifiedTime(materialFile)
    val indexTime = Files.getLastModifiedTime(indexFile)
    val outputTime = Files.getLastModifiedTime(outputDir)

    Codegen.run(CodegenArgs(outputDir))

    assertEquals(materialTime, Files.getLastModifiedTime(materialFile))
    assertEquals(indexTime, Files.getLastModifiedTime(indexFile))
    assertEquals(outputTime, Files.getLastModifiedTime(outputDir))

  private def golden(name: String): String =
    val path = s"/com/pixdane/gregicality/codegen/golden/$name"
    val stream = Option(getClass.getResourceAsStream(path))
      .getOrElse(throw new AssertionError(s"missing golden resource $path"))
    try String(stream.readAllBytes(), StandardCharsets.UTF_8)
    finally stream.close()

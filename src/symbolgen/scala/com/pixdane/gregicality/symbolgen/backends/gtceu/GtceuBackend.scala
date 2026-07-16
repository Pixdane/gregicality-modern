package com.pixdane.gregicality.symbolgen.backends.gtceu

import cats.data.Ior
import cats.implicits.*

import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult,
  GtMaterialsScanSpec,
  MaterialFlagPresetScanSpec,
  MaterialFlagScanSpec,
  StaticFieldScanner,
  StaticFieldScanSpec
}
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.flags.{
  MaterialFlagPresetScanner,
  MaterialFlagScanner
}
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.materials.{
  MaterialScanInput,
  MaterialScanner
}
import com.pixdane.gregicality.symbolgen.framework.{
  GeneratedScalaFile,
  RefOutputSpec,
  ScannedMaterialFlagPresetRef,
  ScannedMaterialFlagRef,
  ScannedMaterialRef,
  ScannedPathRef,
  SourceArchive,
  SymbolGenerator,
  SymbolJob
}
import com.pixdane.gregicality.symbolgen.render.{
  RefAggregateRenderer,
  RefOutputRenderer
}

/** The GTCEu symbol generator backend.
  *
  * Owns the output package, the six symbol jobs (materials, elements, material
  * icon sets, fluid attributes, material flags, flag presets), and the
  * aggregate `GTRefs` object that re-exports them. Externally only `generator`
  * (the [[SymbolGenerator]] registered with the registry) is needed; everything
  * else is private to this object.
  */
object GtceuBackend:
  private val OutputPackage =
    "com.pixdane.gregicality.core.refs.gtceu"

  private val AggregateObject = "GTRefs"

  private val gtMaterials: SymbolJob[
    GtceuScanDiagnostic,
    MaterialScanInput,
    Vector[ScannedMaterialRef]
  ] =
    val spec = GtMaterialsScanSpec(
      declarationPath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
      assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
      namespace = "gtceu",
      idFactoryFqcn = "com.gregtechceu.gtceu.GTCEu"
    )

    SymbolJob(
      id = "gt-materials",
      target = RefOutputSpec(
        outputPackage = OutputPackage,
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef"
      ),
      scan = MaterialScanner.scan(spec),
      preprocess = MaterialScanner.preprocess,
      render = RefOutputRenderer.generateMaterialFile
    )

  private val gtElements =
    staticPathOnly(
      id = "gt-elements",
      outputObject = "GTElementsRef",
      valueType = "ElementRef",
      sourcePath = "com/gregtechceu/gtceu/common/data/GTElements.java",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTElements",
      memberTypeSimpleName = "Element"
    )

  private val materialIconSets =
    staticPathOnly(
      id = "material-icon-sets",
      outputObject = "MaterialIconSetsRef",
      valueType = "MaterialIconRef",
      sourcePath =
        "com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialIconSet.java",
      ownerFqcn =
        "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet",
      memberTypeSimpleName = "MaterialIconSet"
    )

  private val fluidAttributes =
    staticPathOnly(
      id = "fluid-attributes",
      outputObject = "FluidAttributesRef",
      valueType = "FluidAttributeRef",
      sourcePath =
        "com/gregtechceu/gtceu/api/fluids/attribute/FluidAttributes.java",
      ownerFqcn = "com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes",
      memberTypeSimpleName = "FluidAttribute"
    )

  private val materialFlags: SymbolJob[
    GtceuScanDiagnostic,
    Vector[ScannedMaterialFlagRef],
    Vector[ScannedMaterialFlagRef]
  ] =
    val spec = MaterialFlagScanSpec(
      sourcePath =
        "com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialFlags.java",
      ownerFqcn =
        "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags",
      propertyKeyOwnerFqcn =
        "com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey"
    )

    SymbolJob(
      id = "material-flags",
      target = RefOutputSpec(
        outputPackage = OutputPackage,
        outputObject = "MaterialFlagsRef",
        valueType = "MaterialFlagRef"
      ),
      scan = MaterialFlagScanner.scan(spec),
      preprocess = passThrough,
      render = RefOutputRenderer.generateMaterialFlagFile
    )

  private val materialFlagPresets: SymbolJob[
    GtceuScanDiagnostic,
    Vector[ScannedMaterialFlagPresetRef],
    Vector[ScannedMaterialFlagPresetRef]
  ] =
    val spec = MaterialFlagPresetScanSpec(
      sourcePath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
      flagOwnerFqcn =
        "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags"
    )

    SymbolJob(
      id = "material-flag-presets",
      target = RefOutputSpec(
        outputPackage = OutputPackage,
        outputObject = "MaterialFlagPresetsRef",
        valueType = "MaterialFlagPresetRef"
      ),
      scan = MaterialFlagPresetScanner.scan(spec),
      preprocess = passThrough,
      render = RefOutputRenderer.generateMaterialFlagPresetFile
    )

  private val jobs: Vector[SymbolJob[GtceuScanDiagnostic, ?, ?]] =
    Vector(
      gtMaterials,
      gtElements,
      materialIconSets,
      fluidAttributes,
      materialFlags,
      materialFlagPresets
    )

  private def staticPathOnly(
      id: String,
      outputObject: String,
      valueType: String,
      sourcePath: String,
      ownerFqcn: String,
      memberTypeSimpleName: String
  ): SymbolJob[
    GtceuScanDiagnostic,
    Vector[ScannedPathRef],
    Vector[ScannedPathRef]
  ] =
    val spec = StaticFieldScanSpec(
      sourcePath = sourcePath,
      ownerFqcn = ownerFqcn,
      memberTypeSimpleName = memberTypeSimpleName
    )

    SymbolJob(
      id = id,
      target = RefOutputSpec(
        outputPackage = OutputPackage,
        outputObject = outputObject,
        valueType = valueType
      ),
      scan = StaticFieldScanner.scan(spec),
      preprocess = passThrough,
      render = RefOutputRenderer.generatePathFile
    )

  private def passThrough[A](value: A): GtceuScanResult[A] =
    Ior.right(value)

  private def aggregateFile: GeneratedScalaFile =
    RefAggregateRenderer.generateFile(
      outputPackage = OutputPackage,
      outputObject = AggregateObject,
      exports = jobs.map(_.target.outputObject)
    )

  def generate(
      archive: SourceArchive
  ): GtceuScanResult[Vector[GeneratedScalaFile]] =
    jobs
      .traverse(_.run(archive))
      .map(refs => refs :+ aggregateFile)

  val generator: SymbolGenerator[GtceuScanDiagnostic] =
    SymbolGenerator(
      kind = "gtceu",
      generate = generate
    )
end GtceuBackend

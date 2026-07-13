package com.pixdane.gregicality.symbolgen.gtceu

import cats.data.Ior

import com.pixdane.gregicality.symbolgen.gtceu.scan.{
  GtceuScanDiagnostic,
  GtceuScanResult,
  GtMaterialsScanSpec,
  StaticFieldScanSpec
}
import com.pixdane.gregicality.symbolgen.gtceu.scan.materials.{
  MaterialScanInput,
  MaterialScanner
}
import com.pixdane.gregicality.symbolgen.job.SymbolJob
import com.pixdane.gregicality.symbolgen.render.{
  RefObjectRenderer,
  RefObjectTarget
}
import com.pixdane.gregicality.symbolgen.scan.ScannedMaterialRef
import com.pixdane.gregicality.symbolgen.scan.ScannedPathRef

object GtceuRefJobs:
  private val OutputPackage =
    "com.pixdane.gregicality.core.refs.gtceu"

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
      target = RefObjectTarget(
        outputPackage = OutputPackage,
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef"
      ),
      scan = MaterialScanner.scan(spec),
      preprocess = MaterialScanner.preprocess,
      render = RefObjectRenderer.generateMaterialFile
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

  private val materialFlags =
    staticPathOnly(
      id = "material-flags",
      outputObject = "MaterialFlagsRef",
      valueType = "MaterialFlagRef",
      sourcePath =
        "com/gregtechceu/gtceu/api/data/chemical/material/info/MaterialFlags.java",
      ownerFqcn =
        "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags",
      memberTypeSimpleName = "MaterialFlag"
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
      target = RefObjectTarget(
        outputPackage = OutputPackage,
        outputObject = outputObject,
        valueType = valueType
      ),
      scan = archive =>
        Ior.right(GtceuSourceScanners.scanStaticMembers(spec)(archive)),
      preprocess = passThrough,
      render = RefObjectRenderer.generatePathFile
    )

  private def passThrough[A](value: A): GtceuScanResult[A] =
    Ior.right(value)

  val jobs: Vector[SymbolJob[GtceuScanDiagnostic, ?, ?]] =
    Vector(
      gtMaterials,
      gtElements,
      materialIconSets,
      fluidAttributes,
      materialFlags
    )

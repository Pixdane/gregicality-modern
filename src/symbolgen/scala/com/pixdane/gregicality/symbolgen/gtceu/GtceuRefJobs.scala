package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.render.RefObjectTarget

object GtceuRefJobs:
  private val OutputPackage =
    "com.pixdane.gregicality.codegen.dsl.refs.gtceu"

  private val gtMaterials: GtceuRefJob =
    GtceuRefJob.Materials(
      id = "gt-materials",
      spec = GtMaterialsScanSpec(
        declarationPath = "com/gregtechceu/gtceu/common/data/GTMaterials.java",
        assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
        ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
        namespace = "gtceu"
      ),
      objectTarget = RefObjectTarget(
        outputPackage = OutputPackage,
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef"
      )
    )

  private val gtElements: GtceuRefJob =
    staticPathOnly(
      id = "gt-elements",
      outputObject = "GTElementsRef",
      valueType = "ElementRef",
      sourcePath = "com/gregtechceu/gtceu/common/data/GTElements.java",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTElements",
      memberTypeSimpleName = "Element"
    )

  private val materialIconSets: GtceuRefJob =
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

  private val fluidAttributes: GtceuRefJob =
    staticPathOnly(
      id = "fluid-attributes",
      outputObject = "FluidAttributesRef",
      valueType = "FluidAttributeRef",
      sourcePath =
        "com/gregtechceu/gtceu/api/fluids/attribute/FluidAttributes.java",
      ownerFqcn = "com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes",
      memberTypeSimpleName = "FluidAttribute"
    )

  private val materialFlags: GtceuRefJob =
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
  ): GtceuRefJob =
    GtceuRefJob.Paths(
      id = id,
      spec = StaticFieldScanSpec(
        sourcePath = sourcePath,
        ownerFqcn = ownerFqcn,
        memberTypeSimpleName = memberTypeSimpleName
      ),
      objectTarget = RefObjectTarget(
        outputPackage = OutputPackage,
        outputObject = outputObject,
        valueType = valueType
      )
    )

  val jobs: Vector[GtceuRefJob] =
    Vector(
      gtMaterials,
      gtElements,
      materialIconSets,
      fluidAttributes,
      materialFlags
    )

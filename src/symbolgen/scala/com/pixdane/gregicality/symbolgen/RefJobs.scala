package com.pixdane.gregicality.symbolgen

object RefJobs:
  private val OutputPackage =
    "com.pixdane.gregicality.codegen.dsl.refs"

  private val gtMaterials: RefJob =
    RefJob(
      id = "gt-materials",
      source = GtceuSourceScanners.scanGtMaterials(
        GtMaterialsSource(
          declarationPath =
            "com/gregtechceu/gtceu/common/data/GTMaterials.java",
          assignmentDir = "com/gregtechceu/gtceu/common/data/materials/",
          ownerFqcn = "com.gregtechceu.gtceu.common.data.GTMaterials",
          namespace = "gtceu"
        )
      ),
      target = RefObjectTarget(
        outputPackage = OutputPackage,
        outputObject = "GTMaterialsRef",
        valueType = "MaterialRef",
        renderKind = RefRenderKind.WithId
      )
    )

  private val gtElements: RefJob =
    staticPathOnly(
      id = "gt-elements",
      outputObject = "GTElementsRef",
      valueType = "ElementRef",
      sourcePath = "com/gregtechceu/gtceu/common/data/GTElements.java",
      ownerFqcn = "com.gregtechceu.gtceu.common.data.GTElements",
      memberTypeSimpleName = "Element"
    )

  private val materialIconSets: RefJob =
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

  private val fluidAttributes: RefJob =
    staticPathOnly(
      id = "fluid-attributes",
      outputObject = "FluidAttributesRef",
      valueType = "FluidAttributeRef",
      sourcePath =
        "com/gregtechceu/gtceu/api/fluids/attribute/FluidAttributes.java",
      ownerFqcn = "com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes",
      memberTypeSimpleName = "FluidAttribute"
    )

  private val materialFlags: RefJob =
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
  ): RefJob =
    RefJob(
      id = id,
      source = GtceuSourceScanners.scanStaticMembers(
        StaticMemberSource(
          sourcePath = sourcePath,
          ownerFqcn = ownerFqcn,
          memberTypeSimpleName = memberTypeSimpleName
        )
      ),
      target = RefObjectTarget(
        outputPackage = OutputPackage,
        outputObject = outputObject,
        valueType = valueType,
        renderKind = RefRenderKind.PathOnly
      )
    )

  val all: Vector[RefJob] =
    Vector(
      gtMaterials,
      gtElements,
      materialIconSets,
      fluidAttributes,
      materialFlags
    )

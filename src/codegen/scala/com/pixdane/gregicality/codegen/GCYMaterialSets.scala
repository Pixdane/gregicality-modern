package com.pixdane.gregicality.codegen

import cats.data.NonEmptyVector

import com.pixdane.gregicality.codegen.core.materials.*
import com.pixdane.gregicality.core.refs.gtceu.{
  FluidStorageKeysRef,
  GTMaterialsRef,
  MaterialFlagsRef,
  MaterialIconSetsRef
}

/** Directly authored material packages used by the first codegen integration
  * slice.
  *
  * These values contain only author intent. They do not copy properties, flags,
  * formulas, or display names that GTCEu derives internally.
  */
object GCYMaterialSets:

  /** Polymer registrations in the `chemistry/polymers` migration package. */
  val chemistryPolymers: MaterialSet =
    MaterialSet(
      NonEmptyVector.one(
        MaterialDeclaration.NewMaterial(
          NewMaterialSpec(
            id = valid(RegistryPath.from("polyimide")),
            field = valid(ScalaIdent.from("Polyimide")),
            visuals = VisualSpec(
              primaryColor = ColorSpec.Explicit(valid(HexRgb.from(0xff7f50))),
              iconSet = Some(MaterialIconSetsRef.DULL)
            ),
            composition = CompositionSpec(
              components = Vector(
                component(GTMaterialsRef.Carbon, 22),
                component(GTMaterialsRef.Hydrogen, 12),
                component(GTMaterialsRef.Nitrogen, 2),
                component(GTMaterialsRef.Oxygen, 6)
              )
            ),
            properties = MaterialProperties(
              dust = Some(
                DustPropertySpec(
                  harvestLevel = Some(valid(HarvestLevel.from(1)))
                )
              ),
              polymer = Some(PolymerPropertySpec()),
              fluid = Some(
                FluidPropertySpec(
                  NonEmptyVector.one(
                    FluidEntry(FluidStorageKeysRef.LIQUID)
                  )
                )
              )
            ),
            flags = MaterialFlagSpec(
              flags = Set(MaterialFlagsRef.GENERATE_PLATE)
            )
          )
        )
      )
    )

  private def component(
      material: com.pixdane.gregicality.core.refs.MaterialRef,
      amount: Int
  ): ComponentSpec =
    ComponentSpec(material, valid(PositiveInt.from(amount)))

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      issues =>
        throw new IllegalArgumentException(
          issues.iterator.map(_.message).mkString("\n")
        ),
      identity
    )
end GCYMaterialSets

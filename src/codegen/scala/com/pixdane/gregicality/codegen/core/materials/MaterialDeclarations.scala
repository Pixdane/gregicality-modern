package com.pixdane.gregicality.codegen.core.materials

import cats.data.NonEmptyVector
import com.pixdane.gregicality.core.refs.{FluidStorageKeyRef, MaterialRef}

/** The three material declaration lifecycles implemented in the first slice. */
enum MaterialDeclaration:
  case NewMaterial(spec: NewMaterialSpec)
  case MarkerMaterial(spec: MarkerMaterialSpec)
  case ExistingPatch(spec: MaterialPatchSpec)

/** Non-empty collection of material declarations for validation and rendering.
  */
final case class MaterialSet(
    declarations: NonEmptyVector[MaterialDeclaration]
)

/** Marker material identity; marker materials bypass Material.Builder. */
final case class MarkerMaterialSpec(id: RegistryPath, field: ScalaIdent)

/** Non-empty authored mutations for an already-registered material. */
final case class MaterialPatchSpec(
    target: MaterialRef,
    operations: NonEmptyVector[PatchOperation]
)

/** Post-registration operations implemented by the first renderer slice. */
enum PatchOperation:
  case SetOreByproducts(byproducts: Vector[MaterialRef])
  case SetWashedIn(material: MaterialRef, amount: PositiveInt)
  case SetSeparatedInto(materials: Vector[MaterialRef])
  case SetDirectSmeltResult(material: MaterialRef)
  case SetMagneticMaterial(material: MaterialRef)
  case SetArcSmeltingInto(material: MaterialRef)
  case SetPrimaryKey(fluid: FluidStorageKeyRef)

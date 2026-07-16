package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.{
  FluidStorageKeyRef,
  MaterialRef,
  ScalaSymbolPath
}

/** Runtime-facing location and id factory for one generated material object. */
final case class MaterialOutputSpec(
    packageName: String,
    objectName: ScalaIdent,
    idFactory: ScalaSymbolPath
)

/** Deterministic imports and declaration plans for one generated Scala object.
  */
final case class MaterialPlan(
    output: MaterialOutputSpec,
    imports: Vector[ScalaSymbolPath],
    declarations: Vector[MaterialDeclarationPlan]
)

/** Planned form of one authored material declaration. */
enum MaterialDeclarationPlan:
  case NewMaterial(plan: NewMaterialPlan)
  case MarkerMaterial(plan: MarkerMaterialPlan)
  case ExistingPatch(plan: MaterialPatchPlan)

/** Ordered builder calls and post-build intent for one new material. */
final case class NewMaterialPlan(
    id: RegistryPath,
    field: ScalaIdent,
    builderCalls: Vector[BuilderCall],
    primaryKey: Option[FluidStorageKeyRef]
)

/** Direct constructor plan for a marker material. */
final case class MarkerMaterialPlan(id: RegistryPath, field: ScalaIdent)

/** Ordered post-registration operations for an existing material. */
final case class MaterialPatchPlan(
    target: MaterialRef,
    operations: Vector[PatchOperation]
)

/** One method in a generated builder chain. */
final case class BuilderCall(
    method: String,
    arguments: Vector[ScalaExpr] = Vector.empty
)

/** Small expression vocabulary used by material source plans. */
enum ScalaExpr:
  case StringValue(value: String)
  case IntValue(value: Int)
  case LongValue(value: Long)
  case DoubleValue(value: Double)
  case BooleanValue(value: Boolean)
  case HexValue(value: Int)
  case Symbol(path: ScalaSymbolPath)
  case NewInstance(
      typePath: ScalaSymbolPath,
      calls: Vector[BuilderCall] = Vector.empty
  )
  case Lambda(parameter: String, calls: Vector[BuilderCall])
  case ArrayAccess(array: ScalaSymbolPath, index: ScalaSymbolPath)
  case ToInt(value: ScalaExpr)

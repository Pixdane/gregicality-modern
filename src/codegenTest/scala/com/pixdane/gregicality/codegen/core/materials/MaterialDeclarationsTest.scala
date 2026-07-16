package com.pixdane.gregicality.codegen.core.materials

import cats.data.NonEmptyVector
import com.pixdane.gregicality.core.refs.*
import org.junit.jupiter.api.Assertions.{assertEquals, assertNotEquals}
import org.junit.jupiter.api.Test

class MaterialDeclarationsTest:

  @Test
  def materialSetKeepsNewMarkerAndPatchDeclarations(): Unit =
    val newMaterial = NewMaterialSpec(
      id = valid(RegistryPath.from("polyimide")),
      field = valid(ScalaIdent.from("Polyimide"))
    )
    val marker = MarkerMaterialSpec(
      id = valid(RegistryPath.from("null")),
      field = valid(ScalaIdent.from("Null"))
    )
    val patch = MaterialPatchSpec(
      target = iron,
      operations = NonEmptyVector.one(PatchOperation.SetPrimaryKey(liquidKey))
    )
    val declarations = NonEmptyVector.of(
      MaterialDeclaration.NewMaterial(newMaterial),
      MaterialDeclaration.MarkerMaterial(marker),
      MaterialDeclaration.ExistingPatch(patch)
    )
    val set = MaterialSet(declarations)

    assertEquals(3, set.declarations.length)
    assertEquals(declarations, set.declarations)

  @Test
  def firstSlicePatchOperationsPreserveAuthoredArguments(): Unit =
    val amount = valid(PositiveInt.from(100))
    val operations = NonEmptyVector.of(
      PatchOperation.SetOreByproducts(Vector.empty),
      PatchOperation.SetWashedIn(iron, amount),
      PatchOperation.SetSeparatedInto(Vector(copper)),
      PatchOperation.SetDirectSmeltResult(copper),
      PatchOperation.SetMagneticMaterial(copper),
      PatchOperation.SetArcSmeltingInto(copper),
      PatchOperation.SetPrimaryKey(liquidKey)
    )
    val patch = MaterialPatchSpec(iron, operations)

    assertEquals(7, patch.operations.length)
    assertEquals(
      PatchOperation.SetOreByproducts(Vector.empty),
      patch.operations.head
    )
    assertEquals(7, patch.operations.toVector.distinct.size)
    assertNotEquals(patch.operations.head, patch.operations.last)

  private val symbol = ScalaSymbolPath.fromFqcn("com.example.Symbol")
  private val iron =
    MaterialRef(ResourceId("gtceu", "iron"), symbol.append("Iron"))
  private val copper =
    MaterialRef(ResourceId("gtceu", "copper"), symbol.append("Copper"))
  private val liquidKey = FluidStorageKeyRef(symbol.append("Liquid"))

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      errors => throw new AssertionError(errors.toString),
      identity
    )

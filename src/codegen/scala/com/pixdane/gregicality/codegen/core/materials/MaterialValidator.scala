package com.pixdane.gregicality.codegen.core.materials

import cats.data.{NonEmptyChain, Validated}
import com.pixdane.gregicality.core.refs.{
  MaterialFlagRef,
  MaterialPropertyKeyRef
}

import scala.collection.mutable

/** Checks authored material data without normalizing, expanding, or rewriting
  * it. Successful validation returns the exact input instance.
  */
object MaterialValidator:

  /** Validates one new material against semantic and flag requirements. */
  def validateSpec(
      spec: NewMaterialSpec,
      symbols: MaterialValidationSymbols = MaterialValidationSymbols.gtceu
  ): ValidationResult[NewMaterialSpec] =
    validationResult(spec, specIssues(spec, symbols))

  /** Validates declaration identity plus every new material's semantic content.
    * Marker declarations participate in id/field uniqueness but not canonical
    * new-material collision checks.
    */
  def validateSet(
      set: MaterialSet,
      symbols: MaterialValidationSymbols = MaterialValidationSymbols.gtceu
  ): ValidationResult[MaterialSet] =
    val newSpecs = set.declarations.toVector.collect {
      case MaterialDeclaration.NewMaterial(spec) => spec
    }
    val identities = set.declarations.toVector.collect {
      case MaterialDeclaration.NewMaterial(spec) =>
        DeclarationIdentity(spec.id, spec.field, isNew = true)
      case MaterialDeclaration.MarkerMaterial(spec) =>
        DeclarationIdentity(spec.id, spec.field, isNew = false)
    }
    val found =
      identityIssues(identities, symbols) ++
        newSpecs.flatMap(specIssues(_, symbols))

    validationResult(set, found)

  private def specIssues(
      spec: NewMaterialSpec,
      symbols: MaterialValidationSymbols
  ): Vector[ValidationIssue] =
    val effective = EffectivePropertyView.from(spec.properties)
    val conflictIssues =
      if effective.ingot && effective.gem then
        Vector(ValidationIssue.IngotGemConflict(spec.field))
      else Vector.empty

    conflictIssues ++
      fluidIssues(spec) ++
      flagIssues(spec, effective, symbols)

  private def fluidIssues(spec: NewMaterialSpec): Vector[ValidationIssue] =
    spec.properties.fluid.toVector.flatMap { fluid =>
      val keys = fluid.fluids.toVector.map(_.key)
      val duplicates = keys
        .groupBy(identity)
        .collect {
          case (key, occurrences) if occurrences.sizeIs > 1 =>
            ValidationIssue.DuplicateFluidKey(spec.field, key)
        }
        .toVector
        .sortBy(issue => refKey(issue.key.path))
      val primaryIssue = fluid.primaryKey.toVector.collect {
        case primary if !keys.contains(primary) =>
          ValidationIssue.PrimaryFluidKeyMissing(spec.field, primary)
      }

      duplicates ++ primaryIssue
    }

  private def flagIssues(
      spec: NewMaterialSpec,
      effective: EffectivePropertyView,
      symbols: MaterialValidationSymbols
  ): Vector[ValidationIssue] =
    val presetIssues = Vector.newBuilder[ValidationIssue]
    val presetFlags = spec.flags.presets
      .sortBy(preset => refKey(preset.path))
      .flatMap { preset =>
        symbols.members(preset) match
          case Some(flags) => flags
          case None        =>
            presetIssues +=
              ValidationIssue.UnknownMaterialFlagPreset(spec.field, preset)
            Vector.empty
      }
    val authoredFlags = (
      spec.flags.flags.toVector.sortBy(flag => refKey(flag.path)) ++ presetFlags
    ).distinct
    val authoredSet = authoredFlags.toSet
    val discovered = mutable.LinkedHashSet.empty[MaterialFlagRef]
    val edges = mutable.ArrayBuffer.empty[(MaterialFlagRef, MaterialFlagRef)]
    val unknownIssues = mutable.ArrayBuffer.empty[ValidationIssue]

    def visit(flag: MaterialFlagRef): Unit =
      if discovered.add(flag) then
        symbols.requirements(flag) match
          case None =>
            unknownIssues += ValidationIssue.UnknownMaterialFlag(
              spec.field,
              flag
            )
          case Some(requirements) =>
            requirements.requiredFlags.foreach { required =>
              edges += flag -> required
              visit(required)
            }

    authoredFlags.foreach(visit)

    val missingFlagIssues = edges.toVector.distinct.collect {
      case (flag, required) if !authoredSet.contains(required) =>
        ValidationIssue.MissingRequiredFlag(spec.field, flag, required)
    }
    val propertyIssues = discovered.toVector.flatMap { flag =>
      symbols.requirements(flag).toVector.flatMap { requirements =>
        requirements.requiredProperties.collect {
          case required if !effective.has(required) =>
            ValidationIssue.MissingRequiredProperty(spec.field, flag, required)
        }
      }
    }

    presetIssues.result() ++
      unknownIssues.toVector ++
      missingFlagIssues ++
      propertyIssues

  private def identityIssues(
      identities: Vector[DeclarationIdentity],
      symbols: MaterialValidationSymbols
  ): Vector[ValidationIssue] =
    val duplicateIds = identities
      .groupBy(_.id)
      .collect {
        case (id, declarations) if declarations.sizeIs > 1 =>
          ValidationIssue.DuplicateMaterialId(
            id,
            declarations.map(_.field).sortBy(_.value)
          )
      }
      .toVector
      .sortBy(_.id.value)
    val duplicateFields = identities
      .groupBy(_.field)
      .collect {
        case (field, declarations) if declarations.sizeIs > 1 =>
          ValidationIssue.DuplicateMaterialField(
            field,
            declarations.map(_.id).sortBy(_.value)
          )
      }
      .toVector
      .sortBy(_.field.value)
    val trailingUnderscores = identities
      .collect {
        case identity if identity.id.value.endsWith("_") =>
          ValidationIssue.MaterialIdTrailingUnderscore(identity.id)
      }
      .distinct
      .sortBy(_.id.value)
    val canonicalCollisions = identities
      .collect {
        case identity
            if identity.isNew &&
              symbols.isCanonicalMaterialPath(identity.id) =>
          ValidationIssue.CanonicalMaterialIdCollision(identity.id)
      }
      .distinct
      .sortBy(_.id.value)

    duplicateIds ++
      duplicateFields ++
      trailingUnderscores ++
      canonicalCollisions

  private def validationResult[A](
      value: A,
      issues: Vector[ValidationIssue]
  ): ValidationResult[A] =
    NonEmptyChain.fromSeq(issues.distinct) match
      case Some(errors) => Validated.Invalid(errors)
      case None         => Validated.Valid(value)

  private def refKey(
      path: com.pixdane.gregicality.core.refs.ScalaSymbolPath
  ): String =
    path.parts.mkString(".")

  private final case class DeclarationIdentity(
      id: RegistryPath,
      field: ScalaIdent,
      isNew: Boolean
  )

  private final case class EffectivePropertyView(
      names: Set[String],
      ingot: Boolean,
      gem: Boolean
  ):
    def has(property: MaterialPropertyKeyRef): Boolean =
      property.path.parts.lastOption.exists(names.contains)

  private object EffectivePropertyView:
    def from(properties: MaterialProperties): EffectivePropertyView =
      val ingot =
        properties.ingot.isDefined ||
          properties.polymer.isDefined ||
          properties.blast.isDefined
      val dust =
        properties.dust.isDefined ||
          ingot ||
          properties.gem.isDefined ||
          properties.wood.isDefined ||
          properties.ore.isDefined
      val present = Vector(
        "DUST" -> dust,
        "INGOT" -> ingot,
        "GEM" -> properties.gem.isDefined,
        "WOOD" -> properties.wood.isDefined,
        "POLYMER" -> properties.polymer.isDefined,
        "FLUID" -> properties.fluid.isDefined,
        "ORE" -> properties.ore.isDefined,
        "BLAST" -> properties.blast.isDefined
      ).collect { case (name, true) => name }.toSet

      EffectivePropertyView(
        names = present,
        ingot = ingot,
        gem = properties.gem.isDefined
      )

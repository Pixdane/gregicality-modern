package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.{
  MaterialFlagPresetRef,
  MaterialFlagRef,
  MaterialFlagRequirements,
  ResourceId
}
import com.pixdane.gregicality.core.refs.gtceu.{
  GTMaterialsRef,
  MaterialFlagPresetsRef,
  MaterialFlagsRef
}

/** Read-only symbol metadata required by material validation. Production uses
  * generated GTCEu lookups; tests may provide map-backed data.
  */
trait MaterialValidationSymbols:
  def requirements(flag: MaterialFlagRef): Option[MaterialFlagRequirements]

  def members(preset: MaterialFlagPresetRef): Option[Vector[MaterialFlagRef]]

  def isCanonicalMaterialPath(path: RegistryPath): Boolean

object MaterialValidationSymbols:

  /** Generated metadata for the configured GTCEu source artifact. */
  val gtceu: MaterialValidationSymbols = new MaterialValidationSymbols:
    override def requirements(
        flag: MaterialFlagRef
    ): Option[MaterialFlagRequirements] =
      MaterialFlagsRef.requirements(flag)

    override def members(
        preset: MaterialFlagPresetRef
    ): Option[Vector[MaterialFlagRef]] =
      MaterialFlagPresetsRef.members(preset)

    override def isCanonicalMaterialPath(path: RegistryPath): Boolean =
      GTMaterialsRef.resolve(ResourceId("gtceu", path.value)).isDefined

  /** Builds deterministic metadata lookups for validator unit tests. */
  def fromMaps(
      flagRequirements: Map[MaterialFlagRef, MaterialFlagRequirements] =
        Map.empty,
      presetMembers: Map[MaterialFlagPresetRef, Vector[MaterialFlagRef]] =
        Map.empty,
      canonicalMaterialPaths: Set[String] = Set.empty
  ): MaterialValidationSymbols =
    new MaterialValidationSymbols:
      override def requirements(
          flag: MaterialFlagRef
      ): Option[MaterialFlagRequirements] =
        flagRequirements.get(flag)

      override def members(
          preset: MaterialFlagPresetRef
      ): Option[Vector[MaterialFlagRef]] =
        presetMembers.get(preset)

      override def isCanonicalMaterialPath(path: RegistryPath): Boolean =
        canonicalMaterialPaths.contains(path.value)

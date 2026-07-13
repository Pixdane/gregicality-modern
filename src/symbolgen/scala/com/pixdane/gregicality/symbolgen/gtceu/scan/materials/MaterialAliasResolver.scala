package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.pixdane.gregicality.codegen.dsl.model.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.gtceu.scan.{
  GtceuScanDiagnostic,
  SourceSite
}
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialAliasRef,
  ScannedRegisteredMaterialRef
}

final case class AliasResolution(
    refs: Vector[ScannedMaterialAliasRef],
    cycles: Vector[GtceuScanDiagnostic.AliasCycle],
    unresolved: Vector[GtceuScanDiagnostic.UnresolvedAlias]
)

object MaterialAliasResolver:
  def resolve(
      aliases: Vector[LocatedMaterialAlias],
      refsByName: Map[String, ScannedRegisteredMaterialRef],
      ownerFqcn: String
  ): AliasResolution =
    val aliasTargets: Map[String, String] =
      aliases.map(alias => alias.name -> alias.targetName).toMap
    val aliasSites: Map[String, SourceSite] =
      aliases.map(alias => alias.name -> alias.site).toMap

    val declaredAliasNames = aliases.iterator.map(_.name).toVector.sorted

    val (resolvedRefs, cycles, unresolved) =
      declaredAliasNames.foldLeft(
        (
          Vector.empty[ScannedMaterialAliasRef],
          Vector.empty[GtceuScanDiagnostic.AliasCycle],
          Vector.empty[GtceuScanDiagnostic.UnresolvedAlias]
        )
      ) { case ((accRefs, accCycles, accUnresolved), name) =>
        resolveChain(
          name,
          aliasTargets,
          aliasSites,
          refsByName,
          ownerFqcn,
          Vector.empty
        ) match
          case ResolvedAlias(ref) =>
            (
              accRefs :+ ref.copy(
                name = name,
                path = ScalaSymbolPath.member(ownerFqcn, name)
              ),
              accCycles,
              accUnresolved
            )
          case CycleDetected(chain) =>
            (
              accRefs,
              accCycles :+ GtceuScanDiagnostic.AliasCycle(chain),
              accUnresolved
            )
          case DanglingAlias(target, site) =>
            (
              accRefs,
              accCycles,
              accUnresolved :+ GtceuScanDiagnostic.UnresolvedAlias(
                name,
                target,
                site
              )
            )
      }

    AliasResolution(resolvedRefs, cycles, unresolved)

  private sealed trait ChainOutcome
  private case class ResolvedAlias(ref: ScannedMaterialAliasRef)
      extends ChainOutcome
  private case class CycleDetected(chain: Vector[String]) extends ChainOutcome
  private case class DanglingAlias(target: String, site: SourceSite)
      extends ChainOutcome

  private def resolveChain(
      name: String,
      aliasTargets: Map[String, String],
      aliasSites: Map[String, SourceSite],
      refsByName: Map[String, ScannedRegisteredMaterialRef],
      ownerFqcn: String,
      seen: Vector[String]
  ): ChainOutcome =
    refsByName.get(name) match
      case Some(ref) =>
        ResolvedAlias(
          ScannedMaterialAliasRef(
            name = name,
            id = ref.id,
            path = ScalaSymbolPath.member(ownerFqcn, name)
          )
        )
      case None =>
        aliasTargets.get(name) match
          case None =>
            DanglingAlias(
              name,
              aliasSites.getOrElse(name, SourceSite(name, None))
            )
          case Some(target) =>
            if seen.contains(target) then CycleDetected(seen :+ name :+ target)
            else
              resolveChain(
                target,
                aliasTargets,
                aliasSites,
                refsByName,
                ownerFqcn,
                seen :+ name
              ) match
                case resolved: ResolvedAlias => resolved
                case cycle: CycleDetected    => cycle
                case DanglingAlias(_, _)     =>
                  DanglingAlias(
                    target,
                    aliasSites.getOrElse(name, SourceSite(name, None))
                  )
end MaterialAliasResolver

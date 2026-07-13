package com.pixdane.gregicality.symbolgen.gtceu

import cats.data.Ior
import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.gtceu.scan.GtceuScanResult
import com.pixdane.gregicality.symbolgen.render.RefObjectTarget
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialRef,
  ScannedPathRef
}

enum GtceuScannedRefs:
  case Materials(target: RefObjectTarget, refs: Vector[ScannedMaterialRef])
  case Paths(target: RefObjectTarget, refs: Vector[ScannedPathRef])

object GtceuRefScanner:
  def scan(
      job: GtceuRefJob,
      archive: SourceArchive
  ): GtceuScanResult[GtceuScannedRefs] =
    job match
      case GtceuRefJob.Materials(_, spec, target) =>
        GtceuSourceScanners
          .scanGtMaterials(spec)(archive)
          .map(refs => GtceuScannedRefs.Materials(target = target, refs = refs))
      case GtceuRefJob.Paths(_, spec, target) =>
        Ior.right(
          GtceuScannedRefs.Paths(
            target = target,
            refs = GtceuSourceScanners.scanStaticMembers(spec)(archive)
          )
        )

package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialRef,
  ScannedPathRef
}
import com.pixdane.gregicality.symbolgen.render.RefObjectTarget

enum GtceuScannedRefs:
  case Materials(target: RefObjectTarget, refs: Vector[ScannedMaterialRef])
  case Paths(target: RefObjectTarget, refs: Vector[ScannedPathRef])

object GtceuRefScanner:
  def scan(job: GtceuRefJob, archive: SourceArchive): GtceuScannedRefs =
    job match
      case GtceuRefJob.Materials(_, spec, target) =>
        GtceuScannedRefs.Materials(
          target = target,
          refs = GtceuSourceScanners.scanGtMaterials(spec)(archive)
        )
      case GtceuRefJob.Paths(_, spec, target) =>
        GtceuScannedRefs.Paths(
          target = target,
          refs = GtceuSourceScanners.scanStaticMembers(spec)(archive)
        )

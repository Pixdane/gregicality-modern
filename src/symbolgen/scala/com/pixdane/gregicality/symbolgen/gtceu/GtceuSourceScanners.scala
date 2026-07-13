package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.gtceu.scan.GtceuScanResult
import com.pixdane.gregicality.symbolgen.gtceu.scan.materials.MaterialScanner
import com.pixdane.gregicality.symbolgen.gtceu.scan.StaticFieldScanner
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialRef,
  ScannedPathRef
}

object GtceuSourceScanners:
  def scanStaticMembers(input: StaticFieldScanSpec)(
      archive: SourceArchive
  ): Vector[ScannedPathRef] =
    StaticFieldScanner.scan(input)(archive)

  def scanGtMaterials(input: GtMaterialsScanSpec)(
      archive: SourceArchive
  ): GtceuScanResult[Vector[ScannedMaterialRef]] =
    MaterialScanner
      .scan(input)(archive)
      .flatMap(MaterialScanner.preprocess)

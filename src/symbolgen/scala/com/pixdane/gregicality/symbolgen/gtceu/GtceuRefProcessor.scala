package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.render.{
  GeneratedScalaFile,
  RefObjectRenderer
}

object GtceuRefProcessor:
  def render(refs: GtceuScannedRefs): GeneratedScalaFile =
    refs match
      case GtceuScannedRefs.Materials(target, materialRefs) =>
        RefObjectRenderer.generateMaterialFile(target, materialRefs)
      case GtceuScannedRefs.Paths(target, pathRefs) =>
        RefObjectRenderer.generatePathFile(target, pathRefs)

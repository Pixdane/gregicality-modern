package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.pixdane.gregicality.symbolgen.gtceu.scan.SourceSite
import com.pixdane.gregicality.symbolgen.scan.ScannedMaterialRef

final case class LocatedMaterialAssignment(
    ref: ScannedMaterialRef,
    site: SourceSite
)

final case class RejectedMaterialAssignment(
    name: String,
    reason: String,
    site: SourceSite
)

enum MaterialAssignmentTarget:
  case Accepted(name: String)
  case ForeignOwner(name: String)
  case Unrelated

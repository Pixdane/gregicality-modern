package com.pixdane.gregicality.symbolgen.gtceu.scan.materials

import com.pixdane.gregicality.symbolgen.gtceu.scan.SourceSite
import com.pixdane.gregicality.symbolgen.scan.ScannedMaterialRef

final case class MaterialScanInput(
    declarations: Map[String, SourceSite],
    assignments: Vector[LocatedMaterialAssignment],
    rejectedAssignments: Vector[RejectedMaterialAssignment]
)

final case class LocatedMaterialAssignment(
    ref: ScannedMaterialRef,
    site: SourceSite
)

final case class RejectedMaterialAssignment(
    name: String,
    reason: String,
    site: SourceSite
)

package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.render.RefObjectTarget

enum GtceuRefJob:
  case Materials(
      override val id: String,
      spec: GtMaterialsScanSpec,
      override val target: RefObjectTarget
  )
  case Paths(
      override val id: String,
      spec: StaticFieldScanSpec,
      override val target: RefObjectTarget
  )

  def id: String
  def target: RefObjectTarget
end GtceuRefJob

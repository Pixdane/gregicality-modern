package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.render.RefObjectTarget

enum GtceuRefJob:
  case Materials(
      id: String,
      spec: GtMaterialsScanSpec,
      objectTarget: RefObjectTarget
  )
  case Paths(
      id: String,
      spec: StaticFieldScanSpec,
      objectTarget: RefObjectTarget
  )

  def target: RefObjectTarget =
    this match
      case Materials(_, _, target) => target
      case Paths(_, _, target)     => target

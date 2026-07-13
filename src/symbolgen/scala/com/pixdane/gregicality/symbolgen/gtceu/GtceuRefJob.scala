package com.pixdane.gregicality.symbolgen.gtceu

import com.pixdane.gregicality.symbolgen.render.RefObjectTarget

enum GtceuRefJob:
  case Materials(
      override val id: String,
      spec: GtMaterialsScanSpec,
      objectTarget: RefObjectTarget
  )
  case Paths(
      override val id: String,
      spec: StaticFieldScanSpec,
      objectTarget: RefObjectTarget
  )

  def id: String =
    this match
      case Materials(id, _, _) => id
      case Paths(id, _, _)     => id

  def target: RefObjectTarget =
    this match
      case Materials(_, _, target) => target
      case Paths(_, _, target)     => target

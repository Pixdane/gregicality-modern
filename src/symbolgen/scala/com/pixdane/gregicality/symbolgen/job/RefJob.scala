package com.pixdane.gregicality.symbolgen.job

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.render.RefObjectTarget
import com.pixdane.gregicality.symbolgen.scan.{
  ScannedMaterialRef,
  ScannedPathRef
}

enum RefJob:
  case Materials(
      id: String,
      scan: SourceArchive => Vector[ScannedMaterialRef],
      objectTarget: RefObjectTarget
  )
  case Paths(
      id: String,
      scan: SourceArchive => Vector[ScannedPathRef],
      objectTarget: RefObjectTarget
  )

  def target: RefObjectTarget =
    this match
      case Materials(_, _, target) => target
      case Paths(_, _, target)     => target

package com.pixdane.gregicality.symbolgen.scan

import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}

sealed trait ScannedRef:
  def name: String
  def path: ScalaSymbolPath

final case class ScannedMaterialRef(
    name: String,
    id: ResourceId,
    path: ScalaSymbolPath
) extends ScannedRef

final case class ScannedPathRef(
    name: String,
    path: ScalaSymbolPath
) extends ScannedRef

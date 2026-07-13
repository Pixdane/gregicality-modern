package com.pixdane.gregicality.symbolgen.scan

import com.pixdane.gregicality.codegen.dsl.model.{ResourceId, ScalaSymbolPath}

sealed trait ScannedRef:
  def name: String
  def path: ScalaSymbolPath

sealed trait ScannedMaterialRef extends ScannedRef:
  def id: ResourceId

final case class ScannedRegisteredMaterialRef(
    name: String,
    id: ResourceId,
    path: ScalaSymbolPath
) extends ScannedMaterialRef

final case class ScannedMaterialAliasRef(
    name: String,
    id: ResourceId,
    path: ScalaSymbolPath
) extends ScannedMaterialRef

final case class ScannedPathRef(
    name: String,
    path: ScalaSymbolPath
) extends ScannedRef

package com.pixdane.gregicality.symbolgen.framework

import com.pixdane.gregicality.core.refs.{ResourceId, ScalaSymbolPath}

/** A symbol extracted from a source archive, to be rendered into a ref
  * accessor. The sealed hierarchy lets renderers handle the material and plain
  * path variants without dynamic dispatch.
  */
sealed trait ScannedRef:
  def name: String
  def path: ScalaSymbolPath

/** A material symbol carrying its registry id in addition to its Scala path. */
final case class ScannedMaterialRef(
    name: String,
    id: ResourceId,
    path: ScalaSymbolPath
) extends ScannedRef

/** A plain static-field symbol identified only by its Scala path. */
final case class ScannedPathRef(
    name: String,
    path: ScalaSymbolPath
) extends ScannedRef

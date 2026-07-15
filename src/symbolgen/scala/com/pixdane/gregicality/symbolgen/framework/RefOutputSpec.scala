package com.pixdane.gregicality.symbolgen.framework

/** Describes the Scala object a job renders into.
  *
  * `outputPackage` and `outputObject` locate the generated object; `valueType`
  * is the ref type each accessor returns (for example `MaterialRef`). Renderers
  * consume this spec to emit package declarations, imports, and accessor
  * signatures.
  */
final case class RefOutputSpec(
    outputPackage: String,
    outputObject: String,
    valueType: String
)

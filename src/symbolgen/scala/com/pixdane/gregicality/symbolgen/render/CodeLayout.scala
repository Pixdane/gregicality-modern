package com.pixdane.gregicality.symbolgen.render

final case class CodeLayout(
    prefix: ScalaCode,
    separator: ScalaCode,
    suffix: ScalaCode
):
  def apply(items: Vector[ScalaCode]): ScalaCode =
    prefix ++ ScalaCode.joinWith(separator)(items) ++ suffix

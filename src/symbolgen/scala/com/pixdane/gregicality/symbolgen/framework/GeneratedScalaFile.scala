package com.pixdane.gregicality.symbolgen.framework

/** A fully rendered Scala source file ready to be written under the output
  * directory. `relativePath` is relative to that owned output directory
  * (package-directory / file) and `content` is the final source text.
  */
final case class GeneratedScalaFile(
    relativePath: String,
    content: String
)

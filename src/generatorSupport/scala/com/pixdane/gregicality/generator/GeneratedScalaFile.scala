package com.pixdane.gregicality.generator

/** A fully rendered Scala source file ready to be written under an owned output
  * directory.
  *
  * `relativePath` is relative to that output directory (package directory plus
  * file name), and `content` is the complete source text.
  */
final case class GeneratedScalaFile(
    relativePath: String,
    content: String
)

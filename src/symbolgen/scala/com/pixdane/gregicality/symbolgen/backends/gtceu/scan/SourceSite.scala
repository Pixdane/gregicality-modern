package com.pixdane.gregicality.symbolgen.backends.gtceu.scan

import com.github.javaparser.ast.Node

/** A location in a source archive file, used to attribute diagnostics to a
  * concrete `path:line`.
  */
final case class SourceSite(path: String, line: Option[Int]):
  def render: String =
    line.fold(path)(value => s"$path:$value")

  def sortKey: (String, Int) =
    path -> line.getOrElse(Int.MaxValue)

object SourceSite:
  def fromNode(sourcePath: String, node: Node): SourceSite =
    val begin = node.getBegin
    SourceSite(
      path = sourcePath,
      line = Option.when(begin.isPresent)(begin.get.line)
    )

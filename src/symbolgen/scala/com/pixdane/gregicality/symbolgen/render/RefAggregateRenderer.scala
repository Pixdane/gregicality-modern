package com.pixdane.gregicality.symbolgen.render

import com.pixdane.gregicality.symbolgen.framework.GeneratedScalaFile

/** Renders the aggregate `export` object that re-exports every individual ref
  * object produced by a backend, giving consumers a single entry point.
  */
object RefAggregateRenderer:
  def generateFile(
      outputPackage: String,
      outputObject: String,
      exports: Vector[String]
  ): GeneratedScalaFile =
    val exportLines = exports.sorted.map(name => s"  export $name.*")
    val lines =
      Vector(
        s"package $outputPackage",
        "",
        s"object $outputObject:"
      ) ++ exportLines

    GeneratedScalaFile(
      relativePath =
        outputPackage.replace('.', '/') + "/" + outputObject + ".scala",
      content = ScalaCode
        .lines(lines*)
        .render
    )

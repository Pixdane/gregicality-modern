package com.pixdane.gregicality.symbolgen.render

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RefAggregateRendererTest:
  @Test
  def generateFileSortsExportsAndBuildsTheOutputPath(): Unit =
    val file = RefAggregateRenderer.generateFile(
      outputPackage = "com.pixdane.gregicality.core.refs.gtceu",
      outputObject = "GTRefs",
      exports = Vector("MaterialFlagsRef", "GTMaterialsRef", "GTElementsRef")
    )

    assertEquals(
      "com/pixdane/gregicality/core/refs/gtceu/GTRefs.scala",
      file.relativePath
    )
    assertEquals(
      """|package com.pixdane.gregicality.core.refs.gtceu
         |
         |object GTRefs:
         |  export GTElementsRef.*
         |  export GTMaterialsRef.*
         |  export MaterialFlagsRef.*
         |""".stripMargin,
      file.content
    )

  @Test
  def generateFileHandlesEmptyExports(): Unit =
    val file = RefAggregateRenderer.generateFile(
      outputPackage = "example.refs",
      outputObject = "EmptyRefs",
      exports = Vector.empty
    )

    assertEquals(
      """|package example.refs
         |
         |object EmptyRefs:
         |""".stripMargin,
      file.content
    )

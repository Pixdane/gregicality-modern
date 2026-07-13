package com.pixdane.gregicality.symbolgen.gtceu.scan

import com.github.javaparser.ast.body.FieldDeclaration
import com.pixdane.gregicality.codegen.dsl.model.ScalaSymbolPath
import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.gtceu.StaticFieldScanSpec
import com.pixdane.gregicality.symbolgen.scan.ScannedPathRef

import scala.jdk.CollectionConverters.*

object StaticFieldScanner:
  def scan(input: StaticFieldScanSpec)(
      archive: SourceArchive
  ): Vector[ScannedPathRef] =
    val unit = archive.parse(input.sourcePath)

    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(field =>
        field.isPublic &&
          field.isStatic &&
          field.isFinal &&
          !isDeprecated(field) &&
          field.getElementType.asString == input.memberTypeSimpleName
      )
      .flatMap(_.getVariables.asScala.toVector)
      .map { variable =>
        val name = variable.getNameAsString

        ScannedPathRef(
          name = name,
          path = ScalaSymbolPath.member(input.ownerFqcn, name)
        )
      }

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

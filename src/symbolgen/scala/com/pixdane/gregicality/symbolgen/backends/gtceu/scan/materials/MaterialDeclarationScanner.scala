package com.pixdane.gregicality.symbolgen.backends.gtceu.scan.materials

import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.FieldDeclaration
import com.pixdane.gregicality.symbolgen.backends.gtceu.scan.SourceSite

import scala.jdk.CollectionConverters.*

/** Scans the `GTMaterials` declaration file for public static `Material`
  * fields, returning each declared name with its source site. Deprecated fields
  * are skipped.
  */
object MaterialDeclarationScanner:
  def scan(sourcePath: String, unit: CompilationUnit): Map[String, SourceSite] =
    unit
      .findAll(classOf[FieldDeclaration])
      .asScala
      .toVector
      .filter(field =>
        field.isPublic &&
          field.isStatic &&
          !isDeprecated(field)
      )
      .flatMap(_.getVariables.asScala.toVector)
      .filter(_.getType.asString == "Material")
      .map(variable =>
        variable.getNameAsString -> SourceSite.fromNode(sourcePath, variable)
      )
      .toMap

  private def isDeprecated(field: FieldDeclaration): Boolean =
    field.getAnnotations.asScala.exists { annotation =>
      val name = annotation.getNameAsString
      name == "Deprecated" || name.endsWith(".Deprecated")
    }

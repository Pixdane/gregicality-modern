package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.ScalaSymbolPath

/** Renders a material plan as one runtime-facing Scala object. */
object MaterialRenderer:

  /** Produces deterministic Scala source with one final newline. */
  def render(plan: MaterialPlan): ScalaCode =
    val importLines = plan.imports.map(path => s"import ${pathKey(path)}")
    val fields = renderFields(plan.declarations)
    val registrations = plan.declarations.collect {
      case MaterialDeclarationPlan.NewMaterial(value) =>
        renderNewMaterial(value, plan.output)
      case MaterialDeclarationPlan.MarkerMaterial(value) =>
        renderMarkerMaterial(value, plan.output)
    }
    val patches = plan.declarations.collect {
      case MaterialDeclarationPlan.ExistingPatch(value) =>
        renderPatch(value)
    }
    val objectLines =
      Vector(s"object ${plan.output.objectName.value}:") ++
        fields ++
        Option.when(fields.nonEmpty)(Vector("")).toVector.flatten ++
        renderMethod("register", registrations) ++
        Vector("") ++
        renderMethod("patch", patches)
    val lines =
      Vector(s"package ${plan.output.packageName}", "") ++
        importLines ++
        Option.when(importLines.nonEmpty)(Vector("")).toVector.flatten ++
        objectLines

    ScalaCode(lines)

  private def renderFields(
      declarations: Vector[MaterialDeclarationPlan]
  ): Vector[String] =
    declarations.flatMap {
      case MaterialDeclarationPlan.NewMaterial(plan) =>
        Vector(s"  var ${plan.field.value}: Material = _")
      case MaterialDeclarationPlan.MarkerMaterial(plan) =>
        Vector(s"  var ${plan.field.value}: MarkerMaterial = _")
      case MaterialDeclarationPlan.ExistingPatch(_) =>
        Vector.empty
    }

  private def renderMethod(
      name: String,
      body: Vector[Vector[String]]
  ): Vector[String] =
    val renderedBody =
      if body.isEmpty then Vector("    ()")
      else
        body.zipWithIndex.flatMap { case (lines, index) =>
          val separator = if index == body.size - 1 then Vector.empty
          else Vector("")
          lines ++ separator
        }

    Vector(s"  def $name(): Unit =") ++ renderedBody

  private def renderNewMaterial(
      plan: NewMaterialPlan,
      output: MaterialOutputSpec
  ): Vector[String] =
    val assignment =
      Vector(
        s"    ${plan.field.value} = new Material.Builder(" +
          s"${renderSymbol(output.idFactory)}(${quote(plan.id.value)}))"
      ) ++
        plan.builderCalls.flatMap(call => renderBuilderCall(call, 6))
    val primary = plan.primaryKey.toVector.map { key =>
      s"    ${plan.field.value}.getProperty(PropertyKey.FLUID)" +
        s".setPrimaryKey(${renderSymbol(key.path)})"
    }

    assignment ++ primary

  private def renderMarkerMaterial(
      plan: MarkerMaterialPlan,
      output: MaterialOutputSpec
  ): Vector[String] =
    Vector(
      s"    ${plan.field.value} = new MarkerMaterial(" +
        s"${renderSymbol(output.idFactory)}(${quote(plan.id.value)}))"
    )

  private def renderBuilderCall(
      call: BuilderCall,
      indent: Int
  ): Vector[String] =
    call.arguments match
      case Vector(ScalaExpr.Lambda(parameter, calls)) =>
        Vector(spaces(indent) + s".${call.method}($parameter =>") ++
          Vector(spaces(indent + 2) + parameter) ++
          calls.flatMap(value => renderBuilderCall(value, indent + 4)) ++
          Vector(spaces(indent) + ")")
      case arguments if arguments.exists {
            case _: ScalaExpr.NewInstance => true
            case _                        => false
          } =>
        Vector(spaces(indent) + s".${call.method}(") ++
          renderMultilineArguments(arguments, indent + 2) ++
          Vector(spaces(indent) + ")")
      case arguments if call.method == "components" && arguments.nonEmpty =>
        Vector(spaces(indent) + ".components(") ++
          arguments
            .grouped(2)
            .toVector
            .zipWithIndex
            .map { case (pair, index) =>
              val suffix =
                if index == arguments.size / 2 - 1 then "" else ","
              spaces(indent + 2) +
                pair.map(renderInline).mkString(", ") +
                suffix
            } ++
          Vector(spaces(indent) + ")")
      case arguments =>
        val inline = arguments.map(renderInline)
        val rendered =
          s".${call.method}(${inline.mkString(", ")})"
        if arguments.sizeIs > 2 || indent + rendered.length > 100 then
          Vector(spaces(indent) + s".${call.method}(") ++
            inline.zipWithIndex.map { case (value, index) =>
              val suffix = if index == inline.size - 1 then "" else ","
              spaces(indent + 2) + value + suffix
            } ++
            Vector(spaces(indent) + ")")
        else Vector(spaces(indent) + rendered)

  private def renderMultilineArguments(
      arguments: Vector[ScalaExpr],
      indent: Int
  ): Vector[String] =
    arguments.zipWithIndex.flatMap { case (argument, index) =>
      val lines = renderArgument(argument, indent)
      if index == arguments.size - 1 then lines
      else appendSuffix(lines, ",")
    }

  private def renderArgument(
      argument: ScalaExpr,
      indent: Int
  ): Vector[String] =
    argument match
      case ScalaExpr.NewInstance(typePath, calls) =>
        Vector(spaces(indent) + s"new ${simpleName(typePath)}()") ++
          calls.flatMap(call => renderBuilderCall(call, indent + 2))
      case other =>
        Vector(spaces(indent) + renderInline(other))

  private def appendSuffix(
      lines: Vector[String],
      suffix: String
  ): Vector[String] =
    lines.lastOption match
      case None       => lines
      case Some(last) => lines.init :+ (last + suffix)

  private def renderPatch(plan: MaterialPatchPlan): Vector[String] =
    plan.operations.map(operation =>
      "    " + renderPatchOperation(plan, operation)
    )

  private def renderPatchOperation(
      plan: MaterialPatchPlan,
      operation: PatchOperation
  ): String =
    val target = renderSymbol(plan.target.path)
    operation match
      case PatchOperation.SetOreByproducts(byproducts) =>
        propertyCall(
          target,
          "ORE",
          "setOreByProducts",
          byproducts.map(value => renderSymbol(value.path))
        )
      case PatchOperation.SetWashedIn(material, amount) =>
        propertyCall(
          target,
          "ORE",
          "setWashedIn",
          Vector(renderSymbol(material.path), amount.value.toString)
        )
      case PatchOperation.SetSeparatedInto(materials) =>
        propertyCall(
          target,
          "ORE",
          "setSeparatedInto",
          materials.map(value => renderSymbol(value.path))
        )
      case PatchOperation.SetDirectSmeltResult(material) =>
        propertyCall(
          target,
          "ORE",
          "setDirectSmeltResult",
          Vector(renderSymbol(material.path))
        )
      case PatchOperation.SetMagneticMaterial(material) =>
        propertyCall(
          target,
          "INGOT",
          "setMagneticMaterial",
          Vector(renderSymbol(material.path))
        )
      case PatchOperation.SetArcSmeltingInto(material) =>
        propertyCall(
          target,
          "INGOT",
          "setArcSmeltingInto",
          Vector(renderSymbol(material.path))
        )
      case PatchOperation.SetPrimaryKey(fluid) =>
        propertyCall(
          target,
          "FLUID",
          "setPrimaryKey",
          Vector(renderSymbol(fluid.path))
        )

  private def propertyCall(
      target: String,
      property: String,
      method: String,
      arguments: Vector[String]
  ): String =
    s"$target.getProperty(PropertyKey.$property).$method(" +
      arguments.mkString(", ") +
      ")"

  private def renderInline(expression: ScalaExpr): String =
    expression match
      case ScalaExpr.StringValue(value) =>
        quote(value)
      case ScalaExpr.IntValue(value) =>
        value.toString
      case ScalaExpr.DoubleValue(value) =>
        java.lang.Double.toString(value)
      case ScalaExpr.BooleanValue(value) =>
        value.toString
      case ScalaExpr.HexValue(value) =>
        f"0x${value & 0xffffff}%06x"
      case ScalaExpr.Symbol(path) =>
        renderSymbol(path)
      case ScalaExpr.ArrayAccess(array, index) =>
        s"${renderSymbol(array)}(${renderSymbol(index)})"
      case ScalaExpr.ToInt(value) =>
        s"${renderInline(value)}.toInt"
      case _: ScalaExpr.NewInstance =>
        throw new IllegalArgumentException(
          "new-instance expressions require multiline rendering"
        )
      case _: ScalaExpr.Lambda =>
        throw new IllegalArgumentException(
          "lambda expressions require multiline rendering"
        )

  private def renderSymbol(path: ScalaSymbolPath): String =
    path.parts.takeRight(2).mkString(".")

  private def simpleName(path: ScalaSymbolPath): String =
    path.parts.lastOption.getOrElse("")

  private def pathKey(path: ScalaSymbolPath): String =
    path.parts.mkString(".")

  private def spaces(count: Int): String =
    " " * count

  private def quote(value: String): String =
    "\"" + value.flatMap {
      case '\\' => "\\\\"
      case '"'  => "\\\""
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case char => char.toString
    } + "\""

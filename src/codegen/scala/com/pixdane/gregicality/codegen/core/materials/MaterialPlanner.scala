package com.pixdane.gregicality.codegen.core.materials

import com.pixdane.gregicality.core.refs.*

/** Converts validated authored material data into deterministic source plans.
  *
  * The planner selects public GTCEu builder overloads but never expands
  * properties, flags, presets, or defaults.
  */
object MaterialPlanner:
  private val MaterialType = ScalaSymbolPath.fromFqcn(
    "com.gregtechceu.gtceu.api.data.chemical.material.Material"
  )
  private val MarkerMaterialType = ScalaSymbolPath.fromFqcn(
    "com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterial"
  )
  private val FluidBuilderType =
    ScalaSymbolPath.fromFqcn("com.gregtechceu.gtceu.api.fluids.FluidBuilder")
  private val FluidStateOwner =
    ScalaSymbolPath.fromFqcn("com.gregtechceu.gtceu.api.fluids.FluidState")
  private val FluidStorageKeysOwner = ScalaSymbolPath.fromFqcn(
    "com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys"
  )
  private val PropertyKeyOwner = ScalaSymbolPath.fromFqcn(
    "com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey"
  )
  private val GTValuesOwner =
    ScalaSymbolPath.fromFqcn("com.gregtechceu.gtceu.api.GTValues")

  /** Plans one generated object. Callers must validate the set first. */
  def plan(set: MaterialSet, output: MaterialOutputSpec): MaterialPlan =
    val declarations = set.declarations.toVector.map {
      case MaterialDeclaration.NewMaterial(spec) =>
        MaterialDeclarationPlan.NewMaterial(planNew(spec))
      case MaterialDeclaration.MarkerMaterial(spec) =>
        MaterialDeclarationPlan.MarkerMaterial(
          MarkerMaterialPlan(spec.id, spec.field)
        )
      case MaterialDeclaration.ExistingPatch(spec) =>
        MaterialDeclarationPlan.ExistingPatch(
          MaterialPatchPlan(spec.target, spec.operations.toVector)
        )
    }

    MaterialPlan(
      output = output,
      imports = collectImports(declarations, output),
      declarations = declarations
    )

  private def planNew(spec: NewMaterialSpec): NewMaterialPlan =
    val properties = spec.properties
    val calls =
      spec.identity.displayName.toVector.map(value =>
        BuilderCall("langValue", Vector(ScalaExpr.StringValue(value)))
      ) ++
        solidCalls(properties) ++
        ingotTransformationCalls(properties.ingot) ++
        separateBurnTimeCall(properties) ++
        properties.fluid.toVector.flatMap(_.fluids.toVector.map(fluidCall)) ++
        properties.ore.toVector.flatMap(oreCalls) ++
        visualCalls(spec.visuals) ++
        flagCalls(spec.flags) ++
        spec.identity.element.toVector.map(element =>
          BuilderCall("element", Vector(ScalaExpr.Symbol(element.path)))
        ) ++
        componentCalls(spec.composition) ++
        spec.composition.formulaOverride.toVector.map(formula =>
          BuilderCall(
            "formula",
            Vector(
              ScalaExpr.StringValue(formula.text),
              ScalaExpr.BooleanValue(formula.formatSubscripts)
            )
          )
        ) ++
        properties.blast.toVector.map(blastCall) ++
        tagCalls(spec.tags) ++
        Vector(BuilderCall("buildAndRegister"))

    NewMaterialPlan(
      id = spec.id,
      field = spec.field,
      builderCalls = calls,
      primaryKey = properties.fluid.flatMap(_.primaryKey)
    )

  private def solidCalls(
      properties: MaterialProperties
  ): Vector[BuilderCall] =
    val present = SolidOrder.filter(_.isPresent(properties))
    val carrier =
      present.headOption.orElse(properties.dust.map(_ => SolidKind.Dust))
    val solid = present.flatMap { kind =>
      solidCall(
        kind,
        if carrier.contains(kind) then properties.dust else None
      )
    }
    val dustOnly =
      if carrier.contains(SolidKind.Dust) then
        solidCall(SolidKind.Dust, properties.dust)
      else Vector.empty

    solid ++ dustOnly

  private def solidCall(
      kind: SolidKind,
      dust: Option[DustPropertySpec]
  ): Vector[BuilderCall] =
    val arguments = dust.toVector.flatMap { settings =>
      (settings.harvestLevel, settings.burnTime) match
        case (Some(harvest), Some(burn)) if kind != SolidKind.Polymer =>
          Vector(
            ScalaExpr.IntValue(harvest.value),
            ScalaExpr.IntValue(burn.value)
          )
        case (Some(harvest), _) =>
          Vector(ScalaExpr.IntValue(harvest.value))
        case _ =>
          Vector.empty
    }
    val burnOnly =
      kind == SolidKind.Dust &&
        dust.exists(settings =>
          settings.harvestLevel.isEmpty && settings.burnTime.isDefined
        )

    if burnOnly then Vector.empty
    else Vector(BuilderCall(kind.method, arguments))

  private def separateBurnTimeCall(
      properties: MaterialProperties
  ): Vector[BuilderCall] =
    val present = SolidOrder.filter(_.isPresent(properties))
    val carrier =
      present.headOption.orElse(properties.dust.map(_ => SolidKind.Dust))
    val settings = properties.dust
    val separate = settings.exists { dust =>
      dust.burnTime.isDefined &&
      (dust.harvestLevel.isEmpty || carrier.contains(SolidKind.Polymer))
    }

    if separate then
      settings
        .flatMap(_.burnTime)
        .toVector
        .map(burn =>
          BuilderCall("burnTime", Vector(ScalaExpr.IntValue(burn.value)))
        )
    else Vector.empty

  private def ingotTransformationCalls(
      ingot: Option[IngotPropertySpec]
  ): Vector[BuilderCall] =
    ingot.toVector.flatMap { property =>
      Vector(
        property.smeltingInto.map("ingotSmeltInto" -> _),
        property.arcSmeltingInto.map("arcSmeltInto" -> _),
        property.macerateInto.map("macerateInto" -> _),
        property.magneticMaterial.map("polarizesInto" -> _)
      ).flatten.map { case (method, material) =>
        BuilderCall(method, Vector(ScalaExpr.Symbol(material.path)))
      }
    }

  private def fluidCall(entry: FluidEntry): BuilderCall =
    val builder = entry.builder
    shorthandFor(entry.key) match
      case Some((method, expectedState))
          if builder.state.forall(_ == expectedState) =>
        if hasNoFluidBuilderContent(builder, ignoreState = true) then
          BuilderCall(method)
        else if hasOnlyFluidTemperature(builder, ignoreState = true) then
          BuilderCall(
            method,
            Vector(ScalaExpr.IntValue(builder.temperature.get.value))
          )
        else
          BuilderCall(
            method,
            Vector(
              ScalaExpr.NewInstance(
                FluidBuilderType,
                fluidBuilderCalls(builder, includeState = false)
              )
            )
          )
      case _ if hasOnlyFluidState(builder) =>
        BuilderCall(
          "fluid",
          Vector(
            ScalaExpr.Symbol(entry.key.path),
            ScalaExpr.Symbol(fluidStatePath(builder.state.get))
          )
        )
      case _ =>
        BuilderCall(
          "fluid",
          Vector(
            ScalaExpr.Symbol(entry.key.path),
            ScalaExpr.NewInstance(
              FluidBuilderType,
              fluidBuilderCalls(builder, includeState = true)
            )
          )
        )

  private def shorthandFor(
      key: FluidStorageKeyRef
  ): Option[(String, FluidState)] =
    if key.path == FluidStorageKeysOwner.append("LIQUID") then
      Some("liquid" -> FluidState.Liquid)
    else if key.path == FluidStorageKeysOwner.append("GAS") then
      Some("gas" -> FluidState.Gas)
    else if key.path == FluidStorageKeysOwner.append("PLASMA") then
      Some("plasma" -> FluidState.Plasma)
    else None

  private def hasOnlyFluidState(builder: FluidBuilderSpec): Boolean =
    builder.state.isDefined &&
      hasNoFluidBuilderContent(builder, ignoreState = true)

  private def hasOnlyFluidTemperature(
      builder: FluidBuilderSpec,
      ignoreState: Boolean
  ): Boolean =
    builder.temperature.isDefined &&
      hasNoFluidBuilderContent(
        builder.copy(temperature = None),
        ignoreState = ignoreState
      )

  private def hasNoFluidBuilderContent(
      builder: FluidBuilderSpec,
      ignoreState: Boolean
  ): Boolean =
    builder.temperature.isEmpty &&
      (ignoreState || builder.state.isEmpty) &&
      builder.color == FluidColor.Inferred &&
      builder.density.isEmpty &&
      builder.luminosity.isEmpty &&
      builder.viscosity.isEmpty &&
      builder.attributes.isEmpty &&
      builder.textures == FluidTextures.Inferred &&
      !builder.createBlock &&
      !builder.disableBucket &&
      builder.burnTime.isEmpty &&
      builder.name.isEmpty &&
      builder.translation.isEmpty

  private def fluidBuilderCalls(
      builder: FluidBuilderSpec,
      includeState: Boolean
  ): Vector[BuilderCall] =
    builder.temperature.toVector.map(value =>
      BuilderCall("temperature", Vector(ScalaExpr.IntValue(value.value)))
    ) ++
      Option
        .when(includeState)(builder.state)
        .flatten
        .toVector
        .map(state =>
          BuilderCall("state", Vector(ScalaExpr.Symbol(fluidStatePath(state))))
        ) ++
      (builder.color match
        case FluidColor.Inferred      => Vector.empty
        case FluidColor.Explicit(rgb) =>
          Vector(BuilderCall("color", Vector(ScalaExpr.HexValue(rgb.value))))
        case FluidColor.Disabled =>
          Vector(BuilderCall("disableColor"))) ++
      builder.density.toVector.map {
        case FluidDensity.GramsPerCubicCentimeter(value) =>
          BuilderCall("density", Vector(ScalaExpr.DoubleValue(value)))
        case FluidDensity.Minecraft(value) =>
          BuilderCall("density", Vector(ScalaExpr.IntValue(value)))
      } ++
      builder.luminosity.toVector.map(value =>
        BuilderCall("luminosity", Vector(ScalaExpr.IntValue(value)))
      ) ++
      builder.viscosity.toVector.map {
        case FluidViscosity.Poise(value) =>
          BuilderCall("viscosity", Vector(ScalaExpr.DoubleValue(value)))
        case FluidViscosity.Minecraft(value) =>
          BuilderCall("viscosity", Vector(ScalaExpr.IntValue(value)))
      } ++
      Option
        .when(builder.attributes.nonEmpty)(
          BuilderCall(
            "attributes",
            builder.attributes.map(value => ScalaExpr.Symbol(value.path))
          )
        )
        .toVector ++
      (builder.textures match
        case FluidTextures.Inferred    => Vector.empty
        case FluidTextures.CustomStill =>
          Vector(BuilderCall("customStill"))
        case FluidTextures.CustomStillAndFlowing =>
          Vector(
            BuilderCall(
              "textures",
              Vector(
                ScalaExpr.BooleanValue(true),
                ScalaExpr.BooleanValue(true)
              )
            )
          )) ++
      Option.when(builder.createBlock)(BuilderCall("block")).toVector ++
      Option
        .when(builder.disableBucket)(BuilderCall("disableBucket"))
        .toVector ++
      builder.burnTime.toVector.map(value =>
        BuilderCall("burnTime", Vector(ScalaExpr.IntValue(value.value)))
      ) ++
      builder.name.toVector.map(value =>
        BuilderCall("name", Vector(ScalaExpr.StringValue(value.value)))
      ) ++
      builder.translation.toVector.map(value =>
        BuilderCall("translation", Vector(ScalaExpr.StringValue(value)))
      )

  private def fluidStatePath(state: FluidState): ScalaSymbolPath =
    FluidStateOwner.append(
      state match
        case FluidState.Liquid => "LIQUID"
        case FluidState.Gas    => "GAS"
        case FluidState.Plasma => "PLASMA"
    )

  private def oreCalls(ore: OrePropertySpec): Vector[BuilderCall] =
    val oreArguments = ore.multipliers match
      case None if ore.emissive =>
        Vector(ScalaExpr.BooleanValue(true))
      case None =>
        Vector.empty
      case Some(values) =>
        Vector(
          ScalaExpr.IntValue(values.ore.value),
          ScalaExpr.IntValue(values.byproduct.value)
        ) ++ Option
          .when(ore.emissive)(ScalaExpr.BooleanValue(true))
          .toVector
    val direct = ore.directSmeltResult.toVector.map(material =>
      BuilderCall("oreSmeltInto", Vector(ScalaExpr.Symbol(material.path)))
    )
    val washed = ore.washedIn.toVector.map { wash =>
      BuilderCall(
        "washedIn",
        Vector(ScalaExpr.Symbol(wash.material.path)) ++
          wash.amount.toVector.map(value => ScalaExpr.IntValue(value.value))
      )
    }
    val separated =
      Option
        .when(ore.separatedInto.nonEmpty)(
          BuilderCall(
            "separatedInto",
            ore.separatedInto.map(value => ScalaExpr.Symbol(value.path))
          )
        )
        .toVector
    val byproducts =
      Option
        .when(ore.byproducts.nonEmpty)(
          BuilderCall(
            "addOreByproducts",
            ore.byproducts.map(value => ScalaExpr.Symbol(value.path))
          )
        )
        .toVector

    Vector(BuilderCall("ore", oreArguments)) ++
      direct ++ washed ++ separated ++ byproducts

  private def visualCalls(visuals: VisualSpec): Vector[BuilderCall] =
    val primary = visuals.primaryColor match
      case ColorSpec.Default       => Vector.empty
      case ColorSpec.Explicit(rgb) =>
        val arguments =
          Vector(ScalaExpr.HexValue(rgb.value)) ++
            Option
              .when(visuals.fluidColor == FluidColorPolicy.Disabled)(
                ScalaExpr.BooleanValue(false)
              )
              .toVector
        Vector(BuilderCall("color", arguments))
      case ColorSpec.AverageComponents =>
        Vector(BuilderCall("colorAverage"))
    val secondary = visuals.secondaryColor.toVector.map(rgb =>
      BuilderCall("secondaryColor", Vector(ScalaExpr.HexValue(rgb.value)))
    )
    val icon = visuals.iconSet.toVector.map(value =>
      BuilderCall("iconSet", Vector(ScalaExpr.Symbol(value.path)))
    )

    primary ++ secondary ++ icon

  private def flagCalls(flags: MaterialFlagSpec): Vector[BuilderCall] =
    val sortedFlags =
      flags.flags.toVector.sortBy(value => pathKey(value.path))
    if flags.presets.isEmpty then
      Option
        .when(sortedFlags.nonEmpty)(
          BuilderCall(
            "flags",
            sortedFlags.map(value => ScalaExpr.Symbol(value.path))
          )
        )
        .toVector
    else
      flags.presets.zipWithIndex.map { case (preset, index) =>
        val extras =
          if index == 0 then
            sortedFlags.map(value => ScalaExpr.Symbol(value.path))
          else Vector.empty
        BuilderCall(
          "appendFlags",
          Vector(ScalaExpr.Symbol(preset.path)) ++ extras
        )
      }

  private def componentCalls(
      composition: CompositionSpec
  ): Vector[BuilderCall] =
    Option
      .when(composition.components.nonEmpty)(
        BuilderCall(
          "components",
          composition.components.flatMap { component =>
            Vector(
              ScalaExpr.Symbol(component.material.path),
              ScalaExpr.IntValue(component.amount.value)
            )
          }
        )
      )
      .toVector

  private def blastCall(blast: BlastPropertySpec): BuilderCall =
    require(
      blast.eutOverride.isDefined || blast.durationOverride.isEmpty,
      "blast duration override requires an EU/t override"
    )
    require(
      blast.vacuumEutOverride.isDefined ||
        blast.vacuumDurationOverride.isEmpty,
      "vacuum duration override requires an EU/t override"
    )
    val hasStats =
      blast.eutOverride.isDefined ||
        blast.durationOverride.isDefined ||
        blast.vacuumEutOverride.isDefined ||
        blast.vacuumDurationOverride.isDefined
    val tempArguments =
      Vector(ScalaExpr.IntValue(blast.temperature.value)) ++
        blast.gasTier.toVector.map(value => ScalaExpr.Symbol(value.path))

    if !hasStats then BuilderCall("blast", tempArguments)
    else
      val body =
        Vector(BuilderCall("temp", tempArguments)) ++
          statsCall("blastStats", blast.eutOverride, blast.durationOverride) ++
          statsCall(
            "vacuumStats",
            blast.vacuumEutOverride,
            blast.vacuumDurationOverride
          )
      BuilderCall("blast", Vector(ScalaExpr.Lambda("b", body)))

  private def statsCall(
      method: String,
      eut: Option[VoltageExpr],
      duration: Option[DurationTicks]
  ): Vector[BuilderCall] =
    eut.toVector.map { value =>
      BuilderCall(
        method,
        Vector(voltageExpr(value)) ++
          duration.toVector.map(value => ScalaExpr.IntValue(value.value))
      )
    }

  private def voltageExpr(value: VoltageExpr): ScalaExpr =
    value match
      case VoltageExpr.Tier(name) =>
        ScalaExpr.ToInt(
          ScalaExpr.ArrayAccess(
            GTValuesOwner.append("V"),
            GTValuesOwner.append(name.value)
          )
        )
      case VoltageExpr.VA(name) =>
        ScalaExpr.ArrayAccess(
          GTValuesOwner.append("VA"),
          GTValuesOwner.append(name.value)
        )
      case VoltageExpr.Literal(value) =>
        ScalaExpr.IntValue(Math.toIntExact(value.value))

  private def tagCalls(tags: MaterialTagConfig): Vector[BuilderCall] =
    Option
      .when(tags.ignoredTagPrefixes.nonEmpty)(
        BuilderCall(
          "ignoredTagPrefixes",
          tags.ignoredTagPrefixes.map(value => ScalaExpr.Symbol(value.path))
        )
      )
      .toVector ++
      tags.customItemTags.map(value =>
        BuilderCall("customTags", Vector(ScalaExpr.Symbol(value.path)))
      )

  private def collectImports(
      declarations: Vector[MaterialDeclarationPlan],
      output: MaterialOutputSpec
  ): Vector[ScalaSymbolPath] =
    declarations
      .flatMap {
        case MaterialDeclarationPlan.NewMaterial(plan) =>
          Vector(MaterialType, ownerPath(output.idFactory)) ++
            plan.builderCalls.flatMap(callImports) ++
            plan.primaryKey.toVector.flatMap(key =>
              Vector(PropertyKeyOwner, ownerPath(key.path))
            )
        case MaterialDeclarationPlan.MarkerMaterial(_) =>
          Vector(MarkerMaterialType, ownerPath(output.idFactory))
        case MaterialDeclarationPlan.ExistingPatch(plan) =>
          Vector(PropertyKeyOwner, ownerPath(plan.target.path)) ++
            plan.operations.flatMap(patchImportPaths)
      }
      .distinct
      .sortBy(pathKey)

  private def callImports(call: BuilderCall): Vector[ScalaSymbolPath] =
    call.arguments.flatMap(expressionImports)

  private def expressionImports(
      expression: ScalaExpr
  ): Vector[ScalaSymbolPath] =
    expression match
      case ScalaExpr.Symbol(path) =>
        Vector(ownerPath(path))
      case ScalaExpr.NewInstance(typePath, calls) =>
        Vector(typePath) ++ calls.flatMap(callImports)
      case ScalaExpr.Lambda(_, calls) =>
        calls.flatMap(callImports)
      case ScalaExpr.ArrayAccess(array, index) =>
        Vector(ownerPath(array), ownerPath(index))
      case ScalaExpr.ToInt(value) =>
        expressionImports(value)
      case _ =>
        Vector.empty

  private def patchImportPaths(
      operation: PatchOperation
  ): Vector[ScalaSymbolPath] =
    val paths = operation match
      case PatchOperation.SetOreByproducts(byproducts) =>
        byproducts.map(_.path)
      case PatchOperation.SetWashedIn(material, _) =>
        Vector(material.path)
      case PatchOperation.SetSeparatedInto(materials) =>
        materials.map(_.path)
      case PatchOperation.SetDirectSmeltResult(material) =>
        Vector(material.path)
      case PatchOperation.SetMagneticMaterial(material) =>
        Vector(material.path)
      case PatchOperation.SetArcSmeltingInto(material) =>
        Vector(material.path)
      case PatchOperation.SetPrimaryKey(fluid) =>
        Vector(fluid.path)

    paths.map(ownerPath)

  private def ownerPath(path: ScalaSymbolPath): ScalaSymbolPath =
    if path.parts.sizeIs > 1 then ScalaSymbolPath(path.parts.dropRight(1))
    else path

  private def pathKey(path: ScalaSymbolPath): String =
    path.parts.mkString(".")

  private enum SolidKind(val method: String):
    case Wood extends SolidKind("wood")
    case Ingot extends SolidKind("ingot")
    case Gem extends SolidKind("gem")
    case Polymer extends SolidKind("polymer")
    case Dust extends SolidKind("dust")

    def isPresent(properties: MaterialProperties): Boolean =
      this match
        case Wood    => properties.wood.isDefined
        case Ingot   => properties.ingot.isDefined
        case Gem     => properties.gem.isDefined
        case Polymer => properties.polymer.isDefined
        case Dust    => properties.dust.isDefined

  private val SolidOrder =
    Vector(SolidKind.Wood, SolidKind.Ingot, SolidKind.Gem, SolidKind.Polymer)

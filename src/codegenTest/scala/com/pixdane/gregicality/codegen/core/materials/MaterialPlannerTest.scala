package com.pixdane.gregicality.codegen.core.materials

import cats.data.NonEmptyVector
import com.pixdane.gregicality.core.refs.*
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse}
import org.junit.jupiter.api.Test

class MaterialPlannerTest:

  @Test
  def dustSettingsUseOneCarrierAndPolymerBurnIsSeparate(): Unit =
    val ingot = planned(
      material(
        properties = MaterialProperties(
          dust = Some(
            DustPropertySpec(
              harvestLevel = Some(valid(HarvestLevel.from(3))),
              burnTime = Some(valid(BurnTimeTicks.from(120)))
            )
          ),
          ingot = Some(IngotPropertySpec())
        )
      )
    )
    val polymer = planned(
      material(
        properties = MaterialProperties(
          dust = Some(
            DustPropertySpec(
              harvestLevel = Some(valid(HarvestLevel.from(4))),
              burnTime = Some(valid(BurnTimeTicks.from(200)))
            )
          ),
          polymer = Some(PolymerPropertySpec())
        )
      )
    )
    val burnOnlyDust = planned(
      material(
        properties = MaterialProperties(
          dust = Some(
            DustPropertySpec(
              burnTime = Some(valid(BurnTimeTicks.from(80)))
            )
          )
        )
      )
    )
    val woodAndIngot = planned(
      material(
        properties = MaterialProperties(
          dust = Some(
            DustPropertySpec(
              harvestLevel = Some(valid(HarvestLevel.from(1))),
              burnTime = Some(valid(BurnTimeTicks.from(400)))
            )
          ),
          ingot = Some(IngotPropertySpec()),
          wood = Some(WoodPropertySpec())
        )
      )
    )

    assertEquals(
      Vector(
        BuilderCall(
          "ingot",
          Vector(ScalaExpr.IntValue(3), ScalaExpr.IntValue(120))
        ),
        BuilderCall("buildAndRegister")
      ),
      ingot.builderCalls
    )
    assertEquals(
      Vector(
        BuilderCall("polymer", Vector(ScalaExpr.IntValue(4))),
        BuilderCall("burnTime", Vector(ScalaExpr.IntValue(200))),
        BuilderCall("buildAndRegister")
      ),
      polymer.builderCalls
    )
    assertEquals(
      Vector(
        BuilderCall("burnTime", Vector(ScalaExpr.IntValue(80))),
        BuilderCall("buildAndRegister")
      ),
      burnOnlyDust.builderCalls
    )
    assertEquals(
      Vector(
        BuilderCall(
          "wood",
          Vector(ScalaExpr.IntValue(1), ScalaExpr.IntValue(400))
        ),
        BuilderCall("ingot"),
        BuilderCall("buildAndRegister")
      ),
      woodAndIngot.builderCalls
    )

  @Test
  def fluidAndOreOverloadsKeepDefaultsInsideGtceu(): Unit =
    val plan = planned(
      material(
        properties = MaterialProperties(
          fluid = Some(
            FluidPropertySpec(
              NonEmptyVector.of(
                FluidEntry(liquidKey),
                FluidEntry(
                  gasKey,
                  FluidBuilderSpec(
                    temperature = Some(valid(FluidTemperature.from(350))),
                    state = Some(FluidState.Gas)
                  )
                ),
                FluidEntry(
                  customFluidKey,
                  FluidBuilderSpec(state = Some(FluidState.Plasma))
                )
              )
            )
          ),
          ore = Some(
            OrePropertySpec(
              washedIn = Some(OreWashSpec(mercury))
            )
          )
        )
      )
    )

    assertEquals(
      Vector(
        BuilderCall("liquid"),
        BuilderCall("gas", Vector(ScalaExpr.IntValue(350))),
        BuilderCall(
          "fluid",
          Vector(
            ScalaExpr.Symbol(customFluidKey.path),
            ScalaExpr.Symbol(fluidStateOwner.append("PLASMA"))
          )
        ),
        BuilderCall("ore"),
        BuilderCall(
          "washedIn",
          Vector(ScalaExpr.Symbol(mercury.path))
        ),
        BuilderCall("buildAndRegister")
      ),
      plan.builderCalls
    )

  @Test
  def plannerKeepsAuthoredOrderWithoutRenderingInducedContent(): Unit =
    val plan = planned(
      material(
        identity = MaterialIdentity(displayName = Some("Test Material")),
        properties = MaterialProperties(
          fluid = Some(
            FluidPropertySpec(NonEmptyVector.one(FluidEntry(liquidKey)))
          ),
          blast = Some(BlastPropertySpec(valid(Kelvin.from(3900))))
        ),
        visuals = VisualSpec(
          primaryColor = ColorSpec.Explicit(valid(HexRgb.from(0x123456)))
        ),
        composition = CompositionSpec(
          components = Vector(
            ComponentSpec(iron, valid(PositiveInt.from(1)))
          ),
          formulaOverride = Some(FormulaOverride("Fe", true))
        )
      )
    )
    val methods = plan.builderCalls.map(_.method)

    assertEquals(
      Vector(
        "langValue",
        "liquid",
        "color",
        "components",
        "formula",
        "blast",
        "buildAndRegister"
      ),
      methods
    )
    assertFalse(methods.contains("dust"))
    assertFalse(methods.contains("ingot"))
    assertFalse(methods.contains("flags"))

  @Test
  def multipleFlagPresetsRemainSeparateCalls(): Unit =
    val plan = planned(
      material(
        flags = MaterialFlagSpec(
          presets = Vector(stdMetal, extMetal),
          flags = Set(generatePlate, generateFoil)
        )
      )
    )

    assertEquals(
      Vector(
        BuilderCall(
          "appendFlags",
          Vector(
            ScalaExpr.Symbol(stdMetal.path),
            ScalaExpr.Symbol(generateFoil.path),
            ScalaExpr.Symbol(generatePlate.path)
          )
        ),
        BuilderCall(
          "appendFlags",
          Vector(ScalaExpr.Symbol(extMetal.path))
        ),
        BuilderCall("buildAndRegister")
      ),
      plan.builderCalls
    )

  private def planned(spec: NewMaterialSpec): NewMaterialPlan =
    val set = MaterialSet(
      NonEmptyVector.one(MaterialDeclaration.NewMaterial(spec))
    )
    MaterialPlanner.plan(set, output).declarations.head match
      case MaterialDeclarationPlan.NewMaterial(plan) => plan
      case other                                     =>
        throw new AssertionError(s"expected new material plan, found $other")

  private def material(
      properties: MaterialProperties = MaterialProperties(),
      identity: MaterialIdentity = MaterialIdentity(),
      visuals: VisualSpec = VisualSpec(),
      composition: CompositionSpec = CompositionSpec(),
      flags: MaterialFlagSpec = MaterialFlagSpec()
  ): NewMaterialSpec =
    NewMaterialSpec(
      id = valid(RegistryPath.from("test_material")),
      field = valid(ScalaIdent.from("TestMaterial")),
      identity = identity,
      visuals = visuals,
      composition = composition,
      properties = properties,
      flags = flags
    )

  private val output = MaterialOutputSpec(
    packageName = "com.pixdane.gregicality.common.data.materials",
    objectName = valid(ScalaIdent.from("GCYMaterialsTest")),
    idFactory = ScalaSymbolPath.member(
      "com.pixdane.gregicality.Gregicality",
      "id"
    )
  )
  private val fluidStorageKeysOwner = ScalaSymbolPath.fromFqcn(
    "com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys"
  )
  private val fluidStateOwner =
    ScalaSymbolPath.fromFqcn("com.gregtechceu.gtceu.api.fluids.FluidState")
  private val liquidKey =
    FluidStorageKeyRef(fluidStorageKeysOwner.append("LIQUID"))
  private val gasKey =
    FluidStorageKeyRef(fluidStorageKeysOwner.append("GAS"))
  private val customFluidKey = FluidStorageKeyRef(
    ScalaSymbolPath.member("com.example.CustomFluidKeys", "GAS")
  )
  private val iron = materialRef("Iron", "iron")
  private val mercury = materialRef("Mercury", "mercury")
  private val stdMetal = preset("STD_METAL")
  private val extMetal = preset("EXT_METAL")
  private val generatePlate = flag("GENERATE_PLATE")
  private val generateFoil = flag("GENERATE_FOIL")

  private def materialRef(name: String, id: String): MaterialRef =
    MaterialRef(
      ResourceId("gtceu", id),
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.common.data.GTMaterials",
        name
      )
    )

  private def preset(name: String): MaterialFlagPresetRef =
    MaterialFlagPresetRef(
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.common.data.GTMaterials",
        name
      )
    )

  private def flag(name: String): MaterialFlagRef =
    MaterialFlagRef(
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags",
        name
      )
    )

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      errors => throw new AssertionError(errors.toString),
      identity
    )

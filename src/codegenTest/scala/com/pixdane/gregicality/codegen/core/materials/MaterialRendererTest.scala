package com.pixdane.gregicality.codegen.core.materials

import cats.data.NonEmptyVector
import com.pixdane.gregicality.core.refs.*
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse}
import org.junit.jupiter.api.Test

import java.nio.charset.StandardCharsets

class MaterialRendererTest:

  @Test
  def rendersRepresentativeNewMaterialGolden(): Unit =
    val set = MaterialSet(
      NonEmptyVector.one(
        MaterialDeclaration.NewMaterial(
          NewMaterialSpec(
            id = registryPath("polyimide"),
            field = ident("Polyimide"),
            identity = MaterialIdentity(displayName = Some("Polyimide")),
            visuals = VisualSpec(
              primaryColor = ColorSpec.Explicit(rgb(0x2d2d2d)),
              secondaryColor = Some(rgb(0x111111)),
              iconSet = Some(icon("METALLIC")),
              fluidColor = FluidColorPolicy.Disabled
            ),
            composition = CompositionSpec(
              components = Vector(
                component("Carbon", "carbon", 22),
                component("Hydrogen", "hydrogen", 10),
                component("Nitrogen", "nitrogen", 2),
                component("Oxygen", "oxygen", 5)
              ),
              formulaOverride = Some(
                FormulaOverride("(C22H10N2O5)n", formatSubscripts = true)
              )
            ),
            properties = MaterialProperties(
              dust = Some(
                DustPropertySpec(
                  harvestLevel = Some(valid(HarvestLevel.from(4))),
                  burnTime = Some(valid(BurnTimeTicks.from(200)))
                )
              ),
              polymer = Some(PolymerPropertySpec()),
              fluid = Some(
                FluidPropertySpec(
                  fluids = NonEmptyVector.of(
                    FluidEntry(
                      liquidKey,
                      FluidBuilderSpec(
                        temperature = Some(valid(FluidTemperature.from(700))),
                        color = FluidColor.Disabled,
                        attributes = Vector(fluidAttribute("ACID")),
                        textures = FluidTextures.CustomStillAndFlowing,
                        createBlock = true,
                        disableBucket = true,
                        burnTime = Some(valid(BurnTimeTicks.from(400))),
                        name = Some(registryPath("liquid_polyimide")),
                        translation = Some("gregicality.fluid.liquid_polyimide")
                      )
                    ),
                    FluidEntry(gasKey)
                  ),
                  primaryKey = Some(gasKey)
                )
              ),
              blast = Some(
                BlastPropertySpec(
                  temperature = valid(Kelvin.from(3900)),
                  gasTier = Some(gasTier("HIGH")),
                  eutOverride = Some(VoltageExpr.VA(ident("EV"))),
                  durationOverride = Some(valid(DurationTicks.from(1000))),
                  vacuumEutOverride = Some(VoltageExpr.VA(ident("HV"))),
                  vacuumDurationOverride = Some(valid(DurationTicks.from(300)))
                )
              )
            ),
            flags = MaterialFlagSpec(
              presets = Vector(preset("STD_METAL")),
              flags = Set(flag("GENERATE_PLATE"), flag("GENERATE_FOIL"))
            )
          )
        )
      )
    )

    assertEquals(
      golden("polyimide.scala.golden"),
      render(set, "GCYMaterialsChemistryPolymers")
    )

  @Test
  def rendersMarkerWithoutMaterialBuilderGolden(): Unit =
    val set = MaterialSet(
      NonEmptyVector.one(
        MaterialDeclaration.MarkerMaterial(
          MarkerMaterialSpec(registryPath("uev"), ident("UEV"))
        )
      )
    )
    val rendered = render(set, "GCYMaterialsMarkers")

    assertEquals(golden("marker.scala.golden"), rendered)
    assertFalse(rendered.contains("Material.Builder"))

  @Test
  def rendersPatchOperationsIntoPatchMethodGolden(): Unit =
    val iron = materialRef("Iron", "iron")
    val set = MaterialSet(
      NonEmptyVector.one(
        MaterialDeclaration.ExistingPatch(
          MaterialPatchSpec(
            target = iron,
            operations = NonEmptyVector.of(
              PatchOperation.SetOreByproducts(
                Vector(
                  materialRef("Nickel", "nickel"),
                  materialRef("Tin", "tin")
                )
              ),
              PatchOperation.SetWashedIn(
                materialRef("Mercury", "mercury"),
                valid(PositiveInt.from(100))
              ),
              PatchOperation.SetSeparatedInto(
                Vector(materialRef("Gold", "gold"))
              ),
              PatchOperation.SetDirectSmeltResult(iron),
              PatchOperation.SetMagneticMaterial(
                materialRef("NeodymiumMagnetic", "neodymium_magnetic")
              ),
              PatchOperation.SetArcSmeltingInto(
                materialRef("WroughtIron", "wrought_iron")
              ),
              PatchOperation.SetPrimaryKey(gasKey)
            )
          )
        )
      )
    )

    assertEquals(
      golden("patch.scala.golden"),
      render(set, "GCYMaterialsPatches")
    )

  private def render(set: MaterialSet, objectName: String): String =
    val output = MaterialOutputSpec(
      packageName = "com.pixdane.gregicality.common.data.materials",
      objectName = ident(objectName),
      idFactory = ScalaSymbolPath.member(
        "com.pixdane.gregicality.Gregicality",
        "id"
      )
    )
    MaterialRenderer.render(MaterialPlanner.plan(set, output)).render

  private def golden(name: String): String =
    val path =
      s"/com/pixdane/gregicality/codegen/core/materials/golden/$name"
    val stream = Option(getClass.getResourceAsStream(path))
      .getOrElse(throw new AssertionError(s"missing golden resource $path"))
    try String(stream.readAllBytes(), StandardCharsets.UTF_8)
    finally stream.close()

  private val fluidStorageKeysOwner = ScalaSymbolPath.fromFqcn(
    "com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys"
  )
  private val liquidKey =
    FluidStorageKeyRef(fluidStorageKeysOwner.append("LIQUID"))
  private val gasKey =
    FluidStorageKeyRef(fluidStorageKeysOwner.append("GAS"))

  private def ident(value: String): ScalaIdent =
    valid(ScalaIdent.from(value))

  private def registryPath(value: String): RegistryPath =
    valid(RegistryPath.from(value))

  private def rgb(value: Int): HexRgb =
    valid(HexRgb.from(value))

  private def materialRef(name: String, id: String): MaterialRef =
    MaterialRef(
      ResourceId("gtceu", id),
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.common.data.GTMaterials",
        name
      )
    )

  private def component(name: String, id: String, amount: Int): ComponentSpec =
    ComponentSpec(
      materialRef(name, id),
      valid(PositiveInt.from(amount))
    )

  private def icon(name: String): MaterialIconRef =
    MaterialIconRef(
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet",
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

  private def fluidAttribute(name: String): FluidAttributeRef =
    FluidAttributeRef(
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes",
        name
      )
    )

  private def gasTier(name: String): GasTierRef =
    GasTierRef(
      ScalaSymbolPath.member(
        "com.gregtechceu.gtceu.api.data.chemical.material.properties.BlastProperty.GasTier",
        name
      )
    )

  private def valid[A](result: ValidationResult[A]): A =
    result.fold(
      errors => throw new AssertionError(errors.toString),
      identity
    )

package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.Element
import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet
import com.gregtechceu.gtceu.api.data.tag.TagPrefix
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

/** Namespace and adapter factory in scope while authoring materials.
  *
  * A `RegistryContext` is supplied as a `given` so the top-level `material`
  * entry point can build the namespaced `ResourceLocation` and obtain a
  * [[MaterialBuilderAdapter]] without the DSL body ever seeing the raw
  * `Material.Builder`. The default factory is the real GTCEu-backed one; tests
  * pass a recording fake.
  *
  * `namespace` must be non-empty and `factory` non-null. The companion `apply`
  * is package-private so the [[MaterialBuilderFactory]] type cannot escape the
  * DSL package through a public constructor, while callers inside `dsl`
  * (including tests in the same package) can still write
  * `RegistryContext("gregicality", factory)`.
  */
final class RegistryContext private (
    val namespace: String,
    private[dsl] val factory: MaterialBuilderFactory
):
  require(
    namespace != null && namespace.nonEmpty,
    "RegistryContext namespace must be non-empty"
  )
  require(factory != null, "RegistryContext factory must not be null")

object RegistryContext:
  /** Builds a context for `namespace` using `factory`.
    *
    * Package-private so the `MaterialBuilderFactory` parameter cannot leak out
    * of the DSL package via a public constructor.
    */
  private[dsl] def apply(
      namespace: String,
      factory: MaterialBuilderFactory
  ): RegistryContext =
    new RegistryContext(namespace, factory)

  /** Convenience constructor that uses the real GTCEu-backed factory. */
  def apply(namespace: String): RegistryContext =
    new RegistryContext(namespace, MaterialAdapterFactory.real)

/** Authoring scope for one material.
  *
  * Created by the top-level `material` function and made available as a `given`
  * inside its body. Direct-call DSL methods (`langValue`, `formula`, `ingot`,
  * `visual`, `flags`, `components`) forward to the underlying
  * [[MaterialBuilderAdapter]] immediately, in authoring order. Nested fluid,
  * blast, and ore blocks accumulate their section values and commit one
  * immutable specification to the adapter when the block returns normally.
  *
  * The raw `Material.Builder` is never exposed as a `given`; only this context
  * is. `buildAndRegister` is invoked exactly once by `material` after a normal
  * body return, and is suppressed if the body throws.
  */
private[dsl] final class MaterialContext(
    private val adapter: MaterialBuilderAdapter
):

  /** Sets the localized display name. */
  def langValue(name: String): Unit = adapter.langValue(name)

  /** Sets the chemical formula string. */
  def formula(f: String): Unit = adapter.formula(f)

  /** Sets the chemical formula with GTCEu's formatting switch. */
  def formula(f: String, withFormatting: Boolean): Unit =
    adapter.formula(f, withFormatting)

  /** Associates the current material with a GTCEu chemical element. */
  def element(value: Element): Unit = adapter.element(value)

  /** Adds a dust property using one of GTCEu's overload shapes. */
  def dust(): Unit = adapter.dust(None, None)

  /** Adds a dust property with a harvest level. */
  def dust(level: Int): Unit = adapter.dust(Some(level), None)

  /** Adds a dust property with a harvest level and burn time. */
  def dust(level: Int, burnTime: Int): Unit =
    adapter.dust(Some(level), Some(burnTime))

  /** Adds a wood property using one of GTCEu's overload shapes. */
  def wood(): Unit = adapter.wood(None, None)

  /** Adds a wood property with a harvest level. */
  def wood(level: Int): Unit = adapter.wood(Some(level), None)

  /** Adds a wood property with a harvest level and burn time. */
  def wood(level: Int, burnTime: Int): Unit =
    adapter.wood(Some(level), Some(burnTime))

  /** Marks the material as an ingot with the given harvest level. */
  def ingot(level: Int): Unit = adapter.ingotForm(Some(level), None)

  /** Adds an ingot property using the no-argument GTCEu overload. */
  def ingot(): Unit = adapter.ingotForm(None, None)

  /** Adds an ingot property with a harvest level and burn time. */
  def ingot(level: Int, burnTime: Int): Unit =
    adapter.ingotForm(Some(level), Some(burnTime))

  /** Adds a gem property using one of GTCEu's overload shapes. */
  def gem(): Unit = adapter.gem(None, None)

  /** Adds a gem property with a harvest level. */
  def gem(level: Int): Unit = adapter.gem(Some(level), None)

  /** Adds a gem property with a harvest level and burn time. */
  def gem(level: Int, burnTime: Int): Unit =
    adapter.gem(Some(level), Some(burnTime))

  /** Adds a polymer property using one of GTCEu's overload shapes. */
  def polymer(): Unit = adapter.polymer(None, None)

  /** Adds a polymer property with a harvest level. */
  def polymer(level: Int): Unit = adapter.polymer(Some(level), None)

  /** Adds a polymer property with a harvest level and burn time. */
  def polymer(level: Int, burnTime: Int): Unit =
    adapter.polymer(Some(level), Some(burnTime))

  /** Sets the material-level burn time. */
  def materialBurnTime(value: Int): Unit = adapter.materialBurnTime(value)

  /** Replaces the material color with GTCEu's component average. */
  def colorAverage(): Unit = adapter.colorAverage()

  /** Applies color, icon set, and optional secondary color. */
  def visual(spec: VisualSpec): Unit = adapter.visual(spec)

  /** Applies a compact visual configuration with named optional values.
    *
    * `hasFluidColor = false` selects GTCEu's two-argument color overload;
    * leaving it at its default preserves GTCEu's normal fluid-color behavior.
    */
  def visual(
      color: HexColor,
      iconSet: MaterialIconSet,
      secondary: Option[HexColor] = None,
      hasFluidColor: Boolean = true
  ): Unit =
    adapter.visual(
      VisualSpec(
        color = color,
        iconSet = iconSet,
        secondary = secondary,
        hasFluidColor = Some(hasFluidColor)
      )
    )

  /** Adds flags by varargs. */
  def flags(fs: MaterialFlag*): Unit = adapter.flags(fs)

  /** Adds flags from any Scala collection. */
  def flags(fs: Iterable[MaterialFlag]): Unit = adapter.flags(fs.toSeq)

  /** Adds a Java-collection preset plus extra varargs flags. */
  def flags(
      preset: java.util.Collection[MaterialFlag],
      extras: MaterialFlag*
  ): Unit =
    adapter.appendFlags(preset, extras)

  /** Sets the composition from typed material/amount pairs by varargs. */
  def components(amounts: MaterialAmount*): Unit = adapter.components(amounts)

  /** Sets the composition from any Scala collection of amounts. */
  def components(amounts: Iterable[MaterialAmount]): Unit =
    adapter.components(amounts.toSeq)

  /** Opens an ore configuration block.
    *
    * Settings inside `body` accumulate into a single [[OreSpec]] that is
    * committed to the adapter (as one `ore` call) when `body` returns normally,
    * at this block's position in the authoring order.
    */
  def ore(body: OreContext ?=> Unit): Unit =
    val ctx = new OreContext
    given OreContext = ctx
    body(using ctx)
    adapter.ore(ctx.toSpec)

  /** Adds a liquid storage entry with inferred GTCEu settings. */
  def liquid(): Unit =
    adapter.fluid(FluidSpec(FluidKind.Liquid))

  /** Adds a liquid storage entry with an explicit temperature. */
  def liquid(temperature: Kelvin): Unit =
    adapter.fluid(
      FluidSpec(FluidKind.Liquid, temperature = Some(temperature))
    )

  /** Opens a gas configuration block. */
  def gas(body: FluidContext ?=> Unit): Unit =
    configureFluid(FluidKind.Gas, body)

  /** Opens a plasma configuration block. */
  def plasma(body: FluidContext ?=> Unit): Unit =
    configureFluid(FluidKind.Plasma, body)

  /** Opens a blast-property configuration block.
    *
    * Temperature and gas tier are collected separately for concise authoring,
    * then combined by the adapter because GTCEu exposes gas tier only through
    * `BlastProperty.Builder.temp(int, GasTier)`.
    */
  def blast(body: BlastContext ?=> Unit): Unit =
    val ctx = new BlastContext
    given BlastContext = ctx
    body(using ctx)
    adapter.blast(ctx.toSpec)

  /** Opens a tool-property configuration block.
    *
    * The four entry parameters map directly to
    * `ToolProperty.Builder.of(speed, damage, durability, level)`. Optional
    * settings are accumulated in [[ToolContext]] and submitted once when the
    * body returns normally.
    */
  def tool(
      speed: Double,
      damage: Double,
      durability: Int,
      level: Int
  )(body: ToolContext ?=> Unit): Unit =
    val ctx = new ToolContext(speed, damage, durability, level)
    given ToolContext = ctx
    body(using ctx)
    adapter.tool(ctx.toSpec)

  /** Opens an armor-property configuration block.
    *
    * `protection` guarantees the four values required by
    * `ArmorProperty.Builder.of`, ordered helmet, chestplate, leggings, boots.
    */
  def armor(
      durability: Int,
      protection: Armor
  )(body: ArmorContext ?=> Unit): Unit =
    val ctx = new ArmorContext(durability, protection)
    given ArmorContext = ctx
    body(using ctx)
    adapter.armor(ctx.toSpec)

  /** Sets the ore-smelting target directly on the material builder. */
  def oreSmeltInto(material: Material): Unit = adapter.oreSmeltInto(material)

  /** Sets the polarizing target directly on the material builder. */
  def polarizesInto(material: Material): Unit = adapter.polarizesInto(material)

  /** Sets the arc-smelting target directly on the material builder. */
  def arcSmeltInto(material: Material): Unit = adapter.arcSmeltInto(material)

  /** Sets the macerating target directly on the material builder. */
  def macerateInto(material: Material): Unit = adapter.macerateInto(material)

  /** Sets the ingot-smelting target directly on the material builder. */
  def ingotSmeltInto(material: Material): Unit =
    adapter.ingotSmeltInto(material)

  /** Applies turbine rotor statistics. */
  def rotor(spec: RotorSpec): Unit = adapter.rotor(spec)

  /** Applies cable properties. */
  def cable(spec: CableSpec): Unit = adapter.cable(spec)

  /** Applies fluid-pipe properties. */
  def fluidPipe(spec: FluidPipeSpec): Unit = adapter.fluidPipe(spec)

  /** Applies item-pipe properties. */
  def itemPipe(spec: ItemPipeSpec): Unit = adapter.itemPipe(spec)

  /** Marks tag prefixes as ignored for the current material. */
  def ignoredTagPrefixes(prefixes: TagPrefix*): Unit =
    adapter.ignoredTagPrefixes(prefixes)

  /** Marks tag prefixes as ignored from any Scala collection. */
  def ignoredTagPrefixes(prefixes: Iterable[TagPrefix]): Unit =
    adapter.ignoredTagPrefixes(prefixes.toSeq)

  /** Adds custom item tags by varargs. */
  def customTags(tags: TagKey[Item]*): Unit =
    adapter.customTags(tags)

  /** Adds custom item tags from any Scala collection. */
  def customTags(tags: Iterable[TagKey[Item]]): Unit =
    adapter.customTags(tags.toSeq)

  /** Removes any hazard property from the current material. */
  def removeHazard(): Unit =
    adapter.removeHazard()

  /** Applies GTCEu's standard radioactive hazard. */
  def radioactiveHazard(multiplier: Double): Unit =
    adapter.radioactiveHazard(multiplier)

  /** Applies a complete hazard configuration. */
  def hazard(spec: HazardSpec): Unit =
    adapter.hazard(spec)

  /** Runs one fluid block and submits its assembled specification once. */
  private[dsl] def configureFluid(
      kind: FluidKind,
      body: FluidContext ?=> Unit
  ): Unit =
    val ctx = new FluidContext(kind)
    given FluidContext = ctx
    body(using ctx)
    adapter.fluid(ctx.toSpec)

  /** Finalizes and registers the material. Called by `material`, not user code.
    */
  private[dsl] def finalizeRegistration(): Material = adapter.buildAndRegister()

/** Accumulator for an `ore:` block.
  *
  * GTCEu has no `OreProperty.Builder`, so the DSL collects ore settings here
  * and folds them into a single [[OreSpec]] at block exit. Collection-typed
  * settings (`separatedInto`, `byproducts`) append across repeated calls;
  * scalar settings (`settings`, `washedIn`) take the last value written.
  *
  * `settings` re-validates its positive-multiplier invariants directly, rather
  * than relying on [[OreSpec]]'s `case class` `require`, so that a later `copy`
  * inside `toSpec` cannot bypass validation.
  */
private[dsl] final class OreContext:

  private var multiplier: Int = 1
  private var byproductMultiplier: Int = 1
  private var emissive: Boolean = false
  private var washedIn: Option[WashSpec] = None
  private val separatedInto: scala.collection.mutable.ListBuffer[Material] =
    scala.collection.mutable.ListBuffer.empty
  private val byproducts: scala.collection.mutable.ListBuffer[Material] =
    scala.collection.mutable.ListBuffer.empty

  /** Sets the ore multipliers and emissive flag. Last call wins. */
  def settings(
      multiplier: Int = 1,
      byproduct: Int = 1,
      emissive: Boolean = false
  ): Unit =
    require(multiplier > 0, s"ore multiplier must be > 0, got $multiplier")
    require(
      byproduct > 0,
      s"ore byproduct multiplier must be > 0, got $byproduct"
    )
    this.multiplier = multiplier
    this.byproductMultiplier = byproduct
    this.emissive = emissive

  /** Sets the washing fluid and amount. Last call wins. */
  def washedIn(fluid: Material, amount: Int = 100): Unit =
    this.washedIn = Some(WashSpec(fluid, amount))

  /** Appends materials to the separatedInto list. */
  def separatedInto(ms: Material*): Unit =
    separatedInto ++= ms

  /** Appends materials to the byproducts list. */
  def byproducts(ms: Material*): Unit =
    byproducts ++= ms

  /** Builds the immutable [[OreSpec]] accumulated by this context. */
  def toSpec: OreSpec =
    OreSpec(
      multiplier = multiplier,
      byproductMultiplier = byproductMultiplier,
      emissive = emissive,
      washedIn = washedIn,
      separatedInto = separatedInto.toList,
      byproducts = byproducts.toList
    )

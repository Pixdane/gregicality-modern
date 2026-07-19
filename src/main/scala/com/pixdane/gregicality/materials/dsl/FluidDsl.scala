package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute

/** Field-assignment carrier shared by every `:=` DSL slot.
  *
  * Each `:=`-assignable DSL name (`temperature`, `color`, `luminosity`,
  * `burnTime`, and later blast/tool/armor fields) resolves to an `Assigner`
  * bound to a setter on the current context. Authors never construct these
  * directly; the top-level DSL functions return them so the `field := value`
  * shape stays uniform across the whole material DSL.
  *
  * `T` is contravariant because an `Assigner[FluidAttribute]` should accept any
  * subtype the author supplies, and the setter only consumes the value.
  */
private[dsl] final class Assigner[-T](private val set: T => Unit):
  /** Records `value` on the current context field. Last call wins. */
  infix def :=(value: T): Unit = set(value)

/** Density assignment carrier accepting either Minecraft units or `g/cm^3`.
  *
  * GTCEu's `FluidBuilder.density(...)` is overloaded on `int` and `double`;
  * this carrier lets `density := 0.8` and `density := 1200` route to the
  * matching [[FluidDensity]] variant without the author naming the unit.
  */
private[dsl] final class FluidDensityAssigner(
    private val set: FluidDensity => Unit
):
  /** Records a Minecraft-unit density. */
  infix def :=(value: Int): Unit = set(FluidDensity.Minecraft(value))

  /** Records a `g/cm^3` density. */
  infix def :=(value: Double): Unit = set(
    FluidDensity.GramsPerCubicCentimeter(value)
  )

/** Viscosity assignment carrier accepting either Minecraft units or poise.
  *
  * Mirrors [[FluidDensityAssigner]] for `FluidBuilder.viscosity(...)`.
  */
private[dsl] final class FluidViscosityAssigner(
    private val set: FluidViscosity => Unit
):
  /** Records a Minecraft-unit viscosity. */
  infix def :=(value: Int): Unit = set(FluidViscosity.Minecraft(value))

  /** Records a poise viscosity. */
  infix def :=(value: Double): Unit = set(FluidViscosity.Poise(value))

/** Capability shared by sections that expose `temperature := Kelvin`. */
private[dsl] trait TemperatureTarget:
  /** Records a temperature value on the current section. */
  def setTemperature(value: Kelvin): Unit

/** Accumulator for a `fluid(FluidKind):`, `gas:`, or `plasma:` block.
  *
  * The outer material context fixes the [[FluidKind]] and constructs a
  * `FluidContext` with it before running the block body. Inside the body, the
  * top-level `temperature`/`color`/`density`/... functions write to this
  * accumulator; on block exit, [[toSpec]] produces the immutable [[FluidSpec]]
  * that the adapter receives as one `fluid` call.
  *
  * `colorEnabled` flips to `false` the first time the author issues
  * `customStill`, `textures(...)`, or `disableColor`. The `color` field itself
  * is preserved so the adapter can still read what the author wrote, but the
  * flag tells it to call `disableColor()` rather than `color(int)`. The DSL
  * does not replicate GTCEu's color-range or `0xFFFFFFFF` handling.
  *
  * `attributes` append in authoring order; scalar fields take the last value
  * written. `hasBucket` defaults to `true` and flips to `false` on
  * `disableBucket`. `hasBlock` defaults to `false` and flips to `true` on
  * `block`.
  */
private[dsl] final class FluidContext(val kind: FluidKind)
    extends TemperatureTarget:
  private var temperature: Option[Kelvin] = None
  private var color: Option[HexColor] = None
  private var density: Option[FluidDensity] = None
  private var luminosity: Option[Int] = None
  private var viscosity: Option[FluidViscosity] = None
  private var burnTime: Option[Int] = None
  private val attributes: scala.collection.mutable.ListBuffer[FluidAttribute] =
    scala.collection.mutable.ListBuffer.empty
  private var textures: Option[FluidTextures] = None
  private var hasBlock: Boolean = false
  private var hasBucket: Boolean = true
  private var colorEnabled: Boolean = true

  /** Records an explicit kelvin temperature. Last call wins. */
  def setTemperature(v: Kelvin): Unit = temperature = Some(v)

  /** Records an explicit RGB color. Last call wins. */
  private[dsl] def setColor(v: HexColor): Unit = color = Some(v)

  /** Records an explicit density. Last call wins. */
  private[dsl] def setDensity(v: FluidDensity): Unit = density = Some(v)

  /** Records an explicit luminosity. Last call wins. */
  private[dsl] def setLuminosity(v: Int): Unit = luminosity = Some(v)

  /** Records an explicit viscosity. Last call wins. */
  private[dsl] def setViscosity(v: FluidViscosity): Unit = viscosity = Some(v)

  /** Records an explicit burn time. Last call wins. */
  private[dsl] def setBurnTime(v: Int): Unit = burnTime = Some(v)

  /** Appends `attrs` to the attribute list in order. */
  private[dsl] def addAttributes(attrs: IterableOnce[FluidAttribute]): Unit =
    attributes ++= attrs

  /** Records custom texture flags and disables authoritative color. */
  private[dsl] def setTextures(t: FluidTextures): Unit =
    textures = Some(t)
    colorEnabled = false

  /** Marks this fluid as having a fluid block. */
  private[dsl] def setBlock(): Unit = hasBlock = true

  /** Marks this fluid as having no bucket. */
  private[dsl] def setDisableBucket(): Unit = hasBucket = false

  /** Disables authoritative color without clearing the recorded value. */
  private[dsl] def setDisableColor(): Unit = colorEnabled = false

  /** Builds the immutable [[FluidSpec]] accumulated by this context. */
  private[dsl] def toSpec: FluidSpec =
    FluidSpec(
      kind = kind,
      temperature = temperature,
      color = color,
      density = density,
      luminosity = luminosity,
      viscosity = viscosity,
      burnTime = burnTime,
      attributes = attributes.toList,
      textures = textures,
      hasBlock = hasBlock,
      hasBucket = hasBucket,
      colorEnabled = colorEnabled
    )

// Top-level fluid-section DSL. These definitions live at package scope so
// authors (and tests in the same package) can call `temperature := ...`,
// `color := ...`, `block`, `disableBucket`, `disableColor`, `customStill`,
// `textures(...)`, and `attributes(...)` inside any fluid section without an
// explicit import. A `FluidContext` must be in `given` scope; the outer
// material entry point supplies it.

/** Returns the assigner for the current section's kelvin temperature.
  *
  * Fluid and blast contexts both implement [[TemperatureTarget]], so one
  * capability-based definition preserves the shared authoring token without
  * introducing ambiguous same-name overloads.
  */
def temperature(using target: TemperatureTarget): Assigner[Kelvin] =
  Assigner(target.setTemperature)

/** Returns the assigner for the fluid's RGB color. */
def color(using fc: FluidContext): Assigner[HexColor] =
  Assigner(fc.setColor)

/** Returns the assigner for the fluid's density, in Minecraft units or
  * `g/cm^3`.
  */
def density(using fc: FluidContext): FluidDensityAssigner =
  FluidDensityAssigner(fc.setDensity)

/** Returns the assigner for the fluid's luminosity. */
def luminosity(using fc: FluidContext): Assigner[Int] =
  Assigner(fc.setLuminosity)

/** Returns the assigner for the fluid's viscosity, in Minecraft units or poise.
  */
def viscosity(using fc: FluidContext): FluidViscosityAssigner =
  FluidViscosityAssigner(fc.setViscosity)

/** Returns the assigner for the fluid's burn time in ticks. */
def burnTime(using fc: FluidContext): Assigner[Int] =
  Assigner(fc.setBurnTime)

/** Sets the material-level burn time on the current material. */
def burnTime(value: Int)(using mc: MaterialContext): Unit =
  mc.materialBurnTime(value)

/** Marks the current fluid as having a fluid block. */
def block(using fc: FluidContext): Unit = fc.setBlock()

/** Marks the current fluid as having no bucket. */
def disableBucket(using fc: FluidContext): Unit = fc.setDisableBucket()

/** Disables authoritative color on the current fluid. */
def disableColor(using fc: FluidContext): Unit = fc.setDisableColor()

/** Enables a custom still texture and disables authoritative color.
  *
  * Equivalent to `textures(customStill = true, customFlowing = false)`.
  */
def customStill(using fc: FluidContext): Unit =
  fc.setTextures(FluidTextures(customStill = true))

/** Sets custom still/flowing texture flags and disables authoritative color.
  *
  * Named arguments are the intended call shape:
  * `textures(customStill = false, customFlowing = true)`.
  */
def textures(customStill: Boolean, customFlowing: Boolean)(using
    fc: FluidContext
): Unit =
  fc.setTextures(
    FluidTextures(customStill = customStill, customFlowing = customFlowing)
  )

/** Appends fluid attributes by varargs to the current fluid. */
def attributes(attrs: FluidAttribute*)(using fc: FluidContext): Unit =
  fc.addAttributes(attrs)

/** Appends fluid attributes from any Scala collection to the current fluid. */
def attributes(attrs: Iterable[FluidAttribute])(using fc: FluidContext): Unit =
  fc.addAttributes(attrs)

/** Appends a single fluid attribute to the current fluid. */
def attributes(attr: FluidAttribute)(using fc: FluidContext): Unit =
  fc.addAttributes(Seq(attr))

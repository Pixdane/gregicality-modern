package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.data.chemical.material.Material
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlag
import net.minecraft.resources.ResourceLocation

/** Top-level material authoring DSL.
  *
  * Usage:
  * {{{
  * given RegistryContext = RegistryContext("gregicality")
  * material("hyperion"):
  *   langValue("Hyperion Alloy")
  *   formula("C16H12N2O4")
  *   ingot(4)
  *   flags(GENERATE_PLATE, GENERATE_ROD)
  *   components(Tungsten * 2, Hydrogen * 4)
  *   ore:
  *     settings(multiplier = 2, byproduct = 3, emissive = true)
  *     washedIn(SulfuricAcid, 250)
  * }}}
  *
  * A [[RegistryContext]] must be in `given` scope. `material` builds a
  * namespaced `ResourceLocation` from the context's namespace and `path`, asks
  * the context's factory for a [[MaterialBuilderAdapter]], and runs `body` with
  * a [[MaterialContext]] in `given` scope. If `body` returns normally, the
  * material is finalized and registered exactly once; if `body` throws, no
  * finalization happens and the exception propagates.
  *
  * The DSL methods are top-level package definitions so that authors in the
  * `dsl` package (and tests in the same package) can call `material`,
  * `langValue`, `flags`, etc. without an explicit import.
  */

/** Authoring entry point for one material.
  *
  * @param path
  *   the material's path segment; combined with the current [[RegistryContext]]
  *   namespace to form the `ResourceLocation`
  * @param body
  *   the authoring block, run with a [[MaterialContext]] in `given` scope
  * @return
  *   the registered [[Material]]
  */
def material(path: String)(body: MaterialContext ?=> Unit)(using
    ctx: RegistryContext
): Material =
  val id = new ResourceLocation(ctx.namespace, path)
  val adapter = ctx.factory.create(id)
  val materialCtx = new MaterialContext(adapter)
  given MaterialContext = materialCtx
  body(using materialCtx)
  materialCtx.finalizeRegistration()

/** Sets the localized display name of the current material. */
def langValue(name: String)(using mc: MaterialContext): Unit =
  mc.langValue(name)

/** Sets the chemical formula string of the current material. */
def formula(f: String)(using mc: MaterialContext): Unit = mc.formula(f)

/** Marks the current material as an ingot with the given harvest level. */
def ingot(level: Int)(using mc: MaterialContext): Unit = mc.ingot(level)

/** Applies color, icon set, and optional secondary color to the current
  * material.
  */
def visual(spec: VisualSpec)(using mc: MaterialContext): Unit = mc.visual(spec)

/** Adds flags by varargs to the current material. */
def flags(fs: MaterialFlag*)(using mc: MaterialContext): Unit = mc.flags(fs*)

/** Adds flags from any Scala collection to the current material. */
def flags(fs: Iterable[MaterialFlag])(using mc: MaterialContext): Unit =
  mc.flags(fs)

/** Adds a Java-collection preset plus extra varargs flags to the current
  * material.
  */
def flags(preset: java.util.Collection[MaterialFlag], extras: MaterialFlag*)(
    using mc: MaterialContext
): Unit = mc.flags(preset, extras*)

/** Sets the composition of the current material from typed material/amount
  * pairs by varargs.
  */
def components(amounts: MaterialAmount*)(using mc: MaterialContext): Unit =
  mc.components(amounts*)

/** Sets the composition of the current material from any Scala collection of
  * amounts.
  */
def components(amounts: Iterable[MaterialAmount])(using
    mc: MaterialContext
): Unit =
  mc.components(amounts)

/** Opens an ore configuration block on the current material. */
def ore(body: OreContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.ore(body)

/** Opens a fluid configuration block for an explicit standard storage kind.
  *
  * This form covers full liquid, gas, plasma, and molten configuration without
  * colliding with compact direct calls such as `liquid(2800.K)`.
  */
def fluid(kind: FluidKind)(body: FluidContext ?=> Unit)(using
    mc: MaterialContext
): Unit =
  mc.configureFluid(kind, body)

/** Adds a liquid with inferred GTCEu settings. */
def liquid()(using mc: MaterialContext): Unit = mc.liquid()

/** Adds a liquid with an explicit temperature. */
def liquid(temperature: Kelvin)(using mc: MaterialContext): Unit =
  mc.liquid(temperature)

/** Opens a gas configuration block. */
def gas(body: FluidContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.configureFluid(FluidKind.Gas, body)

/** Opens a plasma configuration block. */
def plasma(body: FluidContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.configureFluid(FluidKind.Plasma, body)

/** Opens a blast-property configuration block. */
def blast(body: BlastContext ?=> Unit)(using mc: MaterialContext): Unit =
  mc.blast(body = body)

/** Sets ore multipliers and emissive flag. Last call wins. */
def settings(
    multiplier: Int = 1,
    byproduct: Int = 1,
    emissive: Boolean = false
)(using
    oc: OreContext
): Unit = oc.settings(multiplier, byproduct, emissive)

/** Sets the washing fluid and amount for the current ore block. Last call wins.
  */
def washedIn(fluid: Material, amount: Int = 100)(using oc: OreContext): Unit =
  oc.washedIn(fluid, amount)

/** Appends materials to the separatedInto list of the current ore block. */
def separatedInto(ms: Material*)(using oc: OreContext): Unit =
  oc.separatedInto(ms*)

/** Appends materials to the byproducts list of the current ore block. */
def byproducts(ms: Material*)(using oc: OreContext): Unit = oc.byproducts(ms*)

package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.item.tool.GTToolType
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.enchantment.Enchantment

import java.util.function.Supplier

/** Capability shared by sections that expose `enchantability := Int`.
  *
  * Both [[ToolContext]] and [[ArmorContext]] accept an enchantability value,
  * and the DSL wants a single top-level `enchantability` symbol rather than two
  * context-specific overloads that could create resolution ambiguity. By having
  * both contexts implement this trait, one `def enchantability` (with a
  * `using EnchantabilityTarget` constraint) serves both sections.
  */
private[dsl] trait EnchantabilityTarget:
  /** Records the enchantability value. Last call wins. */
  def setEnchantability(value: Int): Unit

/** Capability shared by sections that expose the bare `unbreakable` command.
  *
  * Tool and armor contexts both accept `unbreakable`; a single top-level
  * `def unbreakable` (with a `using UnbreakableTarget` constraint) serves both
  * without creating two context-specific overloads.
  */
private[dsl] trait UnbreakableTarget:
  /** Marks the section as unbreakable. Idempotent. */
  def setUnbreakable(): Unit

/** Accumulator for a `tool(...)` block.
  *
  * Created package-private by [[MaterialContexts.MaterialContext#tool]] and
  * placed in `given` scope for the block body. The four constructor parameters
  * are mandatory because GTCEu's `ToolProperty.Builder.of(float, float, int,
  * int)` has no setters for them. Inside the body, the top-level `types`,
  * `enchantability`, `enchantment`, `attackSpeed`, `durabilityMultiplier`,
  * `magnetic`, `unbreakable`, and `ignoreCraftingTools` symbols write to this
  * accumulator; on block exit, [[toSpec]] produces the immutable [[ToolSpec]]
  * that the adapter receives as one `tool` call.
  *
  * `types :=` replaces the base `types` list and clears any `additionalTypes`
  * accumulated before that call, so a later `types +=` starts fresh. This
  * matches the contract that one `types :=` resets the append-buffer.
  * `types +=` always appends to `additionalTypes`.
  *
  * Scalar fields take the last value written, matching [[BlastContext]]'s
  * scalar semantics. The bare-command booleans flip to `true` and stay there.
  */
private[dsl] final class ToolContext(
    val speed: Double,
    val damage: Double,
    val durability: Int,
    val level: Int
) extends EnchantabilityTarget
    with UnbreakableTarget:

  private var types: Option[List[GTToolType]] = None
  private val additionalTypes: scala.collection.mutable.ListBuffer[GTToolType] =
    scala.collection.mutable.ListBuffer.empty
  private val enchantments
      : scala.collection.mutable.ListBuffer[(Enchantment, Int)] =
    scala.collection.mutable.ListBuffer.empty
  private var enchantability: Option[Int] = None
  private var attackSpeed: Option[Double] = None
  private var durabilityMultiplier: Option[Int] = None
  private var magnetic: Boolean = false
  private var unbreakable: Boolean = false
  private var ignoreCraftingTools: Boolean = false

  /** Replaces the base types list and clears any previously appended types.
    * Last call wins for the base list.
    */
  private[dsl] def setTypes(ts: Iterable[GTToolType]): Unit =
    types = Some(ts.toList)
    additionalTypes.clear()

  /** Appends a tool type to the additional-types buffer. */
  private[dsl] def addType(t: GTToolType): Unit =
    additionalTypes += t

  /** Records the enchantability value. Last call wins. */
  def setEnchantability(value: Int): Unit =
    enchantability = Some(value)

  /** Appends a default tool enchantment in authoring order. */
  private[dsl] def addEnchantment(
      enchantment: Enchantment,
      level: Int
  ): Unit = enchantments += ((enchantment, level))

  /** Records the attack speed. Last call wins. */
  private[dsl] def setAttackSpeed(value: Double): Unit =
    attackSpeed = Some(value)

  /** Records the durability multiplier. Last call wins. */
  private[dsl] def setDurabilityMultiplier(value: Int): Unit =
    durabilityMultiplier = Some(value)

  /** Marks the tool as magnetic. Idempotent. */
  private[dsl] def setMagnetic(): Unit = magnetic = true

  /** Marks the tool as unbreakable. Idempotent. */
  def setUnbreakable(): Unit = unbreakable = true

  /** Marks the tool as ignoring crafting tools. Idempotent. */
  private[dsl] def setIgnoreCraftingTools(): Unit = ignoreCraftingTools = true

  /** Freezes the accumulated settings into an immutable [[ToolSpec]]. */
  private[dsl] def toSpec: ToolSpec =
    ToolSpec(
      speed = speed,
      damage = damage,
      durability = durability,
      level = level,
      types = types,
      additionalTypes = additionalTypes.toList,
      enchantability = enchantability,
      enchantments = enchantments.toList,
      attackSpeed = attackSpeed,
      durabilityMultiplier = durabilityMultiplier,
      magnetic = magnetic,
      unbreakable = unbreakable,
      ignoreCraftingTools = ignoreCraftingTools
    )

/** Accumulator for an `armor(...)` block.
  *
  * Created package-private by [[MaterialContexts.MaterialContext#armor]] and
  * placed in `given` scope for the block body. The `durability` and
  * `protection` parameters are mandatory because GTCEu's
  * `ArmorProperty.Builder.of(int, int[])` requires them at construction. Inside
  * the body, the top-level `toughness`, `knockbackResistance`,
  * `enchantability`, `repairIngredient`, `noRepair`, `dyeable`, and
  * `unbreakable` symbols write to this accumulator; on block exit, [[toSpec]]
  * produces the immutable [[ArmorSpec]] that the adapter receives as one
  * `armor` call.
  *
  * Scalar fields take the last value written. The bare-command booleans flip to
  * `true` and stay there.
  */
private[dsl] final class ArmorContext(
    val durability: Int,
    val protection: Armor
) extends EnchantabilityTarget
    with UnbreakableTarget:

  private var toughness: Option[Double] = None
  private var knockbackResistance: Option[Double] = None
  private var enchantability: Option[Int] = None
  private var repairIngredient: Option[Supplier[Ingredient]] = None
  private var noRepair: Boolean = false
  private var dyeable: Boolean = false
  private var unbreakable: Boolean = false

  /** Records the armor toughness. Last call wins. */
  private[dsl] def setToughness(value: Double): Unit =
    toughness = Some(value)

  /** Records the knockback resistance. Last call wins. */
  private[dsl] def setKnockbackResistance(value: Double): Unit =
    knockbackResistance = Some(value)

  /** Records the enchantability value. Last call wins. */
  def setEnchantability(value: Int): Unit =
    enchantability = Some(value)

  /** Sets a custom repair ingredient and clears an earlier no-repair choice. */
  private[dsl] def setRepairIngredient(
      value: Supplier[Ingredient]
  ): Unit =
    repairIngredient = Some(value)
    noRepair = false

  /** Disables armor repair and clears an earlier custom ingredient. */
  private[dsl] def setNoRepair(): Unit =
    repairIngredient = None
    noRepair = true

  /** Marks the armor as dyeable. Idempotent. */
  private[dsl] def setDyeable(): Unit = dyeable = true

  /** Marks the armor as unbreakable. Idempotent. */
  def setUnbreakable(): Unit = unbreakable = true

  /** Freezes the accumulated settings into an immutable [[ArmorSpec]]. */
  private[dsl] def toSpec: ArmorSpec =
    ArmorSpec(
      durability = durability,
      protection = protection,
      toughness = toughness,
      knockbackResistance = knockbackResistance,
      enchantability = enchantability,
      repairIngredient = repairIngredient,
      noRepair = noRepair,
      dyeable = dyeable,
      unbreakable = unbreakable
    )

/** Carrier for the `types :=` / `types +=` tool-type slot.
  *
  * Bound to a [[ToolContext]] so that `types := Iterable[GTToolType]` routes to
  * [[ToolContext.setTypes]] (which also clears the append buffer) and
  * `types += GTToolType` routes to [[ToolContext.addType]]. Both operations
  * mutate the bound context directly.
  */
private[dsl] final class ToolTypesAssigner(private val ctx: ToolContext):

  /** Replaces the base types list and clears any previously appended types. */
  infix def :=(ts: Iterable[GTToolType]): Unit = ctx.setTypes(ts)

  /** Appends a single tool type to the additional-types buffer. */
  def +=(t: GTToolType): Unit = ctx.addType(t)

// Top-level equipment-section DSL. These definitions live at package scope so
// authors (and tests in the same package) can call `types := ...`,
// `types += ...`, `enchantability := ...`, `enchantment(...)`, `attackSpeed := ...`,
// `durabilityMultiplier := ...`, `toughness := ...`, `knockbackResistance :=`,
// and the bare `magnetic`, `unbreakable`, `ignoreCraftingTools`, `noRepair`, and
// `dyeable` commands inside any tool or armor section without an explicit
// import. The matching context must be in `given` scope; the outer material
// entry point supplies it.

/** Returns the assigner for the current tool section's types.
  *
  * `types := List(PICKAXE, SHOVEL)` replaces the base tool-type list and clears
  * any `types +=` appends made so far. `types += AXE` appends a single type to
  * the additional buffer.
  */
def types(using tc: ToolContext): ToolTypesAssigner =
  ToolTypesAssigner(tc)

/** Returns the assigner for the current section's enchantability.
  *
  * Works inside both `tool(...)` and `armor(...)` blocks because both contexts
  * implement [[EnchantabilityTarget]]. A single definition avoids two
  * context-specific overloads that could create resolution ambiguity.
  */
def enchantability(using target: EnchantabilityTarget): Assigner[Int] =
  Assigner(target.setEnchantability)

/** Adds a default enchantment to tools in the current tool section. */
def enchantment(enchantment: Enchantment, level: Int)(using
    tc: ToolContext
): Unit = tc.addEnchantment(enchantment, level)

/** Returns the assigner for the current tool section's attack speed. */
def attackSpeed(using tc: ToolContext): Assigner[Double] =
  Assigner(tc.setAttackSpeed)

/** Returns the assigner for the current tool section's durability multiplier.
  */
def durabilityMultiplier(using tc: ToolContext): Assigner[Int] =
  Assigner(tc.setDurabilityMultiplier)

/** Marks the current tool section as magnetic. */
def magnetic(using tc: ToolContext): Unit = tc.setMagnetic()

/** Marks the current tool or armor section as unbreakable.
  *
  * Works inside both `tool(...)` and `armor(...)` blocks because both contexts
  * implement [[UnbreakableTarget]]. A single definition avoids two
  * context-specific overloads that could create resolution ambiguity.
  */
def unbreakable(using target: UnbreakableTarget): Unit = target.setUnbreakable()

/** Marks the current tool section as ignoring crafting tools. */
def ignoreCraftingTools(using tc: ToolContext): Unit =
  tc.setIgnoreCraftingTools()

/** Returns the assigner for the current armor section's toughness. */
def toughness(using ac: ArmorContext): Assigner[Double] =
  Assigner(ac.setToughness)

/** Returns the assigner for the current armor section's knockback resistance.
  */
def knockbackResistance(using ac: ArmorContext): Assigner[Double] =
  Assigner(ac.setKnockbackResistance)

/** Sets the ingredient used to repair armor in an anvil. */
def repairIngredient(ingredient: Supplier[Ingredient])(using
    ac: ArmorContext
): Unit = ac.setRepairIngredient(ingredient)

/** Disables repair for armor made from the current material. */
def noRepair(using ac: ArmorContext): Unit = ac.setNoRepair()

/** Marks the current armor section as dyeable. */
def dyeable(using ac: ArmorContext): Unit = ac.setDyeable()

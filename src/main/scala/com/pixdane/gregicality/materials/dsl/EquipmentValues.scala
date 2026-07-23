package com.pixdane.gregicality.materials.dsl

import com.gregtechceu.gtceu.api.item.tool.GTToolType
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.enchantment.Enchantment

import java.util.function.Supplier

/** Four-slot armor protection values, ordered helmet, chestplate, leggings,
  * boots.
  *
  * GTCEu's `ArmorProperty.Builder.of(int, int[])` requires exactly four
  * protection values in this fixed order. Wrapping them in a small case class
  * lets the DSL author write `Armor(4, 8, 7, 4)` positionally while keeping the
  * slot names documented, and lets the adapter call [[toArray]] to produce the
  * `int[]` that GTCEu expects.
  *
  * The DSL does not replicate GTCEu's non-negativity or range checks on the
  * individual values; GTCEu remains authoritative for those at build time.
  *
  * @param helmet
  *   protection value for the helmet slot
  * @param chestplate
  *   protection value for the chestplate slot
  * @param leggings
  *   protection value for the leggings slot
  * @param boots
  *   protection value for the boots slot
  */
final case class Armor(
    helmet: Int,
    chestplate: Int,
    leggings: Int,
    boots: Int
):

  /** Returns the four values as an `Array[Int]` in helmet, chestplate,
    * leggings, boots order, matching `ArmorProperty.Builder.of(int, int[])`.
    */
  def toArray: Array[Int] = Array(helmet, chestplate, leggings, boots)

/** Immutable snapshot of one tool section's accumulated configuration.
  *
  * Produced by the package-private `ToolContext.toSpec` method at the end of a
  * `tool(...)` block and forwarded to the material adapter as a single `tool`
  * call. The four constructor parameters (`speed`, `damage`, `durability`,
  * `level`) are mandatory because GTCEu's
  * `ToolProperty.Builder.of(float, float, int, int)` has no setters for them;
  * every other field is optional.
  *
  * `types` and `additionalTypes` model the replace-then-append semantics of the
  * `types :=` / `types +=` DSL pair. A `types := List(...)` call replaces the
  * base `types` list and clears any `additionalTypes` accumulated so far; a
  * subsequent `types += T` appends to `additionalTypes`. The adapter maps
  * `types` to `ToolProperty.Builder.types(...)` (full replace) and
  * `additionalTypes` to `addTypes(...)` (append), matching GTCEu's two builder
  * methods.
  *
  * The bare-command fields (`magnetic`, `unbreakable`, `ignoreCraftingTools`)
  * are booleans that flip to `true` when the matching command is issued; they
  * are not toggles and cannot be reset to `false` from the DSL.
  *
  * No GTCEu range validation is performed here. In particular, the DSL does not
  * check harvest-level or durability ranges, nor the interaction between
  * `unbreakable` and `durabilityMultiplier` (GTCEu's `unbreakable()` only
  * affects `ToolProperty.isUnbreakable`, distinct from the armor-side
  * behavior).
  *
  * @param speed
  *   the tool harvest speed, narrowed to `float` by the adapter
  * @param damage
  *   the tool attack damage, narrowed to `float` by the adapter
  * @param durability
  *   the base tool durability
  * @param level
  *   the harvest level
  * @param types
  *   the replacement tool-type list from the last `types :=`, if any
  * @param additionalTypes
  *   tool types appended via `types +=` after the last `types :=`
  * @param enchantability
  *   the enchantability value, if set
  * @param enchantments
  *   default tool enchantments in authoring order
  * @param attackSpeed
  *   the attack speed, if set; narrowed to `float` by the adapter
  * @param durabilityMultiplier
  *   the durability multiplier, if set
  * @param magnetic
  *   whether the bare `magnetic` command was issued
  * @param unbreakable
  *   whether the bare `unbreakable` command was issued
  * @param ignoreCraftingTools
  *   whether the bare `ignoreCraftingTools` command was issued
  */
final case class ToolSpec(
    speed: Double,
    damage: Double,
    durability: Int,
    level: Int,
    types: Option[List[GTToolType]] = None,
    additionalTypes: List[GTToolType] = Nil,
    enchantability: Option[Int] = None,
    enchantments: List[(Enchantment, Int)] = Nil,
    attackSpeed: Option[Double] = None,
    durabilityMultiplier: Option[Int] = None,
    magnetic: Boolean = false,
    unbreakable: Boolean = false,
    ignoreCraftingTools: Boolean = false
)

/** Immutable snapshot of one armor section's accumulated configuration.
  *
  * Produced by the package-private `ArmorContext.toSpec` method at the end of
  * an `armor(...)` block and forwarded to the material adapter as a single
  * `armor` call. The `durability` and `protection` parameters are mandatory
  * because GTCEu's `ArmorProperty.Builder.of(int, int[])` requires them at
  * construction; every other field is optional.
  *
  * The bare-command fields (`dyeable`, `unbreakable`) are booleans that flip to
  * `true` when the matching command is issued. Note that GTCEu's
  * `ArmorProperty.Builder.unbreakable()` sets `durabilityMultiplier = 0` rather
  * than an independent flag; the adapter forwards the author's intent and lets
  * GTCEu apply that semantics. The DSL does not guard against an author issuing
  * both `unbreakable` and a durability multiplier elsewhere.
  *
  * No GTCEu range validation is performed here.
  *
  * @param durability
  *   the armor durability multiplier passed to `ArmorProperty.Builder.of`
  * @param protection
  *   the four-slot protection values, helmet through boots
  * @param toughness
  *   the armor toughness, if set; narrowed to `float` by the adapter
  * @param knockbackResistance
  *   the knockback resistance, if set; narrowed to `float` by the adapter
  * @param enchantability
  *   the enchantability value, if set
  * @param repairIngredient
  *   an explicit armor repair ingredient supplier, if set
  * @param noRepair
  *   whether the armor explicitly disables repair
  * @param dyeable
  *   whether the bare `dyeable` command was issued
  * @param unbreakable
  *   whether the bare `unbreakable` command was issued
  */
final case class ArmorSpec(
    durability: Int,
    protection: Armor,
    toughness: Option[Double] = None,
    knockbackResistance: Option[Double] = None,
    enchantability: Option[Int] = None,
    repairIngredient: Option[Supplier[Ingredient]] = None,
    noRepair: Boolean = false,
    dyeable: Boolean = false,
    unbreakable: Boolean = false
)

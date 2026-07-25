package com.pixdane.gregicality.dsl.api

import com.tterrag.registrate.providers.RegistrateLangProvider

import scala.collection.mutable.ArrayBuffer

/** DSL for collecting English lang entries during datagen.
  *
  * Mirrors the `tooltips`/`ComponentDsl` pattern: a block scopes an accumulator,
  * `en` appends `(key, value)` pairs, and the block is flushed onto the
  * `RegistrateLangProvider` at the end.
  *
  * {{{
  * translations:
  *   en("gregicality.machine.void_miner.tooltip.0", "The Void Ore Miner will produce tons of ores.")
  * }}}
  */
object LangDsl:

  /** Append an English lang entry. */
  def en(key: String, value: String)(using
      entries: ArrayBuffer[(String, String)]
  ): Unit =
    entries += (key -> value)

  /** Append a numbered tooltip entry under `gregicality.machine.<name>.tooltip.<n>`,
    * numbered `.0` through `.<count-1>` (CEU convention).
    */
  def tooltip(machine: String, index: Int, value: String)(using
      entries: ArrayBuffer[(String, String)]
  ): Unit =
    entries += (s"gregicality.machine.$machine.tooltip.$index" -> value)

  /** Append multiple tooltip entries under `gregicality.machine.<name>.tooltip`,
    * numbered `.0` through `.<lines.length-1>`. Mirrors CEU's `multiLang`.
    */
  def machineTooltip(machine: String, lines: String*)(using
      entries: ArrayBuffer[(String, String)]
  ): Unit =
    multiLang(s"gregicality.machine.$machine.tooltip", lines*)

  /** Append multiple lang entries under `baseKey`, numbered `.0` through
    * `.<lines.length-1>`. General form of CEU's `multiLang`; pass an explicit
    * base key (e.g. `"gregicality.machine.void_miner.tooltip"`).
    */
  def multiLang(baseKey: String, lines: String*)(using
      entries: ArrayBuffer[(String, String)]
  ): Unit =
    lines.zipWithIndex.foreach { (line, i) =>
      entries += (s"$baseKey.$i" -> line)
    }

  /** Collect lang entries in a block and flush them onto the provider. */
  def translations(child: ArrayBuffer[(String, String)] ?=> Unit)(using
      provider: RegistrateLangProvider
  ): Unit =
    given entries: ArrayBuffer[(String, String)] = ArrayBuffer()
    child
    entries.foreach { (k, v) => provider.add(k, v) }
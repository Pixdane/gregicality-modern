package com.pixdane.gregicality.dsl.api

import net.minecraft.network.chat.Component

import scala.collection.mutable.ArrayBuffer

object ComponentDsl:

  def literal(text: String)(using components: ArrayBuffer[Component]): Unit =
    components += Component.literal(text)

  def translatable(key: String, args: Object*)(using
      components: ArrayBuffer[Component]
  ): Unit =
    components += Component.translatable(key, args)

  /** Auto-generate `count` translatable components from a base key, numbered
    * `.0` through `.<count-1>`. Mirrors CEU's `multiLang` key scheme.
    */
  def multiTranslatable(baseKey: String, count: Int)(using
      components: ArrayBuffer[Component]
  ): Unit =
    (0 until count).foreach { i =>
      components += Component.translatable(s"$baseKey.$i")
    }

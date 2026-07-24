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

package com.pixdane.gregicality.symbolgen.backends

import com.pixdane.gregicality.symbolgen.backends.gtceu.GtceuBackend
import com.pixdane.gregicality.symbolgen.framework.{Diagnostic, SymbolGenerator}

/** Registry of the symbol generators available to the `GenerateRefs` entry
  * point. The entry point looks up a generator by `kind` (the `--kind`
  * argument) and runs it against the source archive.
  */
object SymbolGenerators:
  val all: Vector[SymbolGenerator[? <: Diagnostic]] =
    Vector(GtceuBackend.generator)

  def find(kind: String): Option[SymbolGenerator[? <: Diagnostic]] =
    all.find(_.kind == kind)

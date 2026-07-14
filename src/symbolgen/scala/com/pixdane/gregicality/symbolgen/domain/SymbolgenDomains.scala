package com.pixdane.gregicality.symbolgen.domain

import com.pixdane.gregicality.symbolgen.gtceu.GtceuBackend

object SymbolgenDomains:
  val all: Vector[SymbolgenDomain[? <: Diagnostic]] =
    Vector(GtceuBackend.domain)

  def find(kind: String): Option[SymbolgenDomain[? <: Diagnostic]] =
    all.find(_.kind == kind)

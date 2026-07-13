package com.pixdane.gregicality.symbolgen.domain

import com.pixdane.gregicality.symbolgen.gtceu.GtceuPipelines

object SymbolgenDomains:
  val all: Vector[SymbolgenDomain] =
    Vector(GtceuPipelines.domain)

  def find(kind: String): Option[SymbolgenDomain] =
    all.find(_.kind == kind)

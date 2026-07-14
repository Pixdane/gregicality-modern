package com.pixdane.gregicality.symbolgen.domain

import cats.data.IorNec

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.render.GeneratedScalaFile

final case class SymbolgenDomain[D <: Diagnostic](
    kind: String,
    generate: SourceArchive => IorNec[D, Vector[GeneratedScalaFile]]
)

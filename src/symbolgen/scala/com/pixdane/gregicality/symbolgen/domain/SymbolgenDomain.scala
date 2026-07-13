package com.pixdane.gregicality.symbolgen.domain

import cats.data.IorNec

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.render.GeneratedScalaFile

final case class SymbolgenDomain(
    kind: String,
    generate: SourceArchive => IorNec[String, Vector[GeneratedScalaFile]]
)

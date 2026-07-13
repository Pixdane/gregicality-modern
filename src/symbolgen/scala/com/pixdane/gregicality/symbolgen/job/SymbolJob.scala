package com.pixdane.gregicality.symbolgen.job

import cats.data.IorNec

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.render.{
  GeneratedScalaFile,
  RefObjectTarget
}

final case class SymbolJob[E, A, B](
    id: String,
    target: RefObjectTarget,
    scan: SourceArchive => IorNec[E, A],
    preprocess: A => IorNec[E, B],
    render: (RefObjectTarget, B) => GeneratedScalaFile
):
  def run(archive: SourceArchive): IorNec[E, GeneratedScalaFile] =
    scan(archive)
      .flatMap(preprocess)
      .map(value => render(target, value))

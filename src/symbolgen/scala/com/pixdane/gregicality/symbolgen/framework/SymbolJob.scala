package com.pixdane.gregicality.symbolgen.framework

import cats.data.IorNec
import com.pixdane.gregicality.generator.GeneratedScalaFile

/** A single backend pipeline stage composed of scan, preprocess, and render.
  *
  * `SymbolJob` is the unit of work inside a backend: each job scans a slice of
  * the archive into an intermediate value `A`, preprocesses it (validation,
  * deduplication, ordering) into `B`, and renders `B` against its
  * [[RefOutputSpec]] into a single [[GeneratedScalaFile]]. The `IorNec` result
  * lets a job emit diagnostics while still producing output.
  */
final case class SymbolJob[E, A, B](
    id: String,
    target: RefOutputSpec,
    scan: SourceArchive => IorNec[E, A],
    preprocess: A => IorNec[E, B],
    render: (RefOutputSpec, B) => GeneratedScalaFile
):
  def run(archive: SourceArchive): IorNec[E, GeneratedScalaFile] =
    scan(archive)
      .flatMap(preprocess)
      .map(value => render(target, value))

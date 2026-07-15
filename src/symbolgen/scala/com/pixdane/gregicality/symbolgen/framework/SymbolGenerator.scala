package com.pixdane.gregicality.symbolgen.framework

import cats.data.IorNec

/** A renderable, user-facing problem surfaced from a backend's scan or
  * preprocessing stage. Implementations describe the kind of symbol pipeline
  * they belong to (for example GTCEu) and how the problem should be reported to
  * the operator running `GenerateRefs`.
  */
trait Diagnostic:
  def render: String

/** A named symbol generation pipeline.
  *
  * A `SymbolGenerator` is identified by its `kind` (the value matched against
  * the `--kind` CLI argument) and produces a vector of generated Scala files
  * from a `SourceArchive`. The `generate` function returns `IorNec` so that
  * diagnostics and successful output can be returned together, letting the
  * entry point decide whether partial output is acceptable.
  *
  * Concrete generators are registered in
  * [[com.pixdane.gregicality.symbolgen.backends.SymbolGenerators]].
  */
final case class SymbolGenerator[D <: Diagnostic](
    kind: String,
    generate: SourceArchive => IorNec[D, Vector[GeneratedScalaFile]]
)

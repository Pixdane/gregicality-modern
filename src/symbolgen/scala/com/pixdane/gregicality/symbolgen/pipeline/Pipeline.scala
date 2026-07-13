package com.pixdane.gregicality.symbolgen.pipeline

import cats.data.IorNec

final case class Pipeline[I, E, O](id: String, run: I => IorNec[E, O]):
  def map[P](f: O => P): Pipeline[I, E, P] =
    Pipeline(id, in => run(in).map(f))

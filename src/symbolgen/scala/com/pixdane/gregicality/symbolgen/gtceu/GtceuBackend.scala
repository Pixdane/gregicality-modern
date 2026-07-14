package com.pixdane.gregicality.symbolgen.gtceu

import cats.data.IorNec
import cats.implicits.*

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.domain.SymbolgenDomain
import com.pixdane.gregicality.symbolgen.gtceu.scan.GtceuScanDiagnostic
import com.pixdane.gregicality.symbolgen.render.{
  GeneratedScalaFile,
  RefAggregateRenderer
}

object GtceuBackend:
  private val AggregateObject = "GTRefs"

  private def aggregateFile: GeneratedScalaFile =
    RefAggregateRenderer.generateFile(
      outputPackage = GtceuRefJobs.OutputPackage,
      outputObject = AggregateObject,
      exports = GtceuRefJobs.jobs.map(_.target.outputObject)
    )

  def generate(
      archive: SourceArchive
  ): IorNec[GtceuScanDiagnostic, Vector[GeneratedScalaFile]] =
    GtceuRefJobs.jobs
      .traverse(_.run(archive))
      .map(refs => refs :+ aggregateFile)

  val domain: SymbolgenDomain[GtceuScanDiagnostic] =
    SymbolgenDomain(
      kind = "gtceu",
      generate = generate
    )

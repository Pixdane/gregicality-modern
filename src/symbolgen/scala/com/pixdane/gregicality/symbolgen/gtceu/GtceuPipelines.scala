package com.pixdane.gregicality.symbolgen.gtceu

import cats.data.IorNec
import cats.implicits.*

import com.pixdane.gregicality.symbolgen.archive.SourceArchive
import com.pixdane.gregicality.symbolgen.domain.SymbolgenDomain
import com.pixdane.gregicality.symbolgen.gtceu.scan.GtceuScanDiagnostic
import com.pixdane.gregicality.symbolgen.pipeline.Pipeline
import com.pixdane.gregicality.symbolgen.render.{
  GeneratedScalaFile,
  RefAggregateRenderer
}

object GtceuPipelines:
  private val OutputPackage =
    "com.pixdane.gregicality.core.refs.gtceu"

  private val AggregateObject = "GTRefs"

  val pipelines: Vector[
    Pipeline[SourceArchive, GtceuScanDiagnostic, GeneratedScalaFile]
  ] =
    GtceuRefJobs.jobs.map { job =>
      Pipeline(
        id = job.id,
        run = archive =>
          GtceuRefScanner
            .scan(job, archive)
            .map(GtceuRefProcessor.render)
      )
    }

  private def aggregateFile: GeneratedScalaFile =
    RefAggregateRenderer.generateFile(
      outputPackage = OutputPackage,
      outputObject = AggregateObject,
      exports = GtceuRefJobs.jobs.map(_.target.outputObject)
    )

  def generate(
      archive: SourceArchive
  ): IorNec[GtceuScanDiagnostic, Vector[GeneratedScalaFile]] =
    pipelines
      .traverse(_.run(archive))
      .map(refs => refs :+ aggregateFile)

  val domain: SymbolgenDomain =
    SymbolgenDomain(
      kind = "gtceu",
      generate = archive => generate(archive).leftMap(_.map(_.render))
    )

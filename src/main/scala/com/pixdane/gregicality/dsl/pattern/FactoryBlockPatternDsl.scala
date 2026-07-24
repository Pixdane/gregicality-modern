package com.pixdane.gregicality.dsl.pattern

import com.gregtechceu.gtceu.api.pattern.{
  FactoryBlockPattern,
  TraceabilityPredicate
}

object FactoryBlockPatternDsl:

  def aisle(aisle: String*)(using
      factory: FactoryBlockPattern
  ): Unit =
    factory.aisle(aisle*)

  def aisleRepeatable(
      minRepeat: Int,
      maxRepeat: Int,
      aisle: String*
  )(using
      factory: FactoryBlockPattern
  ): Unit =
    factory.aisleRepeatable(minRepeat, maxRepeat, aisle*)

  def setRepeatable(minRepeat: Int, maxRepeat: Int)(using
      factory: FactoryBlockPattern
  ): Unit =
    factory.setRepeatable(minRepeat, maxRepeat)

  def setRepeatable(repeatCount: Int)(using
      factory: FactoryBlockPattern
  ): Unit =
    factory.setRepeatable(repeatCount)

  trait WhereScope
  private object WhereScope extends WhereScope

  def where(proc: WhereScope ?=> Unit)(using
      factory: FactoryBlockPattern
  ): Unit =
    given WhereScope = WhereScope
    proc

  trait StructureMarker
  private object StructureMarker extends StructureMarker

  def structure(using factory: FactoryBlockPattern): StructureMarker =
    StructureMarker

  extension (structure: StructureMarker)
    def :=(aisels: String)(using factory: FactoryBlockPattern): Unit =
      val lines =
        aisels.stripMargin.linesIterator.filter(_.trim.nonEmpty).toList
      for line <- lines do
        val cells = line.split("\\|")
        factory.aisle(cells*)

  extension (symbol: Char)
    def :=(blockMatcher: TraceabilityPredicate)(using
        factory: FactoryBlockPattern,
        scope: WhereScope
    ): Unit =
      factory.where(symbol, blockMatcher)

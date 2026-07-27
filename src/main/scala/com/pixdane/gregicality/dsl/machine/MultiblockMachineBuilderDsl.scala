package com.pixdane.gregicality.dsl.machine

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition
import com.gregtechceu.gtceu.api.machine.feature.multiblock.{
  IMultiController,
  IMultiPart
}
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection
import com.gregtechceu.gtceu.api.pattern.{
  BlockPattern,
  FactoryBlockPattern,
  MultiblockShapeInfo
}
import com.gregtechceu.gtceu.api.registry.registrate.MultiblockMachineBuilder
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.state.BlockState

import java.util.Comparator
import java.util.List as JList
import java.util.regex.Pattern
import scala.jdk.FunctionConverters.*
import scala.jdk.CollectionConverters.*

object MultiblockMachineBuilderDsl:

  def multiblock(
      proc: MultiblockMachineBuilder[_, _] ?=> Unit
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): MultiblockMachineDefinition =
    proc
    builder.register()

  def generator(generator: Boolean)(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.generator(generator)

  def pattern(direction: String = """
        | +X<-o----> +Y
        |     |
        |    -Z
        """)(
      proc: (FactoryBlockPattern, MultiblockMachineDefinition) ?=> Unit
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    given factoryBlockPattern: FactoryBlockPattern = parseDirection(direction)

    def patternGenerator(using
        definition: MultiblockMachineDefinition
    ): BlockPattern =
      proc
      factoryBlockPattern.build

    builder.pattern(patternGenerator(using _))

  def allowFlip(allowFlip: Boolean)(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.allowFlip(allowFlip)

  def partSorter(
      partSorter: MultiblockControllerMachine => Comparator[IMultiPart]
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.partSorter(partSorter.asJava)

  def partAppearance(
      partAppearance: (IMultiController, IMultiPart, Direction) => BlockState
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.partAppearance(
      (controller: IMultiController, part: IMultiPart, side: Direction) =>
        partAppearance(controller, part, side)
    )

  def additionalDisplay(
      additionalDisplay: (IMultiController, JList[Component]) => Unit
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.additionalDisplay(additionalDisplay.asJava)

  def shapeInfo(
      shape: MultiblockMachineDefinition => MultiblockShapeInfo
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.shapeInfo(shape.asJava)

  def shapeInfos(
      shapes: MultiblockMachineDefinition => Seq[MultiblockShapeInfo]
  )(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.shapeInfos((d: MultiblockMachineDefinition) => shapes(d).asJava)

  def recoveryItems(items: () => Array[ItemLike])(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.recoveryItems(items.asJava)

  def recoveryStacks(stacks: () => Array[ItemStack])(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.recoveryStacks(stacks.asJava)

  def partSorter(sorter: Comparator[IMultiPart])(using
      builder: MultiblockMachineBuilder[_, _]
  ): Unit =
    builder.partSorter(sorter)

  private def parseDirection(
      raw: String
  ): FactoryBlockPattern =
    val lines = raw.stripMargin.linesIterator.filter(_.trim.nonEmpty).toList
    require(
      lines.length == 3,
      s"expected 3 orientation lines, found ${lines.size}"
    )
    val first = lines.head.trim
    val third = lines(2).trim
    val parts = first.split(Pattern.quote("<-o---->")).map(_.trim)
    require(
      parts.length == 2,
      s"expected '<-o---->' separator, found: $first"
    )

    val charDir = toDir(parts(0)).getOpposite
    val stringDir = toDir(parts(1))
    val aisleDir = toDir(third)

    FactoryBlockPattern.start(
      charDir,
      stringDir,
      aisleDir
    )

  private def toDir(signed: String): RelativeDirection =
    val s = signed.trim
    require(s.length >= 2, s"invalid signed axis: $signed")
    val sign = s(0)
    val axis = s.substring(1).trim.toUpperCase
    require(axis.length == 1, s"invalid signed axis: $signed")
    (sign, axis(0)) match
      case ('+', 'X') => RelativeDirection.RIGHT
      case ('-', 'X') => RelativeDirection.LEFT
      case ('+', 'Y') => RelativeDirection.UP
      case ('-', 'Y') => RelativeDirection.DOWN
      case ('+', 'Z') => RelativeDirection.BACK
      case ('-', 'Z') => RelativeDirection.FRONT
      case _          =>
        throw new IllegalArgumentException(s"invalid signed axis: $signed")

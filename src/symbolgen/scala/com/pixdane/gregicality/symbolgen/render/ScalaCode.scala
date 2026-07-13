package com.pixdane.gregicality.symbolgen.render

final case class ScalaCode(lines: Vector[String]):
  def ++(other: ScalaCode): ScalaCode =
    ScalaCode(lines ++ other.lines)

  def render: String =
    lines.mkString("\n") + "\n"

object ScalaCode:
  val empty: ScalaCode =
    ScalaCode(Vector.empty)

  def line(value: String): ScalaCode =
    ScalaCode(Vector(value))

  def lines(values: String*): ScalaCode =
    ScalaCode(values.toVector)

  def joinWith(separator: ScalaCode)(items: Vector[ScalaCode]): ScalaCode =
    val nonEmptyItems = items.filterNot(_.lines.isEmpty)

    if nonEmptyItems.isEmpty then empty
    else
      nonEmptyItems.tail.foldLeft(nonEmptyItems.head) { (acc, item) =>
        acc ++ separator ++ item
      }

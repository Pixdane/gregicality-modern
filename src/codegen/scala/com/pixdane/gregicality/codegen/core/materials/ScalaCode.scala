package com.pixdane.gregicality.codegen.core.materials

/** Small line-oriented value used to compose generated Scala source. */
final case class ScalaCode(lines: Vector[String]):

  /** Appends another source fragment. */
  def ++(other: ScalaCode): ScalaCode =
    ScalaCode(lines ++ other.lines)

  /** Renders the fragment with one final newline. */
  def render: String =
    lines.mkString("\n") + "\n"

object ScalaCode:
  val empty: ScalaCode = ScalaCode(Vector.empty)

  def line(value: String): ScalaCode = ScalaCode(Vector(value))

  def lines(values: String*): ScalaCode = ScalaCode(values.toVector)

  def joinWith(separator: ScalaCode)(items: Vector[ScalaCode]): ScalaCode =
    val nonEmpty = items.filterNot(_.lines.isEmpty)
    nonEmpty.headOption match
      case None       => empty
      case Some(head) =>
        nonEmpty.tail.foldLeft(head) { (acc, item) =>
          acc ++ separator ++ item
        }

package com.pixdane.gregicality.codegen.core.materials

/** Authored voltage expression. Named forms preserve whether generated source
  * should use GTValues.V or GTValues.VA instead of eagerly computing a number.
  */
enum VoltageExpr:
  case Tier(name: ScalaIdent)
  case VA(name: ScalaIdent)
  case Literal(value: Voltage)

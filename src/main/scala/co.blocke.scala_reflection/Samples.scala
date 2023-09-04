package co.blocke.scala_reflection

import scala.quoted.*

case class Person(name: String, age: Int, thing: Thing)
case class Thing( a: Int )

// object Liftables:

  // given ToExpr[Thing[_]] with {
  //   def apply(x: Thing[_])(using Quotes) =
  //     '{ Thing(${ Expr(x.a) }) }
  // }

  // given ToExpr[StringRType] with {
  //   def apply(x: StringRType)(using Quotes) =
  //     '{ StringRType() }
  // }

  // given ToExpr[IntRType] with {
  //   def apply(x: IntRType)(using Quotes) =
  //     '{ IntRType() }
  // }


package co.blocke.scala_reflection

import scala.quoted.*

trait RType[R]:
  type T = R
  val name: String
  val infoClass: Class[R]
  //def toType(quotes: Quotes): quotes.reflect.TypeRepr = quotes.reflect.TypeRepr.typeConstructorOf(infoClass)
  def toType(quotes: Quotes): quoted.Type[R] = quotes.reflect.TypeRepr.typeConstructorOf(infoClass).asType.asInstanceOf[Type[R]]


case class StringRType() extends RType[String]{ val name = "String"; val infoClass = classOf[String] }
case class IntRType() extends RType[Int]{ val name = "Int"; val infoClass = classOf[Int] }

case class ClassRType[C](
  name: String,
  fields: List[FieldInfo]
) extends RType[C]:
    val infoClass = Class.forName(name).asInstanceOf[Class[C]]

case class FieldInfo(
  name: String,
  fieldType: RType[_]
)

object RType:
    inline def of[T]: RType[T] = ${ ofImpl[T]() }

    def ofImpl[T]()(implicit qctx: Quotes, ttype: scala.quoted.Type[T]): Expr[RType[T]] =
      import qctx.reflect.*
      // This is hard-wired to return RType[Person].  In real life this is reflected.
      '{ ClassRType[Person]("co.blocke.scala_reflection.Person",
            List(FieldInfo("name",StringRType()), FieldInfo("age",IntRType()), FieldInfo("thing",ClassRType[Thing]("co.blocke.scala_reflection.Thing", List(FieldInfo("a",IntRType()),FieldInfo("b",StringRType()))).asInstanceOf[RType[Thing]]))
            ).asInstanceOf[RType[T]] }

package co.blocke.scala_reflection

import scala.quoted.*
import quoted.Quotes

import java.io.*
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.rtypes.*
import scala.jdk.CollectionConverters.*

/*
Theory of Operation:

Use scala_reflection to get RType, which holds type information, T.def 
For each type, generate a functioon (Any,StringBuilder) => StringBuilder
This function will accept a value and a StringBuilder, render the value into
the StringBuilder, and return the buidler.  Chaining these together achieves
nested rendering.  Top-level runtime call initates.
*/

object ZType:

  /** Default implementation of `ToExpr[BigInt]` */
  given StringBuilderToExpr: ToExpr[StringBuilder] with {
    def apply(x: StringBuilder)(using Quotes): Expr[StringBuilder] =
      '{ new StringBuilder( { ${Expr(x.toString)} } ) }
  }


  // given RTypeToExpr[T: Type: ToExpr]: ToExpr[RType[T]] with
  //   def apply(rt: RType[T])(using Quotes): Expr[RType[T]] = rt match 
  //     case r: RType[T] => Expr(r)

  // inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t, '{RType.of[T]}) }

  // def toJsonImpl[T:Type](t: Expr[T], r: Expr[RType[T]])(using quotes:Quotes): Expr[String] = {
  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t) }

  def toJsonImpl[T:Type](t: Expr[T])(using quotes:Quotes): Expr[String] = {
    import quotes.reflect.* 

    // Given some value, render the Json string
    def renderJsonFn[Z](rt: RType[Z])(using Type[Z]): Expr[(Any,StringBuilder) => StringBuilder] = {
      rt match {
        case _: StringRType => 
          '{(a:Any, sb:StringBuilder) =>
            sb.append('"')
            sb.append(a.toString)
            sb.append('"')
          }

        case classRType: ClassRType[Z] =>
          val zipped = classRType.fields.map{ f => 
            // Gymnastis to get Type of each field from class->TypeRepr->Type
            (renderJsonFn(f.fieldType)(using RType.getTypeFor(f.fieldType)), f) 
          }
          '{(a:Any, sb:StringBuilder) => 
            val typedA = a.asInstanceOf[Z]
            sb.append('{')
            val stuff = ${
              val statements = zipped.map{ case (fn, field) => 
                '{ 
                  sb.append(${Expr(field.name)})
                  sb.append(':')
                  val fieldValue = ${ 
                    Select.unique('{ typedA }.asTerm, field.name).asExpr
                  }
                  $fn(fieldValue, sb)
                  sb.append(',')
                }
              }
              Expr.ofList(statements)
            }
            sb.setCharAt(sb.length()-1,'}')
          }

            /*
        case c:CollectionRType =>
          val elementFn = renderJsonFn(c.elementType)
          // '{(a:Any, sb:StringBuilder) => (qq:Quotes) => 
          '{(a:T, sb:StringBuilder) => 
            sb.append('[')
            a.asInstanceOf[List[_]].map{item => 
              $elementFn(item,sb)
              sb.append(',')
            }
            sb.setCharAt(sb.length()-1,']')
          }
          */

        case _ =>
          '{(a:Any, sb:StringBuilder) => 
            sb.append(a.toString)
          }
      }
    }

    println("HEY In compiler!")
    // '{"foo"}

    val renderMe = renderJsonFn(RType.unwindType(quotes)(TypeRepr.of[T]))
    val sb = Expr(new StringBuilder())
    '{ $renderMe($t, $sb).toString }
  }

  /*
  Interesting.... save this.  Has nothing to do with this project but may come in handy...def 

          val toStringSym = Symbol.requiredMethod("java.lang.Object.toString")
          checkNotOverridden(toStringSym)
          val toStringOverrideSym = Symbol.newMethod(cls, "toString", toStringSym.info, Flags.Override, Symbol.noSymbol)
          val toStringDef = DefDef(toStringOverrideSym, _ =>
            given Quotes = toStringOverrideSym.asQuotes
            Some(toStringExpr(className, fields).asTerm)
          )

          Basically how to generate a new method.
          Comes from: dotty/tests/run-macros/annot-mod-class-data/Macro_1.scala
  */
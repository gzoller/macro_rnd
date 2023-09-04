package co.blocke.scala_reflection

import scala.quoted.*
import quoted.Quotes

import java.io.*
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import co.blocke.scala_reflection.RType
// import co.blocke.scala_reflection.info.*
// import co.blocke.scala_reflection.impl.*
import scala.jdk.CollectionConverters.*

object ZType:

  /** Default implementation of `ToExpr[BigInt]` */
  given StringBuilderToExpr: ToExpr[StringBuilder] with {
    def apply(x: StringBuilder)(using Quotes): Expr[StringBuilder] =
      '{ new StringBuilder( { ${Expr(x.toString)} } ) }
  }

  // given RTypeToExpr[T: Type: ToExpr]: ToExpr[RType[T]] with
  //   def apply(rt: RType[T])(using Quotes): Expr[RType[T]] = 
  //     import quotes.reflect.*
  //     '{ rt }

  given ToExpr[Any] with {                               
    def apply(x: Any)(using Quotes) = {
      import quotes.reflect._
      Ref(defn.AnyClass).appliedToType(TypeRepr.typeConstructorOf(x.getClass)).asExpr.asInstanceOf[Expr[Any]]
    }
  }


  given RTypeToExpr[T: Type: ToExpr]: ToExpr[RType[T]] with
    def apply(rt: RType[T])(using Quotes): Expr[RType[T]] = rt match 
      case r: RType[T] => Expr(r)

      // case s: StringRType => Expr(s.asInstanceOf[RType[T]])
      // case i: IntRType => Expr(i.asInstanceOf[RType[T]])
      // case c: ClassRType[T] => Expr(c.asInstanceOf[RType[T]])

  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t, '{RType.of[T]}) }

  def toJsonImpl[T:Type](t: Expr[T], r: Expr[RType[T]])(using quotes:Quotes): Expr[String] = {
    import quotes.reflect.* 

    println("HEY In compiler!")

    // def fooFn(): Expr[(Any,StringBuilder) => StringBuilder] = 
    // // quotes visible here

    // Given some value, render the Json string
    // def renderJsonFn[T:Type](rt: RType): Expr[(T,StringBuilder) => StringBuilder] = 
    def renderJsonFn[Z](rt: RType[Z])(using Type[Z]): Expr[(Any,StringBuilder) => StringBuilder] = 
      rt match {
        case _: StringRType => 
          '{(a:Any, sb:StringBuilder) =>
            sb.append('"')
            sb.append(a.toString)
            sb.append('"')
          }

        case classRType: ClassRType[Z] =>
          // println("HERE: "+classRType)
          //  def toType(quotes: Quotes): quotes.reflect.TypeRepr  = quotes.reflect.TypeRepr.typeConstructorOf(infoClass)
          val zipped = classRType.fields.map{ f => 
            val z = quotes.reflect.TypeRepr.typeConstructorOf(f.fieldType.infoClass)
            val v = z.asType.asInstanceOf[Type[f.fieldType.T]]
            given ft: Type[f.fieldType.T] = v  //Type.of[f.fieldType.T]
            (renderJsonFn(f.fieldType)(using ft), f) 
          }
          '{(a:Any, sb:StringBuilder) => 
            val b = a.asInstanceOf[Z]
            sb.append('{')
            val stuff = ${
              val statements = zipped.map{ case (fn, field) => 
                '{ 
                  sb.append(${Expr(field.name)})
                  sb.append(':')
                  val fieldValue = ${ 
                    Select.unique('{ b }.asTerm, field.name).asExpr
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

    // val renderMe = renderJsonFn(RType.unwindType(quotes)(TypeRepr.of[T]))
    val renderMe = renderJsonFn(RType.of[T])
    val sb = Expr(new StringBuilder())
    '{ 
      // val s = scala.Symbol($t.getClass.getName)
      // import scala.quoted.*
      // given Quotes = s.asQuotes
      $renderMe($t, $sb).toString 
    }
  }

    /*
    RType.unwindType(q)(TypeRepr.of[T]) match {
      case c: ClassInfo =>
        // This part creates a set of render functions for each class field (w/embedded value)--not run yet tho!
        val stmts = c.fields.toList.map{ f =>  // List[Expr[StringBuilder => StringBuilder]]
          val key = Expr(s"\"${f.name}\":")
          val theValue = f.asInstanceOf[ScalaFieldInfo].resolve(t)
          val fieldRenderFn = renderJsonFn(f.fieldType) // Expr[(Any, StringBuilder) => StringBuilder
          '{(sb: StringBuilder) =>
            sb.append($key)
            $fieldRenderFn($theValue, sb)
            sb.append(',')
           }
        }
        val sb = Expr(new StringBuilder("{"))
        // Here's where we actually run (fold up, actually) the field render functions
        val fieldsRendered = stmts.foldLeft(sb)((b:Expr[StringBuilder], fn: Expr[StringBuilder => StringBuilder]) => '{$fn($b)} )
        '{ val gen = ${fieldsRendered}; gen.setCharAt(gen.length()-1,'}'); gen.toString } // wierd 'gen' stuff here to avoid double-calling maco!

      case _ => 
        Expr("unknown")
      }
  
  }*/


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


  /* From Discord
  '{(a:Any, sb:StringBuilder) => 
  sb.append('{')
  ${
    val statements = classInfo.fields.map{ field =>
      '{ 
          val fieldvalue = ${Select.unique('{a}.asTerm, fieldname)}
          ${encodeFunction}.apply(fieldValue)
      }
    }
    Block(statements.init, statements.last) // blocks may not be empty, and take a list of statements, followed by a final expression, I looked up the API
  }
  sb.append('}')
}


def renderJsonFn[T](rt: RType): Expr[(T,StringBuilder) => StringBuilder] = 
  …
  (a: T) =>
  …
*/
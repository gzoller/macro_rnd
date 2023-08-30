package co.blocke.scala_reflection

import scala.quoted.*
import quoted.Quotes

import java.io.*
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.info.*
import co.blocke.scala_reflection.impl.*
import scala.jdk.CollectionConverters.*

object ZType:

  /** Default implementation of `ToExpr[BigInt]` */
  given StringBuilderToExpr: ToExpr[StringBuilder] with {
    def apply(x: StringBuilder)(using Quotes): Expr[StringBuilder] =
      '{ new StringBuilder( { ${Expr(x.toString)} } ) }
  }

  given ToExpr[Any] with {                               
    def apply(x: Any)(using Quotes) = {
      import quotes.reflect._
      Ref(defn.AnyClass).appliedToType(TypeRepr.typeConstructorOf(x.getClass)).asExpr.asInstanceOf[Expr[Any]]
    }
  }

  given ToExpr[Quotes] with {                               
    def apply(x: Quotes)(using q:Quotes) = {
      import q.reflect._
      Ref(defn.AnyClass).appliedToType(TypeRepr.typeConstructorOf(x.getClass)).asExpr.asInstanceOf[Expr[Quotes]]
    }
  }

  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t, '{RType.of[T]}) }

  def toJsonImpl[T:Type](t: Expr[T], r: Expr[RType])(using quotes:Quotes): Expr[String] = {
    import quotes.reflect.* 

    println("HEY In compiler!")

    // def fooFn(): Expr[(Any,StringBuilder) => StringBuilder] = 
    // // quotes visible here

    // val qq = Expr(quotes) //wrap quotes in Expr to access inside Expr below
    // ‘{(a:Any, sb:StringBuilder) =>
    //     implicit val qt:Quotes = $qq
    //   	// use qt here for something that needs a Quotes instance
    //     sb.apply(“ok”)
    //  }

/*
def exprOfOption[T: Type](x: Expr[Option[T]])(using Quotes): Option[Expr[T]] =
  x match
    case '{ Some($x: T) } => Some(x) // x: Expr[T]
                // ^^^ type ascription with generic type T
*/

    // Given some value, render the Json string
    // def renderJsonFn(rt: RType): Expr[(Any,StringBuilder) => (Quotes) => StringBuilder] = 
    // def renderJsonFn[T: Type](rt: RType): Expr[(T,StringBuilder) => StringBuilder] = 
    def renderJsonFn[T: Type](rt: RType): Expr[(T,StringBuilder) => StringBuilder] = 
      rt match {
        case PrimitiveType.Scala_String => 
          // '{(a:Any, sb:StringBuilder) => (qq:Quotes) => 
          '{(a:T, sb:StringBuilder) =>
            sb.append('"')
            sb.append(a.toString)
            sb.append('"')
          }

        case classInfo: ClassInfo =>
          val ciExp = Expr(rt)
          val fieldFns = classInfo.fields.toList.map(f => renderJsonFn(f.fieldType))
          val zipped = fieldFns.zip(classInfo.fields)

          '{(a:T, sb:StringBuilder) => 
            sb.append('{')
            val stuff = ${
              val statements = zipped.map{ case (fn, field) => 
                val scalaField = field.asInstanceOf[ScalaFieldInfo]
                '{ 
                  val fieldValue = ${ Select.unique('{ a }.asTerm, scalaField.name).asExpr }
                  ${Expr(scalaField.name)} + ":" + fieldValue.toString
                }
              }
              Expr.ofList(statements)
              // Block(statements.init, statements.last) // blocks may not be empty, and take a list of statements, followed by a final expression, I looked up the API
            }
            sb.append(stuff.mkString(","))
            sb.append('}')
          }
/*
            sb.append('{')

            // implicit val qt:Quotes = $qq
            val classInfo = $rtExp.asInstanceOf[ClassInfo]
            val classInstanceValue = '{a}.asTerm //Expr(a)

            // resolve() here calls Select.unique(thing, "foom").asExpr
            // val fieldValues = classInfo.fields.map{ _.asInstanceOf[ScalaFieldInfo].resolve(thing) }.toList
            // println("VALUES : "+fieldValues)

            val zipped = ${fieldFns}.zip(classInfo.fields)
            //val ssb = Expr(StringBuilder)
            zipped.map{ case (fn,v) =>
              sb.append('"')
              sb.append(v.name)
              sb.append("\":")
              //def apply(v1: Any, v2: StringBuilder): StringBuilder
              val fieldInstanceValue = Select.unique(classInstanceValue, v.name) // equiv of person.name
              fn(fieldInstanceValue, sb)
              sb.append(',')
            }
            sb.setCharAt(sb.length()-1,'}')
           }
            */

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
//          '{(a:Any, sb:StringBuilder) => (qq:Quotes) => 
          '{(a:T, sb:StringBuilder) => 
            sb.append(a.toString)
          }
      }

    val renderMe = renderJsonFn[T](RType.unwindType(quotes)(TypeRepr.of[T]))
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
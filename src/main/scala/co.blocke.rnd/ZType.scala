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

  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t, '{RType.of[T]}) }

  def toJsonImpl[T:Type](t: Expr[T], r: Expr[RType])(using q:Quotes): Expr[String] = {
    import quotes.reflect.* 

    println("HEY In compiler!")

    // inline def later[T: Type, U: Type](f: Expr[T] => Expr[U]): Expr[T => U] = '{ (x: T) => ${ f('x) } }

    inline def appendStr(f: (Expr[StringBuilder]) => Expr[StringBuilder]): Expr[(StringBuilder) => StringBuilder] = 
      '{ (sb: StringBuilder) => ${ f('sb) } }

    // Given some value, render the Json string
    def renderJsonFn(rt: RType): Expr[(Any,StringBuilder) => StringBuilder] = 
      rt match {
        case PrimitiveType.Scala_String => 
          '{(a:Any, sb:StringBuilder) => 
            sb.append('"')
            sb.append(a.toString)
            sb.append('"')
          }
        case c:CollectionRType =>
          val elementFn = renderJsonFn(c.elementType)
          '{(a:Any, sb:StringBuilder) => 
            sb.append('[')
            a.asInstanceOf[List[_]].map{item => 
              $elementFn(item,sb)
              sb.append(',')
            }
            sb.setCharAt(sb.length()-1,']')
          }
        case _ =>
          '{(a:Any, sb:StringBuilder) => sb.append(a.toString)}
      }

    RType.unwindType(q)(TypeRepr.of[T]) match {
      case c: ClassInfo =>
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
        val y = stmts.foldLeft(sb)((b:Expr[StringBuilder], fn: Expr[StringBuilder => StringBuilder]) => '{$fn($b)} )
        '{ val gen = ${y}; gen.setCharAt(gen.length()-1,'}'); gen.toString } // wierd 'gen' stuff here to avoid double-calling maco!

      case _ => 
        Expr("unknown")
      }
  
  }
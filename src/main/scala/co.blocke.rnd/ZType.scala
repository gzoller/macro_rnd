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

    inline def renderFn(f: (Expr[Any], Expr[StringBuilder], Expr[Option[RType]]) => Expr[StringBuilder]): Expr[(Any, StringBuilder, Option[RType]) => StringBuilder] = 
      '{ (value: Any, sb: StringBuilder, subtype: Option[RType]) => ${ f('value, 'sb, 'subtype) } }

    def renderJsonFn(rt: RType): Expr[Any => String] = 
      rt match {
        case PrimitiveType.Scala_String => 
          '{(a:Any) => "\""+a.toString+"\""}
        // case c:CollectionRType =>
        //   elementFn = renderJsonFn(c.elementType)
        //   '{(a:Any) => "["+a.toString+"]"}
        //   sb.append('[')
        //   value.asInstanceOf[List[_]].map{item => 
        //     toJson(sb, c.elementType, item)
        //     sb.append(',')
        //   }
        //   sb.setCharAt(sb.length()-1,']')
        case _ =>
          '{(a:Any) => a.toString}

      }

    RType.unwindType(q)(TypeRepr.of[T]) match {
      case c: ClassInfo =>
        val stmts = c.fields.toList.map{ f =>
          val key = Expr(s"\"${f.name}\":")
          val theValue = f.asInstanceOf[ScalaFieldInfo].resolve(t)
          f.fieldType match {
            case PrimitiveType.Scala_String => 
              appendStr( (xsb: Expr[StringBuilder]) => '{ val dakey = ${key}; val value = ${theValue}; $xsb.append(s"$dakey\"$value\",")})
            case c: CollectionRType =>
              val fn = renderJsonFn(c.elementType)
              appendStr( (xsb: Expr[StringBuilder]) => '{ 
                val dakey = ${key}
                val value = ${theValue}
                $xsb.append(dakey)
                $xsb.append('[')
                value.asInstanceOf[List[_]].map{ item => 
                  $xsb.append($fn(item))
                  $xsb.append(',')
                }
                $xsb.setCharAt($xsb.length()-1,']')
                $xsb.append(',')
                $xsb
              })
            case _ =>
              appendStr( (xsb: Expr[StringBuilder]) => '{ val dakey = ${key}; val value = ${theValue}; $xsb.append(s"$dakey$value,")})
          }
        }
        val sb = Expr(new StringBuilder("{"))
        val y = stmts.foldLeft(sb)((b:Expr[StringBuilder], fn: Expr[StringBuilder => StringBuilder]) => '{$fn($b)} )
        '{ val gen = ${y}; gen.setCharAt(gen.length()-1,'}'); gen.toString }

      case _ => 
        Expr("unknown")
      }
  
  }
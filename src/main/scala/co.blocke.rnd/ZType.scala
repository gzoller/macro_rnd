package co.blocke.scala_reflection

import scala.quoted.*
import quoted.Quotes

import java.io.*
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.info.*
import co.blocke.scala_reflection.impl.*

object ZType:


  inline def logAST[T](inline expression:T) = ${logASTImpl('expression)}
  def logASTImpl[T:Type](expression: Expr[T])(using q:Quotes): Expr[T] = {
    import quotes.reflect.* 
    val term = expression.asTerm
    println(s"============ Tree of type ${Type.show} =============")
    println()
    println(term.show(using Printer.TreeAnsiCode))
    println()
    println(term.show(using Printer.TreeStructure))
    println()
    println("=====================================================")
    expression
  }

  //------------------------
  //  <<  MACRO ENTRY >>
  //------------------------
  /*
  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t, '{RType.of[T]}) }

  // primordial toJson.  Gets RType using scala-reflection, then returns
  // list of field names for the given case class
  def toJsonImpl[T:Type](t: Expr[T], r: Expr[RType])(using q:Quotes): Expr[String] =
    import quotes.reflect.* 

    println("HEY In compiler!")

    inline def later[U: Type](f: () => Expr[U]): Expr[() => U] = '{ () => ${ f() } }

    val runit = later( () => '{
      val sb = new StringBuilder("{")
      $r.asInstanceOf[ClassInfo].fields.map{f =>
        val f2 = f.asInstanceOf[ScalaFieldInfo]
        sb.append(s"\"${f2.name}\":")
        f2.fieldType match {
          case PrimitiveType.Scala_String => 
            sb.append('\"')

            // How can I generate the macro equivalent of:  $t.$f2.name (accessor)?
            // The f2.valueOf works--but wondering if there may be a more performant way...
            sb.append(f2.valueOf(${t}).toString)
            sb.append('\"')

          case c:CollectionRType =>
            sb.append('[')
            f2.valueOf(${t}).asInstanceOf[List[_]].map{item => 
              sb.append(item.toString)
              sb.append(',')
            }
            sb.setCharAt(sb.length()-1,']')
          case _ =>
            sb.append(f2.valueOf(${t}).toString)
        }
        sb.append(",")
        }
      sb.setCharAt(sb.length()-1,'}')
      sb.toString
    })
    '{$runit()}
    */


  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t, '{RType.of[T]}) }

  // primordial toJson.  Gets RType using scala-reflection, then returns
  // list of field names for the given case class
  def toJsonImpl[T:Type](t: Expr[T], r: Expr[RType])(using q:Quotes): Expr[String] =
    import quotes.reflect.* 

    println("HEY In compiler!")

    val rtype = RType.unwindType(q)(TypeRepr.of[T])

    inline def later[U: Type](f: () => Expr[U]): Expr[() => U] = '{ () => ${ f() } }

    def printAllElements(list : List[Expr[Any]])(using Quotes) : Expr[String] = list match {
      case head :: other => '{ ${head}.toString + " :: " + ${ printAllElements(other)} }
      case _ => '{""}
    }
    // Try to get value
    val accessors = rtype.asInstanceOf[ClassInfo].fields.map( f =>
      Select.unique(t.asTerm, f.name).asExpr
    ).toList

    val foo = printAllElements(accessors)
    foo

    /*
    val runit = later( () => '{
      val sb = new StringBuilder("{")
      $r.asInstanceOf[ClassInfo].fields.map{f =>
        val f2 = f.asInstanceOf[ScalaFieldInfo]
        sb.append(s"\"${f2.name}\":")
        f2.fieldType match {
          case PrimitiveType.Scala_String => 
            sb.append('\"')

            // val n = "toString"
            // val g = ${Select.unique(t.asInstanceOf[Expr[Object]].asTerm, n).appliedToNone.asExprOf[String]}
            // sb.append( g )

            // How can I generate the macro equivalent of:  $t.$f2.name (accessor)?
            // The f2.valueOf works--but wondering if there may be a more performant way...
            sb.append(f2.valueOf(${t}).toString)
            sb.append('\"')

          case c:CollectionRType =>
            sb.append('[')
            f2.valueOf(${t}).asInstanceOf[List[_]].map{item => 
              sb.append(item.toString)
              sb.append(',')
            }
            sb.setCharAt(sb.length()-1,']')
          case _ =>
            sb.append(f2.valueOf(${t}).toString)
        }
        sb.append(",")
        }
      sb.setCharAt(sb.length()-1,'}')
      sb.toString
    })
    '{$runit()}
*/
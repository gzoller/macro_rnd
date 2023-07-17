package co.blocke.scala_reflection

import scala.quoted.*
import quoted.Quotes

import java.io.*
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.info.{ClassInfo,ScalaFieldInfo}

object ZType:

  //------------------------
  //  <<  MACRO ENTRY >>
  //------------------------
  inline def toJson[T](t: T): String = ${ toJsonImpl[T]('t) }

  // primordial toJson.  Gets RType using scala-reflection, then returns
  // list of field names for the given case class
  def toJsonImpl[T:Type](t: Expr[T])(implicit qctx: Quotes): Expr[String] =
    import qctx.reflect.*

    println("HEY In compiler!")

    inline def later[T: Type, U: Type](f: Expr[T] => Expr[U]): Expr[T => U] = '{ (x: T) => ${ f('x) } }

    RType.unwindType(qctx)(TypeRepr.of[T]) match {
      case s: ClassInfo => 
        val fldStuff = s.fields.toList.map{f =>
          (f.name, null)
        }

        val fn = later[List[String],String]( (in: Expr[List[String]]) => '{ ${in}.mkString("{",",","}") } )

        val pieces = fldStuff.map{ f => 
          val str = s"\"${f._1}\":\"foo\"" 
          Expr(str)
        }
        val stuff = Expr.ofList(pieces.toIndexedSeq)
        '{ $fn($stuff)} 

      case x =>
        println("Nope: "+x)
        Expr("")
    }
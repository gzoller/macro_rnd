package co.blocke.scala_reflection

import scala.quoted.*
import quoted.Quotes
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.impl.*

/*
trait JsonRenderer:
    def renderJsonFn(rt: RType): Expr[Any => String]

object NakedJsonRenderer extends JsonRenderer:
    def toJson( sb: StringBuilder, value: Any, rtype: Option[RType] ): StringBuilder = 
        sb.append(value.toString)

object QuotedJsonRenderer extends JsonRenderer:
    def toJson( sb: StringBuilder, value: Any, rtype: Option[RType] ): StringBuilder = 
        sb.append('"')
        sb.append(value.toString)
        sb.append('"')

object ListJsonRenderer extends JsonRenderer:
    def toJson( sb: StringBuilder, value: Any, rtype: Option[RType] ): StringBuilder = 
        sb.append('[')
        val (elementRenderer, elementType) = JsonRenderer.renderer(rtype.get) // .get is safe here: Collection must have an element type
        value.asInstanceOf[List[_]].map{ item => 
            elementRenderer.toJson(sb, item, elementType) // rtype is the element type
            sb.append(',')
        }
        sb.setCharAt(sb.length()-1,']')
        sb

object JsonRenderer:
    def renderer(rtype: RType): (JsonRenderer, Option[RType]) =
        rtype match {
            case PrimitiveType.Scala_String => 
                (QuotedJsonRenderer, None)
            case c:CollectionRType =>
                (ListJsonRenderer, Some(c.elementType))
            case _ =>
                (NakedJsonRenderer, None)
        }
        */
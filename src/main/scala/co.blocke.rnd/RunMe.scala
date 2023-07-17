package co.blocke.rnd
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.info.{ClassInfo,ScalaFieldInfo}

case class Person(name:String, age: Int, interests: List[String])

object RunMe extends App:

  val person = Person("Greg",57,List("A"))

  println("=== Using macro ===")
  println(co.blocke.scala_reflection.ZType.toJson(person))

  println("\n=== Using RType at runtime (no macro) ===")
  // -- GOAL:  Get this stuff working in the macro!
  val rt = RType.of[Person]
  rt.asInstanceOf[ClassInfo].fields.toList.asInstanceOf[List[ScalaFieldInfo]].map{f => 
    println("Field "+f.name+" has value "+f.valueOf(person))
    }

  println("Done")

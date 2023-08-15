package co.blocke.rnd
import co.blocke.scala_reflection.RType
import co.blocke.scala_reflection.info.{ClassInfo,ScalaFieldInfo}

case class Person(name:String, age: Int, interests: List[String])

object RunMe extends App:

  val person = Person("Greg",57,List("A","B","C"))

  println("=== Using macro ===")
  println(co.blocke.scala_reflection.ZType.toJson(person))
  // println(sj.render(person))

  val now2 = System.currentTimeMillis()
  for(i<-1 to 1000000)
    co.blocke.scala_reflection.ZType.toJson(person)
  println("Macro-based: "+(System.currentTimeMillis() - now2))

  /*
  val now3 = System.currentTimeMillis()
  val sb = new StringBuilder()
  for(i<-1 to 1000000)
    sb.append('{')
    sb.append("\"name\":")
    sb.append(s"\"${person.name}\"")
    sb.append(',')
    sb.append("\"age\":")
    sb.append(person.age.toString)
    sb.append(',')
    sb.append("\"interests\":")
    sb.append('[')
    person.interests.map{i => 
      sb.append('\"')
      sb.append(i)
      sb.append("\",")
    }
    sb.setCharAt(sb.length()-1,']')
    sb.append('}')
  println("Baseline: "+(System.currentTimeMillis() - now3))

  // co.blocke.scala_reflection.ZType.logAST(person)
  */
  
  println("Done")


  /*
  ScalaJack: 1336
  Baseline: 138
  */
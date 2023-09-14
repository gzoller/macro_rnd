package co.blocke.rnd
import co.blocke.scala_reflection.*

// import co.blocke.scala_reflection.info.{ClassInfo,ScalaFieldInfo}

// case class Person(name:String, age: Int, interests: List[String])
// case class Person2(name:String, age: Int, interests: List[List[Int]])
// case class SimplePerson(name: String, age:Int)

object RunMe extends App:

  val person = Person("Greg",57,Thing(25,"wow"))

  // val p = RType.of[co.blocke.scala_reflection.Thing[List[Person]]]
  // println("RType: "+p)

  println("=== Using macro ===")
  // println(RType.of[Person])
  println(co.blocke.scala_reflection.ZType.toJson(person))
  // println(sj.render(person))

  /*
  val now2 = System.currentTimeMillis()
  for(i<-1 to 1000000)
    co.blocke.scala_reflection.ZType.toJson(person)
  println("Macro-based: "+(System.currentTimeMillis() - now2))

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
    sb.append("\"thing\":")
    sb.append('{')
    sb.append("\"a\":")
    sb.append(person.thing.a)
    sb.append(',')
    sb.append("\"b\":")
    sb.append(s"\"${person.thing.b}\"")
    sb.append('}')
    sb.append('}')
  println("Baseline: "+(System.currentTimeMillis() - now3))
  */
  
  println("Done")


  /*
  ScalaJack: 1336
  Baseline: 138
  */
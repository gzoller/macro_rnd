package co.blocke.scala_reflection

trait SJ_Serialize {
  def toJson(sb: StringBuilder, c:SJConfig): Unit = { println("wrong") }
}

trait Wow2 {
  def wow() =  { println("wow.") }
}

case class SJConfig()

case class Bogus() extends SJ_Serialize
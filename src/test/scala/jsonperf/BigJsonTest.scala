package jsonperf

case class Person(name: String, age: Int)
case class BigJson(colleagues: Seq[Person])

class BigJsonTest extends JsonTest[BigJson] with Serializable {

  val total = 1000
  def colleagues = (for (i ← 1 to 1000) yield s"""{"name": "person-$i", "age": $i}""").mkString(", ")
  val json =
    s"""{
      |  "colleagues": [
      |    $colleagues
      |  ]
      |}
    """.stripMargin

  override val newA = BigJson(colleagues = for (i ← 1 to 1000) yield Person(s"person-$i", i))
  override def playRead = {
    import play.api.libs.json.Json
    implicit val personReads = Json.reads[Person]
    Json.reads[BigJson]
  }
  override def sphereFromJson = {
    import io.sphere.json.generic._
    implicit val personFromJson = jsonProduct((Person.apply _).curried)
    deriveJSON[BigJson]
  }
  override def sprayRead = {
    import spray.json.DefaultJsonProtocol._
    implicit val personFormat = jsonFormat2(Person)
    jsonFormat1(BigJson)
  }
  override def argonautDecodeJson = {
    import argonaut.Argonaut._
    implicit val personDecode = jdecode2(Person)
    jdecode1(BigJson)
  }
  override val clazz = classOf[BigJson]

  override def checkResult(result: BigJson): Unit = {
    assert(result.colleagues.size == total, s"result.colleagues.size(${result.colleagues.size}) != $total")
    for (i ← 1 to 1000) {
      val c = result.colleagues(i - 1)
      assert(c.name == s"person-$i", s"name(${c.name}) != 'person-$i'")
      assert(c.age == i)
    }
  }
}

object BigJsonPerf extends PerfTest[BigJson] {
  override val test = new BigJsonTest

  testRun()

}

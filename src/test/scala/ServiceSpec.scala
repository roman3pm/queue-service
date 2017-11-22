import java.time.LocalDateTime

import org.scalatest.{FlatSpec, Matchers}
import queue.service.Service

import scala.collection.mutable.ListBuffer

class ServiceSpec extends FlatSpec with Matchers {
  behavior of "Service"

  it should "start and fill ListBuffer" in {
    val service = new Service[Unit]
    service.start()

    val list = ListBuffer[String]()
    val time = LocalDateTime.now()
    service.execute(time.plusSeconds(5), () => list += "1")
    service.execute(time.plusSeconds(5), () => list += "2")
    service.execute(time.plusSeconds(5), () => list += "3")
    service.execute(time.plusSeconds(2), () => list += "0")

    TestUtils.waitUntilTrue(() => list.size == 4)

    list shouldEqual ListBuffer("0", "1", "2", "3")
  }
}

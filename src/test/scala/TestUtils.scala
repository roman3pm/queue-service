import scala.concurrent.TimeoutException
import scala.concurrent.duration.{Duration, _}

object TestUtils {
  def waitUntilTrue(f: () => Boolean)(implicit timeout: Duration = 10.seconds): Unit = {
    val start = System.currentTimeMillis()
    var current = start
    while (!f() && current - start <= timeout.toMillis) {
      Thread.sleep(100)
      current = System.currentTimeMillis()
    }
    if (!f() || current - start > timeout.toMillis) {
      throw new TimeoutException("Timeout waiting for test condition completion")
    }
  }
}

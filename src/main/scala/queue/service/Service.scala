package queue.service

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Callable, ExecutorService, Executors, PriorityBlockingQueue}

import scala.annotation.tailrec
import scala.util.Try

/**
  * Test assignee service class
  * <p>
  * Example:
  * {{{
  *   val service = new Service[Int]
  *   service.start()
  *   service.execute(LocalDateTime.now(), () => 2 + 2)
  * }}}
  *
  * @tparam T result type of Callable
  */
class Service[T] {
  private val idx = new AtomicLong(0) // Order index
  private val servicePool: ExecutorService = Executors.newSingleThreadExecutor()

  implicit private val ordering = new Ordering[(LocalDateTime, Callable[T], Long)] {
    override def compare(x: (LocalDateTime, Callable[T], Long), y: (LocalDateTime, Callable[T], Long)): Int = {
      if (x._1.isBefore(y._1) || (x._1.isEqual(y._1) && (x._3 < y._3))) -1
      else 1
    }
  }

  private val queue = new PriorityBlockingQueue[(LocalDateTime, Callable[T], Long)](1024, ordering)

  /**
    * Execute service
    *
    * @param t2 tuple of LocalDateTime and Callable
    */
  def execute(t2: (LocalDateTime, Callable[T])) = Try {
    queue.offer((t2._1, t2._2, idx.incrementAndGet()))
  }

  /**
    * Start service
    */
  def start(): Unit = {
    val service = new Runnable {
      override def run(): Unit = {
        /**
          * Tailrec function for polling queue
          */
        @tailrec
        def tailrec(): Unit = {
          val now = LocalDateTime.now()
          val t2@(time, callable, _) = queue.take()
          if (time.isEqual(now) || time.isBefore(now))
            Try(callable.call()) // Execute callable at the right time...
          else
            queue.offer(t2) // ...or put back to queue
          tailrec()
        }

        tailrec()
      }
    }
    servicePool.execute(service)
  }
}

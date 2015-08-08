package zzz.akka.investigation

import java.util.concurrent.Executors
import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._

object MainAkka {
	val pool = Executors.newCachedThreadPool()
	implicit val ec = ExecutionContext.fromExecutorService(pool)

	def main(args: Array[String]) {
		println("begin")
		val future = Future {"Fibonacci Numbers"}
		val result = Await.result(future, 3.second)
		println(result)
		pool.shutdown()
	}
}
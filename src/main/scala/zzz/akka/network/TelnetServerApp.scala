package zzz.akka.network

import akka.actor.ActorSystem
import akka.actor.Props

object TelnetServerApp extends App {
	val system = ActorSystem.create("TelnetServerApp")
	val server = system.actorOf(Props(new TelnetServer), "Telnet")

	Thread.sleep(5 * 60 * 1000)

	system.shutdown()
}
package zzz.akka.investigation

import akka.actor.{Actor, Props, ActorSystem}

class BadShakespeareanActor extends Actor {
	def receive = {
		case "Good morning" =>
			println("zao shang hao")
		case "You're terrible" =>
			println("Yup")
	}
}

object BadShakespeareanMain {
	val system = ActorSystem("BadShakespearean")
	val actor = system.actorOf(Props[BadShakespeareanActor])

	def send(msg: String) {
		println("Me:  " + msg)
		actor ! msg
		Thread.sleep(100)
	}

	def main(args: Array[String]) {
		send("Good morning")
		send("You're terrible")
		system.shutdown()
	}
}
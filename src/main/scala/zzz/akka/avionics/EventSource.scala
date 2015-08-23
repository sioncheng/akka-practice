package zzz.akka.avionics

import akka.actor.{Actor,ActorRef}

object EventSource {
	case class RegisterListener(listener:ActorRef)
	case class UnregisterListener(listener:ActorRef)
}

trait EventSource {
	def sendEvent[T](e:T):Unit
	def eventSourceReceive : Actor.Receive
}

trait ProductionEventSource extends EventSource {
	this : Actor => //this trait can't be mixed with any class ia not actor

	import EventSource._

	var listeners = Vector.empty[ActorRef]
	
	def sendEvent[T](e:T): Unit = listeners foreach {_ ! e}

	def eventSourceReceive : Receive = {
		case RegisterListener(listener:ActorRef) =>
			listeners = listeners :+ listener
		case UnregisterListener(listener:ActorRef) =>
			listeners = listeners filter {_ != listener} 
	}
}
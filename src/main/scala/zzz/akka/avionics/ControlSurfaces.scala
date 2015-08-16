package zzz.akka.avionics

import akka.actor.{Actor, ActorRef}

object ControlSurfaces {
	case class StickBack(amount: Float)
	case class StickFoward(amount: Float)
}

class ControlSurfaces(altimeter: ActorRef) extends Actor {
	import ControlSurfaces._
	import Altimeter._

	def receive = {
		case StickBack(amount) =>
			altimeter ! RateChange(amount)
		case StickFoward(amount) =>
			altimeter ! RateChange(-1 * amount)
	}
}
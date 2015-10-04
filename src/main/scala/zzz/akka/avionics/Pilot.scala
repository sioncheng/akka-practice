package zzz.akka.avionics

import akka.actor.{Actor, ActorRef}

object Pilots {
	case object ReadyToGo
	case object RelinquishControl
}

class Pilot (plane : ActorRef ) extends Actor {
	import Pilots._
	import Plane._

	var controls: ActorRef = context.system.deadLetters
	var copilot:ActorRef = context.system.deadLetters
	var autopilot:ActorRef = context.system.deadLetters
	val copilotName = context.system.settings.config.getString(
		"zzz.akka.avionics.flightcrew.copilotName")

	def receive = {
		case ReadyToGo => 
			//context.parent ! Plane.GiveMeControl
			println(".......",plane)
			plane ! Plane.GiveMeControl
			copilot = context.actorFor("../" + copilotName)
			autopilot = context.actorFor("../AutoPilot")
		case Controls(controlSurfaces) =>
			controls = controlSurfaces
	}
}

class CoPilot extends Actor {
	import Pilots._

	var controls: ActorRef = context.system.deadLetters
	var pilot:ActorRef = context.system.deadLetters
	var autopilot:ActorRef = context.system.deadLetters
	val pilotName = context.system.settings.config.getString(
		"zzz.akka.avionics.flightcrew.pilotName")

	def receive = {
		case ReadyToGo =>
			pilot = context.actorFor("../" + pilotName)
			autopilot = context.actorFor("../AutoPilot")
	} 
}

class AutoPilot extends Actor {
	import Pilots._

	var pilot:ActorRef = context.system.deadLetters
	var copilot:ActorRef = context.system.deadLetters
	val config = context.system.settings.config
	val pilotName = config.getString("zzz.akka.avionics.flightcrew.pilotName")
	val copilotName = config.getString("zzz.akka.avionics.flightcrew.copilotName")

	def receive = {
		case ReadyToGo =>
			pilot = context.actorFor("../" + pilotName)
			copilot = context.actorFor("../" + copilotName)
	} 
}

trait PilotProvider {
	def newPilot(plane: ActorRef): Actor = new Pilot(plane)
	def newCopilot:Actor = new CoPilot
	def newAutopilot:Actor = new AutoPilot
}
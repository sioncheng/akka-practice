package zzz.akka.avionics

import akka.actor.{Actor, Props, ActorLogging, ActorRef}

object Plane {
	case object GiveMeControl
	case class Controls(controlSurfaces: ActorRef)
}

class Plane extends Actor with ActorLogging {
	import Altimeter._
	import ControlSurfaces._
	import Plane._
	import EventSource._

	val altimeter = context.actorOf(Props[Altimeter])
	val controls = context.actorOf(Props(new ControlSurfaces(altimeter)))
	val config = context.system.settings.config
	val pilot = context.actorOf(Props[Pilot],
		config.getString("zzz.akka.avionics.flightcrew.pilotName"))
	val copilot = context.actorOf(Props[CoPilot],
		config.getString("zzz.akka.avionics.flightcrew.copilotName"))
	val autopilot = context.actorOf(Props[AutoPilot],"AutoPilot")
	val flightAttendant = context.actorOf(Props(LeadFlightAttendant()),
		config.getString("zzz.akka.avionics.flightcrew.leadAttendantName"))

	override def preStart() {
		altimeter ! RegisterListener(self)
		List(pilot, copilot) foreach { _ ! Pilots.ReadyToGo}
	}

	def receive = {
		case GiveMeControl => 
			log.info("give me control")
			sender ! controls
		case AltitudeUpdate(altitude) =>
			log.info(s"altitude update $altitude")
	}
}
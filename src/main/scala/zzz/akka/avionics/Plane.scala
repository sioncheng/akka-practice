package zzz.akka.avionics

import akka.actor.{Actor, Props, ActorLogging, ActorRef}
import Altimeter._
import scala.concurrent.duration._
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout

object Plane {
	case object GiveMeControl
	case class Controls(controlSurfaces: ActorRef)
}

/*
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
} */

class Plane extends Actor with ActorLogging with AltimeterProvider with PilotProvider with LeadFlightAttendantProvider{
	//this: AltimeterProvider with PilotProvider with LeadFlightAttendantProvider =>
	//
	import IsolatedLifeCycleSupervisor._
	import Altimeter._
	import EventSource._
	import ControlSurfaces._
	import Plane._
	import Pilots._

	implicit val askTimeout = Timeout(1 second)

	val config = context.system.settings.config
	val pilotName = config.getString("zzz.akka.avionics.flightcrew.pilotName")
	val copilotName = config.getString("zzz.akka.avionics.flightcrew.copilotName")
	val attendantName = config.getString("zzz.akka.avionics.flightcrew.leadAttendantName")

	def startEquipment() {
		val controls = context.actorOf(Props(
			new IsolatedResumeSupervisor with OneForOneStrategyFactory {
				def childStarter() {
					val alt = context.actorOf(Props(newAltimeter), "Altimeter")
					context.actorOf(Props(newAutopilot), "AutoPilot")
					context.actorOf(Props(new ControlSurfaces(alt)), "ControlSurfaces")
				}
			}
		), "Equipment")
	
		Await.result(controls ? WaitForStart, 1.second)
	}

	def startPeople() {
		val plane = self
		val people = context.actorOf(Props(
			new IsolatedStopSupervisor with OneForOneStrategyFactory {
				def childStarter() {
					context.actorOf(Props(newPilot(plane)), pilotName)
					context.actorOf(Props(newCopilot), copilotName)
				}
			}
		),"Pilots")
		context.actorOf(Props(newFlightAttendant), attendantName)

		Await.result(people ? WaitForStart, 1.second)
	}

	def actorForPilots(name: String) = context.actorFor("Pilots/" + name)
	def actorForControls(name: String) = context.actorFor("Equipment/" + name)

	override def preStart() {
		startEquipment()
		startPeople()
		actorForControls("Altimeter") ! RegisterListener(self)
		actorForPilots(pilotName) ! ReadyToGo
	}

	def receive = {
		case GiveMeControl => 
			log.info("give me control")
			sender ! actorForControls("ControlSurfaces")
		case AltitudeUpdate(altitude) =>
			log.info(s"altitude update $altitude")
	}
}


package zzz.akka.avionics

import akka.actor.{Actor, ActorRef, Props}

object FlightAttendantPathChecker {
	
	def main(args: Array[String]) {
		val system = akka.actor.ActorSystem("PlaneSimulation")
		val lead = system.actorOf(
			Props(new LeadFlightAttendant with AttendantCreationPolicy),
			"LeadFlightAttendant"
		)
		Thread.sleep(2000)
		system.shutdown()
	}
}
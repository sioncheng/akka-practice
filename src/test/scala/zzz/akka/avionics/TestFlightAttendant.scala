package zzz.akka.avionics

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.WordSpecLike
import org.scalatest.matchers.MustMatchers

object TestFlightAttendant {
	def apply() = new FlightAttendant with AttendantResponsiveness {
		val maxResponseTimeMS = 1
	}
}

class FlightAttendantSpec extends TestKit(ActorSystem("FlightAttendantSpec"))
	with ImplicitSender with WordSpecLike with MustMatchers {

	//
	import FlightAttendant._

	"FlightAttendant" should {
		"get a drink when asked" in {
			val a = TestActorRef(Props(TestFlightAttendant()))
			a ! GetDrink("Wine")
			expectMsg(Drink("Wine"))
		}
	}
}
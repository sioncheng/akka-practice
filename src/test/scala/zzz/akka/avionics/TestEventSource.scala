package zzz.akka.avionics

import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{TestKit, TestActorRef, ImplicitSender}
import org.scalatest.{WordSpecLike, BeforeAndAfterAll}
import org.scalatest.matchers.MustMatchers

class TestEventSource extends Actor with ProductionEventSource {
	def receive = eventSourceReceive
}

class EventSourceSpec extends TestKit(ActorSystem("EventSourceSpec"))
	with WordSpecLike
	with MustMatchers
	with BeforeAndAfterAll {

	import EventSource._

	override def afterAll() {system.shutdown()}

	"EventSource" should {
		"allow us to register a listener" in {
			val real = TestActorRef[TestEventSource].underlyingActor
			println("............")
			println(testActor)
			real.receive(RegisterListener(testActor))
			real.listeners must contain(testActor)
		}
		"allow us to unregister a listener" in {
			val real = TestActorRef[TestEventSource].underlyingActor
			real.receive(RegisterListener(testActor))
			real.receive(UnregisterListener(testActor))
			real.listeners.size must be (0)
		}
		"send event to our test actor" in {
			val testA = TestActorRef[TestEventSource]
			testA ! RegisterListener(testActor)
			testA.underlyingActor.sendEvent("Fibonacci")
			expectMsg("Fibonacci")
		}
	}
}
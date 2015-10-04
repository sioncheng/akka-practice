package zzz.akka.avionics

import akka.actor.Actor
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import akka.actor.ActorInitializationException
import akka.actor.ActorKilledException

object IsolatedLifeCycleSupervisor {
	case object WaitForStart
	case object Started	
}

trait IsolatedLifeCycleSupervisor extends Actor {
	import IsolatedLifeCycleSupervisor._

	def receive = {
		case WaitForStart => sender ! Started
		case m => throw new Exception(s"don't call ${self.path.name} directly ($m)")
	}

	def childStarter():Unit

	final override def preStart() {childStarter()}

	final override def postRestart(reason:Throwable){}

	final override def preRestart(reason:Throwable, message:Option[Any]){}
}

abstract class IsolatedResumeSupervisor (maxNrRetries:Int = -1, 
	withinTimeRange : Duration = Duration.Inf ) extends IsolatedLifeCycleSupervisor {
	this: SupervisorStrategyFactory => 

	override val supervisorStrategy = makeStrategy(maxNrRetries, withinTimeRange) {
		case _ : ActorInitializationException => Stop
		case _ : ActorKilledException => Stop
		case _ : Exception => Resume
		case _ => Escalate
	}
}

abstract class IsolatedStopSupervisor (maxNrRetries:Int = -1, 
	withinTimeRange: Duration = Duration.Inf) extends IsolatedLifeCycleSupervisor {
	this: SupervisorStrategyFactory =>

	override val supervisorStrategy = makeStrategy(maxNrRetries, withinTimeRange) {
		case _ : ActorInitializationException => Stop
		case _ : ActorKilledException => Stop
		case _ : Exception => Stop
		case _ => Escalate
	}
}


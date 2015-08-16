package zzz.akka.avionics

import akka.actor.{Actor, ActorSystem, Props, ActorLogging}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Altimeter {
	case class RateChange(amount: Float)
	case class AltitudeUpdate(altitude:Double)
}

class Altimeter extends Actor with  ActorLogging with EventSource {
	import Altimeter._

	case object Tick

	val ceiling = 43000
	val maxRateOfClimb = 5000
	var rateOfClimb : Float = 0
	var altitude : Double = 0
	var lastTick = System.currentTimeMillis
	val ticker = context.system.scheduler.schedule(100.millis,100.millis,self,Tick)

	def altimeterReceive : Receive = {
		case RateChange(amount) => 
			rateOfClimb = amount.min(1.0f).max(-1.0f)*maxRateOfClimb
			log.info(s"Altimeter changed rate of climb to $rateOfClimb")
		case Tick => 
			val tick = System.currentTimeMillis
			altitude = altitude + ((lastTick - tick) / 60000.0) * rateOfClimb
			lastTick = tick
			sendEvent(AltitudeUpdate(altitude))
 	}

 	def receive = eventSourceReceive orElse altimeterReceive

 	override def postStop() : Unit = ticker.cancel
}
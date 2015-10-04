package zzz.akka.avionics

import scala.concurrent.duration._
import akka.actor.SupervisorStrategy
import akka.actor.SupervisorStrategy.Decider
import akka.actor.OneForOneStrategy
import akka.actor.AllForOneStrategy

trait SupervisorStrategyFactory {
	def makeStrategy(maxNrRetries: Int,
		withinTimeRange: Duration) (decider: Decider) : SupervisorStrategy 
}

trait OneForOneStrategyFactory extends SupervisorStrategyFactory {
	def makeStrategy(maxNrRetries: Int,
		withinTimeRange: Duration) (decider: Decider) : SupervisorStrategy =
	OneForOneStrategy(maxNrRetries, withinTimeRange)(decider)
}

trait AllForOneStrategyFactory extends SupervisorStrategyFactory {
	def makeStrategy(maxNrRetries: Int,
		withinTimeRange: Duration) (decider: Decider) : SupervisorStrategy = 
	AllForOneStrategy(maxNrRetries, withinTimeRange)(decider)
}
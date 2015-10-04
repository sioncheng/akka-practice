package zzz.akka.network

import akka.actor.{Actor, ActorRef,  ActorLogging, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress
import scala.collection.mutable.Map
import akka.util.Timeout
import scala.concurrent.duration._

object TelnetServer {
	implicit val askTimeout = Timeout(1.second)

	val welcome = 
	"""|Welcome to the Airplane!
	   |------------------------
	   |
	   |Valid commands are: 'heading' and 'altitude'
	   |
	   |------------------------
	   |""".stripMargin

	def ascii(bytes: ByteString): String = {
		bytes.decodeString("UTF-8").trim
	}

	case class SubServerClosed(remote: InetSocketAddress)

	class SubServer(remote: InetSocketAddress, 
		connection: ActorRef, 
		server: ActorRef) extends Actor {
		import Tcp._

		connection !Write(ByteString(welcome))

		def receive = {
			case Received(data) => 
				val msg = ascii(data)
				msg match {
					case "heading" => connection ! Write(ByteString("you just typed heading\n"))
					case "altitude" => connection ! Write(ByteString("you just typed altitude\n"))
					case m => connection ! Write(ByteString("what?\n"))
				}
    		case PeerClosed     => 
    			context stop self
    			server ! SubServerClosed(remote)
		}
	}
}


class TelnetServer extends Actor with ActorLogging {

	import TelnetServer._
	import Tcp._
	import context.system //implicit used by IO(Tcp)
	val manager = IO(Tcp)

	val subservers = Map.empty[InetSocketAddress, ActorRef]
	var clientsNum = 0

	val serverSocket = manager ! Bind(self, new InetSocketAddress("0.0.0.0",33333))

	def receive = {
		case b @ Bound(localAddress) => 
			println("bound")
			println(localAddress)
		case CommandFailed(_ : Bind) => context stop self
		case c @ Connected(remote, local) =>
			println("incoming",remote,local)
			val connection = sender()
			val handler = system.actorOf(Props(new SubServer(remote,connection,self)))
			connection ! Register(handler)
			subservers += (remote -> handler)
			clientsNum = clientsNum + 1
			println("clients", clientsNum)
		case SubServerClosed(remote) =>
			println("closed",remote)
			clientsNum = clientsNum - 1
			println("clients", clientsNum)
	}
}
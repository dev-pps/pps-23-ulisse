package ulisse.applications.ports

import scala.concurrent.Future

object SimulationPorts:
  trait Output:
    def stepNotification(): Unit

  trait Input:
    def start(): Future[Unit]
    def stop(): Future[Unit]
    def reset(): Future[Unit]

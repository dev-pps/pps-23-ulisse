package ulisse.applications.useCases

import ulisse.applications.AppState
import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class SimulationService[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]
) extends SimulationPorts.Input:
  override def start(): Future[Unit] =
    val p = Promise[Unit]()
    p.success(println("Simulation started"))
    p.future
  override def stop(): Future[Unit] =
    val p = Promise[Unit]()
    p.success(println("Simulation stopped"))
    p.future
  override def reset(): Future[Unit] =
    val p = Promise[Unit]()
    p.success(println("Simulation reset"))
    p.future

package ulisse.applications.useCases

import cats.data.NonEmptyChain
import ulisse.applications.{AppState, QueueState}
import ulisse.applications.managers.StationManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class StationService(private val statesQueue: QueueState) extends StationPorts.Input:

  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    statesQueue.offerUpdateStation(stationManager => { p.success(stationManager.stations); stationManager })
    p.future

  override def addStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    statesQueue.offerUpdateStation(stationManager => {
      val updatedMap = stationManager.addStation(station)
      updateState(p, stationManager, updatedMap)
    })
    p.future

  override def removeStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    statesQueue.offerUpdateStation(stationManager => {
      val updatedMap = stationManager.removeStation(station)
      updateState(p, stationManager, updatedMap)
    })
    p.future

  override def updateStation(oldStation: Station, newStation: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    statesQueue.offerUpdateStation(stationManager => {
      val updatedMap = stationManager.removeStation(oldStation).flatMap(_.addStation(newStation))
      updateState(p, stationManager, updatedMap)
    })
    p.future

  override def findStationAt(coordinate: Coordinate): Future[Option[Station]] =
    val p = Promise[Option[Station]]()
    statesQueue.offerUpdateStation(stationManager => {
      val station = stationManager.findStationAt(coordinate)
      p.success(station)
      stationManager
    })
    p.future

  private def updateState(
      p: Promise[Either[E, SM]],
      stationManager: StationManager,
      updatedMap: Either[NonEmptyChain[StationManager.Error], StationManager]
  ) =
    updatedMap match
      case Left(value: E) => p.success(Left(value)); stationManager
      case Right(value) =>
        p.success(Right(value.stations)); value

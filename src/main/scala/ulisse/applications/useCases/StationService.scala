package ulisse.applications.useCases

import cats.data.NonEmptyChain
import ulisse.applications.AppState
import ulisse.applications.managers.StationManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class StationService(
    eventQueue: LinkedBlockingQueue[AppState => AppState]
) extends StationPorts.Input:

  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.add((state: AppState) => { p.success(state.stationManager.stations); state })
    p.future

  override def addStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState) => {
      val updatedMap = state.stationManager.addStation(station)
      updateState(p, state, updatedMap)
    })
    p.future

  override def removeStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState) => {
      val updatedMap = state.stationManager.removeStation(station)
      updateState(p, state, updatedMap)
    })
    p.future

  override def updateStation(oldStation: Station, newStation: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState) => {
      val updatedMap = state.stationManager.removeStation(oldStation).flatMap(_.addStation(newStation))
      updateState(p, state, updatedMap)
    })
    p.future

  override def findStationAt(coordinate: Coordinate): Future[Option[Station]] =
    val p = Promise[Option[Station]]()
    eventQueue.add((state: AppState) => {
      val station = state.stationManager.findStationAt(coordinate)
      p.success(station)
      state
    })
    p.future

  private def updateState(
      p: Promise[Either[E, SM]],
      state: AppState,
      updatedMap: Either[NonEmptyChain[StationManager.Error], StationManager]
  ) =
    updatedMap match
      case Left(value: E) => p.success(Left(value)); state
      case Right(value) =>
        p.success(Right(value.stations)); state.copy(stationManager = value)

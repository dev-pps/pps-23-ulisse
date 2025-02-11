package ulisse.applications.useCases

import cats.data.NonEmptyChain
import ulisse.applications.AppState
import ulisse.applications.managers.CheckedStationManager
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class StationService[S <: Station[?]](
    eventQueue: LinkedBlockingQueue[AppState[S] => AppState[S]]
) extends StationPorts.Input[S]:

  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.add((state: AppState[S]) => { p.success(state.stationManager.stations); state })
    p.future

  override def addStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[S]) => {
      val updatedMap = state.stationManager.addStation(station)
      updateState(p, state, updatedMap)
    })
    p.future

  override def removeStation(station: Station[?]): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[S]) => {
      val updatedMap = state.stationManager.removeStation(station)
      updateState(p, state, updatedMap)
    })
    p.future

  override def updateStation(oldStation: Station[?], newStation: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[S]) => {
      val updatedMap = state.stationManager.removeStation(oldStation).flatMap(_.addStation(newStation))
      updateState(p, state, updatedMap)
    })
    p.future

  override def findStationAt(coordinate: Coordinate[?]): Future[Option[S]] =
    val p = Promise[Option[S]]()
    eventQueue.add((state: AppState[S]) => {
      val station = state.stationManager.findStationAt(coordinate)
      p.success(station)
      state
    })
    p.future

  private def updateState(
      p: Promise[Either[E, SM]],
      state: AppState[S],
      updatedMap: Either[NonEmptyChain[state.stationManager.E], CheckedStationManager[S]]
  ) =
    updatedMap match
      case Left(value: E) => p.success(Left(value)); state
      case Right(value) =>
        p.success(Right(value.stations)); state.copy(stationManager = value)

package ulisse.applications.useCases

import ulisse.applications.AppState
import ulisse.applications.managers.StationManager.CheckedStationManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class StationPortInputService[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]],
    outputPort: StationPorts.Output
) extends StationPorts.Input[N, C, S]:
  type SM = CheckedStationManager[N, C, S]
  type E  = CheckedStationManager.Error

  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.add((state: AppState[N, C, S]) => { p.success(state.stationMap); state })
    p.future

  override def addStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val updatedMap = state.stationMap.addStation(station)
      updateState(p, state, updatedMap)
    })
    p.future

  override def removeStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val updatedMap = state.stationMap.removeStation(station)
      updateState(p, state, updatedMap)
    })
    p.future
  override def findStationAt(coordinate: C): Future[Option[S]] =
    val p = Promise[Option[S]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val station = state.stationMap.findStationAt(coordinate)
      p.success(station)
      state
    })
    p.future

  private def updateState(p: Promise[Either[E, SM]], state: AppState[N, C, S], updatedMap: state.stationMap.R) = {
    p.success(updatedMap)
    updatedMap match
      case Left(value)  => state
      case Right(value) => state.copy(stationMap = value)
  }

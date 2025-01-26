package ulisse.applications.adapters

import ulisse.applications.AppState
import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.applications.useCases.StationManager
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class StationPortInputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]]()(using
    eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]
) extends StationPorts.Input[N, C, S]:
  type SM = CheckedStationMap[N, C, S]
  type E  = CheckedStationMap.Error
  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.add((state: AppState[N, C, S]) => { p.success(state.stationManager.stationMap); state })
    p.future
  override def addStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val updatedMap = state.stationManager.addStation(station)
      p.success(updatedMap)
      state
    })
    p.future
  override def removeStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val updatedMap = state.stationManager.removeStation(station)
      p.success(updatedMap)
      state
    })
    p.future
  override def findStationAt(coordinate: C): Future[Option[S]] =
    val p = Promise[Option[S]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val station = state.stationManager.findStationAt(coordinate)
      p.success(station)
      state
    })
    p.future

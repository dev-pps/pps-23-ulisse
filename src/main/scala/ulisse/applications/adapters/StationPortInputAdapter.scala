package ulisse.applications.adapters

import ulisse.applications.AppState
import ulisse.applications.ports.StationPorts
import ulisse.applications.station.StationMap.CheckedStationMap
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class StationPortInputAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]],
    outputPort: StationPorts.Output
) extends StationPorts.Input[N, C, S]:
  type SM = CheckedStationMap[N, C, S]
  type E  = CheckedStationMap.Error
  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.add((state: AppState[N, C, S]) => { p.success(state.stationMap); state })
    p.future
  override def addStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val updatedMap = state.stationMap.addStation(station)
      p.success(updatedMap)
      updatedMap match
        case Left(value)  => state
        case Right(value) => state.copy(stationMap = value)
    })
    p.future
  override def removeStation(station: S): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.add((state: AppState[N, C, S]) => {
      val updatedMap = state.stationMap.removeStation(station)
      p.success(updatedMap)
      updatedMap match
        case Left(value)  => state
        case Right(value) => state.copy(stationMap = value)
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

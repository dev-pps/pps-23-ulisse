package ulisse.applications.useCases

import ulisse.applications.event.StationEventQueue
import ulisse.applications.managers.StationManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.entities.station.Station

import scala.concurrent.{Future, Promise}

final case class StationService(private val eventQueue: StationEventQueue) extends StationPorts.Input:

  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.addReadStationEvent(stationManager => p.success(stationManager.stations))
    p.future

  override def addStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.addCreateStationEvent(stationManager =>
      val updatedMap = stationManager.addStation(station)
      Services.updateManager(p, stationManager, updatedMap, _.stations)
    )
    p.future

  override def removeStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.addDeleteStationEvent((stationManager, routeManager, timetableManager) =>
      val updatedMap = stationManager.removeStation(station)
      (Services.updateManager(p, stationManager, updatedMap, _.stations), routeManager, timetableManager)
    )
    p.future

  override def updateStation(oldStation: Station, newStation: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.addUpdateStationEvent((stationManager, routeManager, timetableManager) =>
      val updatedMap = stationManager.removeStation(oldStation).flatMap(_.addStation(newStation))
      (Services.updateManager(p, stationManager, updatedMap, _.stations), routeManager, timetableManager)
    )
    p.future

  override def findStationAt(coordinate: Coordinate): Future[Option[Station]] =
    val p = Promise[Option[Station]]()
    eventQueue.addReadStationEvent(stationManager =>
      val station = stationManager.findStationAt(coordinate)
      p.success(station)
    )
    p.future

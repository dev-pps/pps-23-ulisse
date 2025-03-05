package ulisse.applications.useCases

import cats.data.{Chain, NonEmptyChain}
import ulisse.applications.event.StationEventQueue
import ulisse.applications.managers.StationManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.Route
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

  override def removeStation(station: Station): Future[Either[E, (SM, List[Route])]] =
    val p = Promise[Either[E, (SM, List[Route])]]()
    eventQueue.addDeleteStationEvent((stationManager, routeManager, timetableManager) =>
      val updatedMap    = stationManager.removeStation(station)
      val updatedRoutes = routeManager.deleteByStation(station)
      (updatedMap, updatedRoutes) match
        case (Left(error), _) => p.success(Left(error)); (stationManager, routeManager, timetableManager)
        case (Right(newMap), Left(_)) =>
          p.success(Right((newMap.stations, routeManager.routes))); (newMap, routeManager, timetableManager)
        case (Right(newMap), Right(routes)) =>
          p.success(Right((newMap.stations, routes.routes))); (newMap, routes, timetableManager)
    )
    p.future

  override def updateStation(oldStation: Station, newStation: Station): Future[Either[E, (SM, List[Route])]] =
    val p = Promise[Either[E, (SM, List[Route])]]()
    eventQueue.addUpdateStationEvent((stationManager, routeManager, timetableManager) =>
      val updatedMap    = stationManager.removeStation(oldStation).flatMap(_.addStation(newStation))
      val updatedRoutes = routeManager.modifyAutomaticByStation(oldStation, newStation)
      updatedMap match
        case Left(error) => p.success(Left(error)); (stationManager, routeManager, timetableManager)
        case Right(newMap) =>
          p.success(Right((newMap.stations, updatedRoutes.routes))); (newMap, updatedRoutes, timetableManager)
    )
    p.future

  override def findStationAt(coordinate: Coordinate): Future[Option[Station]] =
    val p = Promise[Option[Station]]()
    eventQueue.addReadStationEvent(stationManager =>
      val station = stationManager.findStationAt(coordinate)
      p.success(station)
    )
    p.future

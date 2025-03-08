package ulisse.applications.useCases

import ulisse.applications.event.StationEventQueue
import ulisse.applications.event.StationEventQueue.StationManagers
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.{StationManager, TimetableManagers}
import ulisse.applications.ports.StationPorts
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station

import scala.concurrent.{Future, Promise}

/** Service for station. */
final case class StationService(private val eventQueue: StationEventQueue) extends StationPorts.Input:
  override def stationMap: Future[SM] =
    val p = Promise[SM]()
    eventQueue.addReadStationManagerEvent(stationManager => p.success(stationManager.stations))
    p.future

  override def addStation(station: Station): Future[Either[E, SM]] =
    val p = Promise[Either[E, SM]]()
    eventQueue.addUpdateStationManagerEvent: stationManager =>
      Services.updateManager(p, stationManager, stationManager.addStation(station), _.stations)
    p.future

  override def removeStation(station: Station): Future[Either[E, (SM, List[Route])]] =
    val p = Promise[Either[E, (SM, List[Route])]]()
    eventQueue.addUpdateStationManagersEvent: managers =>
      val StationManagers(sm, rm, tm) = managers
      sm.removeStation(station) match
        case Left(error) => p.success(Left(error)); managers
        case Right(newSM) =>
          val newRMOpt = rm.deleteByStation(station)
          val newRM    = newRMOpt.getOrElse(rm)
          p.success(Right((newSM.stations, newRM.routes)));
          managers.copy(newSM, newRM, newRMOpt.map(tm.notifyRemove(rm, _)).getOrElse(tm))
    p.future

  override def updateStation(oldStation: Station, newStation: Station): Future[Either[E, (SM, List[Route])]] =
    val p = Promise[Either[E, (SM, List[Route])]]()
    eventQueue.addUpdateStationManagersEvent: managers =>
      val StationManagers(sm, rm, tm) = managers
      sm.updateStation(oldStation, newStation) match
        case Left(error) => p.success(Left(error)); managers
        case Right(newSM) =>
          val newRM = rm.modifyAutomaticByStation(oldStation, newStation)
          p.success(Right((newSM.stations, newRM.routes))); managers.copy(newSM, newRM, tm.notifyUpdate(rm, newRM))
    p.future

  override def findStationAt(coordinate: Coordinate): Future[Option[Station]] =
    val p = Promise[Option[Station]]()
    eventQueue.addReadStationManagerEvent(stationManager => p.success(stationManager.findStationAt(coordinate)))
    p.future

  extension (timetableManager: TimetableManager)
    private def notifyUpdate(oldRouteManager: RouteManager, newRouteManager: RouteManager): TimetableManager =
      val (l1, l2) = oldRouteManager.diff(newRouteManager)
      l1.zip(l2).foldLeft(timetableManager): (tm, rr) =>
        timetableManager.routeUpdated(rr._1, rr._2).getOrElse(timetableManager)

    private def notifyRemove(oldRouteManager: RouteManager, newRouteManager: RouteManager): TimetableManager =
      oldRouteManager.diff(newRouteManager)._1.foldLeft(timetableManager): (tm, r) =>
        timetableManager.routeDeleted(r).getOrElse(timetableManager)

  extension (routeManager: RouteManager)
    private def diff(otherRouteManager: RouteManager): (List[Route], List[Route]) =
      val intersection = routeManager.routes.intersect(otherRouteManager.routes)
      (routeManager.routes.diff(intersection), otherRouteManager.routes.diff(intersection))

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
      (managers.stationManager.removeStation(station), managers.routeManager.deleteByStation(station)) match
        case (Left(error), _) => p.success(Left(error)); managers
        case (Right(newSM), Left(_)) =>
          p.success(Right((newSM.stations, managers.routeManager.routes))); managers.copy(newSM)
        case (Right(newSM), Right(newRM)) =>
          p.success(Right((newSM.stations, newRM.routes))); managers.copy(newSM, newRM)
    p.future

  override def updateStation(oldStation: Station, newStation: Station): Future[Either[E, (SM, List[Route])]] =
    val p = Promise[Either[E, (SM, List[Route])]]()
    eventQueue.addUpdateStationManagersEvent: managers =>
      managers.stationManager.removeStation(oldStation).flatMap(_.addStation(newStation)) match
        case Left(error) => p.success(Left(error)); managers
        case Right(newSM) =>
          val newRM = managers.routeManager.modifyAutomaticByStation(oldStation, newStation)
          p.success(Right((newSM.stations, newRM.routes))); managers.copy(newSM, newRM)
    p.future

  override def findStationAt(coordinate: Coordinate): Future[Option[Station]] =
    val p = Promise[Option[Station]]()
    eventQueue.addReadStationManagerEvent(stationManager => p.success(stationManager.findStationAt(coordinate)))
    p.future

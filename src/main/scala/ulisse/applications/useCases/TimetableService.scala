package ulisse.applications.useCases

import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.{TimetableManager, TimetableManagerErrors}
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.{
  GenericError,
  InvalidStationSelection,
  OverlapError,
  TrainTablesNotExist
}
import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, TimetableServiceErrors}
import ulisse.entities.Technology
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{PartialTimetable, TrainTimetable}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.Times.ClockTime

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

type Route    = MockRoutesService.Route
type StationT = Station[_]

trait AppStateTimetable:
  def trainManager: TrainManager
  def timetableManager: TimetableManager
  def timetableManagerUpdate(timetableManager: TimetableManager): AppStateTimetable
  def stationManager: StationManager[StationT]

object MockRoutesService: // after remove that
  case class Route(startStationName: String, endStationName: String, technology: Technology)
  def routes: List[Route] = List(
    Route("A", "B", Technology("AV", maxSpeed = 300)),
    Route("A", "B1", Technology("AV", maxSpeed = 300)),
    Route("A", "B2", Technology("NORMAL", maxSpeed = 300)),
    Route("B", "C", Technology("AV", maxSpeed = 130)),
    Route("C", "D", Technology("NORMAL", maxSpeed = 130)),
    Route("C", "D1", Technology("magnetic", maxSpeed = 500))
  )

//----------------------------------------------------------------------
/** Timetable service that jobs is like gatekeeper.
  * @param stateEventQueue
  *   Shared application state queue
  */
final case class TimetableService(stateEventQueue: LinkedBlockingQueue[AppStateTimetable => AppStateTimetable])
    extends TimetablePorts.Input:

  private case class ValidRoute(stations: (StationT, StationT), waitTime: Option[Int], speedLimit: Int)

  extension (toCheck: List[((StationT, Option[Int]), (StationT, Option[Int]))])
    /** @param existingRoutes
      *   Route and technology actually saved
      * @return
      *   Returns list of ValidRoute if sequence of Stations name is valid
      */
    private def validateStations(existingRoutes: Map[(StationId, StationId), Technology]): Option[List[ValidRoute]] =
      val routeStations = existingRoutes.keys.toList
      val isValid = toCheck.corresponds(routeStations)((st, route) =>
        val station1 = st._1._1
        val station2 = st._2._1
        (route._1 == station1.name && route._2 == station2.name) ||
        (route._1 == station2.name && route._2 == station1.name)
      )
      Option.when(isValid):
        toCheck.flatMap { (station1, station2) =>
          existingRoutes.collectFirst {
            case route
                if route._1 == (station1._1.name, station2._1.name) || route._1 == (station2._1.name, station1._1.name) =>
              ValidRoute((station1._1, station2._1), station2._2, route._2.maxSpeed)
          }
        }

  extension (routes: List[Route])
    /** Extracts needed info from a List of Route
      * @return
      *   Map with tuple of name stations as key and Technology as value
      */
    private def extractRouteInfo: Map[(StationId, StationId), Technology] =
      routes.map(r =>
        val techType = r.technology
        val st1      = r.startStationName
        val st2      = r.endStationName
        ((st1, st2), techType)
      ).toMap

  extension (stationsName: List[(StationId, Option[Int])])
    private def namesToEntities(stationsMap: List[StationT]): Option[List[(StationT, Option[Int])]] =
      stationsName.map {
        case (name, waitTime) =>
          stationsMap.find(_.name == name).map(station => (station, waitTime))
      }.foldLeft[Option[List[(StationT, Option[Int])]]](Some(List.empty)) {
        case (Some(acc), Some(item)) => Some(item :: acc)
        case _                       => None
      }.map(_.reverse)

  def createTimetable(
      trainName: String,
      departureTime: ClockTime,
      stations: List[(StationId, Option[Int])]
  ): Future[RequestResult] = stateEventQueue.updateWith: (state, promise) =>
    // Maps routes (not mine) into a form that I need
    val savedRoutes: Map[(StationId, StationId), Technology] =
      MockRoutesService.routes.extractRouteInfo // here i must use then state.routeManager.routes
    val savedStations = state.stationManager.stations
    val savedTrains   = state.trainManager.trains
    for
      timetable <-
        // TODO: pass saved stations
        buildTimetable(trainName, departureTime, stations)(savedTrains, List.empty /*savedStations*/, savedRoutes)
      newManager    <- state.timetableManager.save(timetable)
      updatedTables <- newManager.tablesOf(trainName)
    yield
      promise.success(Right(updatedTables))
      newManager

  private def buildTimetable(
      trainName: String,
      departureTime: ClockTime,
      usrStations: List[(StationId, Option[Int])]
  )(
      trains: List[Train],
      stations: List[StationT],
      routes: Map[(StationId, StationId), Technology]
  ): Either[BaseError, TrainTimetable] =
    // TODO: use cats validated (chain of errors) to provide user all errors that occured during timetable creation
    for
      train           <- trains.find(_.name == trainName).toRight(GenericError(s"Train $trainName not found"))
      stationEntities <- usrStations.namesToEntities(stations).toRight(GenericError(s"some station not found"))
      _ <- stationEntities.zip(stationEntities.drop(1))
        .validateStations(routes)
        .toRight(InvalidStationSelection("some station not exists"))
      startFrom        <- stationEntities.headOption.toRight(GenericError(s"some station not found"))
      arriveTo         <- stationEntities.lastOption.toRight(GenericError(s"some station not found"))
      partialTimetable <- PartialTimetable(train, startFrom._1, Right(departureTime))
    yield stationEntities.drop(1).foldLeft(partialTimetable) {
      case (timetable, (station, Some(waitTime))) => timetable.stopsIn(station, waitTime)
      case (timetable, (station, None))           => timetable.transitIn(station)
    }.arrivesTo(arriveTo._1)

  def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] =
    stateEventQueue.updateWith: (state, promise) =>
      for
        newManager <-
          state.timetableManager.remove(trainName, departureTime)
        tablesList <- newManager.tablesOf(trainName)
      yield
        promise.success(Right(tablesList))
        newManager

  def timetableOf(trainName: String): Future[RequestResult] =
    stateEventQueue.updateWith: (state, promise) =>
      for
        res <- state.timetableManager.tablesOf(trainName)
      yield
        promise.success(Right(res))
        state.timetableManager

  extension (error: BaseError)
    /** Error converter: from [[BaseError]] errors to exposed service ones [[TimetableServiceErrors]]
      *
      * @return
      *   `TimetableServiceErrors`, service error.
      */
    private def toServiceError: TimetableServiceErrors =
      error match
        case TimetableManagerErrors.AcceptanceError(reason)      => OverlapError(reason)
        case TimetableManagerErrors.TimetableNotFound(trainName) => TrainTablesNotExist(trainName)
        case _                                                   => GenericError("Unknown error")

  extension (queue: LinkedBlockingQueue[AppStateTimetable => AppStateTimetable])
    private def updateWith(f: (AppStateTimetable, Promise[RequestResult]) => Either[BaseError, TimetableManager])
        : Future[RequestResult] =
      val promise = Promise[RequestResult]
      queue.offer(state =>
        f(state, promise).fold(
          err =>
            promise.success(Left(err.toServiceError))
            state
          ,
          manager => state.timetableManagerUpdate(manager)
        )
      )
      promise.future

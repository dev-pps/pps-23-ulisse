package ulisse.applications.useCases

import ulisse.applications.event.TimeTableEventQueue
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.{TimetableManager, TimetableManagerErrors}
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.{
  GenericError,
  InvalidStation,
  OverlapError,
  TrainTablesNotExist,
  UnavailableTracks
}
import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, TimetableServiceErrors, WaitingTime}
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.Times.ClockTime

import scala.concurrent.{Future, Promise}

/** Timetable service that jobs is like gatekeeper and enqueue edits to app state on `eventQueue` queue. */
final case class TimetableService(eventQueue: TimeTableEventQueue) extends TimetablePorts.Input:

  extension (toCheck: List[(StationId, WaitingTime)])
    /** Returns a list of pair `(Route, WaitingTime)` starting from `toCheck` list.
      * If some route does not exist `InvalidStation` error is returned.
      */
    private def toRoutes(existingRoutes: List[Route]): Either[InvalidStation, List[(Route, WaitingTime)]] =
      extension (route: Route)
        private def hasStationNames(a: String, b: String): Boolean =
          val arrivalName   = route.arrival.name
          val departureName = route.departure.name
          (arrivalName == a && departureName == b) ||
          (arrivalName == b && departureName == a)

      val userStationsPair = toCheck.zip(toCheck.drop(1))
      val routesFound =
        userStationsPair.flatMap((departure, arriving) =>
          existingRoutes.find(_ hasStationNames (departure._1, arriving._1)).map(r => (r, arriving._2))
        )
      if routesFound.sizeIs == userStationsPair.size then
        Right(routesFound)
      else Left(InvalidStation(s"Invalid station sequence: some route not exists"))

  extension (routesWaiting: List[(Route, WaitingTime)])
    private def checkAvailableTracks(
        station: Station,
        departureTime: ClockTime,
        timetables: Seq[Timetable]
    ): Either[UnavailableTracks, List[(Route, WaitingTime)]] =
      val occupiedTracks =
        timetables.filter(t => t.startStation.name == station.name && t.departureTime == departureTime)
      Either.cond(occupiedTracks.sizeIs < station.numberOfPlatforms, routesWaiting, UnavailableTracks(station.name))

  /** Given `trainName`, `departureTime` and stations with its waitingTime a new Timetable should be saved.
    * Returns a `TimetableServiceErrors` in case of error during creation.
    */
  def createTimetable(
      trainName: String,
      departureTime: ClockTime,
      stations: List[(StationId, WaitingTime)]
  ): Future[RequestResult] =
    eventQueue.updateWith: (managers, promise) =>
      val (stationManager, routeManager, trainManager, timetableManager) = managers
      val savedRoutes: List[Route]                                       = routeManager.routes
      val savedTrains                                                    = trainManager.trains
      val savedTimetables                                                = timetableManager.tables
      for
        timetable     <- buildTimetable(trainName, departureTime, stations)(savedTrains, savedRoutes, savedTimetables)
        newManager    <- timetableManager.save(timetable)
        updatedTables <- newManager.tablesOf(trainName)
      yield
        promise.success(Right(updatedTables))
        newManager

  private def buildTimetable(
      trainName: String,
      departureTime: ClockTime,
      usrStations: List[(StationId, WaitingTime)]
  )(
      trains: List[Train],
      routes: List[Route],
      timetables: Seq[Timetable]
  ): Either[BaseError, Timetable] =
    for
      train         <- trains.find(_.name == trainName).toRight(GenericError(s"Train $trainName not found"))
      routesWaiting <- usrStations.toRoutes(routes)
      startFrom     <- routesWaiting.headOption.toRight(InvalidStation("start station not found"))
      _             <- routesWaiting.checkAvailableTracks(startFrom._1.departure, departureTime, timetables)
      arriveTo      <- routesWaiting.lastOption.toRight(InvalidStation("arriving station not found"))
    yield routesWaiting.foldLeft(TimetableBuilder(train, startFrom._1.departure, departureTime)) {
      case (timetable, (route, Some(waitTime))) =>
        timetable.stopsIn(route.arrival, waitTime)(RailInfo(route.length, route.typology))
      case (timetable, (route, None)) => timetable.transitIn(route.arrival)(RailInfo(route.length, route.typology))
    }.arrivesTo(arriveTo._1.arrival)(RailInfo(arriveTo._1.length, arriveTo._1.typology))

  /** Given `trainName` and `departureTime` if timetable exist then is deleted, otherwise an `TrainTablesNotExist` error. */
  def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] =
    eventQueue.updateWith: (managers, promise) =>
      val (stationManager, routeManager, trainManager, timetableManager) = managers
      for
        newManager <- timetableManager.remove(trainName, departureTime)
        tablesList <- newManager.tablesOf(trainName)
      yield
        promise.success(Right(tablesList))
        newManager

  /** Returns list of all Timetable saved for a given `trainName` */
  def timetablesOf(trainName: String): Future[RequestResult] =
    val promise = Promise[RequestResult]()
    eventQueue.addReadTimetableEvent(timetableManager =>
      for res <- timetableManager.tablesOf(trainName)
      yield promise.success(Right(res))
    )
    promise.future

  extension (error: BaseError)
    /** Returns `TimetableServiceErrors` starting from any error that is a `BaseError` errors. */
    private def toServiceError: TimetableServiceErrors =
      error match
        case TimetableManagerErrors.AcceptanceError(reason)      => OverlapError(reason)
        case TimetableManagerErrors.TimetableNotFound(trainName) => TrainTablesNotExist(trainName)
        case e: TimetableServiceErrors                           => e
        case _                                                   => GenericError("Unknown error")

  extension (eventQueue: TimeTableEventQueue)
    private def updateWith(f: (
        (StationManager, RouteManager, TrainManager, TimetableManager),
        Promise[RequestResult]
    ) => Either[BaseError, TimetableManager]): Future[RequestResult] =
      val promise = Promise[RequestResult]

      eventQueue.addUpdateTimetableEvent((stationManager, routeManager, trainManager, timetableManager) =>
        f((stationManager, routeManager, trainManager, timetableManager), promise) match
          case Left(error) =>
            promise.success(Left(error.toServiceError))
            (stationManager, routeManager, trainManager, timetableManager)
          case Right(newManager) =>
            (stationManager, routeManager, trainManager, newManager)
      )
      promise.future

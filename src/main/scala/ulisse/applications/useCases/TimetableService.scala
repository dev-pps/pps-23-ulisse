package ulisse.applications.useCases

import ulisse.applications.managers.TimetableManagers.{TimetableManager, TimetableManagerErrors}
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.{GenericError, OverlapError, TrainTablesNotExist}
import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, TimetableServiceErrors, WaitingTime}
import ulisse.entities.Routes.Route
import ulisse.entities.timetable.MockedEntities.AppStateTimetable
import ulisse.entities.timetable.Timetables.{PartialTimetable, RailInfo, TrainTimetable}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Errors.BaseError
import ulisse.utils.Times.ClockTime

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

//----------------------------------------------------------------------
/** Timetable service that jobs is like gatekeeper and enqueue edits to app state using `stateEventQueue` queue. */
final case class TimetableService(stateEventQueue: LinkedBlockingQueue[AppStateTimetable => AppStateTimetable])
    extends TimetablePorts.Input:
  extension (toCheck: List[(StationId, WaitingTime)])
    /** @param existingRoutes
      *   Route and technology actually saved
      * Returns a list of pair `(Route, WaitingTime)` starting from `toCheck` list. If some route does not exist `None` is returned.
      */
    private def validateStations(existingRoutes: List[Route]): Option[List[(Route, WaitingTime)]] =
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

      Option.when(routesFound.sizeIs == userStationsPair.size):
        routesFound

  /** Given `trainName`, `departureTime` and stations with its waitingTime a new TrainTimetable should be saved.
    * Returns a `TimetableServiceErrors` in case of error during creation.
    */
  def createTimetable(
      trainName: String,
      departureTime: ClockTime,
      stations: List[(StationId, WaitingTime)]
  ): Future[RequestResult] = stateEventQueue.updateWith: (state, promise) =>
    val savedRoutes: List[Route] = state.routeManager.routes
    val savedTrains              = state.trainManager.trains
    for
      timetable     <- buildTimetable(trainName, departureTime, stations)(savedTrains, savedRoutes)
      newManager    <- state.timetableManager.save(timetable)
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
      routes: List[Route]
  ): Either[BaseError, TrainTimetable] =
    for
      train            <- trains.find(_.name == trainName).toRight(GenericError(s"Train $trainName not found"))
      stationEntities  <- usrStations.validateStations(routes).toRight(GenericError(s"some route not exists"))
      startFrom        <- stationEntities.headOption.toRight(GenericError(s"start station not found"))
      arriveTo         <- stationEntities.lastOption.toRight(GenericError(s"arriving station not found"))
      partialTimetable <- PartialTimetable(train, startFrom._1.departure, Right(departureTime))
    yield stationEntities.foldLeft(partialTimetable) {
      case (timetable, (route, Some(waitTime))) =>
        timetable.stopsIn(route.arrival, waitTime)(RailInfo(route.length, route.typology))
      case (timetable, (route, None)) => timetable.transitIn(route.arrival)(RailInfo(route.length, route.typology))
    }.arrivesTo(arriveTo._1.arrival)(RailInfo(arriveTo._1.length, arriveTo._1.typology))

  /** Given `trainName` and `departureTime` if timetable exist then is deleted, otherwise an `TrainTablesNotExist` error. */
  def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] =
    stateEventQueue.updateWith: (state, promise) =>
      for
        newManager <- state.timetableManager.remove(trainName, departureTime)
        tablesList <- newManager.tablesOf(trainName)
      yield
        promise.success(Right(tablesList))
        newManager

  /** Returns list of all TrainTimetable saved for a given `trainName` */
  def timetableOf(trainName: String): Future[RequestResult] =
    stateEventQueue.updateWith: (state, promise) =>
      for
        res <- state.timetableManager.tablesOf(trainName)
      yield
        promise.success(Right(res))
        state.timetableManager

  extension (error: BaseError)
    /** Returns `TimetableServiceErrors` starting from any error that is a `BaseError` errors. */
    private def toServiceError: TimetableServiceErrors =
      error match
        case TimetableManagerErrors.AcceptanceError(reason)      => OverlapError(reason)
        case TimetableManagerErrors.TimetableNotFound(trainName) => TrainTablesNotExist(trainName)
        case GenericError(msg)                                   => GenericError(msg)
        case _                                                   => GenericError("Unknown error")

  extension (queue: LinkedBlockingQueue[AppStateTimetable => AppStateTimetable])
    private def updateWith(f: (AppStateTimetable, Promise[RequestResult]) => Either[BaseError, TimetableManager])
        : Future[RequestResult] =
      val promise = Promise[RequestResult]
      queue.offer: state =>
        f(state, promise).fold(
          err =>
            promise.success(Left(err.toServiceError))
            state
          ,
          manager => state.timetableManagerUpdate(manager)
        )
      promise.future

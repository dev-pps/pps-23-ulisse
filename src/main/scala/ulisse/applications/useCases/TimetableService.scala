//package ulisse.applications.useCases
//
//import ulisse.applications.managers.TimetableManagers.{TimetableManager, TimetableManagerErrors}
//import ulisse.applications.managers.TrainManagers.TrainManager
//import ulisse.applications.ports.TimetablePorts
//import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.{
//  GenericError,
//  InvalidStationSelection,
//  OverlapError,
//  TrainTablesNotExist
//}
//import ulisse.applications.ports.TimetablePorts.{RequestResult, StationId, TimetableServiceErrors}
//import ulisse.entities.Routes.Route
//import ulisse.entities.Technology
//import ulisse.entities.station.Station
//import ulisse.entities.timetable.MockedEntities.AppStateTimetable
//import ulisse.entities.timetable.Timetables.{PartialTimetable, TrainTimetable}
//import ulisse.entities.train.Trains.Train
//import ulisse.utils.Errors.BaseError
//import ulisse.utils.Times.ClockTime
//
//import java.util.concurrent.LinkedBlockingQueue
//import scala.concurrent.{Future, Promise}
//
////----------------------------------------------------------------------
///** Timetable service that jobs is like gatekeeper.
//  * @param stateEventQueue
//  *   Shared application state queue
//  */
//final case class TimetableService(stateEventQueue: LinkedBlockingQueue[AppStateTimetable => AppStateTimetable])
//    extends TimetablePorts.Input:
//
//  private case class ValidRoute(stations: (Station, Station), waitTime: Option[Int], speedLimit: Int)
//
//  extension (toCheck: List[(StationId, Option[Int])])
//    /** @param existingRoutes
//      *   Route and technology actually saved
//      * @return
//      *   Returns list of ValidRoute if sequence of Stations name is valid
//      */
//    private def validateStations(existingRoutes: List[Route]): Option[List[(Route, Option[Int])]] =
//      def routeExists(st1: String, st2: String)(route: Route): Boolean =
//        val arrivalName   = route.arrival.name
//        val departureName = route.departure.name
//        (departureName == st1 && arrivalName == st2) ||
//        (arrivalName == st2 && departureName == st1)
//
//      val isValid = toCheck.zip(toCheck.drop(1)).corresponds(existingRoutes)((s, route) =>
//        val startStationName = s._1._1
//        val finalStationName = s._2._1
//        routeExists(startStationName, finalStationName)(route)
//      )
//      Option.when(isValid):
//        toCheck.zip(toCheck.drop(1)).flatMap { (station1, station2) =>
//          existingRoutes.collectFirst {
//            case route
//                if routeExists(station1._1, station2._1)(route) => (route, station2._2)
//          }
//        }
//
//  def createTimetable(
//      trainName: String,
//      departureTime: ClockTime,
//      stations: List[(StationId, Option[Int])]
//  ): Future[RequestResult] = stateEventQueue.updateWith: (state, promise) =>
//    val savedRoutes: List[Route] = state.routeManager.routes
//    val savedTrains              = state.trainManager.trains
//    for
//      timetable     <- buildTimetable(trainName, departureTime, stations)(savedTrains, savedRoutes)
//      newManager    <- state.timetableManager.save(timetable)
//      updatedTables <- newManager.tablesOf(trainName)
//    yield
//      promise.success(Right(updatedTables))
//      newManager
//
//  def buildTimetable(
//      trainName: String,
//      departureTime: ClockTime,
//      usrStations: List[(StationId, Option[Int])]
//  )(
//      trains: List[Train],
//      routes: List[Route]
//  ): Either[BaseError, TrainTimetable] =
//    // given train name, departureTime, and sequence of route an related wait time  ==> create timetable
//    // TODO: use cats validated (chain of errors) to provide user all errors that occured during timetable creation
//    for
//      train            <- trains.find(_.name == trainName).toRight(GenericError(s"Train $trainName not found"))
//      stationEntities  <- usrStations.validateStations(routes).toRight(GenericError(s"some station not found"))
//      startFrom        <- stationEntities.headOption.toRight(GenericError(s"start station not found"))
//      arriveTo         <- stationEntities.lastOption.toRight(GenericError(s"arriving station not found"))
//      partialTimetable <- PartialTimetable(train, startFrom._1, Right(departureTime))
//    yield stationEntities.drop(1).foldLeft(partialTimetable) {
//      case (timetable, (route, Some(waitTime))) => timetable.stopsIn(route.arrival, waitTime)
//      case (timetable, (route, None))           => timetable.transitIn(route.arrival)
//    }.arrivesTo(arriveTo._1.arrival)
//
//  def deleteTimetable(trainName: String, departureTime: ClockTime): Future[RequestResult] =
//    stateEventQueue.updateWith: (state, promise) =>
//      for
//        newManager <-
//          state.timetableManager.remove(trainName, departureTime)
//        tablesList <- newManager.tablesOf(trainName)
//      yield
//        promise.success(Right(tablesList))
//        newManager
//
//  def timetableOf(trainName: String): Future[RequestResult] =
//    stateEventQueue.updateWith: (state, promise) =>
//      for
//        res <- state.timetableManager.tablesOf(trainName)
//      yield
//        promise.success(Right(res))
//        state.timetableManager
//
//  extension (error: BaseError)
//    /** Error converter: from [[BaseError]] errors to exposed service ones [[TimetableServiceErrors]]
//      *
//      * @return
//      *   `TimetableServiceErrors`, service error.
//      */
//    private def toServiceError: TimetableServiceErrors =
//      error match
//        case TimetableManagerErrors.AcceptanceError(reason)      => OverlapError(reason)
//        case TimetableManagerErrors.TimetableNotFound(trainName) => TrainTablesNotExist(trainName)
//        case _                                                   => GenericError("Unknown error")
//
//  extension (queue: LinkedBlockingQueue[AppStateTimetable => AppStateTimetable])
//    private def updateWith(f: (AppStateTimetable, Promise[RequestResult]) => Either[BaseError, TimetableManager])
//        : Future[RequestResult] =
//      val promise = Promise[RequestResult]
//      queue.offer(state =>
//        f(state, promise).fold(
//          err =>
//            promise.success(Left(err.toServiceError))
//            state
//          ,
//          manager => state.timetableManagerUpdate(manager)
//        )
//      )
//      promise.future

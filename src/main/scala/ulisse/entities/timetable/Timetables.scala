package ulisse.entities.timetable

import ulisse.entities.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.timetable.ScheduleTime.{AutoScheduleTime, EndScheduleTime, StartScheduleTime}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.{ClockTime, ClockTimeErrors}
import ulisse.utils.Times.FluentDeclaration.h

import scala.collection.immutable.ListMap

/** Object containing traits and utility method for creation and use of TrainTimetable */
object Timetables:

  private type Time     = ScheduleTime
  private type WaitTime = Int
  import ulisse.entities.timetable.MockedEntities.*

  extension (i: Int)
    def toWaitTime: WaitTime = i

  trait TimeEstimator:
    def ETA(lastTime: Time, route: Route, train: Train): Option[ClockTime]

  private object UnrealTimeEstimator extends TimeEstimator:
    def ETA(prevTime: Time, curRoute: Route, train: Train): Option[ClockTime] =
      for
        distance         <- Some(curRoute.length)
        travelMinutes    <- Some((distance / Math.min(train.maxSpeed, curRoute.technology.maxSpeed)) * 60)
        prevArrivingTime <- prevTime.arriving
        nextArrivingTime <- h(prevArrivingTime.h).m(prevArrivingTime.m + travelMinutes.toInt).toOption
      yield nextArrivingTime

  /** Default time estimator just considers distance between last station and the given one and train speed. No
    * acceleration and acceleration are considered.
    */
  given defaultTimeEstimator: TimeEstimator = UnrealTimeEstimator

  trait Timetable:
    def train: Train
    def departureTime: ClockTime
    def startStation: Option[Station]
    def table: ListMap[Route, Time]

  trait TrainTimetable extends Timetable:
    def arrivingStation: Option[Station]
    def arrivingTime: Option[ClockTime]
    def stopStations: List[Route]    = table.filter(_._2.waitTime.nonEmpty).keys.toList
    def transitStations: List[Route] = table.filter(_._2.waitTime.isEmpty).keys.toList
    def routes: List[Route]          = table.keys.toList

  // define train timetable
  extension (trainUsed: Train)
    def at(departureTime: ClockTime): DepartRouteBuilder =
      DepartRouteBuilder(trainUsed, departureTime, ListMap.empty)

  case class DepartRouteBuilder(
      train: Train,
      departureTime: ClockTime,
      table: ListMap[Route, Time]
  )(using timeEstimationStrategy: TimeEstimator):
    def travelsOn(r: Route): RouteTableBuilder =
      // TODO save start scheduletime
      RouteTableBuilder(train, departureTime, table)

  case class RouteTableBuilder(
      train: Train,
      departureTime: ClockTime,
      table: ListMap[Route, Time]
  )(using timeEstimationStrategy: TimeEstimator):
    def travelsOn(route: Route): Option[RouteTableBuilder] =
      for
        lastRoute  <- table.lastOption
        arriveTime <- timeEstimationStrategy.ETA(lastRoute._2, route, train)
      yield
        val scheduleTime = AutoScheduleTime(arriveTime, None) // Todo: actually do not consider waits in station
        this.copy(table = table.updated(route, scheduleTime))

  extension (rb: Option[RouteTableBuilder])
    def travelsOn(route: Route)(using timeEstimationStrategy: TimeEstimator): Option[RouteTableBuilder] =
      for
        routeBuilder <- rb
        lastRoute    <- routeBuilder.table.lastOption
        arriveTime   <- timeEstimationStrategy.ETA(lastRoute._2, route, routeBuilder.train)
      yield
        val scheduleTime = AutoScheduleTime(arriveTime, None) // Todo: actually do not consider waits in station
        routeBuilder.copy(table = routeBuilder.table.updated(route, scheduleTime))

  extension (rt: Option[RouteTableBuilder])
    def asFinalRoute: Option[TrainTimetable] =
      rt.map(t => CompleteTimetable(t.train, t.departureTime, t.table))

  case class CompleteTimetable(train: Train, departureTime: ClockTime, table: ListMap[Route, Time])
      extends TrainTimetable:
    override def arrivingStation: Option[Station] = table.lastOption.map((r, _) => r.arrival)
    override def arrivingTime: Option[ClockTime]  = table.lastOption.flatMap((_, t) => t.departure)
    override def startStation: Option[Station]    = table.headOption.map((r, _) => r.departure)

//
//      override def transitIn(route: Route): PartialTimetable =
//    insertRoute(route, AutoScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, route, train), None))
//
//      override def arrivesTo(route: Route): TrainTimetable =
//        TrainTimetableImpl(
//          insertRoute(route, EndScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, route, train))),
//          route.arrival
//        )
//
//      private def insertRoute(route: Route, time: Time): PartialTimetable =
//        this.copy(table = table.updated(route, time))
//

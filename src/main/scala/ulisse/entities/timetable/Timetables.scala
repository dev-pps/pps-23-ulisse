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

  extension (t: TrainTimetable)
    def stopStations: List[Route]    = t.table.filter(_._2.waitTime.nonEmpty).keys.toList
    def transitStations: List[Route] = t.table.filter(_._2.waitTime.isEmpty).keys.toList
    def routes: List[Route]          = t.table.keys.toList

  trait Timetable:
    def train: Train
    def startStation: Station
    def departureTime: ClockTime
    def table: ListMap[Route, Time]

  trait TrainTimetable extends Timetable:
    def arrivingStation: Station
    def arrivingTime: Option[ClockTime]

  trait TimeEstimator:
    def ETA(lastTime: Option[Time], route: Route, train: Train): Option[ClockTime]

  private object UnrealTimeEstimator extends TimeEstimator:
    def ETA(lastTime: Option[Time], lastRoute: Route, train: Train): Option[ClockTime] =
      for
        distance        <- Some(lastRoute.length)
        travelMinutes   <- Some((distance / Math.min(train.maxSpeed, lastRoute.technology.maxSpeed)) * 60)
        offsetTime      <- lastTime
        travelStartTime <- offsetTime.departure
        arrivingTime    <- h(travelStartTime.h).m(travelStartTime.m + travelMinutes.toInt).toOption
      yield arrivingTime

  /** Default time estimator just considers distance between last station and the given one and train speed. No
    * acceleration and acceleration are considered.
    *
    * @return
    *   TimeEstimator
    */
  given defaultTimeEstimator: TimeEstimator = UnrealTimeEstimator

  /** Train timetable to be defined */
  trait PartialTimetable extends Timetable:
    def stopsIn(station: Station, waitTime: WaitTime): PartialTimetable
    def transitIn(station: Station): PartialTimetable
    def arrivesTo(station: Station): TrainTimetable

  /** Factory of PartialTimetable */
  object PartialTimetable:
    def apply(
        train: Train,
        travelsOn: Route,
        departureTime: Either[ClockTimeErrors, ClockTime]
    ): Either[ClockTimeErrors, PartialTimetable] =
      for
        depTime <- departureTime
      yield PartialTrainTimetable(
        train,
        travelsOn,
        depTime,
        ListMap((travelsOn, StartScheduleTime(Some(depTime))))
      )

    private case class PartialTrainTimetable(
        train: Train,
        travelsRoute: Route,
        departureTime: ClockTime,
        table: ListMap[Route, Time]
    )(using timeEstimationStrategy: TimeEstimator) extends PartialTimetable:
      private def lastDepartureTime: Option[Time] = table.lastOption.map(_._2)
      override def stopsIn(route: Route, waitTime: WaitTime): PartialTimetable =
        insertRoute(
          route,
          AutoScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, route, train), Some(waitTime))
        )

      override def transitIn(route: Route): PartialTimetable =
        insertRoute(route, AutoScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, route, train), None))

      override def arrivesTo(route: Route): TrainTimetable =
        TrainTimetableImpl(
          insertRoute(route, EndScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, route, train))),
          route.arrival
        )

      private def insertRoute(route: Route, time: Time): PartialTimetable =
        this.copy(table = table.updated(route, time))

    private case class TrainTimetableImpl(
        private val partialTrainTimetable: PartialTimetable,
        arrivingStation: Station
    ) extends TrainTimetable:
      export partialTrainTimetable.{departureTime, startStation, table, train}
      override def arrivingTime: Option[ClockTime] =
        for
          t  <- table.lastOption
          at <- t._2.arriving
        yield at

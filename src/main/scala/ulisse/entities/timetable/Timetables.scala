package ulisse.entities.timetable

import ulisse.entities.Routes.TypeRoute
import ulisse.entities.station.Station
import ulisse.entities.timetable.ScheduleTime.{AutoScheduleTime, EndScheduleTime, StartScheduleTime}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.{ClockTime, ClockTimeErrors}
import ulisse.utils.Times.FluentDeclaration.h

import scala.collection.immutable.ListMap

/** Object containing traits and utility method for creation and use of TrainTimetable */
object Timetables:

  private type Length   = Double
  private type Time     = ScheduleTime
  private type WaitTime = Int

  /** Rail info `length` and [[TypeRoute]] used to estimate `ScheduleTime` of station. */
  case class RailInfo(length: Length, typeRoute: TypeRoute)

  extension (i: Int)
    def toWaitTime: WaitTime = i

  extension (t: TrainTimetable)
    /** Returns list of [[Station]] where train stops */
    def stopStations: List[Station] = t.table.filter(_._2.waitTime.nonEmpty).keys.toList

    /** Returns list of [[Station]] where train just transit (doesn't stop) */
    def transitStations: List[Station] =
      t.table.filter(_._2.waitTime.isEmpty).keys.toList.filterNot(s =>
        s.equals(t.arrivingStation) || s.equals(t.startStation)
      )

    /** Returns list of nearest stations pair */
    def routes: List[(Station, Station)] = t.table.keys.zip(t.table.keys.drop(1)).toList

  /** Basic timetable */
  trait Timetable:
    def train: Train
    def startStation: Station
    def departureTime: ClockTime
    def table: ListMap[Station, Time]

  /** Complete timetable containing arriving station and time */
  trait TrainTimetable extends Timetable:
    def arrivingStation: Station
    def arrivingTime: Option[ClockTime]

  /** ETA estimator strategy.
    * Method `ETA` requires previous station [[Time]], [[RailInfo]] and [[Train]] in order
    * to calculate Estimated Time of Arrival.
    */
  trait TimeEstimator:
    /** Return optionally arrival ClockTime to travel rail length ([[railInfo.length]]) */
    def ETA(lastTime: Option[Time], railInfo: RailInfo, train: Train): Option[ClockTime]

  /** Default implementation of [[TimeEstimator]]
    * It uses minimum speed between train and rail ones; it doesn't consider train's acceleration and deceleration specs
    */
  private object UnrealTimeEstimator extends TimeEstimator:
    def ETA(lastTime: Option[Time], railInfo: RailInfo, train: Train): Option[ClockTime] =
      for
        travelMinutes <- Some((railInfo.length / Math.min(train.maxSpeed, railInfo.typeRoute.technology.maxSpeed)) * 60)
        offsetTime    <- lastTime
        travelStartTime <- offsetTime.departure
        arrivingTime    <- h(travelStartTime.h).m(travelStartTime.m + travelMinutes.toInt).toOption
      yield arrivingTime

  /** Default time estimator just considers given distance between stations ([[RailInfo.length]]),
    * rail technology speed ([[RailInfo.typeRoute.technology]]) and train speed.
    * No acceleration and acceleration are considered.
    */
  given defaultTimeEstimator: TimeEstimator = UnrealTimeEstimator

  /** Partial train timetable to be defined.
    * Methods allows to define station where train stops, transit or arrives.
    */
  trait PartialTimetable extends Timetable:
    def stopsIn(station: Station, waitTime: WaitTime)(railInfo: RailInfo): PartialTimetable
    def transitIn(station: Station)(railInfo: RailInfo): PartialTimetable
    def arrivesTo(station: Station)(railInfo: RailInfo): TrainTimetable

  /** Factory of PartialTimetable */
  object PartialTimetable:
    /** Returns `PartialTimetable` given train, startStation (where train departs) and departureTime from startStation.
      * If `departureTime` is invalid is returned an [[ClockTimeErrors]]
      */
    def apply(
        train: Train,
        startStation: Station,
        departureTime: Either[ClockTimeErrors, ClockTime]
    ): Either[ClockTimeErrors, PartialTimetable] =
      for
        depTime <- departureTime
      yield PartialTrainTimetable(
        train,
        startStation,
        depTime,
        ListMap((startStation, StartScheduleTime(Some(depTime))))
      )

    private case class PartialTrainTimetable(
        train: Train,
        startStation: Station,
        departureTime: ClockTime,
        table: ListMap[Station, Time]
    )(using timeEstimationStrategy: TimeEstimator) extends PartialTimetable:
      private def lastDepartureTime: Option[Time] = table.lastOption.map(_._2)
      override def stopsIn(station: Station, waitTime: WaitTime)(railInfo: RailInfo): PartialTimetable =
        insertStation(
          station,
          AutoScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, railInfo, train), Some(waitTime))
        )

      override def transitIn(station: Station)(railInfo: RailInfo): PartialTimetable =
        insertStation(station, AutoScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, railInfo, train), None))

      override def arrivesTo(station: Station)(railInfo: RailInfo): TrainTimetable =
        TrainTimetableImpl(
          insertStation(station, EndScheduleTime(timeEstimationStrategy.ETA(lastDepartureTime, railInfo, train))),
          station
        )

      private def insertStation(station: Station, time: Time) =
        this.copy(table = table.updated(station, time))

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

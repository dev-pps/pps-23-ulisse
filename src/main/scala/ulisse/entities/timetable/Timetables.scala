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

  type Length           = Double
  private type Time     = ScheduleTime
  private type WaitTime = Int

  case class RailInfo(length: Length, typeRoute: TypeRoute)

  extension (i: Int)
    def toWaitTime: WaitTime = i

  extension (t: TrainTimetable)
    def stopStations: List[Station] = t.table.filter(_._2.waitTime.nonEmpty).keys.toList
    def transitStations: List[Station] =
      t.table.filter(_._2.waitTime.isEmpty).keys.toList.filterNot(s =>
        s.equals(t.arrivingStation) || s.equals(t.startStation)
      )
    def routes: List[(Station, Station)] = t.table.keys.zip(t.table.keys.drop(1)).toList

  trait Timetable:
    def train: Train
    def startStation: Station
    def departureTime: ClockTime
    def table: ListMap[Station, Time]

  trait TrainTimetable extends Timetable:
    def arrivingStation: Station
    def arrivingTime: Option[ClockTime]

  trait TimeEstimator:
    def ETA(lastTime: Option[Time], railInfo: RailInfo, train: Train): Option[ClockTime]

  private object UnrealTimeEstimator extends TimeEstimator:
    def ETA(lastTime: Option[Time], railInfo: RailInfo, train: Train): Option[ClockTime] =
      for
        travelMinutes <- Some((railInfo.length / Math.min(train.maxSpeed, railInfo.typeRoute.technology.maxSpeed)) * 60)
        offsetTime    <- lastTime
        travelStartTime <- offsetTime.departure
        arrivingTime    <- h(travelStartTime.h).m(travelStartTime.m + travelMinutes.toInt).toOption
      yield arrivingTime

  /** Default time estimator just considers distance between last station and the given one and train speed. No
    * acceleration and acceleration are considered.
    */
  given defaultTimeEstimator: TimeEstimator = UnrealTimeEstimator

  /** Train timetable to be defined */
  trait PartialTimetable extends Timetable:
    def stopsIn(station: Station, waitTime: WaitTime)(railInfo: RailInfo): PartialTimetable
    def transitIn(station: Station)(railInfo: RailInfo): PartialTimetable
    def arrivesTo(station: Station)(railInfo: RailInfo): TrainTimetable

  /** Factory of PartialTimetable */
  object PartialTimetable:
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

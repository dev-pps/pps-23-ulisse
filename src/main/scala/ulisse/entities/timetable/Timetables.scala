package ulisse.entities.timetable

import cats.Id
import ulisse.entities.route.Routes.RouteType
import ulisse.entities.station.Station
import ulisse.entities.timetable.TrainStationTime.{ArrivingStationTime, AutoStationTime, DepartureStationTime}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.{ClockTime, Time}
import ulisse.utils.Times.FluentDeclaration.h

import scala.collection.immutable.ListMap

/** Object containing traits and utility method for creation and use of Timetable */
object Timetables:

  private type Length      = Double
  private type StationTime = TrainStationTime
  private type WaitTime    = Int

  /** Rail info `length` and [[RouteType]] used to estimate `ScheduleTime` of station. */
  case class RailInfo(length: Length, typeRoute: RouteType)

  extension (i: Int)
    def toWaitTime: WaitTime = i

  extension (t: Timetable)
    /** Returns list of [[Station]] where train stops */
    def stopStations: List[Station] = t.table.filter(_._2.stationTime.waitTime.nonEmpty).keys.toList

    /** Returns list of [[Station]] where train just transit (doesn't stop) */
    def transitStations: List[Station] =
      t.table.filter(_._2.stationTime.waitTime.isEmpty).keys.toList.filterNot(s =>
        s.equals(t.arrivingStation) || s.equals(t.startStation)
      )

    /** Returns complete list of all stations */
    def stations: List[Station] = t.table.keys.toList

    /** Returns list of nearest stations pair */
    def routes: List[(Station, Station, Option[RailInfo])] =
      val pair = t.table.zip(t.table.drop(1))
      pair.map((s, f) => (s._1, f._1, f._2.railInfo)).toList

  /** Station infos like `routeType` (to reach station) and `stationTime` */
  case class StationInfo(railInfo: Option[RailInfo], stationTime: StationTime)

  /** Basic timetable */
  trait PartialTimetable:
    def train: Train
    def startStation: Station
    def departureTime: ClockTime
    def table: ListMap[Station, StationInfo]

  /** Complete timetable containing arriving station and ClockTime */
  trait Timetable extends PartialTimetable:
    def arrivingStation: Station
    def arrivingTime: Option[ClockTime]
    override def equals(that: Any): Boolean =
      that match
        case that: Timetable =>
          train == that.train &&
          startStation == that.startStation &&
          departureTime == that.departureTime &&
          table == that.table &&
          arrivingStation == that.arrivingStation &&
          arrivingTime == that.arrivingTime
        case _ => false

  /** ETA estimator strategy.
    * Method `ETA` requires previous station [[StationTime]], [[RailInfo]] and [[Train]] in order
    * to calculate Estimated Time of Arrival.
    */
  trait TimeEstimator:
    /** Return optionally arrival ClockTime to travel rail length ([[railInfo.length]]) */
    def ETA(lastTime: Option[StationTime], railInfo: RailInfo, train: Train): Option[ClockTime]

  /** Default implementation of [[TimeEstimator]]
    * It uses minimum speed between train and rail ones; it doesn't consider train's acceleration and deceleration specs
    */
  private object UnrealTimeEstimator extends TimeEstimator:
    def ETA(lastTime: Option[StationTime], railInfo: RailInfo, train: Train): Option[ClockTime] =
      val travelMinutes =
        (railInfo.length / Math.min(train.maxSpeed, railInfo.typeRoute.technology.maxSpeed) * 60).toInt

      for
        offsetTime      <- lastTime
        travelStartTime <- offsetTime.departure
        arrivingTime    <- (Id(travelStartTime.asTime) + Id(Time(0, travelMinutes, 0))).toClockTime.toOption
      yield arrivingTime

  /** Default time estimator just considers given distance between stations ([[RailInfo.length]]),
    * rail technology speed ([[RailInfo.typeRoute.technology]]) and train speed.
    * No acceleration and acceleration are considered.
    */
  given defaultTimeEstimator: TimeEstimator = UnrealTimeEstimator

  /** Partial train timetable to be defined.
    * Methods allows to define station where train stops, transit or arrives.
    */
  trait TimetableBuilder:
    def stopsIn(station: Station, waitTime: WaitTime)(railInfo: RailInfo): TimetableBuilder
    def transitIn(station: Station)(railInfo: RailInfo): TimetableBuilder
    def arrivesTo(station: Station)(railInfo: RailInfo): Timetable
    def partialTimetable: PartialTimetable

  /** Factory of PartialTimetable */
  object TimetableBuilder:
    /** Returns `PartialTimetable` given train, startStation (where train departs) and departureTime from startStation.
      * If `departureTime` is invalid is returned an [[TimeErrors]]
      */
    def apply(
        train: Train,
        startStation: Station,
        departureTime: ClockTime
    ): TimetableBuilder =
      TimetableBuilderImpl(
        train,
        startStation,
        departureTime,
        ListMap((startStation, StationInfo(None, DepartureStationTime(Some(departureTime)))))
      )

    private case class TimetableBuilderImpl(
        train: Train,
        startStation: Station,
        departureTime: ClockTime,
        table: ListMap[Station, StationInfo]
    )(using timeEstimationStrategy: TimeEstimator) extends TimetableBuilder:
      private def lastDepartureTime: Option[StationTime] = table.lastOption.map(_._2.stationTime)
      private def arrivingTime(railInfo: RailInfo): Option[ClockTime] =
        timeEstimationStrategy.ETA(lastDepartureTime, railInfo, train)
      override def stopsIn(station: Station, waitTime: WaitTime)(railInfo: RailInfo): TimetableBuilder =
        insertStation(
          station,
          AutoStationTime(arrivingTime(railInfo), Some(waitTime)),
          railInfo
        )

      override def transitIn(station: Station)(railInfo: RailInfo): TimetableBuilder =
        insertStation(station, AutoStationTime(arrivingTime(railInfo), None), railInfo)

      override def arrivesTo(station: Station)(railInfo: RailInfo): Timetable =
        TimetableImpl(
          insertStation(
            station,
            ArrivingStationTime(arrivingTime(railInfo)),
            railInfo
          ).partialTimetable,
          station
        )

      private def insertStation(station: Station, stationTime: StationTime, railInfo: RailInfo) =
        val info = StationInfo(Some(railInfo), stationTime)
        this.copy(table = table.updated(station, info))

      override def partialTimetable: PartialTimetable = PartialTimetableImpl(this)

      private case class PartialTimetableImpl(private val builder: TimetableBuilderImpl) extends PartialTimetable:
        export builder.{departureTime, startStation, table, train}

    private case class TimetableImpl(
        private val partialTimetable: PartialTimetable,
        arrivingStation: Station
    ) extends Timetable:
      export partialTimetable.{departureTime, startStation, table, train}
      override def arrivingTime: Option[ClockTime] =
        for
          t  <- table.lastOption
          at <- t._2.stationTime.arriving
        yield at

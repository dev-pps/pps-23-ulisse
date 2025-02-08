package ulisse.entities.timetable

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.entities.timetable.ScheduleTime.{AutoScheduleTime, EndScheduleTime, StartScheduleTime}
import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.{ClockTime, ClockTimeErrors}
import ulisse.utils.Times.FluentDeclaration.h

import scala.collection.immutable.ListMap

/** Object containing traits and utility method for creation and use of TrainTimetable
  */
object Timetables:

  private type Time     = ScheduleTime
  private type WaitTime = Int

  private type N        = Int
  private type C        = Coordinate[N]
  private type StationT = Station[N, C]

  extension (i: Int)
    def toWaitTime: WaitTime = i

  extension (t: TrainTimetable)
    /** @return
      *   Stations where train stops
      */
    def stopStations: List[StationT] = t.table.filter(_._2.waitTime.nonEmpty).keys.toList

    /** @return
      *   Stations where train transits and not stops
      */
    def transitStations: List[StationT] =
      t.table.removedAll(List(t.arrivingStation, t.startStation)).filter(_._2.waitTime.isEmpty).keys.toList

    /** @return
      *   List of a couple of stations that compone train trip
      */
    def routes: List[(StationT, StationT)] = t.table.keys.toList.zip(t.table.keys.drop(1))

  /** Basilar info of a timetable
    */
  trait Timetable:
    /** @return
      *   Timetable's train
      */
    def train: Train

    /** @return
      *   Initial station of train trip
      */
    def startStation: StationT

    /** @return
      *   Departure time
      */
    def departureTime: ClockTime

    /** @return
      *   Map of Train Timetable containing station and its arriving time.
      */
    def table: ListMap[StationT, Time]

  /** Complete Train timetable
    */
  trait TrainTimetable extends Timetable:
    /** @return
      *   Last station of train trip
      */
    def arrivingStation: StationT

    /** @return
      *   Time of arriving to last station
      */
    def arrivingTime: Option[ClockTime]

  /** Context Travel time estimator strategy. It defines how to calculate travelling time between stations.
    */
  trait TimeEstimator:
    /** Calculates Estimated Time of Arrival (ETA) to reach `endStation`
      * @param trainTable
      *   Current timetable situation
      * @param endStation
      *   Station to reach
      * @param train
      *   Train used
      * @return
      *   ETA as Option of `ClockTime`
      */
    def ETA(trainTable: ListMap[StationT, Time], endStation: StationT, train: Train): Option[ClockTime]

  /** Default context TimeEstimator strategy.
    *
    * ETA is calculated considering just speed of train and distance between last previous station and `endStation`.
    */
  private object UnrealTimeEstimator extends TimeEstimator:
    def ETA(trainTable: ListMap[StationT, Time], endStation: StationT, train: Train): Option[ClockTime] =
      for
        prevStation <- trainTable.lastOption
        distance    <- Some(prevStation._1.coordinate.distance(endStation.coordinate))
        // TODO: here speed should be the min(train.maxSpeed, route.technology.speed)
        travelMinutes <- Some((distance / train.maxSpeed) * 60)
        prevDeparture <- prevStation._2.departure
        arrivingTime  <- h(prevDeparture.h).m(prevDeparture.m + travelMinutes.toInt).toOption
      yield arrivingTime

  /** Default time estimator just considers distance between last station and the given one and train speed. No
    * acceleration and acceleration are considered.
    * @return
    *   TimeEstimator
    */
  given defaultTimeEstimator: TimeEstimator = UnrealTimeEstimator

  /** Train timetable to be defined
    */
  trait PartialTimetable extends Timetable:
    def stopsIn(station: StationT, waitTime: WaitTime): PartialTimetable
    def transitIn(station: StationT): PartialTimetable
    def arrivesTo(station: StationT): TrainTimetable

  /** Factory of PartialTimetable
    */
  object PartialTimetable:
    def apply(
        train: Train,
        startFrom: StationT,
        departureTime: Either[ClockTimeErrors, ClockTime]
    ): Either[ClockTimeErrors, PartialTimetable] =
      for
        depTime <- departureTime
      yield PartialTrainTimetable(
        train,
        startFrom,
        depTime,
        ListMap((startFrom, StartScheduleTime(Some(depTime))))
      )

    private case class PartialTrainTimetable(
        train: Train,
        startStation: StationT,
        departureTime: ClockTime,
        table: ListMap[StationT, Time]
    )(using timeEstimationStrategy: TimeEstimator) extends PartialTimetable:
      override def stopsIn(station: StationT, waitTime: WaitTime): PartialTimetable =
        insertStation(
          station,
          AutoScheduleTime(timeEstimationStrategy.ETA(table, station, train), Some(waitTime))
        )

      override def transitIn(station: StationT): PartialTimetable =
        insertStation(station, AutoScheduleTime(timeEstimationStrategy.ETA(table, station, train), None))

      override def arrivesTo(station: StationT): TrainTimetable =
        TrainTimetableImpl(
          insertStation(station, EndScheduleTime(timeEstimationStrategy.ETA(table, station, train))),
          station
        )

      private def insertStation(station: StationT, time: Time): PartialTimetable =
        this.copy(table = table.updated(station, time))

    private case class TrainTimetableImpl(
        private val partialTrainTimetable: PartialTimetable,
        arrivingStation: StationT
    ) extends TrainTimetable:
      export partialTrainTimetable.{departureTime, startStation, table, train}
      override def arrivingTime: Option[ClockTime] =
        for
          t  <- table.get(arrivingStation)
          at <- t.arriving
        yield at

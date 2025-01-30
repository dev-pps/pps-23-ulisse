package ulisse.entities.timetable

import ulisse.entities.train.Trains.Train
import scala.collection.immutable.ListMap

/** Object containing traits and utility method for creation and use of TrainTimetable
  */
object Timetables:

  private type Time     = ScheduleTime
  private type WaitTime = Int
  private type Station  = String

  extension (i: Int)
    def toWaitTime: WaitTime = i

  case class ClockTime(h: Int, m: Int)

  case class ScheduleTime(arriving: Option[ClockTime], waitTime: Option[WaitTime]):
    def departureTime: Option[ClockTime] =
      for
        a <- arriving
        w <- waitTime
      yield a.copy(m = a.m + w)

  extension (t: TrainTimetable)
    /** @return
      *   Stations where train stops
      */
    def stopStations: List[Station] = t.table.filter(_._2.waitTime.nonEmpty).keys.toList

    /** @return
      *   Stations where train transits and not stops
      */
    def transitStations: List[Station] =
      t.table.removedAll(List(t.arrivingStation, t.startStation)).filter(_._2.waitTime.isEmpty).keys.toList

    /** @return
      *   List of a couple of stations that compone train trip
      */
    def routes: List[(Station, Station)] = t.table.keys.toList.zip(t.table.keys.drop(1))

  /** Basilar info of a timetable
    */
  trait Timetable:
    /** @return
      *   Initial station of train trip
      */
    def startStation: Station

    /** @return
      *   Departure time
      */
    def departureTime: ClockTime

    /** @return
      *   Map of Train Timetable containing station and its arriving time.
      */
    def table: ListMap[Station, Time]

  /** Complete Train timetable
    */
  trait TrainTimetable extends Timetable:
    /** @return
      *   Last station of train trip
      */
    def arrivingStation: Station

    /** @return
      *   Time of arriving to last station
      */
    def arrivingTime: Option[Time]

  /** Train timetable to be defined
    */
  trait PartialTimetable extends Timetable:
    def stopsIn(station: String, waitTime: WaitTime): PartialTimetable
    def transitIn(station: Station): PartialTimetable
    def arrivesTo(station: Station): TrainTimetable

  /** Factory of PartialTimetable
    */
  object PartialTimetable:
    def apply(train: Train, startFrom: Station, departureTime: ClockTime): PartialTimetable =
      PartialTrainTimetable(
        train,
        startFrom,
        departureTime,
        ListMap((startFrom, ScheduleTime(None, None)))
      )

    private case class PartialTrainTimetable(
        train: Train,
        startStation: Station,
        departureTime: ClockTime,
        table: ListMap[Station, Time]
    ) extends PartialTimetable:
      override def stopsIn(station: Station, waitTime: WaitTime): PartialTimetable =
        insertStation(station, ScheduleTime(None, Some(waitTime)))
      override def transitIn(station: Station): PartialTimetable = insertStation(station, ScheduleTime(None, None))
      override def arrivesTo(station: Station): TrainTimetable =
        TrainTimetableImpl(insertStation(station, ScheduleTime(None, None)), station)

      private def insertStation(station: Station, time: Time): PartialTimetable =
        this.copy(table = table.updated(station, time))

    private case class TrainTimetableImpl(
        private val partialTrainTimetable: PartialTimetable,
        arrivingStation: Station
    ) extends TrainTimetable:
      export partialTrainTimetable.{departureTime, startStation, table}
      override def arrivingTime: Option[Time] =
        table.get(arrivingStation)

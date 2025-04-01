package ulisse.entities.timetable

import cats.Id
import ulisse.entities.simulation.environments.EnvironmentElements.EnvironmentElement
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Times.{ClockTime, Time, given}

import scala.collection.immutable.ListMap

/** Wrapper for DynamicTimetable. */
object DynamicTimetables:

  /** Timetable for Simulation. */
  trait DynamicTimetable extends Timetable with EnvironmentElement:
    import DynamicTimetableUtils.*

    /** The id of the timetable. */
    def id: Int = hashCode()

    /** The effective table used to store effective time info. */
    def effectiveTable: List[(Station, TrainStationTime)]

    /** The current route the train is in. */
    def currentRoute: Option[(Station, Station)] =
      effectiveTable.routesWithTimingInfo.findRouteWhere(_.isDefined, _.isEmpty).stations

    /** The next route the train has to take. */
    def nextRoute: Option[(Station, Station)] =
      effectiveTable.routesWithTimingInfo.findRouteWhere(_.isEmpty, _.isEmpty).stations

    /** The current delay of the train. */
    def currentDelay: Option[Time] =
      (currentRoute, nextRoute) match
        case (Some((ds, _)), _) =>
          effectiveTable.find(_._1 == ds).flatMap(_._2.departure).map(_.asTime) underflowSub table(
            ds
          ).stationTime.departure.map(_.asTime)
        case (_, Some((ds, _))) =>
          effectiveTable.find(_._1 == ds).flatMap(_._2.arriving).map(_.asTime) underflowSub table(
            ds
          ).stationTime.arriving.map(_.asTime)
        case _ =>
          effectiveTable.lastOption.flatMap(_._2.arriving).map(_.asTime) underflowSub arrivingTime.map(_.asTime)

    /** The delay in a station. */
    def delayIn(station: Station): Option[Time] =
      (table.get(station).map(_.stationTime), effectiveTable.find(_._1 == station).map(_._2)) match
        case (Some(TrainStationTime(_, _, Some(departure))), Some(TrainStationTime(_, _, Some(effectiveDeparture)))) =>
          Some(Id(effectiveDeparture.asTime) underflowSub departure.asTime)
        case (Some(TrainStationTime(Some(arrival), _, _)), Some(TrainStationTime(Some(effectiveArrival), _, _))) =>
          Some(Id(effectiveArrival.asTime) underflowSub arrival.asTime)
        case _ => None

    /** The next departure time. */
    def nextDepartureTime: Option[ClockTime] =
      calculateTimeWithDelay(nextRoute.flatMap(nr => table(nr._1).stationTime.departure))

    /** The next arrival time. */
    def nextArrivalTime: Option[ClockTime] =
      calculateTimeWithDelay(currentRoute.flatMap(cr => table(cr._2).stationTime.arriving))

    private def calculateTimeWithDelay(time: Option[ClockTime]): Option[ClockTime] =
      if currentDelay.isDefined then time + currentDelay else time

    /** Indicate if the scheduled is completed. */
    def completed: Boolean = nextRoute.isEmpty && currentRoute.isEmpty

    /** Update the current timetable state with a new arrival time. */
    def arrivalUpdate(time: ClockTime): Option[DynamicTimetable]

    /** Update the current timetable state with a new departure time. */
    def departureUpdate(time: ClockTime): Option[DynamicTimetable]

    /** Defines equality for DynamicTimetables. */
    override def equals(that: Any): Boolean =
      that match
        case that: DynamicTimetable =>
          effectiveTable == that.effectiveTable && super.equals(that)
        case _ => super.equals(that)

    /** Defines hashcode for DynamicTimetables. */
    override def hashCode: Int =
      (table, train, departureTime, arrivingTime).##

  /** Factory for [[DynamicTimetable]] instances. */
  object DynamicTimetable:

    /** Creates a `DynamicTimetable` instance from a `Timetable`. */
    def apply(timetable: Timetable): DynamicTimetable =
      DynamicTimetableImpl(timetable.table.toList.map(st => (st._1, TrainStationTime(None, None, None))), timetable)

    private final case class DynamicTimetableImpl(
        effectiveTable: List[(Station, TrainStationTime)],
        timetable: Timetable
    ) extends DynamicTimetable:
      export timetable.*
      import DynamicTimetableUtils.*

      override def arrivalUpdate(time: ClockTime): Option[DynamicTimetable] =
        currentRoute.updateTimeInfo(
          (a, b) => b,
          swti =>
            TrainStationTime(
              Some(time),
              swti._2.waitTime,
              swti._2.departure
            )
        )

      override def departureUpdate(time: ClockTime): Option[DynamicTimetable] =
        nextRoute.updateTimeInfo(
          (a, b) => a,
          swti =>
            TrainStationTime(
              swti._2.arriving,
              (Some(time) underflowSub expectedDepartureTime).map(_.toMinutes),
              Some(time)
            )
        )

      private def expectedDepartureTime: Option[ClockTime] =
        nextRoute.flatMap(nr => table.find(_._1 == nr._1).flatMap(_._2.stationTime.departure))

      extension (route: Option[(Station, Station)])
        private def updateTimeInfo(
            ss: ((Station, Station)) => Station,
            tst: StationWithTimingInfo => TrainStationTime
        ): Option[DynamicTimetableImpl] =
          route.map(nr => effectiveTable.updateWhen(_._1.name == ss(nr).name)(swti => (swti._1, tst(swti)))).map(copy(
            _
          ))

  /** Utility for [[DynamicTimetable]] */
  private[DynamicTimetables] object DynamicTimetableUtils:
    type StationWithTimingInfo = (Station, TrainStationTime)
    type RouteWithTimingInfo   = (StationWithTimingInfo, StationWithTimingInfo)

    extension (timetable: List[(Station, TrainStationTime)])
      def routesWithTimingInfo: List[RouteWithTimingInfo] = timetable.zip(timetable.drop(1))

    extension (routesWithTimingInfo: List[RouteWithTimingInfo])
      def findRouteWhere(
          dsdc: Option[ClockTime] => Boolean,
          asac: Option[ClockTime] => Boolean
      ): Option[RouteWithTimingInfo] =
        routesWithTimingInfo.find((ds, as) => dsdc(ds._2.departure) && asac(as._2.arriving))

    extension (rwti: Option[RouteWithTimingInfo])
      def stations: Option[(Station, Station)] = rwti.map((dd, aa) => (dd._1, aa._1))

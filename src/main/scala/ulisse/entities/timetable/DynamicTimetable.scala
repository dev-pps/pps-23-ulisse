package ulisse.entities.timetable

import ulisse.entities.simulation.EnvironmentElements.EnvironmentElement
import ulisse.entities.station.Station
import ulisse.entities.timetable.DynamicTimetable.*
import ulisse.entities.timetable.Timetables.{StationTime, Timetable}
import ulisse.utils.Times.{-, ClockTime, InvalidHours}
import ulisse.utils.CollectionUtils.updateWhen

import scala.collection.immutable.ListMap

trait DynamicTimetable extends Timetable with EnvironmentElement:
  def effectiveTable: List[(Station, TrainStationTime)]
  def currentRoute: Option[(Station, Station)] =
    effectiveTable.routesWithTimingInfo.findRouteWhere(_.isDefined, _.isEmpty).stations
  def nextRoute: Option[(Station, Station)] =
    effectiveTable.routesWithTimingInfo.findRouteWhere(_.isEmpty, _.isEmpty).stations
  def nextDepartureTime: Option[ClockTime] =
    table.routesWithTimingInfo2.findRouteWhere(_.isEmpty, _.isEmpty).flatMap((dd, _) => dd._2.departure)
  def nextWaitingTime: Option[Int] =
    table.routesWithTimingInfo2.findRouteWhere(_.isEmpty, _.isEmpty).flatMap((dd, _) => dd._2.waitTime)
  def completed: Boolean = nextRoute.isEmpty
  def arrivalUpdate(time: ClockTime): Option[DynamicTimetable]
  def departureUpdate(time: ClockTime): Option[DynamicTimetable]

object DynamicTimetable:
  private type StationWithTimingInfo = (Station, TrainStationTime)
  private type RouteWithTimingInfo   = (StationWithTimingInfo, StationWithTimingInfo)

  extension (timetable: List[(Station, TrainStationTime)])
    def routesWithTimingInfo: List[RouteWithTimingInfo] = timetable.zip(timetable.drop(1))
  extension (timetable: ListMap[Station, TrainStationTime])
    def routesWithTimingInfo2: List[RouteWithTimingInfo] = timetable.zip(timetable.drop(1)).toList
  extension (routesWithTimingInfo: List[RouteWithTimingInfo])
    def findRouteWhere(
        departureStationDepartureCondition: Option[ClockTime] => Boolean,
        arrivalStationArrivingCondition: Option[ClockTime] => Boolean
    ): Option[RouteWithTimingInfo] =
      routesWithTimingInfo.find((dd, aa) =>
        departureStationDepartureCondition(dd._2.arriving) && arrivalStationArrivingCondition(aa._2.arriving)
      )
  extension (rwti: Option[RouteWithTimingInfo])
    def stations: Option[(Station, Station)] = rwti.map((dd, aa) => (dd._1, aa._1))

  def apply(timetable: Timetable): DynamicTimetable =
    DynamicTimetableImpl(timetable, timetable.table.toList.map(st => (st._1, TrainStationTime(None, None, None))))
  private final case class DynamicTimetableImpl(timetable: Timetable, effectiveTable: List[(Station, TrainStationTime)])
      extends DynamicTimetable:
    export timetable.*
    extension (newEffectiveTimeTable: Option[List[(Station, TrainStationTime)]])
      private def update: Option[DynamicTimetable] = newEffectiveTimeTable.map(nett => copy(effectiveTable = nett))
    override def arrivalUpdate(time: ClockTime): Option[DynamicTimetable] = currentRoute.map(cr =>
      effectiveTable.updateWhen(swti => swti._1.name == cr._2.name)(swti =>
        (swti._1, TrainStationTime(Some(time), swti._2.waitTime, swti._2.departure))
      )
    ).update
    override def departureUpdate(time: ClockTime): Option[DynamicTimetable] = nextRoute.map(nr =>
      effectiveTable.updateWhen(swti => swti._1.name == nr._1.name)(swti =>
        (
          swti._1,
          TrainStationTime(swti._2.arriving, (Some(time) - swti._2.arriving).map(c => c.h * 60 + c.m), Some(time))
        )
      )
    ).update

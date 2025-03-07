package ulisse.entities.timetable

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.route.Routes.RouteType
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{toWaitTime, RailInfo, StationInfo, Timetable, TimetableBuilder}
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.ClockTime
import ulisse.entities.timetable.TestMockedEntities.*
import ulisse.entities.timetable.TrainStationTime.{ArrivingStationTime, AutoStationTime, DepartureStationTime}

import scala.collection.immutable.ListMap
import scala.language.postfixOps

class TimetableTest extends AnyFlatSpec:

  val timetableBuilder: TimetableBuilder =
    TimetableBuilder(train = AV1000Train, startStation = stationA, departureTime = h(9).m(0).getOrDefault)

  val AV1000TimeTable: Timetable =
    timetableBuilder.stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .stopsIn(stationD, waitTime = 10)(railAV_10)
      .arrivesTo(stationF)(railAV_10)

  "timetable" should "provide list of all stations, where train stops, where it only transits" in:
    AV1000TimeTable.stopStations should be(List(stationB, stationD))
    AV1000TimeTable.transitStations should be(List(stationC))
    AV1000TimeTable.stations should be(List(stationA, stationB, stationC, stationD, stationF))

  "timetable" should "calculate arriving and departure time of each station where train arrives" in:
    val railInfoAB = RailInfo(length = 10, typeRoute = AV)
    val railInfoBC = RailInfo(length = 15, typeRoute = AV)
    val timeTableWithStops =
      timetableBuilder
        .stopsIn(stationB, 5)(railInfoAB)
        .arrivesTo(stationC)(railInfoBC)

    timeTableWithStops.table should be(ListMap(
      stationA -> StationInfo(None, DepartureStationTime(h(9).m(0).toOption)),
      stationB -> StationInfo(
        Some(railInfoAB),
        AutoStationTime(arriving = h(9).m(2).toOption, waitTime = Some(5.toWaitTime))
      ),
      stationC -> StationInfo(Some(railInfoBC), ArrivingStationTime(arriving = h(9).m(10).toOption))
    ))

  "timetable" should "consider also station of transit in estimation of arriving time" in:
    val timetableWithTransits = // 9:00
      timetableBuilder.transitIn(stationB)(RailInfo(length = 10, typeRoute = AV)) // 2 min from A = 9:02
        .transitIn(stationC)(RailInfo(length = 15, typeRoute = AV))               // 3 min from B = 9:05
        .transitIn(stationD)(RailInfo(length = 25, typeRoute = AV))               // 5 min from C = 9:10
        .arrivesTo(stationF)(RailInfo(length = 5, typeRoute = AV))                // 1 min from D = 9:11
    timetableWithTransits.arrivingTime should be(ClockTime(9, 11).toOption)

  "timetable" should "provide couple with nearest stations names" in:
    val AV1000TimeTable =
      timetableBuilder
        .stopsIn(stationB, waitTime = 5)(railAV_10)
        .stopsIn(stationC, waitTime = 5)(railAV_10)
        .arrivesTo(stationD)(railAV_10)

    AV1000TimeTable.routes should be(List(
      (stationA, stationB, Some(railAV_10)),
      (stationB, stationC, Some(railAV_10)),
      (stationC, stationD, Some(railAV_10))
    ))

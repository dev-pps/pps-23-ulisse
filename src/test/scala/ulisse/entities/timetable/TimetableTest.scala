package ulisse.entities.timetable

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes
import ulisse.entities.Routes.{Route, TypeRoute}
import ulisse.entities.station.Station
import ulisse.entities.timetable.ScheduleTime.{AutoScheduleTime, EndScheduleTime, StartScheduleTime}
import ulisse.entities.timetable.Timetables.{toWaitTime, PartialTimetable, TrainTimetable}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons
import ulisse.utils.Times.{ClockTime, ClockTimeErrors}
import ulisse.utils.Times.FluentDeclaration.h

import scala.collection.immutable.ListMap

class TimetableTest extends AnyFlatSpec:
  val trainTechnology: TrainTechnology = TrainTechnology("AV", maxSpeed = 300, acceleration = 1.5, deceleration = 1.0)
  val wagonInfo: Wagons.Wagon          = Wagons.PassengerWagon(300)
  val AV1000Train: Train               = Train(name = "AV1000", trainTechnology, wagonInfo, length = 4)

  private val stationA = Station("Station A", Coordinate(0, 0), 1)
  private val stationB = Station("Station B", Coordinate(10, 0), 1) // 2 min from A
  private val stationC = Station("Station C", Coordinate(25, 0), 1) // 3 min from B
  private val stationD = Station("Station D", Coordinate(50, 0), 1) // 5 min from C
  private val stationF = Station("Station F", Coordinate(55, 0), 1) // 1 min from D
  private val routeA_B = Routes.Route(
    departure = stationA,
    arrival = stationB,
    typeRoute = TypeRoute.AV,
    railsCount = 2,
    length = 20
  )

  private val fixedDistanceRoutes =
    val stations = List(stationA, stationB, stationC, stationD, stationF)
    stations.zip(stations.drop(1)).map((s, s1) =>
      Routes.Route(
        departure = s,
        arrival = s1,
        typeRoute = TypeRoute.AV,
        railsCount = 2,
        length = 20
      )
    )

  val partialTimetable: Either[ClockTimeErrors, PartialTimetable] =
    PartialTimetable(train = AV1000Train, startFrom = stationA, departureTime = h(9).m(0))

  val AV1000TimeTable: Either[ClockTimeErrors, TrainTimetable] =
    partialTimetable.map:
      _.stopsIn(stationB, waitTime = 5)
        .transitIn(stationC)
        .stopsIn(stationD, waitTime = 10)
        .arrivesTo(stationF)

  extension (timetable: Either[ClockTimeErrors, TrainTimetable])
    def performTest(test: TrainTimetable => Unit): Unit =
      timetable.foreach(test(_))

  "When create new Timetable" should "be returned an error if departure time is not valid" in:
    val invalidDepartureTime = h(50).m(0)
    val invalidTimetable: Either[ClockTimeErrors, PartialTimetable] =
      PartialTimetable(train = AV1000Train, travelsOn = routeA_B, invalidDepartureTime)
    import ulisse.utils.Times.InvalidHours
    invalidTimetable should be(Left(InvalidHours()))

  "TrainTimetable" should "provide list of stations where train stops and where it only transits" in:
    AV1000TimeTable.performTest(t =>
      t.stopStations should be(List(stationB, stationD))
      t.transitStations should be(List(stationC))
    )
  "TrainTimetable" should "calculate arriving and departure time of each station where train arrives" in:
    val timeTableWithStops =
      partialTimetable.map:
        _.stopsIn(stationB, 5)
          .arrivesTo(stationC)

    timeTableWithStops.performTest(t =>
      t.table should be(ListMap(
        stationA -> StartScheduleTime(h(9).m(0).toOption),
        stationB -> AutoScheduleTime(arriving = h(9).m(2).toOption, waitTime = Some(5.toWaitTime)),
        stationC -> EndScheduleTime(arriving = h(9).m(10).toOption)
      ))
    )

  "TrainTimetable" should "consider also station of transit in estimation of arriving time" in:
    val timetableWithTransits =
      partialTimetable.map:    // 9:00
        _.transitIn(stationB)  // 2 min from A = 9:02
          .transitIn(stationC) // 3 min from B = 9:05
          .transitIn(stationD) // 5 min from C = 9:10
          .arrivesTo(stationF) // 1 min from D = 9:11
    timetableWithTransits.performTest(_.arrivingTime should be(ClockTime(9, 11).toOption))

  "TrainTimetable" should "provide couple with nearest stations names" in:
    val AV1000TimeTable =
      partialTimetable.map:
        _.stopsIn(stationB, waitTime = 5)
          .stopsIn(stationC, waitTime = 5)
          .arrivesTo(stationD)

    AV1000TimeTable.performTest:
      _.routes should be(List(
        (stationA, stationB),
        (stationB, stationC),
        (stationC, stationD)
      ))

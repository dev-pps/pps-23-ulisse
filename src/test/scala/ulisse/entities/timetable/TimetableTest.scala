package ulisse.entities.timetable

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import ulisse.entities.timetable.Timetables.{ClockTime, PartialTimetable, ScheduleTime, Timetable, TrainTimetable}
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

import scala.collection.immutable.ListMap

class TimetableTest extends AnyFlatSpec:
  val trainTechnology: TrainTechnology = TrainTechnology("AV", maxSpeed = 300, acceleration = 1.5, deceleration = 1.0)
  val wagonInfo: Wagons.Wagon          = Wagons.PassengerWagon(300)
  val AV1000Train: Train               = Train(name = "AV1000", trainTechnology, wagonInfo, length = 4)

  val AV1000TimeTable: TrainTimetable =
    PartialTimetable(train = AV1000Train, startFrom = "Station A", departureTime = ClockTime(h = 9, m = 0))
      .stopsIn("Station B", waitTime = 5)
      .transitIn("Station C")
      .transitIn("Station D")
      .stopsIn("Station E", waitTime = 3)
      .arrivesTo("Station F")

  "TrainTimetable" should "provide list of stations where train stops and where it only transits" in:
    AV1000TimeTable.stopStations should be(List("Station B", "Station E"))
    AV1000TimeTable.transitStations should be(List("Station C", "Station D"))

  "TrainTimetable after creation" should "contains for each station wait time if it stops (excluded starting/arriving stations)" in:
    import ulisse.entities.timetable.Timetables.toWaitTime
    AV1000TimeTable.table should be(ListMap(
      "Station A" -> ScheduleTime(arriving = None, waitTime = None),
      "Station B" -> ScheduleTime(arriving = None, waitTime = Some(5.toWaitTime)),
      "Station C" -> ScheduleTime(None, None),
      "Station D" -> ScheduleTime(None, None),
      "Station E" -> ScheduleTime(arriving = None, waitTime = Some(3.toWaitTime)),
      "Station F" -> ScheduleTime(arriving = None, waitTime = None)
    ))

  "TrainTimetable" should "provide couple with nearest stations" in:
    val AV1000TimeTable =
      PartialTimetable(train = AV1000Train, startFrom = "Station A", departureTime = ClockTime(h = 9, m = 0))
        .stopsIn("Station B", waitTime = 5)
        .stopsIn("Station C", waitTime = 5)
        .arrivesTo("Station F")

    AV1000TimeTable.routes should be(List(
      ("Station A", "Station B"),
      ("Station B", "Station C"),
      ("Station C", "Station F")
    ))

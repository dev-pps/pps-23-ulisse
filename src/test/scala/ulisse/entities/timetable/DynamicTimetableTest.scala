package ulisse.entities.timetable

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.station.Station
import ulisse.entities.station.StationTest.{stationA, stationB, stationC, stationD}
import ulisse.entities.timetable.DynamicTimetableTest.*
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.TestMockedEntities.railAV_10
import ulisse.entities.timetable.Timetables.{Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgentTest.*
import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.{ClockTime, Time}

object DynamicTimetableTest:
  val timetable1: Timetable = defaultTimeTable(train3905)

  val timetable2: Timetable =
    TimetableBuilder(
      train = train3905,
      startStation = stationD,
      departureTime = h(20).m(0).getOrDefault
    ).stopsIn(stationC, waitTime = 5)(railAV_10)
      .transitIn(stationB)(railAV_10)
      .arrivesTo(stationA)(railAV_10)

  val timeTable3: Timetable = defaultTimeTable(train3906)
  val timeTable4: Timetable = defaultTimeTable(train3907)

  def defaultTimeTable(train: Train): Timetable =
    TimetableBuilder(
      train = train,
      startStation = stationA,
      departureTime = h(8).m(0).getOrDefault
    ).stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .arrivesTo(stationD)(railAV_10)

  val dynamicTimetable1 = makeDynamicTimeTable(timetable1)
  val dynamicTimetable2 = makeDynamicTimeTable(timetable2)
  val dynamicTimetable3 = makeDynamicTimeTable(timeTable3)
  val dynamicTimetable4 = makeDynamicTimeTable(timeTable4)

  def makeDynamicTimeTable(tt: Timetable): DynamicTimetable = DynamicTimetable(tt)

  extension (tt: Timetable)
    def stationNr(n: Int): Option[(Station, TrainStationTime)] = tt match
      case dtt: DynamicTimetable => dtt.effectiveTable.drop(n).headOption
      case _                     => tt.table.map((st, info) => (st, info.stationTime)).drop(n).headOption

  extension (r: Option[(Station, Station)])
    def listify: Option[List[Station]] = r.map(t => List(t._1, t._2))

class DynamicTimetableTest extends AnyWordSpec with Matchers:
  extension (dtt: DynamicTimetable)
    def travel(nStations: Int, delay: Option[ClockTime]): Option[DynamicTimetable] =
      (0 until nStations).foldLeft(Option(dtt)) { (dtt, _) =>
        for
          dtt <- dtt
          ndt <- dtt.nextDepartureTime + delay
          dtt <- dtt.departureUpdate(ndt)
          nat <- dtt.nextArrivalTime + delay
          dtt <- dtt.arrivalUpdate(nat)
        yield dtt
      }

  extension (swti: Option[(Station, TrainStationTime)])
    def withTimes(ti: TrainStationTime): Option[(Station, TrainStationTime)] = swti.map(_._1 -> ti)

  "A DynamicTimetable" when:
    "created" should:
      "have the same timetable data" in:
        dynamicTimetable1 shouldBe timetable1

      "have an effective table initialized with empty time data" in:
        dynamicTimetable1.effectiveTable.map(_._1) shouldBe timetable1.table.keys
        dynamicTimetable1.effectiveTable.forall(_._2 == TrainStationTime(None, None, None)) shouldBe true

      "not have a current route" in:
        dynamicTimetable1.currentRoute shouldBe None

      "not have a current delay" in:
        dynamicTimetable1.currentDelay shouldBe None

      "have as a next route the first route" in:
        dynamicTimetable1.nextRoute.listify shouldBe Some(timetable1.table.keys.take(2))
        dynamicTimetable1.nextDepartureTime shouldBe Some(timetable1.departureTime)

      "not be completed" in:
        dynamicTimetable1.completed shouldBe false

    "updated with a departure time" should:
      "update the effective table with the expected departure time" in:
        dynamicTimetable1.departureUpdate(timetable1.departureTime) match
          case Some(newDtt) =>
            newDtt.stationNr(0) shouldBe dynamicTimetable1.stationNr(0).withTimes(TrainStationTime(
              None,
              Some(0),
              Some(timetable1.departureTime)
            ))
            newDtt.nextRoute.listify shouldBe Some(timetable1.table.keys.slice(1, 3))
            newDtt.currentDelay shouldBe ClockTime(0, 0).toOption
            newDtt.nextDepartureTime shouldBe timetable1.table.drop(1).headOption.flatMap(_._2.stationTime.departure)
            newDtt.currentRoute.listify shouldBe Some(timetable1.table.keys.take(2))
            newDtt.nextArrivalTime shouldBe timetable1.table.drop(1).headOption.flatMap(_._2.stationTime.arriving)
            newDtt.completed shouldBe false
          case _ => fail()

      "update the effective table with an effective departure time" in:
        val expectedDepartureTime  = Right(timetable1.departureTime)
        val delay                  = ClockTime(0, 5)
        val effectiveDepartureTime = (expectedDepartureTime + delay).getOrDefault
        dynamicTimetable1.departureUpdate(effectiveDepartureTime) match
          case Some(newDtt) =>
            newDtt.stationNr(0) shouldBe dynamicTimetable1.stationNr(0).withTimes(TrainStationTime(
              None,
              delay.toOption.map(_.toMinutes),
              Some(effectiveDepartureTime)
            ))
            newDtt.nextRoute.listify shouldBe Some(timetable1.table.keys.slice(1, 3))
            newDtt.currentDelay shouldBe delay.toOption
            newDtt.nextDepartureTime shouldBe timetable1.table.drop(1).headOption.flatMap(
              _._2.stationTime.departure + delay.toOption
            )
            newDtt.currentRoute.listify shouldBe Some(timetable1.table.keys.take(2))
            newDtt.nextArrivalTime shouldBe timetable1.table.drop(1).headOption.flatMap(
              _._2.stationTime.arriving + delay.toOption
            )
            newDtt.completed shouldBe false
          case _ => fail()

    "updated with an arrival time" should:
      "update the effective table with the expected arrival time" in:
        (timetable1.stationNr(1).flatMap(_._2.arriving), dynamicTimetable1.travel(1, ClockTime(0, 0).toOption)) match
          case (Some(at), Some(newDtt)) =>
            newDtt.stationNr(1) shouldBe dynamicTimetable1.stationNr(1).withTimes(TrainStationTime(
              Some(at),
              None,
              None
            ))
            newDtt.nextRoute.listify shouldBe Some(timetable1.table.keys.slice(1, 3))
            newDtt.currentDelay shouldBe ClockTime(0, 0).toOption
            newDtt.nextDepartureTime shouldBe dynamicTimetable1.table(stationB).stationTime.departure
            newDtt.currentRoute shouldBe None
            newDtt.completed shouldBe false
          case o => fail()

      "update the effective table with an effective arrival time" in:
        val delay = ClockTime(0, 5).toOption
        (timetable1.stationNr(1).flatMap(_._2.arriving), dynamicTimetable1.travel(1, delay)) match
          case (Some(at), Some(newDtt)) =>
            newDtt.stationNr(1) shouldBe dynamicTimetable1.stationNr(1).withTimes(TrainStationTime(
              Some(at) + delay + delay,
              None,
              None
            ))
            newDtt.nextRoute shouldBe Some(stationB, stationC)
            newDtt.currentDelay shouldBe delay + delay
            newDtt.nextDepartureTime shouldBe dynamicTimetable1.table(stationB).stationTime.departure + delay + delay
            newDtt.currentRoute shouldBe None
            newDtt.completed shouldBe false
          case _ => fail()

    "travels all the route" should:
      "be completed" in:
        dynamicTimetable1.travel(dynamicTimetable1.table.size - 1, ClockTime(0, 0).toOption) match
          case Some(newDtt) =>
            newDtt.completed shouldBe true
          case _ => fail()

      "accumulate delays" in:
        val delay = ClockTime(0, 5).toOption
        dynamicTimetable1.travel(dynamicTimetable1.table.size - 1, delay) match
          case Some(newDtt) =>
            newDtt.currentDelay.map(_.toMinutes) shouldBe delay.map(_.toMinutes * 2 * (newDtt.table.size - 1))
            newDtt.completed shouldBe true
          case _ => fail()

      "catch up on delays" in:
        val delay = ClockTime(0, 5).toOption
        val updatedDtt =
          for
            newDtt <- dynamicTimetable1.travel(dynamicTimetable1.table.size - 2, delay)
            ndt    <- newDtt.nextDepartureTime
            newDtt <- newDtt.departureUpdate(ndt)
            eat    <- timetable1.arrivingTime
            newDtt <- newDtt.arrivalUpdate(eat)
          yield newDtt
        updatedDtt match
          case Some(newDtt) =>
            newDtt.currentDelay shouldBe ClockTime(0, 0).toOption
            newDtt.completed shouldBe true
          case _ => fail()

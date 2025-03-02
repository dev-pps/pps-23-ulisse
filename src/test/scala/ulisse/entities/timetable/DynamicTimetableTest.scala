package ulisse.entities.timetable

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.TypeRoute.AV
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.{ClockTime, Time}

class DynamicTimetableTest extends AnyWordSpec with Matchers:
  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905          = TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val stationA           = Station("A", Coordinate(0, 0), 1)
  private val stationB           = Station("B", Coordinate(0, 1), 1)
  private val stationC           = Station("C", Coordinate(0, 2), 1)
  private val stationD           = Station("D", Coordinate(0, 3), 1)
  private val departureTime      = h(9).m(0).getOrDefault
  private val timetableBuilder: TimetableBuilder =
    TimetableBuilder(train = train3905, startStation = stationA, departureTime = departureTime)
  private val railAV_10: RailInfo = RailInfo(length = 10, typeRoute = AV)
  private val tt: Timetable =
    timetableBuilder.stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .arrivesTo(stationD)(railAV_10)
  private val dtt = DynamicTimetable(tt)

  private def validateTimetableData(dtt: DynamicTimetable): Unit =
    dtt.train shouldBe tt.train
    dtt.startStation shouldBe tt.startStation
    dtt.departureTime shouldBe tt.departureTime
    dtt.arrivingStation shouldBe tt.arrivingStation
    dtt.arrivingTime shouldBe tt.arrivingTime
    dtt.table shouldBe tt.table

  "A DynamicTimetable" when:
    "created" should:
      "have the same timetable data" in:
        validateTimetableData(dtt)

      "have an effective table init with empty time data" in:
        dtt.effectiveTable.map(_._1) shouldBe tt.table.keys
        dtt.effectiveTable.forall(_._2 == TrainStationTime(None, None, None)) shouldBe true

      "not have a current route" in:
        dtt.currentRoute shouldBe None

      "not have a current delay" in:
        dtt.currentDelay shouldBe None

      "have as a next route the first route" in:
        dtt.nextRoute shouldBe Some(stationA, stationB)
        dtt.nextDepartureTime shouldBe Some(tt.departureTime)

      "not be completed" in:
        dtt.completed shouldBe false

    "updated with a departure time" should:
      "update the effective table with the expected departure time" in:
        dtt.departureUpdate(departureTime) match
          case Some(newDtt) =>
            newDtt.effectiveTable.find(_._1 == stationA).map(_._2) shouldBe Some(TrainStationTime(
              None,
              Some(0),
              Some(departureTime)
            ))
            newDtt.nextRoute shouldBe Some(stationB, stationC)
            newDtt.currentDelay shouldBe ClockTime(0, 0).toOption
            newDtt.nextDepartureTime shouldBe tt.table.find(_._1 == stationB).flatMap(tte => tte._2.departure)
            newDtt.currentRoute shouldBe Some(stationA, stationB)
            newDtt.completed shouldBe false
          case _ => fail()

      "update the effective table with an effective departure time" in:
        val expectedDepartureTime  = Right(departureTime)
        val minutesDelay           = 5
        val delay                  = ClockTime(0, minutesDelay)
        val effectiveDepartureTime = (expectedDepartureTime + delay).getOrDefault
        dtt.departureUpdate(effectiveDepartureTime) match
          case Some(newDtt) =>
            newDtt.effectiveTable.find(_._1 == stationA).map(_._2) shouldBe Some(TrainStationTime(
              None,
              Some(minutesDelay),
              Some(effectiveDepartureTime)
            ))
            newDtt.nextRoute shouldBe Some(stationB, stationC)
            newDtt.currentDelay shouldBe delay.toOption
            newDtt.nextDepartureTime shouldBe tt.table.find(_._1 == stationB).flatMap(tte =>
              tte._2.departure + delay.toOption
            )
            newDtt.currentRoute shouldBe Some(stationA, stationB)
            newDtt.completed shouldBe false
          case _ => fail()

    "updated with an arrival time" should:
      "update the effective table with the expected arrival time" in:
        dtt.table(stationB).arriving match
          case Some(at) => dtt.departureUpdate(dtt.departureTime).flatMap(_.arrivalUpdate(at)) match
              case Some(newDtt) =>
                newDtt.effectiveTable.find(_._1 == stationB).map(_._2) shouldBe Some(TrainStationTime(
                  Some(at),
                  None,
                  None
                ))
                newDtt.nextRoute shouldBe Some(stationB, stationC)
                newDtt.currentDelay shouldBe ClockTime(0, 0).toOption
                newDtt.nextDepartureTime shouldBe dtt.table(stationB).departure
                newDtt.currentRoute shouldBe None
                newDtt.completed shouldBe false
              case _ => fail()
          case _ => fail()

      "update the effective table with an effective arrival time" in:
        val expectedArrivalTime = dtt.table(stationB).arriving
        val minutesDelay        = 5
        val delay               = ClockTime(0, minutesDelay).toOption
        val effectiveArrivalTime = (expectedArrivalTime + delay) match
          case Some(at) =>
            dtt.departureUpdate(dtt.departureTime).flatMap(_.arrivalUpdate(at)) match
              case Some(newDtt) =>
                newDtt.effectiveTable.find(_._1 == stationB).map(_._2) shouldBe Some(TrainStationTime(
                  Some(at),
                  None,
                  None
                ))
                newDtt.nextRoute shouldBe Some(stationB, stationC)
                newDtt.currentDelay shouldBe delay
                newDtt.nextDepartureTime shouldBe dtt.table(stationB).departure + delay
                newDtt.currentRoute shouldBe None
                newDtt.completed shouldBe false
              case _ => fail()
          case _ => fail()

    "travels all the route" should:
      "be completed" in:
        for
          stationADepartureTime <- dtt.table(stationA).departure
          takeFirstRoute        <- dtt.departureUpdate(stationADepartureTime)
          stationBArrivalTime   <- takeFirstRoute.table(stationB).arriving
          completeFirstRoute    <- takeFirstRoute.arrivalUpdate(stationBArrivalTime)
          stationBDepartureTime <- completeFirstRoute.table(stationB).departure
          takeSecondRoute       <- completeFirstRoute.departureUpdate(stationBDepartureTime)
          stationCArrivalTime   <- takeSecondRoute.table(stationC).arriving
          completeSecondRoute   <- takeSecondRoute.arrivalUpdate(stationCArrivalTime)
          stationCDepartureTime <- completeSecondRoute.table(stationC).departure
          takeThirdRoute        <- completeSecondRoute.departureUpdate(stationCDepartureTime)
          stationDArrivalTime   <- takeThirdRoute.table(stationD).arriving
          completeThirdRoute    <- takeThirdRoute.arrivalUpdate(stationDArrivalTime)
        yield completeThirdRoute.completed shouldBe true

      "accumulate delays" in:
        val delays = List(
          ClockTime(0, 1),
          ClockTime(0, 2),
          ClockTime(0, 3),
          ClockTime(0, 4),
          ClockTime(0, 5),
          ClockTime(0, 6)
        ).map(_.getOrDefault)
        for
          stationADepartureTime <- dtt.table(stationA).departure + delays.headOption
          takeFirstRoute        <- dtt.departureUpdate(stationADepartureTime)
          stationBArrivalTime   <- takeFirstRoute.table(stationB).arriving + delays.lift(1)
          completeFirstRoute    <- takeFirstRoute.arrivalUpdate(stationBArrivalTime)
          stationBDepartureTime <- completeFirstRoute.table(stationB).departure + delays.lift(2)
          takeSecondRoute       <- completeFirstRoute.departureUpdate(stationBDepartureTime)
          stationCArrivalTime   <- takeSecondRoute.table(stationC).arriving + delays.lift(3)
          completeSecondRoute   <- takeSecondRoute.arrivalUpdate(stationCArrivalTime)
          stationCDepartureTime <- completeSecondRoute.table(stationC).departure + delays.lift(4)
          takeThirdRoute        <- completeSecondRoute.departureUpdate(stationCDepartureTime)
          stationDArrivalTime   <- takeThirdRoute.table(stationD).arriving + delays.lift(5)
          completeThirdRoute    <- takeThirdRoute.arrivalUpdate(stationDArrivalTime)
          _ = println("0000")
        yield completeThirdRoute.currentDelay shouldBe delays.lastOption

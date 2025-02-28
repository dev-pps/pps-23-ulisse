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
import ulisse.utils.Times.Time

class DynamicTimetableTest extends AnyWordSpec with Matchers:
  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905          = TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val stationA           = Station("A", Coordinate(0, 0), 1)
  private val stationB           = Station("B", Coordinate(0, 1), 1)
  private val stationC           = Station("C", Coordinate(0, 2), 1)
  private val stationD           = Station("D", Coordinate(0, 3), 1)
  private val stationE           = Station("E", Coordinate(0, 4), 1)
  private val timetableBuilder: TimetableBuilder =
    TimetableBuilder(train = train3905, startStation = stationA, departureTime = h(9).m(0).getOrDefault)
  private val railAV_10: RailInfo = RailInfo(length = 10, typeRoute = AV)
  private val tt: Timetable =
    timetableBuilder.stopsIn(stationB, waitTime = 5)(railAV_10)
      .transitIn(stationC)(railAV_10)
      .stopsIn(stationD, waitTime = 10)(railAV_10)
      .arrivesTo(stationE)(railAV_10)
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

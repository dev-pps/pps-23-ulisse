package ulisse.entities.simulation.environments.railwayEnvironment

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.Utils.TestUtility.getOrFail
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.RouteEnvironmentElementTest.*
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.route.Routes.{Route, RouteType}
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.*
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.station.StationEnvironmentElementTest.*
import ulisse.entities.station.StationTest.{stationA, stationB, stationC, stationD}
import ulisse.entities.timetable.DynamicTimetableTest.{
  dynamicTimetable1,
  dynamicTimetable2,
  dynamicTimetable3,
  dynamicTimetable4
}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgentTest.*
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.Time

object ConfigurationDataTest:
  val stations    = Seq(stationA, stationB, stationC, stationD)
  val stationsEE  = stations.map(makeStationEE)
  val routes      = Seq(normalRouteAB, routeAB, routeBC, routeCD)
  val routesEE    = routes.map(makeRouteEE).sortBy(_.typology.technology)
  val trainAgents = Seq(trainAgent3905, trainAgent3906, trainAgent3907)
  val timetables  = Seq(dynamicTimetable1, dynamicTimetable2, dynamicTimetable3)
  val simpleConfigurationData = ConfigurationData(
    Seq(stationA_EE, stationB_EE),
    Seq(routeAB_EE),
    Seq(trainAgent3905),
    Seq(dynamicTimetable1)
  )
  val complexConfigurationData = ConfigurationData(
    stationsEE,
    routesEE,
    trainAgents,
    timetables
  )
class ConfigurationDataTest extends AnyWordSpec with Matchers:
  private val cd = ConfigurationData(stationsEE, routesEE, trainAgents, timetables)

  "ConfigurationData" when:
    "created" should:
      "have all stations" in:
        cd.stations shouldBe stations

      "exclude in order duplicate stations" in:
        val stationADuplicate =
          makeStationEE(Station(stationA.name, stationA.coordinate, stationA.numberOfPlatforms + 1))
        val cd = ConfigurationData((stationsEE :+ stationADuplicate), routesEE, trainAgents, timetables)
        cd.stations shouldBe stations

      "have all routes" in:
        cd.routes should contain allElementsOf routesEE

      "have all routes sorted by technology" in:
        cd.routes shouldBe routesEE.sortBy(_.typology.technology).reverse

      "exclude in order duplicate routes" in:
        val routeABDuplicate = makeRouteEE(Route(
          routeAB.departure,
          routeAB.arrival,
          routeAB.typology,
          routeAB.railsCount,
          routeAB.length + 10
        ).getOrFail)
        val cd = ConfigurationData(stationsEE, (routesEE :+ routeABDuplicate), trainAgents, timetables)
        cd.routes should contain allElementsOf routesEE

      "have all trains in stations" in:
        cd.stations.collectTrains.isEmpty shouldBe false
        cd.routes.collectTrains.isEmpty shouldBe true

      "have at least a subset of all trains associated with a timetable" in:
        val timetablesTrains = timetables.map(_.train).distinct
        trainAgents.filter(ta =>
          timetablesTrains.map(_.name).contains(ta.name)
        ) should contain allElementsOf cd.stations.collectTrains

      "exclude in order duplicate trains" in:
        val train3905Duplicate =
          makeTrainAgent(Train(train3905.name, train3905.techType, train3905.wagon, train3905.length + 1))
        val cd = ConfigurationData(stationsEE, routesEE, Seq(trainAgent3905, train3905Duplicate), timetables)
        cd.stations.collectTrains shouldBe Seq(trainAgent3905)

      "have at least a subset of all timetables" in:
        timetables should contain allElementsOf cd.timetablesByTrain.flatMap(_._2)

      "exclude duplicate timetables" in:
        val cd = ConfigurationData(stationsEE, routesEE, trainAgents, Seq(dynamicTimetable1, dynamicTimetable1))
        cd.timetablesByTrain.flatMap(_._2) shouldBe Seq(dynamicTimetable1)

      "have placed the trains in their initial stations" in:
        val cd = ConfigurationData(stationsEE, routesEE, trainAgents, Seq(dynamicTimetable2, dynamicTimetable3))
        cd.stations.find(_.name == dynamicTimetable2.startStation.name).map(_.trains) shouldBe Some(
          Seq(dynamicTimetable2.train)
        )
        cd.stations.find(_.name == dynamicTimetable3.startStation.name).map(_.trains) shouldBe Some(
          Seq(dynamicTimetable3.train)
        )

      "exclude trains given priority for lower id's if its not possible to place in the initial station" in:
        val cd = ConfigurationData(
          stationsEE,
          routesEE,
          trainAgents,
          Seq(dynamicTimetable1, dynamicTimetable4, dynamicTimetable3)
        )
        cd.stations.collectTrains shouldBe Seq(dynamicTimetable1.train, dynamicTimetable3.train)

      "exclude all train timetables if the train fail to position" in:
        val cd = ConfigurationData(
          stationsEE,
          routesEE,
          trainAgents,
          Seq(dynamicTimetable2, dynamicTimetable4, dynamicTimetable3, dynamicTimetable1)
        )
        cd.timetablesByTrain.flatMap(_._2) shouldBe Seq(dynamicTimetable1, dynamicTimetable2, dynamicTimetable3)

    "created empty" should:
      "have no stations" in:
        ConfigurationData.empty().stations.isEmpty shouldBe true

      "have no routes" in:
        ConfigurationData.empty().routes.isEmpty shouldBe true

      "have no timetables" in:
        ConfigurationData.empty().timetablesByTrain.isEmpty shouldBe true

    "departure time" should:
      "be the minimum departure time of all timetables" in:
        cd.departureTime shouldBe dynamicTimetable1.departureTime

      "be Time(0,0,0) if no timetable is present" in:
        ConfigurationData.empty().departureTime shouldBe Time(0, 0, 0)

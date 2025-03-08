package ulisse.entities.simulation.data

import cats.Id
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.data.Statistics.*
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.complexConfigurationData
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.station.StationTest.stationA
import ulisse.entities.timetable.DynamicTimetableTest.{dynamicTimetable1, dynamicTimetable2}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906}
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.utils.Times.{ClockTime, Time}

class StatisticsTest extends AnyWordSpec with Matchers:
  "Statistics for TrainAgentInfo" when:
    val delay = ClockTime(0, 10).getOrDefault
    val updatedDtt1 =
      dynamicTimetable1.departureUpdate(Id(dynamicTimetable1.departureTime) + delay).getOrElse(dynamicTimetable1)
    val tai = TrainAgentInfo(trainAgent3905, Seq(dynamicTimetable2, updatedDtt1))
    "currentDynamicTimetable is requested" should:
      "find it if present" in:
        TrainAgentInfo(trainAgent3905, Seq(dynamicTimetable2, updatedDtt1)).currentDynamicTimetable shouldBe Some(
          updatedDtt1
        )

      "not find it if not present" in:
        TrainAgentInfo(trainAgent3906, Seq(dynamicTimetable2, updatedDtt1)).currentDynamicTimetable shouldBe None

    "delayInCurrentTimetable is requested" should:
      "find it if present" in:
        TrainAgentInfo(trainAgent3905, Seq(dynamicTimetable2, updatedDtt1)).delayInCurrentTimetable shouldBe Some(
          delay.asTime
        )

      "not find it if not present" in:
        TrainAgentInfo(trainAgent3905, Seq(dynamicTimetable2)).delayInCurrentTimetable shouldBe None

      "not find it if no current timetable" in:
        TrainAgentInfo(trainAgent3906, Seq(dynamicTimetable2, updatedDtt1)).delayInCurrentTimetable shouldBe None

  "Statistics for RailwayEnvironment" when:
    val delay = Time(0, 10, 0)
    val environment =
      RailwayEnvironment(Id(complexConfigurationData.departureTime) + Id(delay), complexConfigurationData)
    val updatedEnvironment = environment.doStep(1)
    "percStationsLoad is requested" should:
      "calculate it correctly" in:
        val totalPlatforms = environment.stations.map(_.numberOfPlatforms).sum.toDouble
        environment.percStationsLoad shouldBe environment.stations.collectTrains.size / totalPlatforms
        updatedEnvironment.percStationsLoad shouldBe updatedEnvironment.stations.collectTrains.size / totalPlatforms

    "trainsOnRoutes is requested" should:
      "calculate it correctly" in:
        environment.trainsOnRoutes shouldBe environment.routes.collectTrains.size
        updatedEnvironment.trainsOnRoutes shouldBe updatedEnvironment.routes.collectTrains.size

    "trainsInStations is requested" should:
      "calculate it correctly" in:
        environment.trainsInStations shouldBe environment.stations.collectTrains.size
        updatedEnvironment.trainsInStations shouldBe updatedEnvironment.stations.collectTrains.size

    "percTrainsOnRoutes is requested" should:
      "calculate it correctly" in:
        environment.percTrainsOnRoutes shouldBe environment.trainsOnRoutes.toDouble / environment.trains.size
        updatedEnvironment.percTrainsOnRoutes shouldBe updatedEnvironment.trainsOnRoutes.toDouble / updatedEnvironment.trains.size

    "percTrainsInStations is requested" should:
      "calculate it correctly" in:
        environment.percTrainsInStations shouldBe environment.trainsInStations.toDouble / environment.trains.size
        updatedEnvironment.percTrainsInStations shouldBe updatedEnvironment.trainsInStations.toDouble / updatedEnvironment.trains.size

    "cumulativeDelay is requested" should:
      "calculate it correctly" in:
        environment.cumulativeDelay shouldBe Time(0, 0, 0)
        updatedEnvironment.cumulativeDelay shouldBe Id(delay) + Id(delay)

    "averageDelay is requested" should:
      "calculate it correctly" in:
        environment.averageDelay shouldBe Time(0, 0, 0)
        updatedEnvironment.averageDelay shouldBe delay

    "cumulativeDelayIn is requested" should:
      "calculate it correctly" in:
        environment.cumulativeDelayIn(stationA) shouldBe Time(0, 0, 0)
        updatedEnvironment.cumulativeDelayIn(stationA) shouldBe Id(delay) + Id(delay)

    "averageDelayIn is requested" should:
      "calculate it correctly" in:
        environment.averageDelayIn(stationA) shouldBe Time(0, 0, 0)
        updatedEnvironment.averageDelayIn(stationA) shouldBe delay

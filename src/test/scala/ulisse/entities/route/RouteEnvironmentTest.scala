package ulisse.entities.route

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.StationEnvironmentElementTest.{stationA_EE, stationB_EE, stationC_EE}
import ulisse.entities.train.TrainAgentTest.trainAgent3905

class RouteEnvironmentTest extends AnyWordSpec with Matchers:

  private val mockedConfigurationData = mock[ConfigurationData]
  private val routes                = Seq(routeAB_EE, stationB_EE)
  when(mockedConfigurationData.stations).thenReturn(stations)
  private val stationEnvironment    = StationEnvironment(mockedConfigurationData)
  private val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)

  "StationEnvironment" when:
    "created" should:
      "contains the configuration data stations" in:
        stationEnvironment.environmentElements shouldBe stations

      "have no trains" in:
        stationEnvironment.trainAgents shouldBe Seq()

    "a train is put in" should:
      "return a new StationEnvironment with the train in the station" in:
        stationEnvironment.putTrain(trainAgent3905, stationA_EE) match
          case Some(se) => se.trainAgents shouldBe Seq(trainAgent3905)
          case _        => fail()

      "return None if the station is not in the environment" in:
        stationEnvironment.putTrain(trainAgent3905, stationC_EE) shouldBe None

      "reset the train movement before putting in the station" in:
        stationEnvironment.putTrain(updatedTrainAgent3905, stationA_EE).map(_.trainAgents) match
          case Some(Seq(train)) => train.distanceTravelled shouldBe 0
          case _                => fail()

    "a train is updated" should:
      "return a new StationEnvironment with the updated train" in:
        stationEnvironment.putTrain(trainAgent3905, stationA_EE).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(se) => se.trainAgents shouldBe Seq(updatedTrainAgent3905)
          case _        => fail()

      "return None if the train is not in the environment" in:
        stationEnvironment.updateTrain(trainAgent3905) shouldBe None

    "a train is removed" should:
      "return a new StationEnvironment without the train" in:
        stationEnvironment.putTrain(trainAgent3905, stationA_EE).flatMap(_.removeTrain(updatedTrainAgent3905)) match
          case Some(se) => se.trainAgents shouldBe Seq()
          case _        => fail()

      "return None if the train is not in the environment" in:
        stationEnvironment.removeTrain(trainAgent3905) shouldBe None


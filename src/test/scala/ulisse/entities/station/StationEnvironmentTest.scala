package ulisse.entities.station

import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.StationEnvironmentElementTest.{stationA_EE, stationB_EE, stationC_EE}
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906, trainAgent3907}

class StationEnvironmentTest extends AnyWordSpec with Matchers:

  private val stations                        = Seq(stationA_EE)
  private val configurationData               = ConfigurationData(stations, Seq(), Seq(), Seq())
  private val stationEnvironment              = StationEnvironment(configurationData)
  private val mockedStationEnvironmentElement = mock[StationEnvironmentElement]
  private val updatedTrainAgent3905           = trainAgent3905.updateDistanceTravelled(10)

  "StationEnvironment" when:
    "created" should:
      "contains the configuration data stations" in:
        stationEnvironment.environmentElements shouldBe configurationData.stations

      "have no trains" in:
        stationEnvironment.trainAgents shouldBe Seq()

    "a train is put in" should:
      "return a new StationEnvironment with the train in the station" in:
        stationEnvironment.putTrain(trainAgent3905, stationA_EE) match
          case Some(se) => se.trainAgents shouldBe Seq(trainAgent3905)
          case _        => fail()

      "return None if the station is full" in:
        stationEnvironment.putTrain(trainAgent3905, stationA_EE)
          .flatMap(_.putTrain(trainAgent3906, stationA_EE))
          .flatMap(_.putTrain(trainAgent3907, stationC_EE)) shouldBe None

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

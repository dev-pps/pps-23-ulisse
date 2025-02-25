package ulisse.entities.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsDirection.Forward
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class StationEnvironmentElementTest extends AnyWordSpec with Matchers:
  private val numberOfTracks            = 2
  private val station                   = Station("name", Coordinate(0, 0), numberOfTracks)
  private val stationEnvironmentElement = StationEnvironmentElement.apply(station)
  private val defaultTechnology         = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon              = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber        = 5
  private val train3905                 = TrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906                 = TrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907                 = TrainAgent(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  "A StationEnvironmentElement" when:
    "created" should:
      "have the same station info" in:
        stationEnvironmentElement.name shouldBe station.name
        stationEnvironmentElement.coordinate shouldBe station.coordinate
        stationEnvironmentElement.numberOfTracks shouldBe station.numberOfTracks

      "have 'numberOfTracks' empty tracks" in:
        stationEnvironmentElement.containers.size shouldBe numberOfTracks
        stationEnvironmentElement.containers.forall(_.trains.isEmpty) shouldBe true

  "A train" when:
    "arrive to a stationEnvironmentElement" should:
      "be place in a track if available" in:
        stationEnvironmentElement.putTrain(train3905, Forward) match
          case Some(updatedStation) =>
            updatedStation.containers.flatMap(_.trains) shouldBe Seq(train3905)
          case None => fail()

      "not be place in a track if it's already in the stationEnvironmentElement" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(_.putTrain(train3905, Forward)) shouldBe None
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.putTrain(train3905.updateDistanceTravelled(10), Forward)
        ) shouldBe None

      "not be place in a track if not available" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(_.putTrain(train3906, Forward)).flatMap(
          _.putTrain(train3907, Forward)
        ) shouldBe None

    "put in a platform" should:
      "be place in a track if available" in:
        stationEnvironmentElement.putTrain(train3905, Forward) match
          case Some(updatedStation) =>
            updatedStation.containers.flatMap(_.trains) shouldBe Seq(train3905)
          case None => fail()

      "not be place in a track if it's already occupied" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.putTrain(train3905, Forward)
        ) shouldBe None
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.putTrain(train3905.updateDistanceTravelled(10), Forward)
        ) shouldBe None
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.putTrain(train3906, Forward)
        ).flatMap(_.putTrain(train3907, Forward)) shouldBe None

      "not be place in a track if it's already in the stationEnvironmentElement" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.putTrain(train3905, Forward)
        ) shouldBe None
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.putTrain(train3905.updateDistanceTravelled(10), Forward)
        ) shouldBe None

    "update in a platform" should:
      "be updated if is present in the track" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.updateTrain(train3905)
        ) match
          case Some(updatedStation) =>
            updatedStation.containers.flatMap(_.trains) shouldBe Seq(train3905)
          case None => fail()

        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.updateTrain(train3905.updateDistanceTravelled(10))
        ) match
          case Some(updatedStation) =>
            updatedStation.containers.flatMap(_.trains) shouldBe Seq(train3905.updateDistanceTravelled(10))
          case None => fail()

      "not be updated if is not present in the track" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.updateTrain(train3906)
        ) shouldBe None

    "removed form a stationEnvironmentElement" should:
      "be removed from the track if it's present" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(_.removeTrain(train3905)) match
          case Some(updatedStation) =>
            updatedStation.containers.flatMap(_.trains) shouldBe Seq()
          case None => fail()
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(
          _.removeTrain(train3905.updateDistanceTravelled(10))
        ) match
          case Some(updatedStation) =>
            updatedStation.containers.flatMap(_.trains) shouldBe Seq()
          case None => fail()

      "not be removed from the track if it's not present" in:
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(_.removeTrain(train3906)) shouldBe None
        stationEnvironmentElement.putTrain(train3905, Forward).flatMap(_.removeTrain(train3905)).flatMap(
          _.removeTrain(train3905)
        ) shouldBe None

package ulisse.entities.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.station.StationEnvironmentElementTest.stationA_EE
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.station.StationTest.{
  defaultNumberOfPlatform,
  stationA,
  stationB,
  stationC,
  stationD,
  stationE,
  stationF
}
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906, trainAgent3907}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

object StationEnvironmentElementTest:
  val stationA_EE = makeStationEE(stationA)
  val stationB_EE = makeStationEE(stationB)
  val stationC_EE = makeStationEE(stationC)
  val stationD_EE = makeStationEE(stationD)
  val stationE_EE = makeStationEE(stationE)
  val stationF_EE = makeStationEE(stationF)
  def makeStationEE(station: Station): StationEnvironmentElement =
    StationEnvironmentElement(station)

class StationEnvironmentElementTest extends AnyWordSpec with Matchers:
  "A StationEnvironmentElement" when:
    "created" should:
      "have the same station info" in:
        stationA_EE shouldBe stationA

      "have 'numberOfTracks' empty tracks" in:
        stationA_EE.containers.size shouldBe defaultNumberOfPlatform
        stationA_EE.containers.forall(_.isEmpty) shouldBe true
        stationA_EE.containers.forall(_.isAvailable) shouldBe true

      "tracks are numerated sequentially" in:
        stationA_EE.containers.zipWithIndex.forall((platform, index) => platform.id == index + 1) shouldBe true

    "a train is put in" should:
      "be placed in the first platform" in:
        stationA_EE.putTrain(trainAgent3905) match
          case Some(us) =>
            us shouldBe stationA
            us.isAvailable shouldBe true
            us.containers.find(_.contains(trainAgent3905)).map(_.id) shouldBe Some(1)
          case None => fail()

      "not be placed if it's already in the station" in:
        stationA_EE.putTrain(trainAgent3905).flatMap(_.putTrain(trainAgent3905)) shouldBe None

      "be placed in the first available platform if other trains are present" in:
        stationA_EE.putTrain(trainAgent3905).flatMap(_.putTrain(trainAgent3906)) match
          case Some(us) =>
            us shouldBe stationA
            us.isAvailable shouldBe false
            us.containers.find(_.contains(trainAgent3906)).map(_.id) shouldBe Some(2)
          case None => fail()

      "not be placed if it's not available" in:
        stationA_EE.putTrain(trainAgent3905).flatMap(_.putTrain(trainAgent3906)).flatMap(
          _.putTrain(trainAgent3907)
        ) shouldBe None

    "a train is updated" should:
      "be updated if present" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(1)
        stationA_EE.putTrain(trainAgent3905).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(us) =>
            us shouldBe stationA
            us.containers.find(_.contains(trainAgent3905)).map(c => (c.id, c.trains)) shouldBe Some((
              1,
              Seq(updatedTrainAgent3905)
            ))
          case None => fail()

      "not be updated if not present" in:
        stationA_EE.updateTrain(trainAgent3905) shouldBe None

    "a train is removed" should:
      "be removed if it's present in a platform" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(1)
        stationA_EE.putTrain(trainAgent3905).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(
          _.removeTrain(trainAgent3905)
        ) match
          case Some(us) =>
            us shouldBe stationA
            us.containers.find(_.contains(trainAgent3905)) shouldBe None
          case None => fail()

      "not be removed if not present" in:
        stationA_EE.removeTrain(trainAgent3905) shouldBe None

    "a train is searched" should:
      "be found if there is a train with the same name in a track" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        stationA_EE.putTrain(trainAgent3905) match
          case Some(us) =>
            us.contains(trainAgent3905) shouldBe true
            us.contains(updatedTrainAgent3905) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name in a track" in:
        stationA_EE.putTrain(trainAgent3905).map(_.contains(trainAgent3906)) shouldBe Some(false)

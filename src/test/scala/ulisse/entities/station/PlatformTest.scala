package ulisse.entities.station

import cats.data.{Chain, NonEmptyChain}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.station.Platform.minPlatformId
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class PlatformTest extends AnyWordSpec with Matchers:
  private val id       = 1
  private val platform = Platform(id)

  extension (platform: Platform)
    def checkFields(trains: Seq[TrainAgent], isEmpty: Boolean, isAvailable: Boolean): Unit =
      platform.id shouldBe id
      platform.trains shouldBe trains
      platform.isEmpty shouldBe isEmpty
      platform.isAvailable shouldBe isAvailable

  "A Platform" when:
    "is created" should:
      "have a positive platform id" in:
        List(-1, -2, 0, 1, 2).map(minPlatformId + _).foreach(id =>
          Platform(id).id shouldBe math.max(minPlatformId, id)
        )

      "not contain any train" in:
        platform.checkFields(Seq(), true, true)

    "created checked" should:
      "have a platform id greater or equal than minPlatformId" in:
        List(0, 1, 2).map(minPlatformId + _).foreach(id =>
          Platform(id).id shouldBe math.max(1, id)
        )

      "return errors if the platform id is lower than minPlatformId" in:
        List(-2, -1).map(minPlatformId + _).foreach(id =>
          Platform.createCheckedPlatform(id) shouldBe Left(Chain(Platform.Error.InvalidPlatformId))
        )

      "not contain any train" in:
        Platform.createCheckedPlatform(id) match
          case Right(platform) => platform.checkFields(Seq(), true, true)
          case _               => fail()

    "a train is put in" should:
      "be updated with the specified train" in:
        platform.putTrain(trainAgent3905) match
          case Some(up) => up.checkFields(Seq(trainAgent3905), false, false)
          case _        => fail()

      "not be updated if the platform is not available" in:
        platform.putTrain(trainAgent3905).flatMap(_.putTrain(trainAgent3905)) shouldBe None
        platform.putTrain(trainAgent3905).flatMap(_.putTrain(trainAgent3906)) shouldBe None

      "not be updated if the train is already moved" in:
        platform.putTrain(trainAgent3905.updateDistanceTravelled(10)) shouldBe None

    "a train is updated" should:
      "be updated with the specified train if it's present" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        platform.putTrain(trainAgent3905).flatMap(
          _.updateTrain(updatedTrainAgent3905)
        ) match
          case Some(up) => up.checkFields(Seq(updatedTrainAgent3905), false, false)
          case _        => fail()

      "not be updated if the platform doesn't contain the specified train" in:
        platform.putTrain(trainAgent3905).flatMap(_.updateTrain(trainAgent3906)) shouldBe None

    "a train is removed" should:
      "be updated if the specified train it's present" in:
        platform.putTrain(trainAgent3905).flatMap(
          _.removeTrain(trainAgent3905)
        ) match
          case Some(up) => up.checkFields(Seq(), true, true)
          case _        => fail()

      "not be updated if the platform doesn't contain the specified train" in:
        platform.putTrain(trainAgent3905).flatMap(_.removeTrain(trainAgent3906)) shouldBe None

    "a train is searched" should:
      "be found if there is a train with the same name" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        platform.putTrain(trainAgent3905) match
          case Some(up) =>
            up.contains(trainAgent3905) shouldBe true
            up.contains(updatedTrainAgent3905) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name" in:
        platform.putTrain(trainAgent3905).map(_.contains(trainAgent3906)) shouldBe Some(false)

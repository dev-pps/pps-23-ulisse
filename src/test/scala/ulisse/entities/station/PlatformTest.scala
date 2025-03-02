package ulisse.entities.station

import cats.data.{Chain, NonEmptyChain}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.station.Platforms.Platform
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class PlatformTest extends AnyWordSpec with Matchers:
  private val id       = 1
  private val platform = Platform(id)

  "A Platform" when:
    "is created" should:
      "have a positive platform id" in:
        List(-1, 0, 1, 2).foreach(id =>
          Platform(id).id shouldBe math.max(1, id)
        )

      "not contain any train" in:
        platform.trains shouldBe Seq()
        platform.isEmpty shouldBe true
        platform.isAvailable shouldBe true

    "created checked" should:
      "have a positive platform id" in:
        List(1, 2).foreach(id =>
          Platform(id).id shouldBe math.max(1, id)
        )

      "return errors if the platform id is not positive" in:
        List(-1, 0).foreach(id =>
          Platform.createCheckedPlatform(id) shouldBe Left(Chain(Platforms.Errors.InvalidPlatformId))
        )

      "not contain any train" in:
        Platform.createCheckedPlatform(1) match
          case Right(platform) =>
            platform.trains shouldBe Seq()
            platform.isEmpty shouldBe true
            platform.isAvailable shouldBe true
          case _ => fail()

    "a train is put in" should:
      "be updated with the specified train" in:
        platform.putTrain(trainAgent3905) match
          case Some(up) =>
            up.id shouldBe id
            up.trains shouldBe Seq(trainAgent3905)
            up.isEmpty shouldBe false
            up.isAvailable shouldBe false
          case _ => fail()

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
          case Some(up) =>
            up.id shouldBe id
            up.trains shouldBe Seq(updatedTrainAgent3905)
            up.isEmpty shouldBe false
            up.isAvailable shouldBe false
          case _ => fail()

      "not be updated if the platform doesn't contain the specified train" in:
        platform.putTrain(trainAgent3905).flatMap(_.updateTrain(trainAgent3906)) shouldBe None

    "a train is removed" should:
      "be updated if the specified train it's present" in:
        platform.putTrain(trainAgent3905).flatMap(
          _.removeTrain(trainAgent3905)
        ) match
          case Some(up) =>
            up.id shouldBe id
            up.trains shouldBe Seq()
            up.isEmpty shouldBe true
            up.isAvailable shouldBe true
          case _ => fail()

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

package ulisse.entities.station

import cats.data.{Chain, NonEmptyChain}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class PlatformTest extends AnyWordSpec with Matchers:

  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905          = TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906          = TrainAgent.apply(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val id                = 1
  private val platform           = Platform(id)
  
  "A Platform" when:
    "is created" should:
      "have a positive platform number" in:
        List(-1, 0, 1, 2).foreach(platformNumber =>
          Platform(platformNumber).id shouldBe math.max(1, platformNumber)
        )

      "not contain any train" in:
        platform.trains shouldBe Seq()
        platform.isEmpty shouldBe true
        platform.isAvailable shouldBe true

    "created checked" should:
      "have a positive platform number" in:
        List(1, 2).foreach(platformNumber =>
          Platform(platformNumber).id shouldBe math.max(1, platformNumber)
        )

      "return errors if the platform number is not positive" in:
        List(-1, 0).foreach(platformNumber =>
          Platform.createCheckedPlatform(platformNumber) shouldBe Left(Chain(Platform.Errors.InvalidPlatformNumber))
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
        val id    = 1
        val train = train3905
        Platform(id).putTrain(train) match
          case Some(up) =>
            up.id shouldBe id
            up.trains shouldBe Seq(train)
            up.isEmpty shouldBe false
            up.isAvailable shouldBe false
          case _ => fail()

      "not be updated if the platform is not available" in:
        platform.putTrain(train3905).flatMap(_.putTrain(train3905)) shouldBe None
        platform.putTrain(train3905).flatMap(_.putTrain(train3906)) shouldBe None

      "not be updated if the train is already moved" in:
        platform.putTrain(train3905.updateDistanceTravelled(10)) shouldBe None

    "a train is updated" should:
      "be updated with the specified train if it's present" in:
        val id           = 1
        val train        = train3905
        val updatedTrain3905 = train.updateDistanceTravelled(10)
        Platform(id).putTrain(train).flatMap(
          _.updateTrain(updatedTrain3905)
        ) match
          case Some(up) =>
            up.id shouldBe id
            up.trains shouldBe Seq(updatedTrain3905)
            up.isEmpty shouldBe false
            up.isAvailable shouldBe false
          case _ => fail()

      "not be updated if the platform doesn't contain the specified train" in:
        platform.putTrain(train3905).flatMap(_.updateTrain(train3906)) shouldBe None

    "a train is removed" should:
      "be updated if the specified train it's present" in:
        val id    = 1
        val train = train3905
        Platform(id).putTrain(train).flatMap(
          _.removeTrain(train)
        ) match
          case Some(up) =>
            up.id shouldBe id
            up.trains shouldBe Seq()
            up.isEmpty shouldBe true
            up.isAvailable shouldBe true
          case _ => fail()

      "not be updated if the platform doesn't contain the specified train" in:
        platform.putTrain(train3905).flatMap(_.removeTrain(train3906)) shouldBe None

    "a train is searched" should:
      "be found if there is a train with the same name" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(10)
        platform.putTrain(train) match
          case Some(up) =>
            up.contains(train) shouldBe true
            up.contains(updatedTrain) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name" in:
        platform.putTrain(train3905).map(_.contains(train3906)) shouldBe Some(false)

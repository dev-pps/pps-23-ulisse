package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class PlatformTest extends AnyWordSpec with Matchers:

  "A Platform" when:
    "is created" should:
      "have a positive platform number" in:
        List(-1, 0, 1, 2).foreach(platformNumber =>
          Platform(platformNumber).id shouldBe math.max(1, platformNumber)
        )

      "not contain any train" in:
        Platform(1).trains shouldBe Seq()

    "is checked" should:
      "be created if the platform number is greater than 0" in:
        List(1, 2).foreach(platformNumber =>
          Platform.createCheckedPlatform(platformNumber).map(t => (t.id, t.trains)) shouldBe Right((
            platformNumber,
            Seq()
          ))
        )

      "return chain of errors if the platform number is lesser or equal than 0" in:
        List(-1, 0).foreach(platformNumber =>
          Platform.createCheckedPlatform(platformNumber) shouldBe Left(Chain(Platform.Errors.InvalidPlatformNumber))
        )
    "is created sequentially" should:
      "be a platforms list with sequential number of platform starting from 1" in:
        List(1, 2, 5, 10).foreach: platformNumber =>
          Platform.generateSequentialPlatforms(platformNumber).zip(1 to platformNumber).foreach:
            case (track, expectedPlatformNumber) =>
              (track.id, track.trains) shouldBe (expectedPlatformNumber, Seq())

      "be a empty platforms list if desired number of platform is lesser or equal than 0" in:
        List(-1, 0).foreach(invalidPlatformNumber =>
          Platform.generateSequentialPlatforms(invalidPlatformNumber) shouldBe List()
        )

    "a train is put in" should:
      "return a new platform with the specified train" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val platformNumber = 1
        val platform       = Platform(platformNumber)
        platform.putTrain(train).map(p => (p.id, p.trains)) shouldBe Some(platformNumber, Seq(train))

      "return none if the platform already contains a train" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val otherTrain =
          TrainAgent(Train("3906", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val platformNumber = 1
        val platform       = Platform(platformNumber).putTrain(train)
        platform.flatMap(_.putTrain(train)) shouldBe None
        platform.flatMap(_.putTrain(otherTrain)) shouldBe None

    "a train is updated" should:
      "return a new platform with the specified train if it's present" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val updatedTrain   = train.updateDistanceTravelled(10)
        val platformNumber = 1
        val platform       = Platform(platformNumber).putTrain(train)
        platform.flatMap(_.updateTrain(train)).map(p => (p.id, p.trains)) shouldBe Some(
          platformNumber,
          Seq(train)
        )
        platform.flatMap(_.updateTrain(updatedTrain)).map(p => (p.id, p.trains)) shouldBe Some(
          platformNumber,
          Seq(updatedTrain)
        )

      "return none if the platform doesn't contain the specified train" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val otherTrain =
          TrainAgent(Train("3906", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val platformNumber = 1
        val platform       = Platform(platformNumber).putTrain(train)
        platform.flatMap(_.updateTrain(otherTrain)) shouldBe None

    "a train is removed" should:
      "return a new platform without the specified train if it's present" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val updatedTrain   = train.updateDistanceTravelled(10)
        val platformNumber = 1
        val platform       = Platform(platformNumber).putTrain(train)
        platform.flatMap(_.removeTrain(train)).map(p => (p.id, p.trains)) shouldBe Some((
          platformNumber,
          Seq()
        ))
        platform.flatMap(_.removeTrain(updatedTrain)).map(p => (p.id, p.trains)) shouldBe Some((
          platformNumber,
          Seq()
        ))

      "return none if the platform doesn't contain the specified train" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val otherTrain =
          TrainAgent(Train("3906", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val platformNumber = 1
        val platform       = Platform(platformNumber).putTrain(train)
        platform.flatMap(_.removeTrain(otherTrain)) shouldBe None

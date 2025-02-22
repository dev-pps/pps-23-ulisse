package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.entities.train.TrainAgent

class PlatformTest extends AnyWordSpec with Matchers:

  "A Platform" when:
    "is created" should:
      "have a positive platform number" in:
        List(-1, 0, 1, 2).foreach(platformNumber =>
          Platform(platformNumber).platformNumber shouldBe math.max(1, platformNumber)
        )

      "not contain any train" in:
        Platform(1).train shouldBe None

    "is checked" should:
      "be created if the platform number is greater than 0" in:
        List(1, 2).foreach(platformNumber =>
          Platform.createCheckedPlatform(platformNumber).map(t => (t.platformNumber, t.train)) shouldBe Right((
            platformNumber,
            None
          ))
        )

      "return chain of errors if the platform number is lesser or equal than 0" in:
        List(-1, 0).foreach(platformNumber =>
          Platform.createCheckedPlatform(platformNumber) shouldBe Left(Chain(Platform.Error.InvalidPlatformNumber))
        )
    "is created sequentially" should:
      "be a Platforms list with sequential number of platform starting from 1" in:
        List(1, 2, 5, 10).foreach(platformNumber =>
          Platform.generateSequentialPlatforms(platformNumber).zip(1 to platformNumber).foreach {
            case (track, expectedPlatformNumber) =>
              (track.platformNumber, track.train) shouldBe (expectedPlatformNumber, None)
          }
        )

      "be a empty platforms list if desired number of platform is lesser or equal than 0" in:
        List(-1, 0).foreach(invalidPlatformNumber =>
          Platform.generateSequentialPlatforms(invalidPlatformNumber) shouldBe List()
        )

    "is updated with a train" should:
      "return a new platform with the specified train" in:
        val train =
          TrainAgent(Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5))
        val platformNumber = 1
        val platform       = Platform(platformNumber)
        platform.withTrain(Some(train)).train shouldBe Some(train)
        platform.withTrain(Some(train)).platformNumber shouldBe platformNumber
        platform.withTrain(None).train shouldBe None
        platform.withTrain(None).platformNumber shouldBe platformNumber
        platform.withTrain(Some(train)).withTrain(None).train shouldBe None
        platform.withTrain(Some(train)).withTrain(None).platformNumber shouldBe platformNumber

package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrackTest extends AnyWordSpec with Matchers:

  "A Track" when:
    "is created" should:
      "have a positive platform number" in:
        List(-1, 0, 1, 2).foreach(platformNumber =>
          Track(platformNumber).platform shouldBe math.max(1, platformNumber)
        )

      "not contain any train" in:
        Track(1).train shouldBe None

    "is checked" should:
      "be created if the platform number is greater than 0" in:
        List(1, 2).foreach(platformNumber =>
          Track.createCheckedTrack(platformNumber).map(t => (t.platform, t.train)) shouldBe Right((
            platformNumber,
            None
          ))
        )

      "return chain of errors if the platform number is lesser or equal than 0" in:
        List(-1, 0).foreach(platformNumber =>
          Track.createCheckedTrack(platformNumber) shouldBe Left(Chain(Track.Error.InvalidPlatformNumber))
        )
    "is created sequentially" should:
      "be a Track list with sequential number of platform starting from 1" in:
        List(1, 2, 5, 10).foreach(platformNumber =>
          Track.generateSequentialTracks(platformNumber).zip(1 to platformNumber).foreach {
            case (track, expectedPlatformNumber) =>
              (track.platform, track.train) shouldBe (expectedPlatformNumber, None)
          }
        )

      "be a empty track list if desired number of platform is lesser or equal than 0" in:
        List(-1, 0).foreach(invalidPlatformNumber =>
          Track.generateSequentialTracks(invalidPlatformNumber) shouldBe List()
        )

    "is updated with a train" should:
      "return a new track with the specified train" in:
        val train       = Train("3905", TrainTechnology("HighSpeed", 300, 1.0, 0.5), Wagon(UseType.Passenger, 50), 5)
        val trackNumber = 1
        val track       = Track(trackNumber)
        track.withTrain(Some(train)).train shouldBe Some(train)
        track.withTrain(Some(train)).platform shouldBe trackNumber
        track.withTrain(None).train shouldBe None
        track.withTrain(None).platform shouldBe trackNumber
        track.withTrain(Some(train)).withTrain(None).train shouldBe None
        track.withTrain(Some(train)).withTrain(None).platform shouldBe trackNumber

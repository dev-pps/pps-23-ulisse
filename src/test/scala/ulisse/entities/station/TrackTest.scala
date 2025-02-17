package ulisse.entities.station

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate

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
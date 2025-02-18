package ulisse.entities.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.entities.station.StationEnvironmentElement.*

class StationEnvironmentElementTest extends AnyWordSpec with Matchers:
  private val station = StationEnvironmentElement.createStationEnvironmentElement(Station("name", Coordinate(0, 0), 1))
  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train              = Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber)
  private val otherTrain         = Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber)

  "A train" when:
    "arrive to a station" should:
      "be place in a track if available" in:
        val updatedStation = train.arriveAt(station)
        station.firstAvailableTrack shouldBe Some(Track(1))
        updatedStation.tracks.headOption.flatMap(_.train) shouldBe Some(train)
        updatedStation.firstAvailableTrack shouldBe None

      "not be place in a track if not available" in:
        val station = StationEnvironmentElement.createStationEnvironmentElement(Station("name", Coordinate(0, 0), 1))
        val updatedStation = train.arriveAt(train.arriveAt(station))
        station.firstAvailableTrack shouldBe Some(Track(1))
        updatedStation.tracks.headOption.flatMap(_.train) shouldBe Some(train)
        updatedStation.firstAvailableTrack shouldBe None

    "leave a station" should:
      "be removed from the track if it's present" in:
        val updatedStation = train.leave(train.arriveAt(station))
        updatedStation.tracks.headOption.flatMap(_.train) shouldBe None
        updatedStation.firstAvailableTrack shouldBe Some(Track(1))

      "not be removed from the track if it's not present" in:
        val updatedStation = otherTrain.leave(train.arriveAt(station))
        updatedStation.tracks.headOption.flatMap(_.train) shouldBe Some(train)
        updatedStation.firstAvailableTrack shouldBe None

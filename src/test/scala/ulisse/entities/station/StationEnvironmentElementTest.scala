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
        station.firstAvailableTrack shouldBe Some(Track(1))
        train.arriveAt(station) match
          case Some(updatedStation) =>
            updatedStation.tracks.headOption.flatMap(_.train) shouldBe Some(train)
            updatedStation.firstAvailableTrack shouldBe None
          case None => fail()

      "not be place in a track if not available" in:
        train.arriveAt(station).flatMap(train.arriveAt) shouldBe None

    "leave a station" should:
      "be removed from the track if it's present" in:
        train.arriveAt(station).flatMap(train.leave) match
          case Some(updatedStation) =>
            updatedStation.tracks.headOption.flatMap(_.train) shouldBe None
            updatedStation.firstAvailableTrack shouldBe Some(Track(1))
          case None => fail()

      "not be removed from the track if it's not present" in:
        train.arriveAt(station).flatMap(otherTrain.leave) shouldBe None

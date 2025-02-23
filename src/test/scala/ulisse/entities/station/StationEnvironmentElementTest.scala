package ulisse.entities.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.train.TrainAgent

class StationEnvironmentElementTest extends AnyWordSpec with Matchers:
  private val numberOfTracks            = 2
  private val station                   = Station("name", Coordinate(0, 0), numberOfTracks)
  private val stationEnvironmentElement = StationEnvironmentElement.apply(station)
  private val defaultTechnology         = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon              = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber        = 5
  private val train3905                 = TrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906                 = TrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907                 = TrainAgent(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  "A StationEnvironmentElement" when:
    "created" should:
      "have the same name and coordinate as the station" in:
        stationEnvironmentElement.name shouldBe station.name
        stationEnvironmentElement.coordinate shouldBe station.coordinate

      "have 'numberOfTracks' empty tracks" in:
        stationEnvironmentElement.platforms.size shouldBe numberOfTracks
        stationEnvironmentElement.platforms.forall(_.train.isEmpty) shouldBe true

      "have a first available track" in:
        stationEnvironmentElement.firstAvailablePlatform shouldBe Some(Platform(1))

  "A train" when:
    "arrive to a stationEnvironmentElement" should:
      "be place in a track if available" in:
        stationEnvironmentElement.firstAvailablePlatform shouldBe Some(Platform(1))
        train3905.arriveAt(stationEnvironmentElement) match
          case Some(updatedStation) =>
            updatedStation.platforms.headOption.flatMap(_.train) shouldBe Some(train3905)
            updatedStation.firstAvailablePlatform shouldBe Some(Platform(2))
          case None => fail()

      "not be place in a track if it's already in the stationEnvironmentElement" in:
        train3905.arriveAt(stationEnvironmentElement).flatMap(train3905.arriveAt) shouldBe None

      "not be place in a track if not available" in:
        train3905.arriveAt(stationEnvironmentElement).flatMap(train3906.arriveAt).flatMap(
          train3907.arriveAt
        ) shouldBe None

    "leave a stationEnvironmentElement" should:
      "be removed from the track if it's present" in:
        train3905.arriveAt(stationEnvironmentElement).flatMap(train3905.leave) match
          case Some(updatedStation) =>
            updatedStation.platforms.headOption.flatMap(_.train) shouldBe None
            updatedStation.firstAvailablePlatform shouldBe Some(Platform(1))
          case None => fail()

      "not be removed from the track if it's not present" in:
        train3905.arriveAt(stationEnvironmentElement).flatMap(train3906.leave) shouldBe None

    "find in stations" should:
      "be found if is present" in:
        val seeOption = train3905.arriveAt(stationEnvironmentElement)
        seeOption.flatMap(see => train3905.findInStations(Seq(see))) shouldBe seeOption

      "not be found if is not present" in:
        train3905.findInStations(Seq(stationEnvironmentElement)) shouldBe None

    "update track" should:
      "be updated if the track is available" in:
        stationEnvironmentElement.updatePlatform(Platform(1), Some(train3905)) match
          case Some(updatedStation) =>
            updatedStation.platforms shouldBe List(Platform(1).withTrain(Some(train3905)), Platform(2))
          case None => fail()

      "not be updated if the track is not available" in:
        stationEnvironmentElement.updatePlatform(Platform(1), Some(train3905)) match
          case Some(updatedStation) =>
            updatedStation.platforms shouldBe List(Platform(1).withTrain(Some(train3905)), Platform(2))
          case None => fail()

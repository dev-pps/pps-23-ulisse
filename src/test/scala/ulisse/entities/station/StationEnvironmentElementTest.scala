package ulisse.entities.station

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.Coordinate
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class StationEnvironmentElementTest extends AnyWordSpec with Matchers:
  private val numberOfTracks     = 2
  private val station            = Station("name", Coordinate(0, 0), numberOfTracks)
  private val stationEE          = StationEnvironmentElement.apply(station)
  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905          = TrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906          = TrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907          = TrainAgent(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  private def validateStationInfo(stationEE: StationEnvironmentElement): Unit =
    stationEE.id shouldBe station.id
    stationEE.name shouldBe station.name
    stationEE.coordinate shouldBe station.coordinate
    stationEE.numberOfTracks shouldBe station.numberOfTracks

  "A StationEnvironmentElement" when:
    "created" should:
      "have the same station info" in:
        validateStationInfo(stationEE)

      "have 'numberOfTracks' empty tracks" in:
        stationEE.containers.size shouldBe numberOfTracks
        stationEE.containers.forall(_.isEmpty) shouldBe true
        stationEE.containers.forall(_.isAvailable) shouldBe true

      "tracks are numerated sequentially" in:
        stationEE.containers.zipWithIndex.forall((platform, index) => platform.id == index + 1) shouldBe true

    "a train is put in" should:
      "be placed in the first platform" in:
        stationEE.putTrain(train3905) match
          case Some(us) =>
            validateStationInfo(us)
            us.isAvailable shouldBe true
            us.containers.find(_.contains(train3905)).map(_.id) shouldBe Some(1)
          case None => fail()

      "not be placed if it's already in the station" in:
        stationEE.putTrain(train3905).flatMap(_.putTrain(train3905)) shouldBe None

      "be placed in the first available platform if other trains are present" in:
        stationEE.putTrain(train3905).flatMap(_.putTrain(train3906)) match
          case Some(us) =>
            validateStationInfo(us)
            us.isAvailable shouldBe false
            us.containers.find(_.contains(train3906)).map(_.id) shouldBe Some(2)
          case None => fail()

      "not be placed if it's not available" in:
        stationEE.putTrain(train3905).flatMap(_.putTrain(train3906)).flatMap(
          _.putTrain(train3907)
        ) shouldBe None

    "a train is updated" should:
      "be updated if present" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(1)
        stationEE.putTrain(train).flatMap(_.updateTrain(updatedTrain)) match
          case Some(us) =>
            validateStationInfo(us)
            us.containers.find(_.contains(train3905)).map(c => (c.id, c.trains)) shouldBe Some((1, Seq(updatedTrain)))
          case None => fail()

      "not be updated if not present" in:
        stationEE.updateTrain(train3905) shouldBe None

    "a train is removed" should:
      "be removed if it's present in a platform" in:
        val train        = train3905
        val trainUpdated = train.updateDistanceTravelled(1)
        stationEE.putTrain(train).flatMap(_.updateTrain(trainUpdated)).flatMap(_.removeTrain(train)) match
          case Some(us) =>
            validateStationInfo(us)
            us.containers.find(_.contains(train)) shouldBe None
          case None => fail()

      "not be removed if not present" in:
        stationEE.removeTrain(train3905) shouldBe None

    "a train is searched" should:
      "be found if there is a train with the same name in a track" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(10)
        stationEE.putTrain(train) match
          case Some(us) =>
            us.contains(train) shouldBe true
            us.contains(updatedTrain) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name in a track" in:
        stationEE.putTrain(train3905).map(_.contains(train3906)) shouldBe Some(false)

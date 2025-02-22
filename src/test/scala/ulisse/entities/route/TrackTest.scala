package ulisse.entities.route

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrackTest extends AnyWordSpec with Matchers:
  given configuration: TrackConfiguration = TrackConfiguration(100.0, 200.0)
  private val defaultTechnology           = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon                = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber          = 5
  private val train3905 =
    TrainAgent.createTrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 =
    TrainAgent.createTrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907 =
    TrainAgent.createTrainAgent(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  "A track" when:
    "created" should:
      "be created empty by default" in:
        val track = Track()
        track.isEmpty shouldBe true
        track.configuration shouldBe configuration

      "be created with a list of trains" in:
        val track = Track(train3905, train3906, train3907)
        track.trains.size shouldBe 3
        track.configuration shouldBe configuration

      "contain distinct trains" in:
        val track = Track(train3905, train3905, train3905)
        track.trains.size shouldBe 1
        track.configuration shouldBe configuration

      "contain distinct trains considering the name" in:
        val track = Track(train3905, train3905.updateDistanceTravelled(10))
        track.trains.size shouldBe 1
        track.configuration shouldBe configuration

      "contains trains with distance travelled less than the track length" in:
        val track =
          Track(train3905, train3906.updateDistanceTravelled(configuration.trackLength - train3906.length + 1))
        track.trains.size shouldBe 1
        track.configuration shouldBe configuration

      "contains trains with a length less than the track length" in:
        given configuration: TrackConfiguration = TrackConfiguration(100.0, train3905.length - 1)
        val track                               = Track(train3905)
        track.trains.size shouldBe 0
        track.configuration shouldBe configuration

    "created checked" should:
      "be created if contains distinct trains" in:
        Track.createCheckedTrack(
          train3905,
          train3906,
          train3907
        ) shouldBe Right(Track(train3905, train3906, train3907))

      "return errors when not contains distinct trains" in:
        Track.createCheckedTrack(
          train3905,
          train3905,
          train3905
        ) shouldBe Left(Chain(Track.Errors.DuplicateTrains))

      "return errors when the train length is greater than the track length" in:
        given configuration: TrackConfiguration = TrackConfiguration(100.0, train3905.length - 1)
        Track.createCheckedTrack(
          train3905
        ) shouldBe Left(Chain(Track.Errors.TrainTooLong))

      "return errors when the train distance travelled is greater than the track length" in:
        Track.createCheckedTrack(
          train3905.updateDistanceTravelled(configuration.trackLength - train3905.length + 1)
        ) shouldBe Left(Chain(Track.Errors.TrainOutOfTrack))

    "a new train is added" should:
      "be updated with the new train" in:
        val track = Track(train3905, train3906)
        track :+ train3907 match
          case Left(_) => fail()
          case Right(ut) =>
            ut.trains.size shouldBe 3
            ut.configuration shouldBe configuration

      "not be updated if the train is already moved" in:
        Track(train3905) :+ train3906.updateDistanceTravelled(10) shouldBe Left(Chain(Track.Errors.TrainAlreadyMoved))

      "not be updated if the train is already present" in:
        Track(train3905) :+ train3905 shouldBe Left(Chain(Track.Errors.DuplicateTrains))

      "not be updated if the train is already present with the same name" in:
        Track(train3905) :+ train3905.updateDistanceTravelled(10) shouldBe Left(Chain(
          Track.Errors.TrainAlreadyMoved,
          Track.Errors.DuplicateTrains
        ))

      "not be updated if the train is too long" in:
        given configuration: TrackConfiguration = TrackConfiguration(100.0, train3905.length - 1)
        Track() :+ train3905 shouldBe Left(Chain(Track.Errors.TrainTooLong))

    "a train is updated with a new train" should:
      "be updated with the new train" in:
        Track(train3905, train3906).updateWhen(_.name == train3905.name)(_ =>
          train3907
        ) match
          case Left(_) => fail()
          case Right(ut) =>
            ut.trains shouldBe Seq(train3907, train3906)
            ut.configuration shouldBe configuration

      "not be updated if the train is not present" in:
        Track(train3905, train3906).updateWhen(_.name == train3907.name)(_ =>
          train3907
        ) match
          case Left(_) => fail()
          case Right(ut) =>
            ut.trains shouldBe Seq(train3905, train3906)
            ut.configuration shouldBe configuration

      "not be updated if the train is already present" in:
        Track(train3905, train3906).updateWhen(_.name == train3905.name)(_ =>
          train3906
        ) shouldBe Left(Chain(Track.Errors.DuplicateTrains))

      "not be updated if is already present a train with the same name" in:
        Track(train3905, train3906).updateWhen(_.name == train3905.name)(_ =>
          train3906.updateDistanceTravelled(10)
        ) shouldBe Left(Chain(Track.Errors.DuplicateTrains))

      "not be updated if the train is too long" in:
        val train3908 =
          TrainAgent.createTrainAgent(Train("3908", defaultTechnology, defaultWagon, defaultWagonNumber - 1))
        given configuration: TrackConfiguration = TrackConfiguration(100.0, train3905.length - 1)
        Track(train3908).updateWhen(_.name == train3908.name)(_ =>
          train3905
        ) shouldBe Left(Chain(Track.Errors.TrainTooLong))

      "not be updated if the train is out of track" in:
        Track(train3905).updateWhen(_.name == train3905.name)(_ =>
          train3905.updateDistanceTravelled(configuration.trackLength - train3905.length + 1)
        ) shouldBe Left(Chain(Track.Errors.TrainOutOfTrack))

    "A track" should:
      "be filtered" in:
        Track(train3905, train3906).filterNot(
          _.name == train3905.name
        ).trains shouldBe Seq(train3906)
        Track(train3905, train3906).filterNot(
          _.name == train3907.name
        ).trains shouldBe Seq(train3905, train3906)

      "be available" in:
        Track().isAvailable shouldBe true
        Track(
          train3905.updateDistanceTravelled(configuration.minPermittedDistanceBetweenTrains + train3905.length)
        ).isAvailable shouldBe true

      "not be available" in:
        Track(train3905).isAvailable shouldBe false
        Track(
          train3905.updateDistanceTravelled(configuration.minPermittedDistanceBetweenTrains / 2)
        ).isAvailable shouldBe false

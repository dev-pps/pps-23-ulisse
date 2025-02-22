package ulisse.entities.route

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrackTest extends AnyWordSpec with Matchers:
  private val defaultMinPermittedDistanceBetweenTrains = 100.0
  private val defaultTechnology                        = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon                             = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber                       = 5
  private val train3905 =
    TrainAgent.createTrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 =
    TrainAgent.createTrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907 =
    TrainAgent.createTrainAgent(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  "A track" when:
    "created" should:
      "be created empty by default" in:
        val track = Track(defaultMinPermittedDistanceBetweenTrains)
        track.isEmpty shouldBe true
        track.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

      "be created with a list of trains" in:
        val track = Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906, train3907)
        track.trains.size shouldBe 3
        track.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

      "contain distinct trains" in:
        val track = Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3905, train3905)
        track.trains.size shouldBe 1
        track.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

      "contain distinct trains considering the name" in:
        val track = Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3905.updateDistanceTravelled(10))
        track.trains.size shouldBe 1
        track.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

    "created checked" should:
      "be created if contains distinct trains" in:
        Track.createCheckedTrack(
          defaultMinPermittedDistanceBetweenTrains,
          train3905,
          train3906,
          train3907
        ) shouldBe Right(Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906, train3907))

      "return errors when not contains distinct trains" in:
        Track.createCheckedTrack(
          defaultMinPermittedDistanceBetweenTrains,
          train3905,
          train3905,
          train3905
        ) shouldBe Left(Chain(Track.Errors.DuplicateTrains))

    "a new train is added" should:
      "be updated with the new train" in:
        val track = Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906)
        track :+ train3907 match
          case Left(_) => fail()
          case Right(ut) =>
            ut.trains.size shouldBe 3
            ut.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

    "a already present train is added" should:
      "not be updated" in:
        val track = Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906)
        track :+ train3905 shouldBe Left(Chain(Track.Errors.DuplicateTrains))

    "a train is updated with a new train" should:
      "be updated with the new train" in:
        Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906).updateWhen(_.name == train3905.name)(_ =>
          train3907
        ) match
          case Left(_) => fail()
          case Right(ut) =>
            ut.trains shouldBe Seq(train3907, train3906)
            ut.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

      "not be updated if the train is not present" in:
        Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906).updateWhen(_.name == train3907.name)(_ =>
          train3907
        ) match
          case Left(_) => fail()
          case Right(ut) =>
            ut.trains shouldBe Seq(train3905, train3906)
            ut.minPermittedDistanceBetweenTrains shouldBe defaultMinPermittedDistanceBetweenTrains

      "not be updated if the train is already present" in:
        Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906).updateWhen(_.name == train3905.name)(_ =>
          train3906
        ) shouldBe Left(Chain(Track.Errors.DuplicateTrains))

      "not be updated if is already present a train with the same name" in:
        Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906).updateWhen(_.name == train3905.name)(_ =>
          train3906.updateDistanceTravelled(10)
        ) shouldBe Left(Chain(Track.Errors.DuplicateTrains))

    "A track" should:
      "be filtered" in:
        Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906).filterNot(
          _.name == train3905.name
        ).trains shouldBe Seq(train3906)
        Track(defaultMinPermittedDistanceBetweenTrains, train3905, train3906).filterNot(
          _.name == train3907.name
        ).trains shouldBe Seq(train3905, train3906)

      "be available" in:
        Track(defaultMinPermittedDistanceBetweenTrains).isAvailable shouldBe true
        Track(
          defaultMinPermittedDistanceBetweenTrains,
          train3905.updateDistanceTravelled(defaultMinPermittedDistanceBetweenTrains + train3905.length)
        ).isAvailable shouldBe true

      "not be available" in:
        Track(defaultMinPermittedDistanceBetweenTrains, train3905).isAvailable shouldBe false
        Track(
          defaultMinPermittedDistanceBetweenTrains,
          train3905.updateDistanceTravelled(defaultMinPermittedDistanceBetweenTrains / 2)
        ).isAvailable shouldBe false

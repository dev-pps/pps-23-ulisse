package ulisse.entities.route

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.route.Track.TrainAgentsDirection.{Backward, Forward}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrackTest extends AnyWordSpec with Matchers:
  given minPermittedDistanceBetweenTrains: Double = 100.0
  private val defaultTechnology                   = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon                        = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber                  = 5
  private val train3905 = TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 = TrainAgent.apply(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))

  "A track" when:
    "created" should:
      "have a positive track number" in:
        List(-1, 0, 1, 2).foreach(trackNumber =>
          Track(trackNumber).id shouldBe math.max(1, trackNumber)
        )

      "not contain any train" in:
        Track(1).trains shouldBe Seq()
        Track(1).isEmpty shouldBe true
        Track(1).isAvailable(Forward) shouldBe true
        Track(1).isAvailable(Backward) shouldBe true

      "have a min permitted distance between trains" in:
        Track(1).minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains

      "not have a set direction" in:
        Track(1).currentDirection shouldBe None

    "created checked" should:
      "have a positive track number" in:
        List(1, 2).foreach(trackNumber =>
          Track.createCheckedTrack(trackNumber).map(_.id) shouldBe Right(trackNumber)
        )

      "return errors if the track number is not positive" in:
        List(-1, 0).foreach(trackNumber =>
          Track.createCheckedTrack(trackNumber) shouldBe Left(Chain(Track.Errors.InvalidTrackNumber))
        )

      "not contain any train" in:
        Track.createCheckedTrack(1) match
          case Right(track) =>
            track.trains shouldBe Seq()
            track.isEmpty shouldBe true
            track.isAvailable(Forward) shouldBe true
            track.isAvailable(Backward) shouldBe true
          case _ => fail()

      "have a min permitted distance between trains" in:
        Track.createCheckedTrack(1) match
          case Right(track) =>
            track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
          case _ => fail()

      "not have a set direction" in:
        Track.createCheckedTrack(1) match
          case Right(track) =>
            track.currentDirection shouldBe None
          case _ => fail()

    "a train is put in" should:
      "be updated with the specified train" in:
        val id        = 1
        val train     = train3905
        val direction = Forward
        Track(id).putTrain(train, direction) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(train)
            ut.isEmpty shouldBe false
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "not be updated if the track is not available" in:
        val direction = Forward
        Track(1).putTrain(train3905, direction).flatMap(_.putTrain(train3905, direction)) shouldBe None
        Track(1).putTrain(train3905, direction).flatMap(_.putTrain(train3906, direction)) shouldBe None

      "not be updated if the train is already moved" in:
        Track(1).putTrain(train3905.updateDistanceTravelled(10), Forward) shouldBe None

      "contain multiple trains" in:
        val id           = 1
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain   = train3906
        val direction    = Forward
        Track(1).putTrain(train, direction).flatMap(
          _.updateTrain(updatedTrain)
        ).flatMap(_.putTrain(otherTrain, direction)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrain, otherTrain)
            ut.isEmpty shouldBe false
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case None => fail()

      "not contain multiple train in different directions" in:
        Track(1).putTrain(train3905, Forward).flatMap(
          _.updateTrain(train3905.updateDistanceTravelled(train3905.lengthSize))
        ).flatMap(_.putTrain(train3906, Backward)) shouldBe None

      "not contain the same train multiple times" in:
        val direction = Forward
        Track(1).putTrain(train3905, direction).flatMap(
          _.updateTrain(train3905.updateDistanceTravelled(train3905.lengthSize))
        ).flatMap(_.putTrain(train3905, direction)) shouldBe None

    "a train is updated" should:
      "be updated with the specified train if it's present" in:
        val id           = 1
        val direction    = Forward
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(10)
        Track(id).putTrain(train, direction).flatMap(_.updateTrain(updatedTrain)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrain)
            ut.isEmpty shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "leave track unavailable" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains - 1)
        Track(1).putTrain(train, Forward).flatMap(_.updateTrain(updatedTrain)) match
          case Some(ut) =>
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
          case _ => fail()

      "make track available again" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        Track(1).putTrain(train, Forward).flatMap(_.updateTrain(updatedTrain)) match
          case Some(ut) =>
            ut.isAvailable(Forward) shouldBe true
            ut.isAvailable(Backward) shouldBe false
          case _ => fail()

      "not be updated if the track doesn't contain the specified train" in:
        Track(1).putTrain(train3905, Forward).flatMap(_.updateTrain(train3906)) shouldBe None

      "be updated if the securityDistance is preserved" in:
        val id            = 1
        val direction     = Forward
        val movementDelta = 10
        val train         = train3905
        val updatedTrain =
          train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains + movementDelta)
        val otherTrain        = train3906
        val updatedOtherTrain = otherTrain.updateDistanceTravelled(movementDelta)
        Track(id).putTrain(train, direction).flatMap(_.updateTrain(updatedTrain)).flatMap(
          _.putTrain(otherTrain, direction)
        ).flatMap(_.updateTrain(updatedOtherTrain)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrain, updatedOtherTrain)
            ut.isEmpty shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "not be updated if the trains will be to close" in:
        val direction         = Forward
        val train             = train3905
        val updatedTrain      = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain        = train3906
        val updatedOtherTrain = otherTrain.updateDistanceTravelled(1)
        Track(1).putTrain(train, direction).flatMap(_.updateTrain(updatedTrain)).flatMap(
          _.putTrain(otherTrain, direction)
        ).flatMap(_.updateTrain(updatedOtherTrain)) shouldBe None

      "not be updated if there is an overtake" in:
        val direction    = Forward
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain   = train3906
        val updatedOtherTrain = otherTrain.updateDistanceTravelled(
          train.lengthSize + minPermittedDistanceBetweenTrains + updatedTrain.distanceTravelled
        )
        Track(1).putTrain(train, direction).flatMap(_.updateTrain(updatedTrain)).flatMap(
          _.putTrain(otherTrain, direction)
        ).flatMap(_.updateTrain(updatedOtherTrain)) shouldBe None

    "a train is removed" should:
      "be updated if the specified train is the last" in:
        val id    = 1
        val train = train3905
        Track(id).putTrain(train, Forward).flatMap(_.removeTrain(train)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq()
            ut.isEmpty shouldBe true
            ut.isAvailable(Forward) shouldBe true
            ut.isAvailable(Backward) shouldBe true
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe None
          case _ => fail()

      "not be updated if the track doesn't contain the specified train" in:
        Track(1).putTrain(train3905, Forward).flatMap(_.removeTrain(train3906)) shouldBe None

      "not be updated if the train is not the last in the track" in:
        val id           = 1
        val direction    = Forward
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain   = train3906
        Track(id).putTrain(train, direction).flatMap(_.updateTrain(updatedTrain)).flatMap(_.putTrain(
          otherTrain,
          direction
        )).flatMap(
          _.removeTrain(otherTrain)
        ) shouldBe None

      "maintain direction if the track doesn't become empty" in:
        val id           = 1
        val direction    = Forward
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain   = train3906
        Track(id).putTrain(train, direction).flatMap(_.updateTrain(updatedTrain)).flatMap(_.putTrain(
          otherTrain,
          direction
        )).flatMap(
          _.removeTrain(train)
        ) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(otherTrain)
            ut.isEmpty shouldBe false
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

    "a train is searched" should:
      "be found if there is a train with the same name" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(10)
        Track(1).putTrain(train, Forward) match
          case Some(ut) =>
            ut.contains(train) shouldBe true
            ut.contains(updatedTrain) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name" in:
        Track(1).putTrain(train3905, Forward).map(_.contains(train3906)) shouldBe Some(false)

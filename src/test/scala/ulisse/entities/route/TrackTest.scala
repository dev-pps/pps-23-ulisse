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
  private val train3905 = TrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 = TrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val id        = 1
  private val direction = Forward
  private val track     = Track(id)

  "A track" when:
    "created" should:
      "have a positive track number" in:
        List(-1, 0, 1, 2).foreach(trackNumber =>
          Track(trackNumber).id shouldBe math.max(1, trackNumber)
        )

      "not contain any train" in:
        track.trains shouldBe Seq()
        track.isEmpty shouldBe true
        track.isAvailable(direction) shouldBe true
        track.isAvailable(direction.opposite) shouldBe true

      "have a min permitted distance between trains" in:
        track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains

      "not have a set direction" in:
        track.currentDirection shouldBe None

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
        Track.createCheckedTrack(id) match
          case Right(track) =>
            track.trains shouldBe Seq()
            track.isEmpty shouldBe true
            track.isAvailable(Forward) shouldBe true
            track.isAvailable(Backward) shouldBe true
          case _ => fail()

      "have a min permitted distance between trains" in:
        Track.createCheckedTrack(id) match
          case Right(track) =>
            track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
          case _ => fail()

      "not have a set direction" in:
        Track.createCheckedTrack(id) match
          case Right(track) =>
            track.currentDirection shouldBe None
          case _ => fail()

    "a train is put in" should:
      "be updated with the specified train" in:
        track.putTrain(train3905, direction) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(train3905)
            ut.isEmpty shouldBe false
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "not be updated if the track is not available" in:
        track.putTrain(train3905, direction).flatMap(_.putTrain(train3905, direction)) shouldBe None
        track.putTrain(train3905, direction).flatMap(_.putTrain(train3906, direction)) shouldBe None

      "not be updated if the train is already moved" in:
        track.putTrain(train3905.updateDistanceTravelled(10), direction) shouldBe None

      "contain multiple trains" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain = train3906
        track.putTrain(train3905, direction).flatMap(
          _.updateTrain(updatedTrain3905)
        ).flatMap(_.putTrain(otherTrain, direction)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrain3905, otherTrain)
            ut.isEmpty shouldBe false
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case None => fail()

      "not contain multiple train in different directions" in:
        track.putTrain(train3905, direction).flatMap(
          _.updateTrain(train3905.updateDistanceTravelled(train3905.lengthSize))
        ).flatMap(_.putTrain(train3906, direction.opposite)) shouldBe None

      "not contain the same train multiple times" in:
        track.putTrain(train3905, direction).flatMap(
          _.updateTrain(train3905.updateDistanceTravelled(train3905.lengthSize))
        ).flatMap(_.putTrain(train3905, direction)) shouldBe None

    "a train is updated" should:
      "be updated with the specified train if it's present" in:
        val updatedTrain3905 = train3905.updateDistanceTravelled(10)
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrain3905)
            ut.isEmpty shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "leave track unavailable" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains - 1)
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)) match
          case Some(ut) =>
            ut.isAvailable(Forward) shouldBe false
            ut.isAvailable(Backward) shouldBe false
          case _ => fail()

      "make track available again" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains)
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)) match
          case Some(ut) =>
            ut.isAvailable(Forward) shouldBe true
            ut.isAvailable(Backward) shouldBe false
          case _ => fail()

      "not be updated if the track doesn't contain the specified train" in:
        track.putTrain(train3905, direction).flatMap(_.updateTrain(train3906)) shouldBe None

      "be updated if the securityDistance is preserved" in:
        val movementDelta = 10
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains + movementDelta)
        val updatedTrain3906 = train3906.updateDistanceTravelled(movementDelta)
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)).flatMap(
          _.putTrain(train3906, direction)
        ).flatMap(_.updateTrain(updatedTrain3906)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrain3905, updatedTrain3906)
            ut.isEmpty shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "not be updated if the trains will be to close" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains)
        val updatedTrain3906 = train3906.updateDistanceTravelled(1)
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)).flatMap(
          _.putTrain(train3906, direction)
        ).flatMap(_.updateTrain(updatedTrain3906)) shouldBe None

      "not be updated if there is an overtake" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains)
        val updatedTrain3906 = train3906.updateDistanceTravelled(
          train3905.lengthSize + minPermittedDistanceBetweenTrains + updatedTrain3905.distanceTravelled
        )
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)).flatMap(
          _.putTrain(train3906, direction)
        ).flatMap(_.updateTrain(updatedTrain3906)) shouldBe None

    "a train is removed" should:
      "be updated if the specified train is the last" in:
        track.putTrain(train3905, direction).flatMap(_.removeTrain(train3905)) match
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
        track.putTrain(train3905, direction).flatMap(_.removeTrain(train3906)) shouldBe None

      "not be updated if the train is not the last in the track" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains)
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)).flatMap(_.putTrain(
          train3906,
          direction
        )).flatMap(
          _.removeTrain(train3906)
        ) shouldBe None

      "maintain direction if the track doesn't become empty" in:
        val updatedTrain3905 =
          train3905.updateDistanceTravelled(train3905.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain = train3906
        track.putTrain(train3905, direction).flatMap(_.updateTrain(updatedTrain3905)).flatMap(_.putTrain(
          otherTrain,
          direction
        )).flatMap(
          _.removeTrain(train3905)
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
        val updatedTrain3905 = train3905.updateDistanceTravelled(10)
        track.putTrain(train3905, direction) match
          case Some(ut) =>
            ut.contains(train3905) shouldBe true
            ut.contains(updatedTrain3905) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name" in:
        track.putTrain(train3905, direction).map(_.contains(train3906)) shouldBe Some(false)

  "A direction" when:
    "forward" should:
      "have an opposite direction" in:
        Forward.opposite shouldBe Backward

    "backward" should:
      "have an opposite direction" in:
        Backward.opposite shouldBe Forward

package ulisse.entities.route

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.route.Tracks.Track
import ulisse.entities.route.Tracks.TrackDirection.{Backward, Forward}
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrackTest extends AnyWordSpec with Matchers:
  given minPermittedDistanceBetweenTrains: Double = 100.0
  private val id                                  = 1
  private val direction                           = Forward
  private val track                               = Track(id)

  "A track" when:
    "created" should:
      "have a positive track id" in:
        List(-1, 0, 1, 2).foreach(id =>
          Track(id).id shouldBe math.max(1, id)
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
      "have a positive track id" in:
        List(1, 2).foreach(id =>
          Track.createCheckedTrack(id).map(_.id) shouldBe Right(id)
        )

      "return errors if the track id is not positive" in:
        List(-1, 0).foreach(id =>
          Track.createCheckedTrack(id) shouldBe Left(Chain(Tracks.Errors.InvalidTrackId))
        )

      "not contain any train" in:
        Track.createCheckedTrack(id) match
          case Right(track) =>
            track.trains shouldBe Seq()
            track.isEmpty shouldBe true
            track.isAvailable(direction) shouldBe true
            track.isAvailable(direction.opposite) shouldBe true
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
        track.putTrain(trainAgent3905, direction) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(trainAgent3905)
            ut.isEmpty shouldBe false
            ut.isAvailable(direction) shouldBe false
            ut.isAvailable(direction.opposite) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "not be updated if the track is not available" in:
        track.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3905, direction)) shouldBe None
        track.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction)) shouldBe None

      "not be updated if the train is already moved" in:
        track.putTrain(trainAgent3905.updateDistanceTravelled(10), direction) shouldBe None

      "contain multiple trains" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        track.putTrain(trainAgent3905, direction).flatMap(
          _.updateTrain(updatedTrainAgent3905)
        ).flatMap(_.putTrain(trainAgent3906, direction)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrainAgent3905, trainAgent3906)
            ut.isEmpty shouldBe false
            ut.isAvailable(direction) shouldBe false
            ut.isAvailable(direction.opposite) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case None => fail()

      "not contain multiple train in different directions" in:
        track.putTrain(trainAgent3905, direction).flatMap(
          _.updateTrain(trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize))
        ).flatMap(_.putTrain(trainAgent3906, direction.opposite)) shouldBe None

      "not contain the same train multiple times" in:
        track.putTrain(trainAgent3905, direction).flatMap(
          _.updateTrain(trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize))
        ).flatMap(_.putTrain(trainAgent3905, direction)) shouldBe None

    "a train is updated" should:
      "be updated with the specified train if it's present" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrainAgent3905)
            ut.isEmpty shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "leave track unavailable" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains - 1)
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(ut) =>
            ut.isAvailable(direction) shouldBe false
            ut.isAvailable(direction.opposite) shouldBe false
          case _ => fail()

      "make track available again" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(ut) =>
            ut.isAvailable(direction) shouldBe true
            ut.isAvailable(direction.opposite) shouldBe false
          case _ => fail()

      "not be updated if the track doesn't contain the specified train" in:
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(trainAgent3906)) shouldBe None

      "be updated if the securityDistance is preserved" in:
        val movementDelta = 10
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(
            trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains + movementDelta
          )
        val updatedTrainAgent3906 = trainAgent3906.updateDistanceTravelled(movementDelta)
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(
          _.putTrain(trainAgent3906, direction)
        ).flatMap(_.updateTrain(updatedTrainAgent3906)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(updatedTrainAgent3905, updatedTrainAgent3906)
            ut.isEmpty shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

      "not be updated if the trains will be to close" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        val updatedTrainAgent3906 = trainAgent3906.updateDistanceTravelled(1)
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(
          _.putTrain(trainAgent3906, direction)
        ).flatMap(_.updateTrain(updatedTrainAgent3906)) shouldBe None

      "not be updated if there is an overtake" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        val updatedTrainAgent3906 = trainAgent3906.updateDistanceTravelled(
          trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains + updatedTrainAgent3905.distanceTravelled
        )
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(
          _.putTrain(trainAgent3906, direction)
        ).flatMap(_.updateTrain(updatedTrainAgent3906)) shouldBe None

    "a train is removed" should:
      "be updated if the specified train is the last" in:
        track.putTrain(trainAgent3905, direction).flatMap(_.removeTrain(trainAgent3905)) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq()
            ut.isEmpty shouldBe true
            ut.isAvailable(direction) shouldBe true
            ut.isAvailable(direction.opposite) shouldBe true
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe None
          case _ => fail()

      "not be updated if the track doesn't contain the specified train" in:
        track.putTrain(trainAgent3905, direction).flatMap(_.removeTrain(trainAgent3906)) shouldBe None

      "not be updated if the train is not the last in the track" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(_.putTrain(
          trainAgent3906,
          direction
        )).flatMap(
          _.removeTrain(trainAgent3906)
        ) shouldBe None

      "maintain direction if the track doesn't become empty" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        val otherTrain = trainAgent3906
        track.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(_.putTrain(
          otherTrain,
          direction
        )).flatMap(
          _.removeTrain(trainAgent3905)
        ) match
          case Some(ut) =>
            ut.id shouldBe id
            ut.trains shouldBe Seq(otherTrain)
            ut.isEmpty shouldBe false
            ut.isAvailable(direction) shouldBe false
            ut.isAvailable(direction.opposite) shouldBe false
            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
            ut.currentDirection shouldBe Some(direction)
          case _ => fail()

    "a train is searched" should:
      "be found if there is a train with the same name" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        track.putTrain(trainAgent3905, direction) match
          case Some(ut) =>
            ut.contains(trainAgent3905) shouldBe true
            ut.contains(updatedTrainAgent3905) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name" in:
        track.putTrain(trainAgent3905, direction).map(_.contains(trainAgent3906)) shouldBe Some(false)

  "A TrackDirection" when:
    "Forward" should:
      "have an opposite direction" in:
        Forward.opposite shouldBe Backward

    "Backward" should:
      "have an opposite direction" in:
        Backward.opposite shouldBe Forward

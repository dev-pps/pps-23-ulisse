package ulisse.entities.route

import cats.data.Chain
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.route.Track.TrainAgentsDirection.Forward
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class TrackTest extends AnyWordSpec with Matchers:
  given minPermittedDistanceBetweenTrains: Double = 100.0
  private val defaultTechnology                   = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
//  private val defaultWagon                        = Wagon(UseType.Passenger, 50)
//  private val defaultWagonNumber                  = 5
//  private val train3905 =
//    TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
//  private val train3906 =
//    TrainAgent.apply(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
//  private val train3907 =
//    TrainAgent.apply(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))
//
//  "A track" when:
//    "created" should:
//      "be created empty by default" in:
//        val track = Track(1)
//        track.isEmpty shouldBe true
//        track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//
//      "be created with a list of trains" in:
//        val track = Track(1, train3905, train3906, train3907)
//        track.trains.size shouldBe 3
//        track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//
//      "contain distinct trains" in:
//        val track = Track(1, train3905, train3905, train3905)
//        track.trains.size shouldBe 1
//        track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//
//      "contain distinct trains considering the name" in:
//        val track = Track(1, train3905, train3905.updateDistanceTravelled(10))
//        track.trains.size shouldBe 1
//        track.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//
//    "created checked" should:
//      "be created if contains distinct trains" in:
//        Track.createCheckedTrack(1, train3905, train3906, train3907) shouldBe Right(Track(
//          1,
//          train3905,
//          train3906,
//          train3907
//        ))
//
//      "return errors when not contains distinct trains" in:
//        Track.createCheckedTrack(1, train3905, train3905, train3905) shouldBe Left(Chain(Track.Errors.DuplicateTrains))
//
//    "a new train is added" should:
//      "be updated with the new train" in:
//        val track = Track(1, train3905, train3906)
//        track :+ (train3907, Forward) match
//          case Left(_) => fail()
//          case Right(ut) =>
//            ut.trains.size shouldBe 3
//            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//
//      "not be updated if the train is already moved" in:
//        Track(1, train3905) :+ (train3906.updateDistanceTravelled(10), Forward) shouldBe Left(
//          Chain(Track.Errors.TrainAlreadyMoved)
//        )
//
//      "not be updated if the train is already present" in:
//        Track(1, train3905) :+ (train3905, Forward) shouldBe Left(Chain(Track.Errors.DuplicateTrains))
//
//      "not be updated if the train is already present with the same name" in:
//        Track(1, train3905) :+ (train3905.updateDistanceTravelled(10), Forward) shouldBe Left(Chain(
//          Track.Errors.TrainAlreadyMoved,
//          Track.Errors.DuplicateTrains
//        ))
//
//    "a train is updated with a new train" should:
//      "be updated with the new train" in:
//        Track(1, train3905, train3906).updateTrain(train3905) match
//          case Some(ut) =>
//            ut.trains shouldBe Seq(train3905, train3906)
//            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//          case None => fail()
//
//        Track(1, train3905, train3906).updateTrain(train3905.updateDistanceTravelled(10)) match
//          case Some(ut) =>
//            ut.trains shouldBe Seq(train3905.updateDistanceTravelled(10), train3906)
//            ut.minPermittedDistanceBetweenTrains shouldBe minPermittedDistanceBetweenTrains
//          case None => fail()
//
//      "not be updated if the train is not present" in:
//        Track(1, train3905, train3906).updateTrain(train3907) shouldBe None
//
//      "be available" in:
//        Track(1).isAvailable shouldBe true
//        Track(
//          1,
//          train3905.updateDistanceTravelled(minPermittedDistanceBetweenTrains + train3905.length)
//        ).isAvailable shouldBe true
//
//      "not be available" in:
//        Track(1, train3905).isAvailable shouldBe false
//        Track(
//          1,
//          train3905.updateDistanceTravelled(minPermittedDistanceBetweenTrains / 2)
//        ).isAvailable shouldBe false

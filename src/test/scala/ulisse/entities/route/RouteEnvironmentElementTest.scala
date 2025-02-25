package ulisse.entities.route

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.managers.RouteManagerTest.validateRoute
import ulisse.entities.route.RouteEnvironmentElement.*
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsDirection.Forward
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

class RouteEnvironmentElementTest extends AnyWordSpec with Matchers:
  given minPermittedDistanceBetweenTrains: Double = 100.0
  private val defaultTechnology                   = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon                        = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber                  = 5
  private val train3905 =
    TrainAgent.apply(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 =
    TrainAgent.apply(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907 =
    TrainAgent.apply(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  def route: RouteEnvironmentElement =
    // Create a route with 2 tracks and a length of 200.0 + trainLength, min distance between two train is 100.0 with this factory method
    validateRoute.flatMap(_.withLength(2 * minPermittedDistanceBetweenTrains + train3905.length)) match
      case Left(errors) => fail()
      case Right(route) => RouteEnvironmentElement(route, minPermittedDistanceBetweenTrains)

  "RouteEnvironmentElement" should:
    "have two empty tracks" in:
      route.containers.size shouldBe 2
      route.containers.forall(_.isEmpty) shouldBe true

    "have a default minPermittedDistanceBetweenTrains of 100.0" in:
      route.containers.forall(_.minPermittedDistanceBetweenTrains == 100.0) shouldBe true

    "have a length greater or equal than minPermittedDistanceBetweenTrains + train3905 length" in:
      route.containers.forall(
        route.length >= _.minPermittedDistanceBetweenTrains + train3905.length
      ) shouldBe true

  "A trainAgent" when:
    "take a route" should:
      "be place in a track if it empty" in:
        route.putTrain(train3905, Forward) match
          case Some(updatedRoute) =>
            updatedRoute.containers.find(_.contains(train3905)) shouldBe Some(Track(1, train3905))
            updatedRoute.containers shouldBe Seq(Track(1, train3905), Track(2))
          case None => fail()

      "not be place in a track if not available" in:
        route.putTrain(train3905, Forward).flatMap(_.putTrain(train3906, Forward)) match
          case Some(updatedRoute) =>
            updatedRoute.containers.find(_.contains(train3906)) shouldBe Some(Track(2, train3906))
            updatedRoute.containers shouldBe Seq(Track(1, train3905), Track(2, train3906))
            updatedRoute.putTrain(train3907, Forward) shouldBe None
          case None => fail()

      "not be place if it's already in the route" in:
        route.putTrain(train3905, Forward).flatMap(_.putTrain(train3905, Forward)) shouldBe None
        route.putTrain(train3905, Forward).flatMap(
          _.updateTrain(train3905.updateDistanceTravelled(100.0 + train3905.length))
        ).flatMap(_.putTrain(train3905, Forward)) shouldBe None

      "be place in a track if available" in:
        val train3905Updated = train3905.updateDistanceTravelled(100.0 + train3905.length)
        route.putTrain(train3905, Forward).flatMap(_.updateTrain(train3905Updated)).flatMap(_.putTrain(
          train3906,
          Forward
        )) match
          case Some(updatedRoute) =>
            updatedRoute.containers.find(_.contains(train3906)) shouldBe Some(Track(1, train3905Updated, train3906))
          case None => fail()

    "put in a route" should:
      "be placed in the first available track" in:
        route.putTrain(train3905, Forward) match
          case Some(updatedRoute) =>
            updatedRoute.containers shouldBe Seq(
              Track(1, train3905),
              Track(2)
            )
          case None => fail()

        route.putTrain(train3905, Forward).flatMap(_.putTrain(
          train3906,
          Forward
        )) match
          case Some(updatedRoute) =>
            updatedRoute.containers shouldBe Seq(
              Track(1, train3905),
              Track(2, train3906)
            )
          case None => fail()

      "not be placed if it's already in the route" in:
        route.putTrain(train3905, Forward).flatMap(_.putTrain(
          train3905,
          Forward
        )) shouldBe None

      "not be placed if it's not available" in:
        route.putTrain(train3905, Forward).flatMap(_.putTrain(
          train3906,
          Forward
        )).flatMap(_.putTrain(train3907, Forward)) shouldBe None

      "be place behind if it is possible" in:
        val train3905Updated = train3905.updateDistanceTravelled(100.0 + train3905.length)
        route.putTrain(train3905, Forward).flatMap(_.updateTrain(train3905Updated)).flatMap(
          _.putTrain(train3906, Forward)
        ) match
          case Some(updatedRoute) =>
            updatedRoute.containers shouldBe Seq(
              Track(1, train3905Updated, train3906),
              Track(2)
            )
          case None => fail()

    "update in a route" should:
      "be updated if present" in:
        val updatedTrain3905 = train3905.updateDistanceTravelled(1)
        route.putTrain(train3905, Forward).flatMap(_.updateTrain(updatedTrain3905)) match
          case Some(updatedRoute) =>
            updatedRoute.containers.find(_.contains(train3905)) shouldBe Some(Track(1, updatedTrain3905))
            updatedRoute.containers shouldBe Seq(
              Track(1, updatedTrain3905),
              Track(2)
            )
          case None => fail()

      "not be updated if not present" in:
        route.updateTrain(train3905) shouldBe None

    "remove from a route" should:
      "be removed if present" in:
        route.putTrain(train3905, Forward).flatMap(_.removeTrain(train3905)) match
          case Some(updatedRoute) =>
            updatedRoute.containers.find(_.contains(train3905)) shouldBe None
            updatedRoute.containers shouldBe Seq(
              Track(1),
              Track(2)
            )
          case None => fail()

      "not be removed if not present" in:
        route.removeTrain(train3905) shouldBe None

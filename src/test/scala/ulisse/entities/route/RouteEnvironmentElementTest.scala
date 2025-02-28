package ulisse.entities.route

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.managers.RouteManagerTest.validateRoute
import ulisse.entities.route.RouteEnvironmentElement.*
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Track.TrainAgentsDirection.{Backward, Forward}
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

  def route: Route =
    // Create a route with 2 tracks and a length of 200.0 + trainLength
    validateRoute.flatMap(_.withLength(2 * minPermittedDistanceBetweenTrains + train3905.lengthSize)) match
      case Left(errors) => fail()
      case Right(route) => route
  private val routeEE = RouteEnvironmentElement(route, minPermittedDistanceBetweenTrains)

  private def validateRouteInfo(routeEE: RouteEnvironmentElement): Unit =
    routeEE.id shouldBe route.id
    routeEE.arrival shouldBe route.arrival
    routeEE.departure shouldBe route.departure
    routeEE.railsCount shouldBe route.railsCount
    routeEE.length shouldBe route.length
    routeEE.technology shouldBe route.technology
    routeEE.typology shouldBe route.typology

  "RouteEnvironmentElement" when:
    "created" should:
      "have the same route info" in:
        validateRouteInfo(routeEE)

      "have all empty tracks" in:
        routeEE.containers.size shouldBe route.railsCount
        routeEE.containers.forall(_.isEmpty) shouldBe true
        routeEE.containers.forall(_.isAvailable) shouldBe true

      "have a default minPermittedDistanceBetweenTrains" in:
        routeEE.containers.forall(
          _.minPermittedDistanceBetweenTrains == minPermittedDistanceBetweenTrains
        ) shouldBe true

      "have a length greater or equal than minPermittedDistanceBetweenTrains + train3905 length" in:
        route.length shouldBe >=(minPermittedDistanceBetweenTrains + train3905.lengthSize)

      "tracks are numerated sequentially" in:
        routeEE.containers.zipWithIndex.forall((track, index) => track.id == index + 1) shouldBe true

    "a train is put in" should:
      "be placed in the first track" in:
        routeEE.putTrain(train3905, Forward) match
          case Some(ur) =>
            validateRouteInfo(ur)
            ur.isAvailable shouldBe true
            ur.containers.find(_.contains(train3905)).map(_.id) shouldBe Some(1)
          case None => fail()

      "not be placed if it's already in the route" in:
        val direction = Forward
        routeEE.putTrain(train3905, direction).flatMap(_.putTrain(train3905, direction)) shouldBe None

      "be placed in the first available track if other trains are present" in:
        routeEE.putTrain(train3905, Forward).flatMap(_.putTrain(train3906, Backward)) match
          case Some(ur) =>
            validateRouteInfo(ur)
            ur.isAvailable shouldBe false
            ur.containers.find(_.contains(train3906)).map(_.id) shouldBe Some(2)
          case None => fail()

      "not be placed if it's not available" in:
        val direction = Forward
        routeEE.putTrain(train3905, direction).flatMap(_.putTrain(train3906, direction)).flatMap(
          _.putTrain(train3907, direction)
        ) shouldBe None

      "be placed behind if it is possible" in:
        val train        = train3905
        val trainUpdated = train.updateDistanceTravelled(minPermittedDistanceBetweenTrains + train.lengthSize)
        routeEE.putTrain(train, Forward).flatMap(_.updateTrain(trainUpdated))
          .flatMap(_.putTrain(train3906, Forward)) match
          case Some(ur) =>
            validateRouteInfo(ur)
            ur.isAvailable shouldBe true
            ur.containers.find(_.contains(train)).map(_.id) shouldBe Some(1)
            ur.containers.find(_.contains(train3906)).map(_.id) shouldBe Some(1)
          case None => fail()

      "not be placed behind if it is not possible" in:
        val train        = train3905
        val trainUpdated = train.updateDistanceTravelled(minPermittedDistanceBetweenTrains + train.lengthSize)
        routeEE.putTrain(train, Forward).flatMap(_.putTrain(train3906, Backward)).flatMap(_.updateTrain(trainUpdated))
          .flatMap(_.putTrain(train3907, Backward)) shouldBe None

    "a train is updated" should:
      "be updated if present" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(1)
        routeEE.putTrain(train, Forward).flatMap(_.updateTrain(updatedTrain)) match
          case Some(ur) =>
            validateRouteInfo(ur)
            ur.containers.find(_.contains(train3905)).map(c => (c.id, c.trains)) shouldBe Some((1, Seq(updatedTrain)))
          case None => fail()

      "not be updated if not present" in:
        routeEE.updateTrain(train3905) shouldBe None

      "leave route unavailable" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains - 1)
        routeEE.putTrain(train, Forward).flatMap(_.putTrain(train3906, Backward)).flatMap(
          _.updateTrain(updatedTrain)
        ) match
          case Some(ur) =>
            ur.isAvailable shouldBe false
          case None => fail()

      "make route available again" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(train.lengthSize + minPermittedDistanceBetweenTrains)
        routeEE.putTrain(train, Forward).flatMap(_.putTrain(train3906, Backward)).flatMap(
          _.updateTrain(updatedTrain)
        ) match
          case Some(ur) =>
            ur.isAvailable shouldBe true
          case None => fail()

    "a train is removed" should:
      "be removed if it's last in a track" in:
        val train        = train3905
        val trainUpdated = train.updateDistanceTravelled(1)
        routeEE.putTrain(train, Forward).flatMap(_.updateTrain(trainUpdated)).flatMap(_.removeTrain(train)) match
          case Some(ur) =>
            validateRouteInfo(ur)
            ur.containers.find(_.contains(train)) shouldBe None
          case None => fail()

      "not be removed if is not last in a track" in:
        val train        = train3905
        val trainUpdated = train.updateDistanceTravelled(minPermittedDistanceBetweenTrains + train.lengthSize)
        routeEE.putTrain(train, Forward).flatMap(_.updateTrain(trainUpdated))
          .flatMap(_.putTrain(train3906, Forward)).flatMap(_.removeTrain(train3906)) shouldBe None

      "not be removed if not present" in:
        routeEE.removeTrain(train3905) shouldBe None

    "a train is searched" should:
      "be found if there is a train with the same name in a track" in:
        val train        = train3905
        val updatedTrain = train.updateDistanceTravelled(10)
        routeEE.putTrain(train, Forward) match
          case Some(ur) =>
            ur.contains(train) shouldBe true
            ur.contains(updatedTrain) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name in a track" in:
        routeEE.putTrain(train3905, Forward).map(_.contains(train3906)) shouldBe Some(false)

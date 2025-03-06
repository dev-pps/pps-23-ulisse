package ulisse.entities.route

import org.scalatest.Assertions.fail
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Utils.TestUtility.getOrFail
import ulisse.applications.managers.RouteManagerTest.validateRoute
import ulisse.entities.Technology
import ulisse.entities.route.RouteEnvironmentElement.*
import ulisse.entities.route.RouteEnvironmentElementTest.{
  direction,
  minPermittedDistanceBetweenTrains,
  routeAB,
  routeAB_EE
}
import ulisse.entities.route.RouteTest.*
import ulisse.entities.route.Routes.RouteType.{AV, Normal}
import ulisse.entities.route.Routes.{Route, RouteType}
import ulisse.entities.route.Tracks.TrackDirection.{Backward, Forward}
import ulisse.entities.station.Station
import ulisse.entities.station.StationTest.*
import ulisse.entities.train.TrainAgentTest.*
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}

object RouteEnvironmentElementTest:
  given minPermittedDistanceBetweenTrains: Double = 100.0
  val direction                                   = Forward
  val normalRouteAB                               = makeRoute(stationA, stationB, Normal)
  val routeAB                                     = makeRoute(stationA, stationB, AV)
  val routeBC                                     = makeRoute(stationB, stationC, AV)
  val routeCD                                     = makeRoute(stationC, stationD, AV)
  val routeDE                                     = makeRoute(stationD, stationE, AV)

  val normalRouteAB_EE = makeRouteEE(normalRouteAB)
  val routeAB_EE       = makeRouteEE(routeAB)
  val routeBC_EE       = makeRouteEE(routeBC)
  val routeCD_EE       = makeRouteEE(routeCD)
  val routeDE_EE       = makeRouteEE(routeDE)

  def makeRoute(departure: Station, arrival: Station, routeType: RouteType): Route =
    Route(
      departure,
      arrival,
      routeType,
      railsCount,
      2 * minPermittedDistanceBetweenTrains + trainAgent3905.lengthSize
    ).getOrFail

  def makeRouteEE(route: Route): RouteEnvironmentElement =
    RouteEnvironmentElement(route, minPermittedDistanceBetweenTrains)
class RouteEnvironmentElementTest extends AnyWordSpec with Matchers:

  "RouteEnvironmentElement" when:
    "created" should:
      "have the same route info" in:
        routeAB_EE shouldBe routeAB

      "have all empty tracks" in:
        routeAB_EE.containers.size shouldBe routeAB.railsCount
        routeAB_EE.containers.forall(_.isEmpty) shouldBe true
        routeAB_EE.containers.forall(_.isAvailable(Forward)) shouldBe true
        routeAB_EE.containers.forall(_.isAvailable(Backward)) shouldBe true

      "have a default minPermittedDistanceBetweenTrains" in:
        routeAB_EE.containers.forall(
          _.minPermittedDistanceBetweenTrains == minPermittedDistanceBetweenTrains
        ) shouldBe true

      "have a length greater or equal than minPermittedDistanceBetweenTrains + train3905 length" in:
        routeAB_EE.length shouldBe >=(minPermittedDistanceBetweenTrains + trainAgent3905.lengthSize)

      "tracks are numerated sequentially" in:
        routeAB_EE.containers.zipWithIndex.forall((track, index) => track.id == index + 1) shouldBe true

    "a train is put in" should:
      "be placed in the first track" in:
        routeAB_EE.putTrain(trainAgent3905, direction) match
          case Some(ur) =>
            ur shouldBe routeAB
            ur.isAvailable(Forward) shouldBe true
            ur.isAvailable(Backward) shouldBe true
            ur.containers.find(_.contains(trainAgent3905)).map(_.id) shouldBe Some(1)
          case None => fail()

      "not be placed if it's already in the route" in:
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3905, direction)) shouldBe None

      "be placed in the first available track if other trains are present" in:
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction.opposite)) match
          case Some(ur) =>
            ur shouldBe routeAB
            ur.isAvailable(Forward) shouldBe false
            ur.isAvailable(Backward) shouldBe false
            ur.containers.find(_.contains(trainAgent3906)).map(_.id) shouldBe Some(2)
          case None => fail()

      "not be placed if it's not available" in:
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction)).flatMap(
          _.putTrain(trainAgent3907, direction)
        ) shouldBe None

      "be placed behind if it is possible" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(minPermittedDistanceBetweenTrains + trainAgent3905.lengthSize)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905))
          .flatMap(_.putTrain(trainAgent3906, direction)) match
          case Some(ur) =>
            ur shouldBe routeAB
            ur.containers.find(_.contains(trainAgent3905)).map(_.id) shouldBe Some(1)
            ur.isAvailable(Forward) shouldBe true
            ur.isAvailable(Backward) shouldBe true
            ur.containers.find(_.contains(trainAgent3906)).map(_.id) shouldBe Some(1)
          case None => fail()

      "not be placed behind if it is not possible" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(minPermittedDistanceBetweenTrains + trainAgent3905.lengthSize)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction.opposite)).flatMap(
          _.updateTrain(updatedTrainAgent3905)
        )
          .flatMap(_.putTrain(trainAgent3907, direction.opposite)) shouldBe None

      "not be placed if the train technology is not compatible" in:
        routeAB_EE.putTrain(normalTrainAgent, direction) shouldBe None

    "a train is updated" should:
      "be updated if present" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(1)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(ur) =>
            ur shouldBe routeAB
            ur.containers.find(_.contains(trainAgent3905)).map(c => (c.id, c.trains)) shouldBe Some((
              1,
              Seq(updatedTrainAgent3905)
            ))
          case None => fail()

      "not be updated if not present" in:
        routeAB_EE.updateTrain(trainAgent3905) shouldBe None

      "leave route unavailable" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains - 1)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction.opposite)).flatMap(
          _.updateTrain(updatedTrainAgent3905)
        ) match
          case Some(ur) =>
            ur.isAvailable(direction) shouldBe false
            ur.isAvailable(direction.opposite) shouldBe false
          case None => fail()

      "make route available again" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(trainAgent3905.lengthSize + minPermittedDistanceBetweenTrains)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction.opposite)).flatMap(
          _.updateTrain(updatedTrainAgent3905)
        ) match
          case Some(ur) =>
            ur.isAvailable(Forward) shouldBe true
            ur.isAvailable(Backward) shouldBe false
          case None => fail()

    "a train is removed" should:
      "be removed if it's last in a track" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(1)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905)).flatMap(
          _.removeTrain(trainAgent3905)
        ) match
          case Some(ur) =>
            ur shouldBe routeAB
            ur.containers.find(_.contains(trainAgent3905)) shouldBe None
          case None => fail()

      "not be removed if is not last in a track" in:
        val updatedTrainAgent3905 =
          trainAgent3905.updateDistanceTravelled(minPermittedDistanceBetweenTrains + trainAgent3905.lengthSize)
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.updateTrain(updatedTrainAgent3905))
          .flatMap(_.putTrain(trainAgent3906, direction)).flatMap(_.removeTrain(trainAgent3906)) shouldBe None

      "not be removed if not present" in:
        routeAB_EE.removeTrain(trainAgent3905) shouldBe None

    "a train is searched" should:
      "be found if there is a train with the same name in a track" in:
        val updatedTrainAgent3905 = trainAgent3905.updateDistanceTravelled(10)
        routeAB_EE.putTrain(trainAgent3905, direction) match
          case Some(ur) =>
            ur.contains(trainAgent3905) shouldBe true
            ur.contains(updatedTrainAgent3905) shouldBe true
          case _ => fail()

      "not be found if there isn't a train with the same name in a track" in:
        routeAB_EE.putTrain(trainAgent3905, direction).map(_.contains(trainAgent3906)) shouldBe Some(false)

    "queried for availability" should:
      "be available if the route is available and the train technology is compatible" in:
        routeAB_EE.isAvailable(direction) shouldBe true
        routeAB_EE.isAvailableFor(trainAgent3905, direction) shouldBe true

      "not be available if the route is available and the train technology is not compatible" in:
        routeAB_EE.isAvailable(direction) shouldBe true
        routeAB_EE.isAvailableFor(normalTrainAgent, direction) shouldBe false

      "not be available if the train technology is compatible but the route is not available" in:
        routeAB_EE.putTrain(trainAgent3905, direction).flatMap(_.putTrain(trainAgent3906, direction.opposite)) match
          case Some(ur) =>
            ur.isAvailable(direction) shouldBe false
            ur.isAvailableFor(trainAgent3907, direction) shouldBe false
          case None => fail()

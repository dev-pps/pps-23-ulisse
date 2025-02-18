package ulisse.entities.route

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.applications.managers.RouteManagerTest.validateRoute
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes.{Route, TypeRoute}
import ulisse.entities.station.Station
import ulisse.entities.train.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.entities.route.RouteEnvironmentElement.*

class RouteEnvironmentElementTest extends AnyWordSpec with Matchers:

  private val defaultTechnology  = TrainTechnology("HighSpeed", 300, 1.0, 0.5)
  private val defaultWagon       = Wagon(UseType.Passenger, 50)
  private val defaultWagonNumber = 5
  private val train3905 =
    TrainAgent.createTrainAgent(Train("3905", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3906 =
    TrainAgent.createTrainAgent(Train("3906", defaultTechnology, defaultWagon, defaultWagonNumber))
  private val train3907 =
    TrainAgent.createTrainAgent(Train("3907", defaultTechnology, defaultWagon, defaultWagonNumber))

  def route: RouteEnvironmentElement =
    // Create a route with 2 tracks and a length of 200.0 + trainLength, min distance between two train is 100.0 with this factory method
    validateRoute.flatMap(_.withLength(200.0 + train3905.length)) match
      case Left(errors) => fail()
      case Right(route) => RouteEnvironmentElement.createRouteEnvironmentElement(route)

  "RouteEnvironmentElement" should:
    "have two tracks" in:
      route.tracks.size shouldBe 2

    "have a length greater or equal than minPermittedDistanceBetweenTrains" in:
      route.length shouldBe >=(route.minPermittedDistanceBetweenTrains)

  "A trainAgent" when:
    "take a route" should:
      "be place in a track if it empty" in:
        route.firstAvailableTrack shouldBe Some(Seq())
        train3905.take(route) match
          case Some(updatedRoute) =>
            updatedRoute.tracks.find(_.contains(train3905)) shouldBe Some(Seq(train3905))
            updatedRoute.firstAvailableTrack shouldBe Some(Seq())
            updatedRoute.tracks shouldBe Seq(Seq(train3905), Seq())
          case None => fail()

      "not be place in a track if not available" in:
        train3905.take(route).flatMap(train3906.take) match
          case Some(updatedRoute) =>
            updatedRoute.tracks.find(_.contains(train3906)) shouldBe Some(Seq(train3906))
            updatedRoute.firstAvailableTrack shouldBe None
            updatedRoute.tracks shouldBe Seq(Seq(train3905), Seq(train3906))
            train3907.take(updatedRoute) shouldBe None
          case None => fail()

      "not be place if it's already in the route" in:
        train3905.take(route).flatMap(train3905.take) shouldBe None

      "be place in a track if available" in:
        val train3905Updated = train3905.updateDistanceTravelled(100.0 + train3905.length)
        train3905.take(route).flatMap(_.updateTrain(train3905Updated)) match
          case Some(updatedRoute) =>
            updatedRoute.firstAvailableTrack shouldBe Some(Seq(train3905Updated))
            train3906.take(updatedRoute) match
              case Some(updatedRoute) =>
                updatedRoute.tracks.find(_.contains(train3906)) shouldBe Some(Seq(train3905Updated, train3906))
              case None => fail()
          case None => fail()

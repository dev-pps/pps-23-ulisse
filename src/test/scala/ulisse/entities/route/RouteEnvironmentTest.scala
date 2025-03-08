package ulisse.entities.route

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.Utils.TestUtility.getOrFail
import ulisse.entities.route.RouteEnvironmentElementTest.{makeRouteEE, normalRouteAB_EE, routeAB_EE, routeBC_EE}
import ulisse.entities.route.Tracks.TrackDirection.{Backward, Forward}
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.StationEnvironmentElementTest.{stationA_EE, stationB_EE, stationC_EE}
import ulisse.entities.train.TrainAgentTest.{trainAgent3905, trainAgent3906}

class RouteEnvironmentTest extends AnyWordSpec with Matchers:
  private val routeABTuple           = (stationA_EE, stationB_EE)
  private val routeAB_EE_Single_Slow = makeRouteEE(normalRouteAB_EE.withRailsCount(1).getOrFail)
  private val routeAB_EE_Single      = makeRouteEE(routeAB_EE.withRailsCount(1).getOrFail)
  private val routeBC_EE_Single      = makeRouteEE(routeBC_EE.withRailsCount(1).getOrFail)
  private val routes                 = Seq(routeAB_EE_Single_Slow, routeAB_EE_Single, routeBC_EE_Single)
  private val configurationData      = ConfigurationData(Seq(), routes, Seq(), Seq())
  private val routeEnvironment       = RouteEnvironment(configurationData)
  private val updatedTrainAgent3905  = trainAgent3905.updateDistanceTravelled(10)

  "RouteEnvironment" when:
    "created" should:
      "contains the configuration data routes" in:
        routeEnvironment.environmentElements shouldEqual configurationData.routes

      "have no trains" in:
        routeEnvironment.trainAgents shouldBe Seq()

    "a train is put in" should:
      "return a new RouteEnvironment with the train in the route" in:
        routeEnvironment.putTrain(trainAgent3905, routeABTuple) match
          case Some(re) => re.trainAgents shouldBe Seq(trainAgent3905)
          case _        => fail()

      "return None if the route is not in the environment" in:
        routeEnvironment.putTrain(trainAgent3905, (stationA_EE, stationC_EE)) shouldBe None

      "return None if the route is full" in:
        routeEnvironment.putTrain(trainAgent3905, (stationB_EE, stationC_EE))
          .flatMap(_.putTrain(trainAgent3906, (stationC_EE, stationB_EE))) shouldBe None

      "prioritize the route with the better technology" in:
        routeEnvironment.putTrain(trainAgent3905, routeABTuple).flatMap(
          _.environmentElements.find(_.id == routeAB_EE.id)
        ) match
          case Some(re) => re.trains shouldBe Seq(trainAgent3905)
          case _        => fail()

      "take the route with worst technology if it is the only one available" in:
        routeEnvironment.putTrain(trainAgent3905, routeABTuple).flatMap(
          _.putTrain(trainAgent3906, (stationA_EE, stationB_EE)).flatMap(
            _.environmentElements.find(_.id == normalRouteAB_EE.id)
          )
        ) match
          case Some(re) => re.trains shouldBe Seq(trainAgent3906)
          case _        => fail()

      "reset the train movement before putting in the route" in:
        routeEnvironment.putTrain(updatedTrainAgent3905, routeABTuple).map(_.trainAgents) match
          case Some(Seq(train)) => train.distanceTravelled shouldBe 0
          case _                => fail()

    "a train is updated" should:
      "return a new RouteEnvironment with the updated train" in:
        routeEnvironment.putTrain(trainAgent3905, routeABTuple).flatMap(_.updateTrain(updatedTrainAgent3905)) match
          case Some(re) => re.trainAgents shouldBe Seq(updatedTrainAgent3905)
          case _        => fail()

      "return None if the train is not in the environment" in:
        routeEnvironment.updateTrain(trainAgent3905) shouldBe None

    "a train is removed" should:
      "return a new RouteEnvironment without the train" in:
        routeEnvironment.putTrain(trainAgent3905, routeABTuple).flatMap(_.removeTrain(updatedTrainAgent3905)) match
          case Some(re) => re.trainAgents shouldBe Seq()
          case _        => fail()

      "return None if the train is not in the environment" in:
        routeEnvironment.removeTrain(trainAgent3905) shouldBe None

    "queried for route with direction" should:
      val routeABSeq = Seq(routeAB_EE_Single, routeAB_EE_Single_Slow)
      "return the route with forward direction" in:
        routeEnvironment.findRoutesWithTravelDirection(stationA_EE, stationB_EE) shouldBe routeABSeq.map((_, Forward))

      "return the route with backward direction" in:
        routeEnvironment.findRoutesWithTravelDirection(stationB_EE, stationA_EE) shouldBe routeABSeq.map((_, Backward))

      "return all the possibility also if the route are unavailable" in:
        routeEnvironment.putTrain(trainAgent3905, routeABTuple).map(_.findRoutesWithTravelDirection(
          stationA_EE,
          stationB_EE
        )) match
          case Some(res) => res shouldBe routeABSeq.map((_, Forward))
          case _         => fail()

      "return None if the route with the specified direction is not in the environment" in:
        routeEnvironment.findRoutesWithTravelDirection(stationA_EE, stationC_EE) shouldBe Seq()

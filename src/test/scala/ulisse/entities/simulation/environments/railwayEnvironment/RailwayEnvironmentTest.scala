package ulisse.entities.simulation.environments.railwayEnvironment

import org.mockito.Mockito.{spy, when}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.RouteEnvironmentElementTest.routeAB
import ulisse.entities.route.RouteTest.departureCoordinate
import ulisse.entities.route.Routes.TypeRoute.AV
import ulisse.entities.route.Routes.{Route, TypeRoute}
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.{
  routesEE,
  stationsEE,
  timetables
}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.station.StationTest.{stationA, stationB}
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetableTest.{timetable1, timetable2}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.entities.train.TrainAgentTest.{train3905, train3906, train3907}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.Time

import scala.Seq

class RailwayEnvironmentTest extends AnyWordSpec with Matchers:
  private val dt = 1

  private final case class FakeTrainAgent(train: Train, distanceTravelled: Double) extends TrainAgent:
    export train.*

    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
      val minDistanceTravelled = 0.0
      copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))

    override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[TrainAgent] =
      Some(copy(distanceTravelled = distanceTravelled + routesEE.map(_.length).foldLeft(0.0)(math.max)))

  private val trainAgent3905 = FakeTrainAgent(train3905, 0.0)
  private val trainAgent3906 = FakeTrainAgent(train3906, 0.0)
  private val trainAgent3907 = FakeTrainAgent(train3907, 0.0)

  private val trainAgents = Seq(trainAgent3905, trainAgent3906, trainAgent3907)

  private val env = RailwayEnvironment(
    Time(0, 0, 0),
    ConfigurationData(
      stationsEE,
      routesEE,
      trainAgents,
      timetables
    )
  )

  "RailwayEnvironment" when:
    "created" should:
      "maintain the configuration" in:
        println("todo")

    "doStep" should:
      "move train into route" in:
        env.trains.find(_.name == trainAgent3905.name) match
          case Some(train) =>
            env.stations.flatMap(_.containers.flatMap(_.trains)).map(_.name).contains(trainAgent3905.name) shouldBe true
            val newEnv = env.doStep(dt)
            (newEnv.stations.find(_.name == stationA.name), newEnv.routes.find(_.id == routeAB.id)) match
              case (Some(stationEE), Some(routeEE)) =>
                stationEE.containers.flatMap(_.trains).map(_.name).contains(trainAgent3905.name) shouldBe false
                val updatedAgent = routeEE.containers.flatMap(_.trains).find(_.name == trainAgent3905.name)
                updatedAgent shouldBe defined
                newEnv.findCurrentTimeTableFor(trainAgent3905).flatMap(_.currentRoute).flatMap(
                  newEnv.findRouteWithTravelDirection
                ).flatMap((ree, dir) =>
                  routeEE.containers.find(_.contains(trainAgent3905)).flatMap(_.currentDirection).map(_ == dir)
                ) shouldBe Some(true)
                updatedAgent.map(_.distanceTravelled) shouldBe Some(0.0)
              case _ => fail()
          case None => fail()

      "move train into station" in:
        env.trains.find(_.name == trainAgent3905.name) match
          case Some(train) =>
            val newEnv = env.doStep(dt).doStep(dt)
            println(newEnv.timetables)
            (newEnv.stations.find(_.name == stationB.name), newEnv.routes.find(_.id == routeAB.id)) match
              case (Some(stationEE), Some(routeEE)) =>
                println(stationEE.containers.flatMap(_.trains).map(_.name))
                val updatedAgent = stationEE.containers.flatMap(_.trains).find(_.name == trainAgent3905.name)
                updatedAgent shouldBe defined
                updatedAgent.map(_.distanceTravelled) shouldBe Some(0.0)
                routeEE.containers.flatMap(_.trains).map(_.name).contains(trainAgent3905.name) shouldBe false
                routeEE.containers.foreach(_.currentDirection shouldBe None)
              case _ => fail()
          case None => fail()

      "change schedule" in:
        env.agents.collect({ case ta: TrainAgent => ta }).find(_.name == trainAgent3905.name) match
          case Some(train) =>
            val newEnv0 = env.doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt)
            println("lastStep")
            val newEnv = newEnv0.doStep(dt)
            val ctt = newEnv.findCurrentTimeTableFor(trainAgent3905).map(ctt =>
              println(ctt.table)
              println(ctt.effectiveTable)
              println(ctt.currentRoute)
              println(ctt.nextRoute)
              println(ctt.completed)
              ctt
            )
            newEnv.findCurrentTimeTableFor(trainAgent3905) shouldBe Some(DynamicTimetable(timetable2))
          case _ => fail()

      "complete schedules" in:
        val newEnv = env.doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(dt).doStep(
          dt
        ).doStep(dt).doStep(dt).doStep(dt)
        newEnv.findCurrentTimeTableFor(trainAgent3905) shouldBe None

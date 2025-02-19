package ulisse.applications.useCases

import cats.data.NonEmptyChain
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.GenericError
import ulisse.entities.Routes
import ulisse.entities.timetable.MockedEntities.AppStateMocked
import ulisse.entities.timetable.TestMockedEntities.{stationA, stationB, stationC, AV1000Train}
import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.TrainTimetable
import ulisse.utils.Times.FluentDeclaration.h

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps

/** Configuration of test environment. Initialization of manager needed in tests */
object TestEnvConfig:
  import ulisse.entities.Routes.{Route, TypeRoute}
  import ulisse.entities.timetable.TestMockedEntities.*
  val initState: Either[NonEmptyChain[Routes.Errors], AppStateMocked] =
    for
      routeAB <- Route(stationA, stationB, TypeRoute.AV, railsCount = 1, length = 10)
      routeBC <- Route(stationB, stationC, TypeRoute.AV, railsCount = 1, length = 15)
      routeCD <- Route(stationC, stationD, TypeRoute.AV, railsCount = 1, length = 30)
    yield AppStateMocked(
      trainManager = TrainManager(List(AV1000Train)),
      timetableManager = TimetableManager(List.empty),
      routeManager = RouteManager.createOf(List(routeAB, routeBC, routeCD))
    )

class TimetableServiceTest extends AnyFeatureSpec with GivenWhenThen:
  import ulisse.entities.timetable.MockedEntities.AppStateTimetable
  type AppState = AppStateTimetable

  TestEnvConfig.initState in: initialState =>
    val eventStream                     = LinkedBlockingQueue[AppState => AppState]()
    val inputPort: TimetablePorts.Input = TimetableService(eventStream)
    def updateState()                   = runAll(initialState, eventStream)

    Feature("User can create, get and delete train timetables"):
      h(9).m(30) in: departureTime =>
        Scenario("User creates new valid timetable"):
          Given("A valid train name, station names list and optional wait time for each station")
          val trainName          = AV1000Train.name
          val stationListNoStops = List(stationA, stationB, stationC).map(s => (s.name, None))
          When("User request to save timetable")
          val requestResult = inputPort.createTimetable(trainName, departureTime, stationListNoStops)
          updateState()
          Then("timetable should be saved without errors")
          Await.result(requestResult, Duration.Inf) match
            case Left(e) => fail(s"Timetable not created cause: $e")
            case Right(List(t: TrainTimetable)) =>
              t.departureTime shouldBe departureTime
              t.arrivingStation shouldBe stationC
              t.startStation shouldBe stationA
              t.transitStations shouldBe List(stationB)
            case _ => fail(s"wrong return type")

        Scenario("User tries to create timetable with invalid train name"):
          Given("A train name that not exists, existing station names list and optional wait time for each station")
          val invalidName = "RV1000"
          When("User request to save timetable")
          val requestResult = inputPort.createTimetable(invalidName, departureTime, List())
          updateState()
          Then("should be returned an invalid train name error")
          Await.result(requestResult, Duration.Inf) shouldBe Left(GenericError(s"Train $invalidName not found"))

        Scenario("User tries to create timetable with some not existing station names"):
          Given("A valid train name, existing station names list with some invalid station name")
          When("User request to save timetable")
          Then("should be returned an invalid station error")

        Scenario("User tries to create timetable with invalid order of station names "):
          Given("A valid train name, valid station names with invalid order")
          When("User request to save timetable")
          Then("should be returned an invalid route error")

  extension [E, R](item: Either[E, R])
    def in(test: R => Unit): Unit =
      item match
        case Left(e)  => fail(s"initialization error: ${e.leftSide}")
        case Right(t) => test(t)

  extension [E, R](f: Either[E, R])
    def and[E1, R1](g: Either[E1, R1])(test: (r: R, e: R1) => Unit): Unit =
      f in: r =>
        g in: e =>
          test(r, e)

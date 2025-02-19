package ulisse.applications.useCases

import cats.data.NonEmptyChain
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import ulisse.TestUtility.and
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.GenericError
import ulisse.entities.Routes
import ulisse.entities.timetable.TestMockedEntities.{stationA, stationB, stationC, AV1000Train}
import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.TrainTimetable
import ulisse.utils.Times.FluentDeclaration.h
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps

class TimetableServiceTest extends AnyFeatureSpec with GivenWhenThen:
  def TestEnvironment(): Either[NonEmptyChain[Routes.Errors], TimetableTestEnvironment.TestEnvConfig] =
    TimetableTestEnvironment()

  Feature("User can create, get and delete train timetables"):
    Scenario("User creates new valid timetable"):
      (TestEnvironment() and h(9).m(30)): (env, departureTime) =>
        Given("A valid train name, station names list and optional wait time for each station")
        val trainName          = AV1000Train.name
        val stationListNoStops = List(stationA, stationB, stationC).map(s => (s.name, None))
        When("User request to save timetable")
        val requestResult = env.inputPort.createTimetable(trainName, departureTime, stationListNoStops)
        env.updateState()
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
      (TestEnvironment() and h(9).m(30)): (env, departureTime) =>
        Given("A train name that not exists, existing station names list and optional wait time for each station")
        val invalidName = "RV1000"
        When("User request to save timetable")
        val requestResult = env.inputPort.createTimetable(invalidName, departureTime, List())
        env.updateState()
        Then("should be returned an invalid train name error")
        Await.result(requestResult, Duration.Inf) shouldBe Left(GenericError(s"Train $invalidName not found"))

    Scenario("User tries to create timetable with some not existing station names"):
      (TestEnvironment() and h(9).m(30)): (env, departureTime) =>
        Given("A valid train name, existing station names list with some invalid station name")
        When("User request to save timetable")
        Then("should be returned an invalid station error")

    Scenario("User tries to create timetable with invalid order of station names"):
      (TestEnvironment() and h(8).m(30)): (env, departureTime) =>
        Given("A valid train name, valid station names with invalid order (no links between some station)")
        val invalidOrderStations = List(stationA, stationC).map(s => (s.name, None))
        When("User request to save timetable")
        val requestResult = env.inputPort.createTimetable(AV1000Train.name, departureTime, invalidOrderStations)
        env.updateState()
        Then("should be returned an invalid route error")
        Await.result(requestResult, Duration.Inf) shouldBe Left(GenericError("some route not exists"))

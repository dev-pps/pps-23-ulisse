package ulisse.applications.useCases

import cats.data.NonEmptyChain
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import ulisse.TestUtility.{and, in}
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.GenericError
import ulisse.entities.Routes
import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.TrainTimetable
import ulisse.utils.Times.FluentDeclaration.h

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.postfixOps

class TimetableServiceTest extends AnyFeatureSpec with GivenWhenThen:
  import ulisse.entities.timetable.TestMockedEntities.{stationA, stationB, stationC, stationD, AV1000Train}
  def TestEnvironment(): Either[NonEmptyChain[Routes.Errors], TimetableTestEnvironment.TestEnvConfig] =
    TimetableTestEnvironment()

  Feature("User can create train timetables"):
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
        val notExistingStation = "Station Z"
        val invalidStations    = List(notExistingStation, stationC.name, stationD.name).map(s => (s, None))
        When("User request to save timetable")
        val requestResult = env.inputPort.createTimetable(AV1000Train.name, departureTime, invalidStations)
        env.updateState()
        Then("should be returned an invalid station error")
        Await.result(requestResult, Duration.Inf) shouldBe Left(GenericError("some route not exists"))

    Scenario("User tries to create timetable with invalid order of station names"):
      (TestEnvironment() and h(8).m(30)): (env, departureTime) =>
        Given("A valid train name, valid station names with invalid order (no links between some station)")
        val invalidOrderStations = List(stationA, stationC).map(s => (s.name, None))
        When("User request to save timetable")
        val requestResult = env.inputPort.createTimetable(AV1000Train.name, departureTime, invalidOrderStations)
        env.updateState()
        Then("should be returned an invalid route error")
        Await.result(requestResult, Duration.Inf) shouldBe Left(GenericError("some route not exists"))

  Feature("User can get timetables of a train"):
    Scenario("User request all timetables of a train"):
      TestEnvironment() in: env =>
        (h(9).m(30) and h(18).m(30)): (time1, time2) =>
          Given("Train name and that there are at least one timetable saved")
          val trainName       = AV1000Train.name
          val stationsTable1  = List(stationA, stationB, stationC).map(s => (s.name, None))
          val stationsTable2  = List(stationC, stationD).map(s => (s.name, None))
          val createTable1Res = env.inputPort.createTimetable(trainName, time1, stationsTable1)
          val createTable2Res = env.inputPort.createTimetable(trainName, time2, stationsTable2)
          When("User request timetables giving valid train name")
          val timetableOfResult = env.inputPort.timetablesOf(trainName)
          env.updateState()
          Then("should be returned all saved timetables")
          Await.result(createTable1Res, Duration.Inf) match
            case Left(e)  => fail(s"table 1 not created: $e")
            case Right(_) => println("table 1 OK")
          Await.result(createTable2Res, Duration.Inf) match
            case Left(e)  => fail(s"table 2 not created: $e")
            case Right(_) => println("table 2 OK")
          Await.result(timetableOfResult, Duration.Inf) match
            case Left(e) => fail(s"error in retrieving tables with correct train name: $e")
            case Right(List(t: TrainTimetable, t2: TrainTimetable)) =>
              t.departureTime shouldBe time1
              t.arrivingStation shouldBe stationC
              t.startStation shouldBe stationA
              t.transitStations shouldBe List(stationB)
              t2.departureTime shouldBe time2
              t2.arrivingStation shouldBe stationC
              t2.startStation shouldBe stationD
              t2.transitStations shouldBe List.empty[TrainTimetable]
            case r => fail(s"wrong return type: $r")

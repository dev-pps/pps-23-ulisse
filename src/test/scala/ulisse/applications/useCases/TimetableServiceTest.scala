package ulisse.applications.useCases

import cats.data.NonEmptyChain
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers.shouldBe
import ulisse.TestUtility.{and, in}
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.ports.TimetablePorts.RequestResult
import ulisse.applications.ports.TimetablePorts.TimetableServiceErrors.{GenericError, InvalidStation, UnavailableTracks}
import ulisse.entities.route.Routes
import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.utils.Times.FluentDeclaration.h

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.language.postfixOps

class TimetableServiceTest extends AnyFeatureSpec with GivenWhenThen:
  import ulisse.entities.timetable.TestMockedEntities.{stationA, stationB, stationC, stationD, AV1000Train, AV800Train}
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
          case Right(List(t: Timetable)) =>
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
        Await.result(requestResult, Duration.Inf) shouldBe Left(
          InvalidStation(s"Invalid station sequence: some route not exists")
        )

    Scenario("User tries to create timetable with invalid order of station names"):
      (TestEnvironment() and h(8).m(30)): (env, departureTime) =>
        Given("A valid train name, valid station names with invalid order (no links between some station)")
        val invalidOrderStations = List(stationA, stationC).map(s => (s.name, None))
        When("User request to save timetable")
        val requestResult = env.inputPort.createTimetable(AV1000Train.name, departureTime, invalidOrderStations)
        env.updateState()
        Then("should be returned an invalid route error")
        Await.result(requestResult, Duration.Inf) shouldBe Left(
          InvalidStation(s"Invalid station sequence: some route not exists")
        )

    Scenario(
      "User can create as much as many Timetable (same departure time and station) as there are the tracks in the station"
    ):
      (TestEnvironment() and h(8).m(30)): (env, departureTime) =>
        Given("A departure time and a departing station that has one track")
        val departingStation = stationA
        val stationsSeq      = List(departingStation, stationB).map(s => (s.name, None))
        val doneRes          = env.inputPort.createTimetable(AV1000Train.name, departureTime, stationsSeq)
        When("User save two timetable (one per train) with same departure time and station")
        val failRes = env.inputPort.createTimetable(AV800Train.name, departureTime, stationsSeq)
        env.updateState()
        Then("The second timetable should not be saved due to error UnavailableTracks")
//        Await.result(doneRes, Duration.Inf) shouldBe Right
        Await.result(failRes, Duration.Inf) shouldBe Left(UnavailableTracks(departingStation.name))

  Feature("User can get timetables of a train"):
    Scenario("User request all timetables of a train"):

      def handleTableCreationRes(tableResult: Future[RequestResult], tableName: String): Unit = {
        Await.result(tableResult, Duration.Inf) match
          case Left(e)  => fail(s"$tableName not created: $e")
          case Right(_) => println(s"test $tableName created")
      }

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
            case Right(_) => println("test table 1 created")
          handleTableCreationRes(createTable1Res, "table 1")
          handleTableCreationRes(createTable2Res, "table 2")
          Await.result(timetableOfResult, Duration.Inf) match
            case Left(e) => fail(s"error in retrieving tables with correct train name: $e")
            case Right(List(t: Timetable, t2: Timetable)) =>
              t.departureTime shouldBe time1
              t.arrivingStation shouldBe stationC
              t.startStation shouldBe stationA
              t.transitStations shouldBe List(stationB)
              t2.departureTime shouldBe time2
              t2.arrivingStation shouldBe stationD
              t2.startStation shouldBe stationC
              t2.transitStations shouldBe List.empty[Timetable]
            case r => fail(s"wrong return type: $r")

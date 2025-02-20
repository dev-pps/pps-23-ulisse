package ulisse.applications.managers

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import TimetableManagers.TimetableManagerErrors.{AcceptanceError, TimetableNotFound}
import ulisse.TestUtility.in
import ulisse.entities.Routes.TypeRoute.AV
import ulisse.entities.timetable.Timetables.{PartialTimetable, RailInfo, TrainTimetable}
import ulisse.utils.Times.FluentDeclaration.h

class TimetableManagerTest extends AnyFeatureSpec with GivenWhenThen:

  import ulisse.entities.train.Trains.{Train, TrainTechnology}
  private def trainRV_3905: Train =
    val tech = TrainTechnology("Normal", 160, 1.0, 1.5)
    import ulisse.entities.train.Wagons
    Train("RV-3905", tech, Wagons.PassengerWagon(200), 12)

  import ulisse.entities.station.Station
  import ulisse.entities.Coordinate
  private val stationA  = Station("Station A", Coordinate(0, 0), 1)
  private val stationB  = Station("Station B", Coordinate(16, 0), 1)
  private val stationC  = Station("Station C", Coordinate(20, 0), 1)
  private val railAV_10 = RailInfo(length = 10, typeRoute = AV)

  Feature("Users can save or remove train timetables"):
    val testTimetable =
      PartialTimetable(trainRV_3905, startStation = stationA, departureTime = h(9).m(0)).map(
        _.transitIn(stationB)(railAV_10)
          .arrivesTo(stationC)(railAV_10)
      )

    Scenario("Save new train timetable"):
      testTimetable in: simpleTimetable =>
        val managerWithSimpleTimetable = TimetableManagers.TimetableManager(List(simpleTimetable))
        Given("a TrainTimetable and a new brand TimeTableManager")
        val emptyManager = TimetableManagers.emptyManager()
        When("I request to save timetable built on a sequence of connected stations (exist route between station)")
        Then("timetable should be saved")
        for
          m <- emptyManager.save(simpleTimetable)
        yield m.tablesOf(trainName = trainRV_3905.name) should be(Right(List(simpleTimetable)))

        Given("Timetable manager with some train's timetable saved")
        When("I save new train timetable that overlaps on existing ones")
        val overlappedTimetable =
          PartialTimetable(trainRV_3905, startStation = stationA, departureTime = h(9).m(30)).map(
            _.transitIn(stationB)(railAV_10)
              .arrivesTo(stationC)(railAV_10)
          )
        Then("Should be returned an error and timetable is not saved")
        overlappedTimetable in: t2 =>
          managerWithSimpleTimetable.save(t2) should be(
            Left(AcceptanceError("Overlapped timetable: train not available"))
          )

        Given("Timetable manager with some train's timetable saved")
        When("I save new train timetable that overlaps on existing ones")
        val newValidTimetable = PartialTimetable(trainRV_3905, startStation = stationA, departureTime = h(1).m(0)).map(
          _.transitIn(stationB)(railAV_10)
            .arrivesTo(stationC)(railAV_10)
        )
        Then("Should be returned an error and timetable is not saved")
        newValidTimetable.in: t2 =>
          managerWithSimpleTimetable.save(t2) should be(Right(TimetableManagers.TimetableManager(List(
            simpleTimetable,
            t2
          ))))

    Scenario("Request not existing timetables of a train"):
      Given("A timetable manager without any timetables")
      val timetableManager = TimetableManagers.emptyManager()
      When("I request timetables by train name")
      val res = timetableManager.tablesOf("trainName")
      Then("An error should be returned")
      res should be(Left(TimetableNotFound("trainName")))

    Scenario("Remove a train timetable"):
      testTimetable in: t =>
        Given("A timetable manager with one timetable for a train")
        val manager = TimetableManagers.TimetableManager(List(t))
        When("I request to remove that timetable")
        val requestResult = manager.remove(trainName = t.train.name, departureTime = t.departureTime)
        Then("new empty manager should be returned")
        requestResult should be(Right(TimetableManagers.emptyManager()))
        Then("no timetable should be available for the train if requested (error TimetableNotFound)")
        requestResult match
          case Right(m) => m.tablesOf(t.train.name) should be(Left(TimetableNotFound(t.train.name)))
          case Left(e)  => fail(s"Unexpected result: $e")

    Scenario("Remove a train timetable that not saved"):
      testTimetable in: t =>
        Given("A timetable manager empty")
        val manager = TimetableManagers.emptyManager()
        When("I request to remove that timetable")
        val requestResult = manager.remove(trainName = t.train.name, departureTime = t.departureTime)
        Then("should be returned an error")
        requestResult should be(Left(TimetableNotFound(t.train.name)))

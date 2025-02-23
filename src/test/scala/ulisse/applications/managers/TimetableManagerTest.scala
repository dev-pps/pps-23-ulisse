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
  private val stationA      = Station("Station A", Coordinate(0, 0), 1)
  private val stationB      = Station("Station B", Coordinate(16, 0), 1)
  private val stationC      = Station("Station C", Coordinate(20, 0), 1)
  private val railAV_10     = RailInfo(length = 10, typeRoute = AV)
  private val departTime9_0 = h(9).m(0).getOrDefault

  Feature("Users can save or remove train timetables"):
    val testTimetable =
      PartialTimetable(trainRV_3905, startStation = stationA, departTime9_0)
        .transitIn(stationB)(railAV_10)
        .arrivesTo(stationC)(railAV_10)

    Scenario("Save new train timetable"):
      val managerWithSimpleTimetable = TimetableManagers.TimetableManager(List(testTimetable))

      Given("a TrainTimetable and a new brand TimeTableManager")
      val emptyManager = TimetableManagers.emptyManager()
      When("I request to save timetable built on a sequence of connected stations (exist route between station)")
      Then("timetable should be saved")
      val res = emptyManager.save(testTimetable)
      res match
        case Left(e)  => fail(s"Error in saving timetable: $e")
        case Right(m) => m.tablesOf(trainName = trainRV_3905.name) should be(Right(List(testTimetable)))

      Given("Timetable manager with some train's timetable saved")
      When("I save new train timetable that overlaps on existing ones")
      val overlapOffset        = h(0).m(1)
      val overlappedDepartTime = departTime9_0 ++ overlapOffset
      val overlappedTimetable =
        PartialTimetable(trainRV_3905, startStation = stationA, overlappedDepartTime.getOrDefault)
          .transitIn(stationB)(railAV_10)
          .arrivesTo(stationC)(railAV_10)
      Then("Should be returned an error and timetable is not saved")
      managerWithSimpleTimetable.save(overlappedTimetable) should be(
        Left(AcceptanceError("Overlapped timetable: train not available"))
      )

      Given("Timetable manager with some train's timetable saved")
      When("I save new train timetable that Not overlaps")
      h(10).m(35) in: departureTime =>
        val newValidTimetable =
          PartialTimetable(trainRV_3905, startStation = stationA, departureTime)
            .transitIn(stationB)(railAV_10)
            .arrivesTo(stationC)(railAV_10)

        Then("Should be returned ah updated manager with new timetable")
        val result = managerWithSimpleTimetable.save(newValidTimetable)
        result should be(Right(TimetableManagers.TimetableManager(List(
          testTimetable,
          newValidTimetable
        ))))

    Scenario("Request not existing timetables of a train"):
      Given("A timetable manager without any timetables")
      val timetableManager = TimetableManagers.emptyManager()
      When("I request timetables by train name")
      val res = timetableManager.tablesOf("trainName")
      Then("An error should be returned")
      res should be(Left(TimetableNotFound("trainName")))

    Scenario("Remove a train timetable"):
      Given("A timetable manager with one timetable for a train")
      val manager = TimetableManagers.TimetableManager(List(testTimetable))
      When("I request to remove that timetable")
      val requestResult =
        manager.remove(trainName = testTimetable.train.name, departureTime = testTimetable.departureTime)
      Then("new empty manager should be returned")
      requestResult should be(Right(TimetableManagers.emptyManager()))
      Then("no timetable should be available for the train if requested (error TimetableNotFound)")
      requestResult match
        case Right(m) =>
          m.tablesOf(testTimetable.train.name) should be(Left(TimetableNotFound(testTimetable.train.name)))
        case Left(e) => fail(s"Unexpected result: $e")

    Scenario("Remove a train timetable that not saved"):
      Given("A timetable manager empty")
      val manager = TimetableManagers.emptyManager()
      When("I request to remove that timetable")
      val requestResult = manager.remove(testTimetable.train.name, testTimetable.departureTime)
      Then("should be returned an error")
      requestResult should be(Left(TimetableNotFound(testTimetable.train.name)))

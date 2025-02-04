package ulisse.applications.useCases

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import ulisse.applications.useCases.TimetableManagers.TimetableManagerErrors.{AcceptanceError, TimetableNotFound}
import ulisse.entities.timetable.Timetables.{PartialTimetable, TrainTimetable}
import ulisse.utils.Times.FluentDeclaration.h

class TimetableManagerTest extends AnyFeatureSpec with GivenWhenThen:

  import ulisse.utils.Times.ClockTimeErrors
  extension (timetable: Either[ClockTimeErrors, TrainTimetable])
    def performTest(test: TrainTimetable => Unit): Unit =
      timetable match
        case Left(err) => fail(s"Some invalid timetable was used in test! error: $err")
        case Right(t)  => test(t)

  import ulisse.entities.train.Trains.{Train, TrainTechnology}
  private def trainRV_3905: Train =
    val tech = TrainTechnology("Normal", 160, 1.0, 1.5)
    import ulisse.entities.train.Wagons
    Train("RV-3905", tech, Wagons.PassengerWagon(200), 12)

  import ulisse.entities.station.Station
  import ulisse.entities.Coordinates.Coordinate
  private val stationA = Station("Station A", Coordinate(0, 0), 1)
  private val stationB = Station("Station B", Coordinate(16, 0), 1)
  private val stationC = Station("Station C", Coordinate(20, 0), 1)

  Feature("Users can save multiple timetable of a train"):
    Scenario("Save new train timetable"):
      val timetable =
        PartialTimetable(trainRV_3905, startFrom = stationA, departureTime = h(9).m(0)).map(
          _.transitIn(stationB)
            .arrivesTo(stationC)
        )
      timetable performTest: t =>
        Given("a TrainTimetable and a new brand TimeTableManager")
        val emptyManager = TimetableManagers.emptyManager()
        When("I request to save timetable built on a sequence of connected stations (exist route between station)")
        Then("timetable should be saved")
        for
          m <- emptyManager.save(t)
        yield m.tablesOf(trainName = trainRV_3905.name) should be(Right(List(t)))

        Given("Timetable manager with some train's timetable saved")
        val manager = TimetableManagers.TimetableManager(Map((trainRV_3905, List(t))))
        When("I save new train timetable that overlaps on existing ones")
        val overlappedTimetable = PartialTimetable(trainRV_3905, startFrom = stationA, departureTime = h(9).m(30)).map(
          _.transitIn(stationB)
            .arrivesTo(stationC)
        )
        Then("Should be returned an error and timetable is not saved")
        overlappedTimetable.performTest: t2 =>
          manager.save(t2) should be(Left(AcceptanceError("Overlapped timetable: train not available")))

        Given("Timetable manager with some train's timetable saved")
        When("I save new train timetable that overlaps on existing ones")
        val newValidTimetable = PartialTimetable(trainRV_3905, startFrom = stationA, departureTime = h(1).m(0)).map(
          _.transitIn(stationB)
            .arrivesTo(stationC)
        )
        Then("Should be returned an error and timetable is not saved")
        newValidTimetable.performTest: t2 =>
          manager.save(t2) should be(Right(TimetableManagers.TimetableManager(Map((trainRV_3905, List(t, t2))))))

    Scenario("Request not existing timetables of a train"):
      Given("A timetable manager without any timetables")
      val timetableManager = TimetableManagers.emptyManager()
      When("I request timetables by train name")
      val res = timetableManager.tablesOf("trainName")
      Then("An error should be returned")
      res should be(Left(TimetableNotFound("trainName")))

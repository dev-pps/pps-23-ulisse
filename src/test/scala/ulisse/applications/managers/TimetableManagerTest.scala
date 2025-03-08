package ulisse.applications.managers

import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.must.Matchers.be
import org.scalatest.matchers.should.Matchers.should
import TimetableManagers.TimetableManagerErrors.{AcceptanceError, TimetableNotFound}
import ulisse.Utils.TestUtility.in
import ulisse.entities.route.Routes.RouteType.AV
import ulisse.entities.timetable.Timetables.{RailInfo, Timetable, TimetableBuilder}
import ulisse.utils.Times.FluentDeclaration.h
import ulisse.utils.Times.Time

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
  private val railAV_10     = RailInfo(length = 16, typeRoute = AV)
  private val departTime9_0 = h(9).m(0).getOrDefault

  val timetableAB: Timetable = TimetableBuilder(trainRV_3905, startStation = stationA, h(12).m(0).getOrDefault)
    .arrivesTo(stationB)(railAV_10)
  val timetableABC: Timetable =
    TimetableBuilder(trainRV_3905, startStation = stationA, departTime9_0)
      .transitIn(stationB)(railAV_10)
      .arrivesTo(stationC)(railAV_10)
  val timetableBC: Timetable = TimetableBuilder(trainRV_3905, startStation = stationB, h(20).m(30).getOrDefault)
    .arrivesTo(stationC)(railAV_10)

  Feature("User can init manager giving timetables. Timetables are validated."):
    Scenario("Init manager with a list of timetable"):
      Given("List of timetables for the same train, with one overlapped timetable")
      val overlappedTime = timetableAB.departureTime ++ h(0).m(2)
      val overlappedTable =
        TimetableBuilder(trainRV_3905, stationA, overlappedTime.getOrDefault).arrivesTo(stationB)(railAV_10)
      val timetablesInit = List(timetableAB, overlappedTable)
      When("New TimetableManager is initialized with the list of timetables")
      val manager = TimetableManagers.TimetableManager(timetablesInit)
      Then("TimetableManager should contains timetables without the overlapped on")
      val actualSavedShort = manager.tables.map(t => (t.startStation.name, t.departureTime.asTime))
      actualSavedShort should be(List((stationA.name, Time(12, 0, 0))))

  Feature("Users can save train timetables"):
    Scenario("Save new train timetable"):
      val managerWithSimpleTimetable = TimetableManagers.TimetableManager(List(timetableABC))

      Given("a Timetable and a new brand TimetableManager")
      val emptyManager = TimetableManagers.empty()
      When("I request to save timetable built on a sequence of connected stations (exist route between station)")
      Then("timetable should be saved")
      val res = emptyManager.save(timetableABC)
      res match
        case Left(e)  => fail(s"Error in saving timetable: $e")
        case Right(m) => m.tablesOf(trainName = trainRV_3905.name) should be(Right(List(timetableABC)))

      Given("Timetable manager with some train's timetable saved")
      When("I save new train timetable that overlaps on existing ones")
      val overlapOffset        = h(0).m(1)
      val overlappedDepartTime = departTime9_0 ++ overlapOffset
      val overlappedTimetable =
        TimetableBuilder(trainRV_3905, startStation = stationA, overlappedDepartTime.getOrDefault)
          .transitIn(stationB)(railAV_10)
          .arrivesTo(stationC)(railAV_10)
      Then("Should be returned an error and timetable is not saved")
      managerWithSimpleTimetable.save(overlappedTimetable) should be(
        Left(AcceptanceError("Overlapped timetable: train not available"))
      )

      Given("Timetable manager with some train's timetable saved")
      When("I save new train timetable that Not overlaps")
      h(12).m(35) in: departureTime =>
        val newValidTimetable =
          TimetableBuilder(trainRV_3905, startStation = stationA, departureTime)
            .transitIn(stationB)(railAV_10)
            .arrivesTo(stationC)(railAV_10)

        Then("Should be returned ah updated manager with new timetable")
        val result = managerWithSimpleTimetable.save(newValidTimetable)
        result match
          case Left(e)  => fail(s"save new timetable failed: $e")
          case Right(m) => m.tables should be(Seq(timetableABC, newValidTimetable))

  Feature("Users can retrieve timetables saved"):
    val timetableManager = TimetableManagers.TimetableManager(List(timetableAB, timetableBC, timetableABC))
    Scenario("Request not existing timetables of a train"):
      Given("A timetable manager without any timetables")
      val timetableManager = TimetableManagers.empty()
      When("I request timetables by train name")
      val res = timetableManager.tablesOf("trainName")
      Then("An error should be returned")
      res should be(Left(TimetableNotFound("trainName")))

    Scenario("Get all saved tables"):
      Given("A timetable manager with 3 timetables saved")
      When("User requests all timetables")
      val timetableSavedResult = timetableManager.tables
      Then("Should returned 3 timetables")
      timetableSavedResult.size should be(3)
      timetableSavedResult.map(_.departureTime.asTime) should be(Seq(Time(20, 30, 0), Time(12, 0, 0), Time(9, 0, 0)))

    Scenario("Request all timetables filtered by departure station"):
      Given("A timetable manager with some timetables saved")
      When("User requests timetables filtered by departure station A")
      val res = timetableManager.tables.filter(_.startStation.name == "Station A")
      Then("An error should be returned")
      res.size should be(2)

  Feature("Users can remove a timetable"):
    Scenario("Remove a train timetable"):
      Given("A timetable manager with one timetable for a train")
      val manager = TimetableManagers.TimetableManager(List(timetableABC))
      When("I request to remove that timetable")
      val requestResult =
        manager.remove(trainName = timetableABC.train.name, departureTime = timetableABC.departureTime)
      Then("new empty manager should be returned")
      requestResult should be(Right(TimetableManagers.empty()))
      Then("no timetable should be available for the train if requested (error TimetableNotFound)")
      requestResult match
        case Right(m) =>
          m.tablesOf(timetableABC.train.name) should be(
            Left(TimetableNotFound(timetableABC.train.name))
          )
        case Left(e) => fail(s"Unexpected result: $e")

    Scenario("Remove a train timetable that not saved"):
      Given("A timetable manager empty")
      val manager = TimetableManagers.empty()
      When("I request to remove that timetable")
      val requestResult = manager.remove(timetableABC.train.name, timetableABC.departureTime)
      Then("should be returned an error")
      requestResult should be(Left(TimetableNotFound(timetableABC.train.name)))

  Feature("Timetable containing some deleted train/station/route should be deleted"):
    Scenario("Delete all timetables related to a train"):
      Given("A manager with some timetable for a train")
      val manager = TimetableManagers.TimetableManager(List(timetableABC, timetableBC))
      When("I notify manager that a train is deleted")
      val result = manager.trainDeleted(timetableABC.train)
      Then("should be removed all timetables of train")
      result should be(Right(TimetableManagers.TimetableManager(List())))

    Scenario("Delete all timetables that contains a deleted station"):
      Given("A manager with some timetable containing deleted station")
      val manager = TimetableManagers.TimetableManager(List(timetableABC, timetableBC))
      When("I notify manager that a station is deleted")
      val result = manager.stationDeleted(stationA)
      Then("should be removed all timetables that have deleted station")
      result should be(Right(TimetableManagers.TimetableManager(List(timetableBC))))

    Scenario("Delete all timetables that contains a deleted route"):
      import ulisse.entities.route.Routes
      val deletedRoute = Routes.Route(stationA, stationB, railAV_10.typeRoute, 1, railAV_10.length)
      deletedRoute in: route =>
        Given("A manager with some timetable containing deleted route")
        val manager = TimetableManagers.TimetableManager(List(timetableABC, timetableBC))
        When("I notify manager that a route is deleted")
        val result = manager.routeDeleted(route)
        Then("should be removed all timetables that have deleted station")
        result should be(Right(TimetableManagers.TimetableManager(List(timetableBC))))
//
//      "timetable" should "return error in case of duplicated station" in:
//    import ulisse.entities.timetable.Timetables.TimetableBuilderErrors.DuplicatedStations
//    timetableBuilder.arrivesTo(stationA)(railAV_10) should be(Left(DuplicatedStations(List(stationA))))

  Feature("Autoupdate of timetable when route, train or station are updated"):
    Scenario("Train is updated and then all related timetables should be too"):
      import ulisse.entities.train.Wagons
      val trainAV8080 = Train("AV8080", TrainTechnology("Normal", 160, 1.0, 1.5), Wagons.PassengerWagon(200), 12)

      val newTrainTech        = TrainTechnology("Tech1", 500, 1.0, 2.0)
      val trainName           = trainRV_3905.name
      val updatedTrainRV_3905 = Train(trainName, newTrainTech, trainRV_3905.wagon, trainRV_3905.length)

      val timetableAB_newTrain = TimetableBuilder(
        updatedTrainRV_3905,
        stationA,
        timetableAB.departureTime
      ).arrivesTo(stationB)(railAV_10)

      val timetableABC_newTrain = TimetableBuilder(updatedTrainRV_3905, startStation = stationA, departTime9_0)
        .transitIn(stationB)(railAV_10)
        .arrivesTo(stationC)(railAV_10)

      val updatedIgnoredTimetable =
        TimetableBuilder(trainAV8080, startStation = stationA, departTime9_0).arrivesTo(stationB)(railAV_10)

      Given("A manager with some timetable related to the edited train")
      val manager = TimetableManagers.TimetableManager(List(timetableAB, timetableABC, updatedIgnoredTimetable))
      When("I notify manager that a train has been updated")
      val updatedManager = manager.trainUpdated(updatedTrainRV_3905)
      Then("just all timetables related to train should be updated with the new train")
      updatedManager match
        case Left(err) => fail(s"Manager wrong result: $err")
        case Right(updatedManager) =>
          updatedManager.tables.toSet should be(Set(
            timetableAB_newTrain,
            timetableABC_newTrain,
            updatedIgnoredTimetable
          ))

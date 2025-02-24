package ulisse.applications.useCases

import cats.data.NonEmptyChain
import ulisse.Runner.runAll
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TimetablePorts
import ulisse.entities.Routes
import ulisse.entities.timetable.MockedEntities.AppStateMocked

import java.util.concurrent.LinkedBlockingQueue

/** Configuration of test environment. Initialization of manager needed in tests */
object TimetableTestEnvironment:
  import ulisse.entities.Routes.{Route, TypeRoute}
  import ulisse.entities.timetable.TestMockedEntities.*
  import ulisse.entities.timetable.MockedEntities.AppStateTimetable

  type AppState = AppStateTimetable
  private val initState: Either[NonEmptyChain[Routes.Errors], AppStateMocked] =
    for
      routeAB <- Route(stationA, stationB, TypeRoute.AV, railsCount = 1, length = 10)
      routeBC <- Route(stationB, stationC, TypeRoute.AV, railsCount = 1, length = 15)
      routeCD <- Route(stationC, stationD, TypeRoute.AV, railsCount = 1, length = 30)
    yield AppStateMocked(
      trainManager = TrainManager(List(AV1000Train, AV800Train)),
      timetableManager = TimetableManager(List.empty),
      routeManager = RouteManager.createOf(List(routeAB, routeBC, routeCD))
    )

  def apply(): Either[NonEmptyChain[Routes.Errors], TestEnvConfig] =
    initState.map: state =>
      val eventStream = LinkedBlockingQueue[AppState => AppState]()
      TestEnvConfig(inputPort = TimetableService(eventStream), initialState = state, eventStream = eventStream)

  case class TestEnvConfig(
      inputPort: TimetablePorts.Input,
      private val initialState: AppStateMocked,
      private val eventStream: LinkedBlockingQueue[AppState => AppState]
  ):
    def updateState() = runAll(initialState, eventStream)

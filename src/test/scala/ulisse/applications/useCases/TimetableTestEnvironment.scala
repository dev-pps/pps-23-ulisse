package ulisse.applications.useCases

import cats.data.NonEmptyChain
import ulisse.Runner.runAll
import ulisse.applications.EventQueue
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TimetablePorts
import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.entities.route.Routes
//import ulisse.entities.timetable.MockedEntities.AppStateMocked

import java.util.concurrent.LinkedBlockingQueue

/** Configuration of test environment. Initialization of manager needed in tests */
object TimetableTestEnvironment:
  import ulisse.entities.route.Routes.{Route, TypeRoute}
  import ulisse.entities.timetable.TestMockedEntities.*
//  import ulisse.entities.timetable.MockedEntities.AppStateTimetable

//  type AppState = AppStateTimetable
  private val initState: Either[NonEmptyChain[Routes.Errors], AppState] =
    for
      routeAB <- Route(stationA, stationB, TypeRoute.AV, railsCount = 1, length = 10)
      routeBC <- Route(stationB, stationC, TypeRoute.AV, railsCount = 1, length = 15)
      routeCD <- Route(stationC, stationD, TypeRoute.AV, railsCount = 1, length = 30)
    yield AppState()
      .updateTechnology(_ => TechnologyManager(List(trainTechnology)))
      .updateTrain((_, _) => TrainManager(List(AV1000Train, AV800Train)))
      .updateRoute(_ => RouteManager.createOf(List(routeAB, routeBC, routeCD)))

  def apply(): Either[NonEmptyChain[Routes.Errors], TestEnvConfig] =
    initState.map: state =>
      val eventQueue = EventQueue()
      TestEnvConfig(inputPort = TimetableService(eventQueue), initialState = state, eventQueue = eventQueue)

  case class TestEnvConfig(
      inputPort: TimetablePorts.Input,
      private val initialState: AppState,
      private val eventQueue: EventQueue
  ):
    def updateState(): Seq[AppState] = runAll(initialState, eventQueue.events)

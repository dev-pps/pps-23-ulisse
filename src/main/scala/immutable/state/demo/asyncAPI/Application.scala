package immutable.state.demo.asyncAPI

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object Application:

  object Ports:
    object StationPorts:
      trait Input:
        def stationPortMethod(arg: String): Future[String]

    object RoutePorts:
      trait Input:
        def routePortMethod(arg: Int): Future[Int]

  object Adapters:
    case class StationInputAdapter(stateEventQueue: LinkedBlockingQueue[AppState => AppState])
        extends Ports.StationPorts.Input:
      def stationPortMethod(arg: String): Future[String] =
        val promise = Promise[String]()
        stateEventQueue.offer((state: AppState) => {
          println("Adding station")
          val newStationManager = state.stationManager.addStation(arg)
          state.copy(stationManager = newStationManager)
          promise.success(s"station $arg")
          state
        })
        promise.future

    case class RouteInputAdapter(stateEventQueue: LinkedBlockingQueue[AppState => AppState])
        extends Ports.RoutePorts.Input:
      def routePortMethod(arg: Int): Future[Int] =
        val promise = Promise[Int]()
        stateEventQueue.offer((state: AppState) => {
          println("Adding route")
          val newRouteManager = state.routeManager.addRoute(arg)
          state.copy(routeManager = newRouteManager)
          promise.success(arg)
          state
        })
        promise.future

  case class StationManager(stations: List[String]):
    def addStation(station: String): StationManager = StationManager(station :: stations)
  case class RouteManager(routes: List[Int]):
    def addRoute(route: Int): RouteManager = RouteManager(route :: routes)

  case class AppState(stationManager: StationManager, routeManager: RouteManager)

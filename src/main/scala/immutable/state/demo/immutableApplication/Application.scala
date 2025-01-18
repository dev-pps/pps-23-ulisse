package immutable.state.demo.immutableApplication

import immutable.state.demo.immutableApplication.States.State
import java.util.concurrent.LinkedBlockingQueue

object Application:

  object Ports:
    object StationPorts:
      trait Input:
        def stationPortMethod(arg: String): State[AppState, String]

    object RoutePorts:
      trait Input:
        def routePortMethod(arg: Int): State[AppState, Int]

  object Adapters:
    case class StationInputAdapter() extends Ports.StationPorts.Input:
      def stationPortMethod(arg: String): State[AppState, String] =
        State { state =>
          println("Adding station")
          val newStationManager = state.stationManager.addStation(arg)
          val newState          = state.copy(stationManager = newStationManager)
          (newState, s"station $arg")
        }

    case class RouteInputAdapter() extends Ports.RoutePorts.Input:
      def routePortMethod(arg: Int): State[AppState, Int] =
        State { state =>
          println("Adding route")
          val newRouteManager = state.routeManager.addRoute(arg)
          val newState        = state.copy(routeManager = newRouteManager)
          (newState, arg)
        }

  case class StationManager(stations: List[String]):
    def addStation(station: String): StationManager = StationManager(station :: stations)
  case class RouteManager(routes: List[Int]):
    def addRoute(route: Int): RouteManager = RouteManager(route :: routes)

  case class AppState(stationManager: StationManager, routeManager: RouteManager)

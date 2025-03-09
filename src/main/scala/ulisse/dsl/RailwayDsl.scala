package ulisse.dsl

import ulisse.applications.AppState
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

/** DSL for creating railway entities. */
@SuppressWarnings(Array("org.wartremover.warts.Var"))
object RailwayDsl:

  export CreateStation._, CreateTrain._, CreateRoute._, CreateAppState._ // , CreateDynamicAppState._

  given Conversion[AppStateDSL, AppState] = _.appState

  object CreateDynamicAppState:

    case class WithAppState(var appState: AppState):
      def ->(departure: Station): WithDeparture =
        appState = appState.updateStationManager(stationManager =>
          stationManager.addStation(departure).getOrElse(stationManager)
        )
        WithDeparture(appState, departure)

    case class WithDeparture(var appState: AppState, departure: Station):
      def withType(routeType: Routes.RouteType): WithRouteType = WithRouteType(appState, departure, routeType)

    case class WithRouteType(var appState: AppState, departure: Station, routeType: Routes.RouteType):
      def withPlatform(platform: Int): WithPlatform = WithPlatform(appState, departure, routeType, platform)

    case class WithPlatform(var appState: AppState, departure: Station, routeType: Routes.RouteType, platform: Int):
      def withLength(length: Double): WithLength = WithLength(appState, departure, routeType, platform, length)

    case class WithLength(
        var appState: AppState,
        departure: Station,
        routeType: Routes.RouteType,
        platform: Int,
        length: Double
    ):
      def withArrival(arrival: Station): LoopCreation =
        appState =
          appState.updateStationManager(stationManager => stationManager.addStation(arrival).getOrElse(stationManager))
            .updateRoute(manager =>
              Route(departure, arrival, routeType, platform, length).fold(
                _ => manager,
                route => manager.save(route).getOrElse(manager)
              )
            )
        LoopCreation(appState)

    case class LoopCreation(var appState: AppState):
      def add(departure: Station): WithDeparture = WithDeparture(appState, departure)

    implicit class AppStateDynamicOps(start: CreateDynamicAppState.type):
      def ->(departure: Station): WithAppState = WithAppState(AppState())

  /** Create an application state. */
  object CreateAppState:

    case class AppStateDSL(var appState: AppState):
      def put(train: Train): AppStateDSL =
        appState = appState.updateTrain((trainManager, _) => trainManager.addTrain(train).getOrElse(trainManager))
        this

      def link(route: Either[Routes.RouteError, Route]): AppStateDSL =
        appState =
          appState.updateRoute(manager => route.fold(_ => manager, route => manager.save(route).getOrElse(manager)))
        this

      def set(station: Station): AppStateDSL =
        appState =
          appState.updateStationManager(stationManager => stationManager.addStation(station).getOrElse(stationManager))
        this

    /** Create an application state with technology. */
    implicit class AppStateOps(start: CreateAppState.type):
      def ->(appState: AppState): AppStateDSL = AppStateDSL(appState)

  /** Create a station. */
  object CreateStation:

    /** Create a station with a name. */
    case class StationDSL(name: String):
      def at(coord: (Int, Int)): StationWithCoord = StationWithCoord(name, Coordinate(coord._1, coord._2))

    /** Create a station with a name and a coordinate. */
    case class StationWithCoord(name: String, coordinate: Coordinate):
      def platforms(capacity: Int): Station = Station(name, coordinate, capacity)

    /** Create a station with a name and a coordinate. */
    implicit class StationOps(start: CreateStation.type):
      def ->(name: String): StationDSL = StationDSL(name)

  /** Create a train. */
  object CreateTrain:

    /** Create a train with a name. */
    case class TrainDSL(name: String):
      def technology(technology: TrainTechnology): TrainWithTechnology = TrainWithTechnology(name, technology)

    /** Create a train with a name and a technology. */
    case class TrainWithTechnology(name: String, technology: TrainTechnology):
      def wagon(wagon: Wagons.UseType): TrainWithWagon = TrainWithWagon(name, technology, wagon)

    /** Create a train with a name, a technology, and a wagon. */
    case class TrainWithWagon(name: String, technology: TrainTechnology, wagon: Wagons.UseType):
      def numbers(number: Int): Train = Train(name, technology, Wagons.Wagon(wagon, number), number)

    /** Create a train with a name, a technology, a wagon, and a number. */
    implicit class TrainOps(start: CreateTrain.type):
      def ->(name: String): TrainDSL = TrainDSL(name)

  /** Create a route. */
  object CreateRoute:

    /** Create a route with a departure station. */
    case class RouteDSL(departure: Station):
      def ->(arrival: Station): RouteWithArrival = RouteWithArrival(departure, arrival)

    /** Create a route with a departure and an arrival station. */
    case class RouteWithArrival(departure: Station, arrival: Station):
      def on(routeType: Routes.RouteType): RouteWithType = RouteWithType(departure, arrival, routeType)

    /** Create a route with a departure, an arrival station, and a route type. */
    case class RouteWithType(departure: Station, arrival: Station, routeType: Routes.RouteType):
      def tracks(platform: Int): RouteWithPlatform = RouteWithPlatform(departure, arrival, routeType, platform)

    /** Create a route with a departure, an arrival station, a route type, and a platform. */
    case class RouteWithPlatform(departure: Station, arrival: Station, routeType: Routes.RouteType, platform: Int):
      def length(length: Double): Either[Routes.RouteError, Route] =
        Route(departure, arrival, routeType, platform, length)

    /** Create a route with a departure, an arrival station, a route type, a platform, and a length. */
    implicit class RouteOps(start: CreateRoute.type):
      def ->(departure: Station): RouteDSL = RouteDSL(departure)

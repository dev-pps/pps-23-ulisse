package ulisse.dsl

import ulisse.applications.AppState
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

import scala.annotation.targetName

/** DSL for creating railway entities. */
@SuppressWarnings(Array("org.wartremover.warts.Var", "org.wartremover.warts.ImplicitConversion"))
object RailwayDsl:

  export CreateStation._, CreateTrain._, CreateRoute._, CreateAppState._

  /** Conversion from the DSL to the application state. */
  given Conversion[AppStateDSL, AppState] = _.appState

  /** Dsl for creating an application state. */
  object CreateAppState:

    /** Create an application state with possible to add a station. */
    case class WithAppState(var appState: AppState):
      def start(departure: Station): WithDeparture =
        appState = appState.updateStationManager(stationManager =>
          stationManager.addStation(departure).getOrElse(stationManager)
        )
        WithDeparture(appState, departure)

    /** Create an application state with a departure station with possible to add a route type. */
    case class WithDeparture(var appState: AppState, departure: Station):
      /** Create an application state with a departure station and a route type. */
      def withType(routeType: Routes.RouteType): WithRouteType = WithRouteType(appState, departure, routeType)

    /** Create an application state with a departure station, a route type, and possible to add a platform. */
    case class WithRouteType(var appState: AppState, departure: Station, routeType: Routes.RouteType):
      /** Create an application state with a departure station, a route type, and a platform. */
      def tracks(platform: Int): WithPlatform = WithPlatform(appState, departure, routeType, platform)

    /** Create an application state with a departure station, a route type, a platform, and possible to add a length. */
    case class WithPlatform(var appState: AppState, departure: Station, routeType: Routes.RouteType, platform: Int):
      /** Create an application state with a departure station, a route type, a platform, and a length. */
      def length(length: Double): WithLength = WithLength(appState, departure, routeType, platform, length)

    /** Create an application state with a departure station, a route type, a platform, a length, and possible to add an arrival station. */
    case class WithLength(
        var appState: AppState,
        departure: Station,
        routeType: Routes.RouteType,
        platform: Int,
        length: Double
    ):
      /** Create an application state with a departure station, a route type, a platform, a length, and an arrival station. */
      def end(arrival: Station): AppState =
        appState.updateStationManager(stationManager => stationManager.addStation(arrival).getOrElse(stationManager))
          .updateRoute(manager =>
            Route(departure, arrival, routeType, platform, length).fold(
              _ => manager,
              route => manager.save(route).getOrElse(manager)
            )
          )

    /** Create an application state with possible to add a train, a route, and a station. */
    case class AppStateDSL(var appState: AppState):
      /** Create an application state with a train. */
      def put(train: Train): AppStateDSL =
        appState = appState.updateTrain((trainManager, _) => trainManager.addTrain(train).getOrElse(trainManager))
        this

      /** Create an application state with a route. */
      def link(route: Either[Routes.RouteError, Route]): AppStateDSL =
        appState =
          appState.updateRoute(manager => route.fold(_ => manager, route => manager.save(route).getOrElse(manager)))
        this

      /** Create an application state with a station. */
      def set(station: Station): AppStateDSL =
        appState =
          appState.updateStationManager(stationManager => stationManager.addStation(station).getOrElse(stationManager))
        this

    /** Create an application state with technology. */
    implicit class AppStateOps(start: CreateAppState.type):
      @targetName("To put element on app state")
      def ++(appState: AppState): AppStateDSL = AppStateDSL(appState)
      @targetName("To create route")
      def ->(appState: AppState): WithAppState = WithAppState(appState)

  /** Dsl for creating a station. */
  object CreateStation:

    /** Create a station with a name. */
    case class StationDSL(name: String):
      def at(coord: (Int, Int)): StationWithCoord = StationWithCoord(name, Coordinate(coord._1, coord._2))

    /** Create a station with a name and a coordinate. */
    case class StationWithCoord(name: String, coordinate: Coordinate):
      def platforms(capacity: Int): Station = Station(name, coordinate, capacity)

    /** Create a station with a name and a coordinate. */
    implicit class StationOps(start: CreateStation.type):
      @targetName("To set name")
      def ->(name: String): StationDSL = StationDSL(name)

  /** Dsl for creating a train. */
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
      @targetName("To set name")
      def ->(name: String): TrainDSL = TrainDSL(name)

  /** Dsl for creating a route. */
  object CreateRoute:

    /** Create a route with a departure station. */
    case class RouteDSL(departure: Station):
      @targetName("To add arrival")
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
      @targetName("To add departure")
      def ->(departure: Station): RouteDSL = RouteDSL(departure)

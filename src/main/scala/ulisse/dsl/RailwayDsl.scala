package ulisse.dsl

import ulisse.applications.AppState
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

/** DSL for creating railway entities. */
object RailwayDsl:

  export CreateStation._, CreateTrain._, CreateRoute._

  /** Create an application state. */
//  object CreateAppState:
//
//    type UpdateState = Station | Route
//
//    /** Create an application state. */
//    trait AppStateWith[T <: UpdateState](state: AppState):
//      def ++(el: T): AppStateWith[T]
//
//    /** Create an application state with a station. */
//    case class AppStateWithStation(appState: AppState) extends AppStateWith[Station](appState):
//      def ++(station: Station): AppStateWith[UpdateState] =
//        AppStateWithStation(appState.updateStationManager(manager => manager.addStation(station).getOrElse(manager)))
//
//    /** Create an application state with a route. */
//    case class AppStateWithRoute(appState: AppState):
//      def route(route: Route): AppState =
//        appState.updateRoute(manager => manager.save(route).getOrElse(manager))
//
//    /** Create an application state with a train. */
//    case class AppStateWithTrain(appState: AppState):
//      def train(train: Train): AppState =
//        appState.updateTrain((trainManager, _) => trainManager.addTrain(train).getOrElse(trainManager))
//
//    /** Create an application state with technology. */
//    implicit class AppStateOps(start: CreateAppState.type):
//      def trainTech(techManager: TechnologyManager[TrainTechnology]): AppStateWithStation =
//        AppStateWithStation(AppState.withTechnology(techManager))

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

package ulisse.dsl

import ulisse.applications.AppState
import ulisse.entities.Coordinate
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons
import ulisse.entities.train.Wagons.Wagon

import scala.annotation.targetName

/** DSL for creating railway entities. */
@SuppressWarnings(Array(
  "org.wartremover.warts.Var",
  "org.wartremover.warts.ImplicitConversion",
  "org.wartremover.warts.Overloading"
))
object RailwayDsl:

  export CreateStation._, CreateTrain._, CreateRoute._, CreateAppState._

  /** Conversion from the DSL to the application state. */
  given Conversion[AppStateDSL, AppState] = _.appState

  /** Dsl for creating an application state. */
  object CreateAppState:
    /** Create an application state with possible to add a train, a route, and a station. */
    final case class AppStateDSL(var appState: AppState)

    /** Create an application state with a departure station with possible to add a route type. */
    final case class WithDeparture(var appState: AppState, departure: Station)

    /** Create an application state with a departure station, a route type, and possible to add a platform. */
    final case class WithRouteType(var appState: AppState, departure: Station, routeType: Routes.RouteType)

    /** Create an application state with a departure station, a route type, a platform, and possible to add a length. */
    final case class WithPlatform(
        var appState: AppState,
        departure: Station,
        routeType: Routes.RouteType,
        platform: Int
    )

    /** Create an application state with a departure station, a route type, a platform, a length, and possible to add an arrival station. */
    final case class WithLength(
        var appState: AppState,
        departure: Station,
        routeType: Routes.RouteType,
        platform: Int,
        length: Double
    )

    extension (withDeparture: WithDeparture)
      /** Create an application state with a departure station and a route type. */
      infix def withType(routeType: Routes.RouteType): WithRouteType =
        WithRouteType(withDeparture.appState, withDeparture.departure, routeType)

    extension (withRouteType: WithRouteType)
      /** Create an application state with a departure station, a route type, and a platform. */
      infix def tracks(platform: Int): WithPlatform =
        WithPlatform(withRouteType.appState, withRouteType.departure, withRouteType.routeType, platform)

    extension (withPlatform: WithPlatform)
      /** Create an application state with a departure station, a route type, a platform, and a length. */
      infix def length(length: Double): WithLength =
        WithLength(withPlatform.appState, withPlatform.departure, withPlatform.routeType, withPlatform.platform, length)

    extension (withLength: WithLength)
      /** Create an application state with a departure station, a route type, a platform, a length, and an arrival station. */
      infix def end(arrival: Station): AppState =
        withLength.appState.updateStationManager(stationManager =>
          stationManager.addStation(arrival).getOrElse(stationManager)
        )
          .updateRoute(manager =>
            Route(withLength.departure, arrival, withLength.routeType, withLength.platform, withLength.length).fold(
              _ => manager,
              route => manager.save(route).getOrElse(manager)
            )
          )

    extension (appStateDsl: AppStateDSL)

      /** Create an application state with a departure station form a route. */
      infix def start(departure: Station): WithDeparture =
        appStateDsl.appState = appStateDsl.appState.updateStationManager(stationManager =>
          stationManager.addStation(departure).getOrElse(stationManager)
        )
        WithDeparture(appStateDsl.appState, departure)

      /** Create an application state with a train. */
      infix def put(train: Train): AppStateDSL =
        appStateDsl.appState =
          appStateDsl.appState.createTrain((trainManager, _) => trainManager.addTrain(train).getOrElse(trainManager))
        appStateDsl

      /** Create an application state with a route. */
      infix def link(route: Either[Routes.RouteError, Route]): AppStateDSL =
        appStateDsl.appState =
          appStateDsl.appState.updateRoute(manager =>
            route.fold(_ => manager, route => manager save route getOrElse manager)
          )
        appStateDsl

      /** Create an application state with a station. */
      infix def set(station: Station): AppStateDSL =
        appStateDsl.appState =
          appStateDsl.appState.updateStationManager(stationManager =>
            stationManager.addStation(station).getOrElse(stationManager)
          )
        appStateDsl

      /** Create an application state with a timetable. */
      infix def scheduleA(timetable: Timetable): AppStateDSL =
        appStateDsl.appState =
          appStateDsl.appState.updateTimetable(manager => manager.save(timetable).getOrElse(manager))
        appStateDsl

    /** Create an application state with technology. */
    implicit class AppStateOps(start: CreateAppState.type):
      /** Create an application state with technology. */
      @targetName("To put element on app state")
      infix def ||(appState: AppState): AppStateDSL = AppStateDSL(appState)

      /** Create an application state with technology. */
      @targetName("To create route")
      infix def |->(appState: AppState): AppStateDSL = AppStateDSL(appState)

  /** Dsl for creating a station. */
  object CreateStation:
    /** Create a station with a name. */
    final case class StationDSL(name: String)

    /** Create a station with a name and a coordinate. */
    final case class StationWithCoord(name: String, coordinate: Coordinate)

    extension (station: StationDSL)
      /** Create a station with a coordinate. */
      infix def at(coord: (Int, Int)): StationWithCoord = StationWithCoord(station.name, Coordinate(coord._1, coord._2))

    extension (station: StationWithCoord)
      infix def platforms(capacity: Int): Station = Station(station.name, station.coordinate, capacity)

    /** Create a station with a name and a coordinate. */
    implicit class StationOps(start: CreateStation.type):
      @targetName("To set name")
      infix def ->(name: String): StationDSL = StationDSL(name)

  /** Dsl for creating a train. */
  object CreateTrain:

    /** Create a train with a name. */
    final case class TrainDSL(name: String)

    /** Create a train with a name and a technology. */
    final case class TrainWithTechnology(name: String, technology: TrainTechnology)

    /** Create a train with a name, a technology, and a wagon. */
    final case class TrainWithWagon(name: String, technology: TrainTechnology, wagon: Wagons.UseType)

    /** Create a train with a name, a technology, a wagon, and a capacity. */
    final case class TrainWithCapacity(name: String, technology: TrainTechnology, wagon: Wagon)

    extension (train: TrainDSL)
      /** Create a train with a technology. */
      infix def technology(technology: TrainTechnology): TrainWithTechnology =
        TrainWithTechnology(train.name, technology)

    extension (train: TrainWithTechnology)
      /** Create a train with a wagon. */
      infix def wagon(wagon: Wagons.UseType): TrainWithWagon = TrainWithWagon(train.name, train.technology, wagon)

    extension (train: TrainWithWagon)
      /** Create a train with a capacity. */
      infix def capacity(number: Int): TrainWithCapacity =
        TrainWithCapacity(train.name, train.technology, Wagon(train.wagon, number))

    extension (train: TrainWithCapacity)
      /** Create a train with a number. */
      infix def count(number: Int): Train = Train(train.name, train.technology, train.wagon, number)

    /** Create a train with a name, a technology, a wagon, and a number. */
    implicit class TrainOps(start: CreateTrain.type):
      /** Create a train with a name. */
      @targetName("To set name")
      infix def ->(name: String): TrainDSL = TrainDSL(name)

  /** Dsl for creating a route. */
  object CreateRoute:
    /** Create a route with a departure station. */
    final case class RouteDSL(departure: Station)

    /** Create a route with a departure and an arrival station. */
    final case class RouteWithArrival(departure: Station, arrival: Station)

    /** Create a route with a departure, an arrival station, and a route type. */
    final case class RouteWithType(departure: Station, arrival: Station, routeType: Routes.RouteType)

    /** Create a route with a departure, an arrival station, a route type, and a platform. */
    final case class RouteWithPlatform(departure: Station, arrival: Station, routeType: Routes.RouteType, platform: Int)

    extension (departure: RouteDSL)
      /** Create a route with an arrival station. */
      @targetName("To add arrival")
      infix def ->(arrival: Station): RouteWithArrival = RouteWithArrival(departure.departure, arrival)

    extension (route: RouteWithArrival)
      /** Create a route with a route type. */
      infix def on(routeType: Routes.RouteType): RouteWithType =
        RouteWithType(route.departure, route.arrival, routeType)

    extension (route: RouteWithType)
      /** Create a route with a platform. */
      infix def tracks(platform: Int): RouteWithPlatform =
        RouteWithPlatform(route.departure, route.arrival, route.routeType, platform)

    extension (route: RouteWithPlatform)
      /** Create a route with a length. */
      infix def length(length: Double): Either[Routes.RouteError, Route] =
        Route(route.departure, route.arrival, route.routeType, route.platform, length)

    /** Create a route with a departure, an arrival station, a route type, a platform, and a length. */
    implicit class RouteOps(start: CreateRoute.type):
      @targetName("To add departure")
      infix def ->(departure: Station): RouteDSL = RouteDSL(departure)

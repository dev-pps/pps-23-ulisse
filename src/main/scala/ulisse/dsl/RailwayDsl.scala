package ulisse.dsl

import ulisse.entities.Coordinate
import ulisse.entities.route.Routes
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons

/** DSL for creating railway entities. */
object RailwayDsl:

  export CreateStation._, CreateTrain._, CreateRoute._

  /** Create a station. */
  object CreateStation:

    /** Create a station with a name. */
    case class StationBuilder(name: String):
      def at(coord: (Int, Int)): StationWithCoord = StationWithCoord(name, Coordinate(coord._1, coord._2))

    /** Create a station with a name and a coordinate. */
    case class StationWithCoord(name: String, coordinate: Coordinate):
      def platforms(capacity: Int): Station = Station(name, coordinate, capacity)

    /** Create a station with a name and a coordinate. */
    implicit class StationOps(start: CreateStation.type):
      def ->(name: String): StationBuilder = StationBuilder(name)

  /** Create a train. */
  object CreateTrain:

    /** Create a train with a name. */
    case class TrainBuilder(name: String):
      def technology(technology: TrainTechnology): TrainWithTechnology = TrainWithTechnology(name, technology)

    /** Create a train with a name and a technology. */
    case class TrainWithTechnology(name: String, technology: TrainTechnology):
      def wagon(wagon: Wagons.UseType): TrainWithWagon = TrainWithWagon(name, technology, wagon)

    /** Create a train with a name, a technology, and a wagon. */
    case class TrainWithWagon(name: String, technology: TrainTechnology, wagon: Wagons.UseType):
      def numbers(number: Int): Train = Train(name, technology, Wagons.Wagon(wagon, number), number)

    /** Create a train with a name, a technology, a wagon, and a number. */
    implicit class TrainOps(start: CreateTrain.type):
      def ->(name: String): TrainBuilder = TrainBuilder(name)

  /** Create a route. */
  object CreateRoute:

    /** Create a route with a departure station. */
    case class RouteBuilder(departure: Station):
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
      def ->(departure: Station): RouteBuilder = RouteBuilder(departure)

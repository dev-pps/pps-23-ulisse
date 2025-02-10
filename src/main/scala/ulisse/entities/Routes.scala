package ulisse.entities

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route.TypeRoute
import ulisse.entities.station.Station

import scala.annotation.targetName

object Routes:
  opaque type IdRoute = Int

  trait Route[N: Numeric, C <: Coordinate[N]]:
    val id: IdRoute
    val departure: Station[N, C]
    val arrival: Station[N, C]
    val typology: TypeRoute
    val technology: Technology
    val railsCount: Int
    val length: Double

    def withDeparture(departure: Station[N, C]): Route[N, C]
    def withArrival(arrival: Station[N, C]): Route[N, C]
    def withTypology(typeRoute: TypeRoute): Route[N, C]
    def withRailsCount(railsCount: Int): Route[N, C]
    def withLength(length: Double): Route[N, C]

    @targetName("Equals")
    def ===(other: Route[N, C]): Boolean

  object Route:
    def apply[N: Numeric, C <: Coordinate[N]](
        departure: Station[N, C],
        arrival: Station[N, C],
        typeRoute: TypeRoute,
        railsCount: Int,
        length: Double
    ): Route[N, C] =
      RouteImpl(departure, arrival, typeRoute, railsCount, length)

    enum TypeRoute(val technology: Technology):
      case Normal extends TypeRoute(Technology("Normal", 100))
      case AV     extends TypeRoute(Technology("AV", 300))

    private case class RouteImpl[N: Numeric, C <: Coordinate[N]](
        departure: Station[N, C],
        arrival: Station[N, C],
        typology: TypeRoute,
        railsCount: Int,
        length: Double
    ) extends Route[N, C]:
      export typology._
      override val id: IdRoute = hashCode()

      override def withDeparture(departure: Station[N, C]): Route[N, C] = copy(departure = departure)
      override def withArrival(arrival: Station[N, C]): Route[N, C]     = copy(arrival = arrival)
      override def withTypology(typeRoute: TypeRoute): Route[N, C]      = copy(typology = typeRoute)
      override def withRailsCount(railsCount: Int): Route[N, C]         = copy(railsCount = railsCount)
      override def withLength(length: Double): Route[N, C]              = copy(length = length)

      @targetName("Equals")
      override def ===(other: Route[N, C]): Boolean =
        departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology

      override def equals(obj: Any): Boolean =
        obj match
          case other: Route[N, C] => this === other
          case _                  => false

      override def hashCode(): Int = departure.hashCode() + arrival.hashCode() + typology.hashCode()

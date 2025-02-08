package ulisse.entities

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route.TypeRoute
import ulisse.entities.station.Station

import scala.annotation.targetName

object Routes:
  opaque type IdRoute = Int

  trait Route:
    val id: IdRoute
    val departure: Station[Double, Coordinate[Double]]
    val arrival: Station[Double, Coordinate[Double]]
    val typology: TypeRoute
    val railsCount: Int
    val length: Double

    def withTechnology(typeRoute: TypeRoute): Route
    def withLength(length: Double): Route

    @targetName("Equals")
    def ===(other: Route): Boolean

  object Route:
    def apply(
        departure: Station[Double, Coordinate[Double]],
        arrival: Station[Double, Coordinate[Double]],
        typeRoute: TypeRoute,
        railsCount: Int,
        length: Double
    ): Route =
      RouteImpl(departure, arrival, typeRoute, railsCount, length)

    enum TypeRoute(val technology: Technology):
      case Normal extends TypeRoute(Technology("Normal", 100))
      case AV     extends TypeRoute(Technology("AV", 300))

    private case class RouteImpl(
        departure: Station[Double, Coordinate[Double]],
        arrival: Station[Double, Coordinate[Double]],
        typology: TypeRoute,
        railsCount: Int,
        length: Double
    ) extends Route:
      override val id: IdRoute                                 = hashCode()
      override def withTechnology(typeRoute: TypeRoute): Route = copy(typology = typeRoute)
      override def withLength(length: Double): Route           = copy(length = length)

      @targetName("Equals")
      override def ===(other: Route): Boolean =
        departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology

      override def equals(obj: Any): Boolean = obj match
        case that: Route => this === that
        case _           => false

      override def hashCode(): Int = departure.hashCode() + arrival.hashCode() + typology.hashCode()

package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.Technology
import ulisse.entities.station.Station
import ulisse.utils.Errors.ErrorMessage
import ulisse.utils.ValidationUtils.*

import scala.annotation.targetName
import scala.math.*

/** Define routes between stations, and the errors that can be generated during route creation */
object Routes:
  /** Type of route identifier */
  opaque type IdRoute = Int

  /** Errors that can be generated during route creation */
  type RouteError = NonEmptyChain[Errors]

  /** Errors that can be generated during route creation */
  enum Errors(val text: String) extends ErrorMessage(text):
    case FewRails     extends Errors("Rails count must be greater than 0")
    case TooManyRails extends Errors("Rails count must be less")
    case TooShort     extends Errors("Route length must be greater than 0")

  /** Type of route */
  enum RouteType(val technology: Technology):
    case Normal extends RouteType(Technology("Normal", 100))
    case AV     extends RouteType(Technology("AV", 300))

  /** Represent a route between two stations */
  trait Route:
    /** Unique identifier of route */
    val id: IdRoute

    /** Departure station */
    val departure: Station

    /** Arrival station */
    val arrival: Station

    /** Type of route */
    val typology: RouteType

    /** Number of rails */
    val railsCount: Int

    /** Length of route */
    val length: Double

    /** Update departure station */
    def withDeparture(departure: Station): Either[RouteError, Route]

    /** Update arrival station */
    def withArrival(arrival: Station): Either[RouteError, Route]

    /** Update type of route */
    def withTypology(typeRoute: RouteType): Route

    /** Update number of rails */
    def withRailsCount(railsCount: Int): Either[RouteError, Route]

    /** Update length of route */
    def withLength(length: Double): Either[RouteError, Route]

    /** Check if two routes are equals */
    @targetName("Equals")
    def ===(other: Route): Boolean

    /** Check if two routes are equals */
    def checkAllField(other: Route): Boolean =
      departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology &&
        railsCount == other.railsCount && length == other.length

  /** Companion object of [[Route]] */
  object Route:
    private def validateRailsCount(railsCount: Int, departure: Station, arrival: Station): Either[RouteError, Int] = {
      val minValue = 0
      railsCount.validateChain(
        (_ > minValue, Errors.FewRails),
        (_ <= min(departure.numberOfTracks, arrival.numberOfTracks), Errors.TooManyRails)
      )
    }

    private def validateLength(length: Double, departure: Station, arrival: Station): Either[RouteError, Double] =
      length.validateChain((_ >= departure.coordinate.distance(arrival.coordinate), Errors.TooShort))

    private def validateRoute(
        departure: Station,
        arrival: Station,
        typeRoute: RouteType,
        railsCount: Int,
        length: Double
    ): Either[RouteError, Route] =
      (
        validateRailsCount(railsCount, departure, arrival),
        validateLength(length, departure, arrival)
      ).mapN(RouteImpl(departure, arrival, typeRoute, _, _))

    /** Create a new route between two stations */
    def apply(
        departure: Station,
        arrival: Station,
        typeRoute: RouteType,
        railsCount: Int,
        length: Double
    ): Either[RouteError, Route] = validateRoute(departure, arrival, typeRoute, railsCount, length)

    private case class RouteImpl(
        departure: Station,
        arrival: Station,
        typology: RouteType,
        railsCount: Int,
        length: Double
    ) extends Route:
      export typology._
      override val id: IdRoute = hashCode()

      override def withDeparture(departure: Station): Either[RouteError, Route] =
        validateRoute(departure, arrival, typology, railsCount, length)

      override def withArrival(arrival: Station): Either[RouteError, Route] =
        validateRoute(departure, arrival, typology, railsCount, length)

      override def withTypology(typeRoute: RouteType): Route = copy(typology = typeRoute)

      override def withRailsCount(railsCount: Int): Either[RouteError, Route] =
        validateRailsCount(railsCount, departure, arrival).map(_ => copy(railsCount = railsCount))

      override def withLength(length: Double): Either[RouteError, Route] =
        validateLength(length, departure, arrival).map(_ => copy(length = length))

      @targetName("Equals")
      override def ===(other: Route): Boolean =
        departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology

      override def equals(obj: Any): Boolean =
        obj match
          case other: Route => this === other
          case _            => false

      override def hashCode(): Int = departure.hashCode() + arrival.hashCode() + typology.hashCode()

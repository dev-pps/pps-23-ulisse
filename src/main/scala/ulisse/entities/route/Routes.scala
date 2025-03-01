package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.Technology
import ulisse.entities.station.Station
import ulisse.utils.Errors.ErrorMessage
import ulisse.utils.ValidationUtils.*

import scala.annotation.targetName
import scala.math.*

object Routes:
  opaque type IdRoute = Int

  enum Errors(val text: String) extends ErrorMessage(text):
    case FewRails     extends Errors("Rails count must be greater than 0")
    case TooManyRails extends Errors("Rails count must be less")
    case TooShort     extends Errors("Route length must be greater than 0")

  enum TypeRoute(val technology: Technology):
    case Normal extends TypeRoute(Technology("Normal", 100))
    case AV     extends TypeRoute(Technology("AV", 300))

  trait Route:
    val id: IdRoute
    val departure: Station
    val arrival: Station
    val typology: TypeRoute
    val technology: Technology
    val railsCount: Int
    val length: Double

    def withDeparture(departure: Station): Either[NonEmptyChain[Errors], Route]
    def withArrival(arrival: Station): Either[NonEmptyChain[Errors], Route]
    def withTypology(typeRoute: TypeRoute): Route
    def withRailsCount(railsCount: Int): Either[NonEmptyChain[Errors], Route]
    def withLength(length: Double): Either[NonEmptyChain[Errors], Route]

    @targetName("Equals")
    def ===(other: Route): Boolean

    def checkAllField(other: Route): Boolean =
      departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology &&
        technology == other.technology && railsCount == other.railsCount && length == other.length

  object Route:
    private def validateRailsCount(
        railsCount: Int,
        departure: Station,
        arrival: Station
    ): Either[NonEmptyChain[Errors], Int] =
      railsCount.validateChain(
        (_ > 0, Errors.FewRails),
        (_ <= min(departure.numberOfTracks, arrival.numberOfTracks), Errors.TooManyRails)
      )

    private def validateLength(
        length: Double,
        departure: Station,
        arrival: Station
    ): Either[NonEmptyChain[Errors], Double] =
      length.validateChain((_ >= departure.coordinate.distance(arrival.coordinate), Errors.TooShort))

    private def validateRoute(
        departure: Station,
        arrival: Station,
        typeRoute: TypeRoute,
        railsCount: Int,
        length: Double
    ): Either[NonEmptyChain[Errors], Route] =
      (
        validateRailsCount(railsCount, departure, arrival),
        validateLength(length, departure, arrival)
      ).mapN(RouteImpl(departure, arrival, typeRoute, _, _))

    def apply(
        departure: Station,
        arrival: Station,
        typeRoute: TypeRoute,
        railsCount: Int,
        length: Double
    ): Either[NonEmptyChain[Errors], Route] =
      validateRoute(departure, arrival, typeRoute, railsCount, length)

    private case class RouteImpl(
        departure: Station,
        arrival: Station,
        typology: TypeRoute,
        railsCount: Int,
        length: Double
    ) extends Route:
      export typology._
      override val id: IdRoute = hashCode()

      override def withDeparture(departure: Station): Either[NonEmptyChain[Errors], Route] =
        validateRoute(departure, arrival, typology, railsCount, length)
      override def withArrival(arrival: Station): Either[NonEmptyChain[Errors], Route] =
        validateRoute(departure, arrival, typology, railsCount, length)

      override def withTypology(typeRoute: TypeRoute): Route = copy(typology = typeRoute)

      override def withRailsCount(railsCount: Int): Either[NonEmptyChain[Errors], Route] =
        validateRailsCount(railsCount, departure, arrival).map(_ => copy(railsCount = railsCount))
      override def withLength(length: Double): Either[NonEmptyChain[Errors], Route] =
        validateLength(length, departure, arrival).map(_ => copy(length = length))

      @targetName("Equals")
      override def ===(other: Route): Boolean =
        departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology

      override def equals(obj: Any): Boolean =
        obj match
          case other: Route => this === other
          case _            => false

      override def hashCode(): Int = departure.hashCode() + arrival.hashCode() + typology.hashCode()

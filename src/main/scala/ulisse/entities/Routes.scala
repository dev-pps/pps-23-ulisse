package ulisse.entities

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.utils.Errors.ErrorMessage

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

  private def validateRailsCount(
      railsCount: Int,
      departure: Station[_, _],
      arrival: Station[_, _]
  ): Either[NonEmptyChain[Errors], Int] =
    List(
      Either.cond(railsCount > 0, railsCount, Errors.FewRails),
      Either.cond(
        railsCount <= min(departure.numberOfTracks, arrival.numberOfTracks),
        railsCount,
        Errors.TooManyRails
      )
    ).traverse(_.toValidatedNec).map(_ => railsCount).toEither

  private def validateLength[A: Numeric, B <: Coordinate[A]](
      length: Double,
      departure: Station[A, B],
      arrival: Station[A, B]
  ): Either[NonEmptyChain[Errors], Double] =
    Either.cond(
      length >= departure.coordinate.distance(arrival.coordinate),
      length,
      Errors.TooShort
    ).toValidatedNec.toEither

  trait Route[N: Numeric, C <: Coordinate[N]]:
    val id: IdRoute
    val departure: Station[N, C]
    val arrival: Station[N, C]
    val typology: TypeRoute
    val technology: Technology
    val railsCount: Int
    val length: Double

    def withDeparture(departure: Station[N, C]): Either[NonEmptyChain[Errors], Route[N, C]]
    def withArrival(arrival: Station[N, C]): Either[NonEmptyChain[Errors], Route[N, C]]
    def withTypology(typeRoute: TypeRoute): Route[N, C]
    def withRailsCount(railsCount: Int): Either[NonEmptyChain[Errors], Route[N, C]]
    def withLength(length: Double): Either[NonEmptyChain[Errors], Route[N, C]]

    @targetName("Equals")
    def ===(other: Route[N, C]): Boolean

    def checkAllField(other: Route[N, C]): Boolean =
      departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology &&
        technology == other.technology && railsCount == other.railsCount && length == other.length

  object Route:
    private def validateRoute[N: Numeric, C <: Coordinate[N]](
        departure: Station[N, C],
        arrival: Station[N, C],
        typeRoute: TypeRoute,
        railsCount: Int,
        length: Double
    ): Either[NonEmptyChain[Errors], Route[N, C]] =
      (
        validateRailsCount(railsCount, departure, arrival),
        validateLength(length, departure, arrival)
      ).mapN(RouteImpl(departure, arrival, typeRoute, _, _))

    def apply[N: Numeric, C <: Coordinate[N]](
        departure: Station[N, C],
        arrival: Station[N, C],
        typeRoute: TypeRoute,
        railsCount: Int,
        length: Double
    ): Either[NonEmptyChain[Errors], Route[N, C]] =
      validateRoute(departure, arrival, typeRoute, railsCount, length)

    private case class RouteImpl[N: Numeric, C <: Coordinate[N]](
        departure: Station[N, C],
        arrival: Station[N, C],
        typology: TypeRoute,
        railsCount: Int,
        length: Double
    ) extends Route[N, C]:
      export typology._
      override val id: IdRoute = hashCode()

      override def withDeparture(departure: Station[N, C]): Either[NonEmptyChain[Errors], Route[N, C]] =
        validateRoute(departure, arrival, typology, railsCount, length)
      override def withArrival(arrival: Station[N, C]): Either[NonEmptyChain[Errors], Route[N, C]] =
        validateRoute(departure, arrival, typology, railsCount, length)

      override def withTypology(typeRoute: TypeRoute): Route[N, C] = copy(typology = typeRoute)

      override def withRailsCount(railsCount: Int): Either[NonEmptyChain[Errors], Route[N, C]] =
        validateRailsCount(railsCount, departure, arrival).map(_ => copy(railsCount = railsCount))
      override def withLength(length: Double): Either[NonEmptyChain[Errors], Route[N, C]] =
        validateLength(length, departure, arrival).map(_ => copy(length = length))

      @targetName("Equals")
      override def ===(other: Route[N, C]): Boolean =
        departure.equals(other.departure) && arrival.equals(other.arrival) && typology == other.typology

      override def equals(obj: Any): Boolean =
        obj match
          case other: Route[N, C] => this === other
          case _                  => false

      override def hashCode(): Int = departure.hashCode() + arrival.hashCode() + typology.hashCode()

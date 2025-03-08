package ulisse.entities.station

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.Coordinate
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonBlankString, validatePositive}

/** Defines a `Station`, a place where trains can stop. */
trait Station:
  /** The id of the station. station are unique based on their location */
  def id: Int = hashCode()

  /** The name of the station. */
  def name: String

  /** The location of the station. */
  def coordinate: Coordinate

  /** Change the coordinate of the Station. */
  def withCoordinate(newCoordinate: Coordinate): Station

  /** The number of platforms in the station. */
  def numberOfPlatforms: Int

  /** Defines equality for Station */
  override def equals(that: Any): Boolean = that match
    case s: Station =>
      id === s.id &&
      name === s.name &&
      coordinate === s.coordinate &&
      numberOfPlatforms === s.numberOfPlatforms
    case _ => false

  /** Defines hashCode for Station */
  override def hashCode(): Int = coordinate.##

/** Factory for [[Station]] instances. */
object Station:
  /** Default number of platforms for a station. */
  val minNumberOfPlatforms: Int = 1

  /** Creates a `Station` instance. If the provided numberOfPlatforms are lower than the minNumberOfPlatforms the min value will be set */
  def apply(name: String, coordinate: Coordinate, numberOfPlatforms: Int): Station =
    StationImpl(name, coordinate, math.max(minNumberOfPlatforms, numberOfPlatforms))

  /** Creates a `Station` instance with validation. The resulting station must have non-blank name and numberOfPlatforms higher or equal than minNumberOfPlatforms */
  def createCheckedStation(
      name: String,
      coordinate: Coordinate,
      numberOfPlatforms: Int
  ): Either[NonEmptyChain[Station.Error], Station] =
    (
      validateNonBlankString(name, Station.Error.InvalidName).toValidatedNec,
      validatePositive(numberOfPlatforms, Station.Error.InvalidNumberOfPlatforms).toValidatedNec
    ).mapN(Station(_, coordinate, _)).toEither

  private final case class StationImpl(name: String, coordinate: Coordinate, numberOfPlatforms: Int) extends Station:
    override def withCoordinate(newCoordinate: Coordinate): Station = copy(coordinate = newCoordinate)

  /** Represents errors that can occur during `Station` creation. */
  enum Error extends BaseError:
    case InvalidName, InvalidNumberOfPlatforms

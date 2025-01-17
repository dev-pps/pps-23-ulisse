package ulisse.entities

import scala.math.{pow, sqrt}

trait Coordinate[T](private val x: T, private val y: T):

  def distance(coordinate: Coordinate[T])(using numeric: Numeric[T]): Double =
    sqrt(pow(numeric.toDouble(coordinate.x) - numeric.toDouble(x), 2)
      + pow(numeric.toDouble(coordinate.y) - numeric.toDouble(y), 2))

object Coordinate:
  /** Represents errors that can occur during station creation. */
  enum Error:
    case InvalidLatitude, InvalidLongitude, InvalidRow, InvalidColumn

  def apply[T](x: T, y: T): Coordinate[T] = Gen(x, y)

  def createGeo(latitude: Double, longitude: Double): Geo =
    Geo(latitude, longitude)

  /** Creates a `Geo` instance with validation.
    *
    * @param latitude
    *   The latitude of the location. Must be between -90 and 90.
    * @param longitude
    *   The longitude of the location. Must be between -180 and 180.
    * @return
    *   Either a `Geo` instance or an `Error` indicating the issue.
    */
  def createGeoWithValidation(
      latitude: Double,
      longitude: Double
  ): Either[Error, Geo] =
    for
      validLat <- validateRange(latitude, -90.0, 90.0, Error.InvalidLatitude)
      validLon <-
        validateRange(longitude, -180.0, 180.0, Error.InvalidLongitude)
    yield createGeo(validLat, validLon)

  private case class Gen[T] private[Coordinate] (x: T, y: T)
      extends Coordinate[T](x, y)

  /** Represents a location in the form of latitude and longitude. */
  final case class Geo private[Coordinate] (latitude: Double, longitude: Double)
      extends Coordinate[Double](latitude, longitude)

  private def validateRange(
      value: Double,
      min: Double,
      max: Double,
      error: Error
  ): Either[Error, Double] =
    Either.cond(value >= min && value <= max, value, error)

  private def validateNonNegative(
      value: Int,
      error: Error
  ): Either[Error, Int] =
    Either.cond(value >= 0, value, error)

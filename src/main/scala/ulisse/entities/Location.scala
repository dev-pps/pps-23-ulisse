package ulisse.entities

import cats.Eq
import cats.implicits.catsSyntaxEq

import scala.annotation.targetName

sealed trait Location:
  @targetName("equals")
  def ===(that: Location): Boolean =
    Eq.fromUniversalEquals[Location].eqv(this, that)

/** Factory for [[Location]] instances. */
object Location:

  /** Represents a location in the form of latitude and longitude. */
  final case class Geo private[Location] (latitude: Double, longitude: Double)
      extends Location

  /** Represents a location in the form of a grid. */
  final case class Grid private[Location] (row: Int, column: Int)
      extends Location

  sealed trait Error

  /** Represents errors that can occur during station creation. */
  object Geo:
    enum Error extends Location.Error:
      case InvalidLatitude, InvalidLongitude

  object Grid:
    enum Error extends Location.Error:
      case InvalidRow, InvalidColumn

  private def validateRange[E <: Error](
      value: Double,
      min: Double,
      max: Double,
      error: E
  ): Either[E, Double] =
    Either.cond(value >= min && value <= max, value, error)

  private def validateNonNegative[E <: Error](
      value: Int,
      error: E
  ): Either[E, Int] =
    Either.cond(value >= 0, value, error)

  /** Creates a `Geo` instance with validation.
    *
    * @param latitude
    *   The latitude of the location. Must be between -90 and 90.
    * @param longitude
    *   The longitude of the location. Must be between -180 and 180.
    * @return
    *   Either a `Geo` instance or an `Error` indicating the issue.
    */
  def createGeo(latitude: Double, longitude: Double): Either[Geo.Error, Geo] =
    for
      validLat <- validateRange(latitude, -90.0, 90.0, Geo.Error.InvalidLatitude)
      validLon <-
        validateRange(longitude, -180.0, 180.0, Geo.Error.InvalidLongitude)
    yield Geo(validLat, validLon)

  /** Creates a `Grid` instance with validation.
    *
    * @param row
    *   The row of the grid. Must be non-negative.
    * @param column
    *   The column of the grid. Must be non-negative.
    * @return
    */
  def createGrid(row: Int, column: Int): Either[Grid.Error, Grid] =
    for
      validRow <- validateNonNegative(row, Grid.Error.InvalidRow)
      validCol <- validateNonNegative(column, Grid.Error.InvalidColumn)
    yield Grid(validRow, validCol)

package ulisse.entities

import scala.math.{pow, sqrt}
import ulisse.utils.Errors.AppError
import ulisse.utils.ValidationUtils.{validateNonNegative, validateRange}

import scala.annotation.targetName

object Coordinates:

  /** A generic trait representing a 2D coordinate point in space.
    *
    * @tparam T
    *   A numeric type that represents the coordinate values (e.g., Int, Double, etc.). Must have a `Numeric` type class
    *   instance available.
    * @constructor
    *   Creates a new coordinate with the specified x and y values.
    * @param x
    *   The x-coordinate value.
    * @param y
    *   The y-coordinate value.
    */
  trait Coordinate[T: Numeric](private val x: T, private val y: T):
    @targetName("equals")
    def ===(that: Coordinate[T])(using numeric: Numeric[T]): Boolean =
      numeric.equiv(x, that.x) && numeric.equiv(y, that.y)
    def distance(coordinate: Coordinate[T])(using numeric: Numeric[T]): Double =
      sqrt(pow(numeric.toDouble(coordinate.x) - numeric.toDouble(x), 2)
        + pow(numeric.toDouble(coordinate.y) - numeric.toDouble(y), 2))

  /** Factory for [Coordinate] instances. */
  object Coordinate:

    def apply[T: Numeric](x: T, y: T): Coordinate[T] = CoordinateImpl(x, y)

    /** Creates a `Geo` instance with validation.
      *
      * @param latitude
      *   The latitude of the location. Must be between -90 and 90.
      * @param longitude
      *   The longitude of the location. Must be between -180 and 180.
      * @return
      *   Either a `Geo` instance or an `Error` indicating the issue.
      */
    def createGeo(latitude: Double, longitude: Double): Either[AppError, Geo] =
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
      *   Either a `Grid` instance or an `Error` indicating the issue.
      */
    def createGrid(row: Int, column: Int): Either[AppError, Grid] =
      for
        validRow <- validateNonNegative(row, Grid.Error.InvalidRow)
        validCol <- validateNonNegative(column, Grid.Error.InvalidColumn)
      yield Grid(validRow, validCol)

    private final case class CoordinateImpl[T: Numeric](x: T, y: T) extends Coordinate[T](x, y)

  object Geo:
    /** Represents errors that can occur during [Geo] creation. */
    enum Error extends AppError:
      case InvalidLatitude, InvalidLongitude

  /** A 2D geographic coordinate point. */
  final case class Geo private[Coordinates] (latitude: Double, longitude: Double)
      extends Coordinate(latitude, longitude)

  object Grid:
    /** Represents errors that can occur during [Grid] creation. */
    enum Error extends AppError:
      case InvalidRow, InvalidColumn

  /** A 2D grid coordinate point. */
  final case class Grid private[Coordinates] (row: Int, column: Int)
      extends Coordinate(row, column)

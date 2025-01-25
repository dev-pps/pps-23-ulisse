package ulisse.entities

import scala.math.{pow, sqrt}
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonNegative, validateRange}

import scala.annotation.targetName

object Coordinates:
  /** A generic trait representing a 2D coordinate point in space.
    *
    * @tparam T
    *   A numeric type that represents the coordinate values (e.g., `Int`, `Double`, etc.). Must have a `Numeric` type
    *   class instance available.
    * @constructor
    *   Creates a new coordinate with the specified x and y values.
    * @param x
    *   The x-coordinate value.
    * @param y
    *   The y-coordinate value.
    */
  trait Coordinate[T: Numeric](private val x: T, private val y: T):
    /** Checks if this coordinate is equal to another coordinate.
      *
      * @param that
      *   The coordinate to compare with.
      * @return
      *   `true` if both coordinates have the same x and y values, `false` otherwise.
      */
    @targetName("equals")
    def ===(that: Coordinate[T])(using numeric: Numeric[T]): Boolean =
      numeric.equiv(x, that.x) && numeric.equiv(y, that.y)

    /** Checks for equality with another object.
      *
      * @param obj
      *   The object to compare with.
      * @return
      *   `true` if the object is a coordinate with the same x and y values, `false` otherwise.
      */
    override def equals(obj: Any): Boolean =
      obj match
        case that: Coordinate[?] => this.x == that.x && this.y == that.y
        case _                   => false

    /** Calculates the Euclidean distance to another coordinate.
      *
      * @param coordinate
      *   The other coordinate.
      * @return
      *   The Euclidean distance as a `Double`.
      */
    def distance(coordinate: Coordinate[T])(using numeric: Numeric[T]): Double =
      sqrt(
        pow(numeric.toDouble(coordinate.x) - numeric.toDouble(x), 2) +
          pow(numeric.toDouble(coordinate.y) - numeric.toDouble(y), 2)
      )

  /** Factory for [[Coordinate]] instances. */
  object Coordinate:
    /** Creates a new `Coordinate` instance.
      *
      * @tparam T
      *   A numeric type for the coordinate values.
      * @param x
      *   The x-coordinate value.
      * @param y
      *   The y-coordinate value.
      * @return
      *   A new `Coordinate` instance.
      */
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
    def createGeo(latitude: Double, longitude: Double): Either[Geo.Error, Geo] =
      for
        validLat <- validateRange(latitude, -90.0, 90.0, Geo.Error.InvalidLatitude)
        validLon <- validateRange(longitude, -180.0, 180.0, Geo.Error.InvalidLongitude)
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
    def createGrid(row: Int, column: Int): Either[Grid.Error, Grid] =
      for
        validRow <- validateNonNegative(row, Grid.Error.InvalidRow)
        validCol <- validateNonNegative(column, Grid.Error.InvalidColumn)
      yield Grid(validRow, validCol)

    private final case class CoordinateImpl[T: Numeric](x: T, y: T) extends Coordinate[T](x, y)

  object Geo:
    /** Represents errors that can occur during [[Geo]] creation. */
    enum Error extends BaseError:
      case InvalidLatitude, InvalidLongitude

  /** A 2D geographic coordinate point.
    *
    * Represents a location defined by latitude and longitude values.
    *
    * @param latitude
    *   The latitude value, which must be between -90 and 90.
    * @param longitude
    *   The longitude value, which must be between -180 and 180.
    *
    * **Note**: Instances of `Geo` can only be created through the `Coordinates.createGeo` method to ensure validation.
    */
  final case class Geo private[Coordinates] (latitude: Double, longitude: Double)
      extends Coordinate[Double](latitude, longitude)

  object Grid:
    /** Represents errors that can occur during [[Grid]] creation. */
    enum Error extends BaseError:
      case InvalidRow, InvalidColumn

  /** A 2D grid coordinate point.
    *
    * Represents a grid location defined by row and column values.
    *
    * @param row
    *   The row value, which must be non-negative.
    * @param column
    *   The column value, which must be non-negative.
    *
    * **Note**: Instances of `Grid` can only be created through the `Coordinates.createGrid` method to ensure
    * validation.
    */
  final case class Grid private[Coordinates] (row: Int, column: Int)
      extends Coordinate[Int](row, column)

  given ((Int, Int) => Either[BaseError, Coordinate[Int]]) = (x, y) => Right(Coordinate(x, y))
  given ((Int, Int) => Either[BaseError, Grid])            = Coordinate.createGrid
  given ((Double, Double) => Either[BaseError, Geo])       = Coordinate.createGeo

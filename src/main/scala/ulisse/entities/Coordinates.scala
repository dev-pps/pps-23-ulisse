package ulisse.entities

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonNegative, validateRange}

import scala.annotation.targetName
import scala.math.{atan2, pow, sqrt}
import scala.util.Random

object Coordinates:
  given [N: Numeric]: ((N, N) => Either[BaseError, Coordinate[N]]) = (x, y) => Right(Coordinate(x, y))

  given [N: Numeric]: ((N, N) => Either[NonEmptyChain[BaseError], Coordinate[N]]) = (x, y) => Right(Coordinate(x, y))

  given ((Int, Int) => Either[BaseError, Grid]) = Coordinate.createGrid

  given ((Int, Int) => Either[NonEmptyChain[BaseError], Grid]) = Coordinate.createValidatedGrid

  given ((Double, Double) => Either[BaseError, Geo]) = Coordinate.createGeo

  given ((Int, Int) => Either[NonEmptyChain[BaseError], Geo]) = Coordinate.createValidatedGeo

  opaque type GeoRange = (Double, Double)
  val latitudeRange: GeoRange  = (-90.0d, 90.0d)
  val longitudeRange: GeoRange = (-180.0d, 180.0d)

  /** A generic trait representing a 2D coordinate point in space.
    *
    * @tparam T
    *   A numeric type that represents the coordinate values (e.g., `Int`, `Double`). Must have a `Numeric` type class
    *   instance available.
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

    def angle(coordinate: Coordinate[T])(using numeric: Numeric[T]): Double =
      atan2(numeric.toDouble(coordinate.y) - numeric.toDouble(y), numeric.toDouble(coordinate.x) - numeric.toDouble(x))

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

    // TODO: to comment
    def createValidRandomGeo(): Geo =
      Geo(Random.between(latitudeRange._1, latitudeRange._2), Random.between(longitudeRange._1, longitudeRange._2))

    def uiPoint(x: Double, y: Double): UIPoint = UIPoint(x, y)

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
        validLat <- validateRange(latitude, latitudeRange._1, latitudeRange._2, Geo.Error.InvalidLatitude)
        validLon <- validateRange(longitude, longitudeRange._1, longitudeRange._2, Geo.Error.InvalidLongitude)
      yield Geo(validLat, validLon)

    /** Creates a `Geo` instance with validation.
      *
      * @param latitude
      *   The latitude of the location. Must be between -90 and 90.
      * @param longitude
      *   The longitude of the location. Must be between -180 and 180.
      * @return
      *   Either a `Geo` instance or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def createValidatedGeo(latitude: Double, longitude: Double): Either[NonEmptyChain[Geo.Error], Geo] =
      (
        validateRange(latitude, latitudeRange._1, latitudeRange._2, Geo.Error.InvalidLatitude).toValidatedNec,
        validateRange(longitude, longitudeRange._1, longitudeRange._2, Geo.Error.InvalidLongitude).toValidatedNec
      )
        .mapN(Geo(_, _)).toEither

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

    /** Creates a `Grid` instance with validation.
      *
      * @param row
      *   The row of the grid. Must be non-negative.
      * @param column
      *   The column of the grid. Must be non-negative.
      * @return
      *   Either a `Grid` instance or a `NonEmptyChain` of `Errors` indicating the issues.
      */
    def createValidatedGrid(row: Int, column: Int): Either[NonEmptyChain[Grid.Error], Grid] =
      (
        validateNonNegative(row, Grid.Error.InvalidRow).toValidatedNec,
        validateNonNegative(column, Grid.Error.InvalidColumn).toValidatedNec
      )
        .mapN(Grid(_, _)).toEither

    private final case class CoordinateImpl[T: Numeric](x: T, y: T) extends Coordinate[T](x, y)

  object Geo:
    /** Represents errors that can occur during [[Geo]] creation. */
    enum Error extends BaseError:
      case InvalidLatitude, InvalidLongitude

  object Grid:
    /** Represents errors that can occur during [[Grid]] creation. */
    enum Error extends BaseError:
      case InvalidRow, InvalidColumn
  object UI

  final case class UIPoint private[Coordinates] (x: Double, y: Double) extends Coordinate(x, y)

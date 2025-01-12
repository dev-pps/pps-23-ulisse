package entities

sealed trait Location

object Location:

  final case class Geo private[Location] (latitude: Double, longitude: Double)
      extends Location
  final case class Grid private[Location] (row: Int, column: Int)
      extends Location

  enum Error:
    case InvalidLatitude, InvalidLongitude, InvalidRow, InvalidColumn

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

  def createGeo(latitude: Double, longitude: Double): Either[Error, Geo] =
    for
      validLat <- validateRange(latitude, -90.0, 90.0, Error.InvalidLatitude)
      validLon <-
        validateRange(longitude, -180.0, 180.0, Error.InvalidLongitude)
    yield Geo(validLat, validLon)

  def createGrid(row: Int, column: Int): Either[Error, Grid] =
    for
      validRow <- validateNonNegative(row, Error.InvalidRow)
      validCol <- validateNonNegative(column, Error.InvalidColumn)
    yield Grid(validRow, validCol)

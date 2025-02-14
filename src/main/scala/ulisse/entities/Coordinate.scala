package ulisse.entities

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateNonNegative, validateRange}

import scala.annotation.targetName
import scala.math.{atan2, pow, sqrt}
import scala.util.Random

/** 2D coordinate (x, y). */
trait Coordinate:
  val x: Int
  val y: Int

  /** Compares two coordinates. */
  @targetName("equals")
  def ===(that: Coordinate): Boolean = x == that.x && y == that.y

  /** Compares this coordinate with another for equality. */
  override def equals(obj: Any): Boolean =
    obj match
      case that: Coordinate => this.x == that.x && this.y == that.y
      case _                => false

  /** Euclidean distance to another coordinate. */
  def distance(coordinate: Coordinate): Double =
    sqrt(pow(coordinate.x.toDouble - x.toDouble, 2) + pow(coordinate.y.toDouble - y.toDouble, 2))

  /** Angle in radians relative to the X-axis. */
  def angle(coordinate: Coordinate): Double =
    atan2(coordinate.y.toDouble - y.toDouble, coordinate.x.toDouble - x.toDouble)

/** Companion object for `Coordinate`. Provides methods to create and manipulate coordinates. */
object Coordinate:

  /** Creates a new `Coordinate` with specified x and y values. */
  def apply(x: Int, y: Int): Coordinate = CoordinateImpl(x, y)

  /** Private case class implementation of `Coordinate`. */
  private case class CoordinateImpl(x: Int, y: Int) extends Coordinate

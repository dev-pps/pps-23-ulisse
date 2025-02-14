package ulisse.entities

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.math.{pow, sqrt}

class CoordinateTest extends AnyFlatSpec with Matchers:
  val x          = 0
  val y          = 0
  val coordinate = Coordinate(x, y)

  "create coordinates" should "set x and y" in:
    coordinate.x should be(x)
    coordinate.y should be(y)

  "check equals coordinates" should "be same x and y" in:
    val other = Coordinate(x, y)
    coordinate === other should be(true)

  "check different coordinates" should "have different x or y" in:
    val other = Coordinate(x + 1, y + 1)
    coordinate === other should be(false)

  "check distance between coordinates" should "be the Euclidean distance" in:
    val x1       = x
    val y1       = 2
    val other    = Coordinate(x, y1)
    val distance = sqrt(pow(x1.toDouble - x.toDouble, 2) + pow(y1.toDouble - y.toDouble, 2))
    coordinate.distance(other) should be(distance)

  "check angle between coordinates" should "be the angle in radians relative to the X-axis" in:
    val x1    = x + 2
    val y1    = y
    val other = Coordinate(x1, y1)
    val angle = math.atan2(y1.toDouble - y.toDouble, x1.toDouble - x.toDouble)
    coordinate.angle(other) should be(angle)

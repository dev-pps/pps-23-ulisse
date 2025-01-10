package utils

import scala.math.{pow, sqrt}

object Points:

  private def computeDistance(
      x1: Double,
      y1: Double,
      x2: Double,
      y2: Double
  ): Double =
    sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2))

  def computePointsDistance(
      p1: (Double, Double),
      p2: (Double, Double)
  ): Double =
    computeDistance(p1._1, p1._2, p2._1, p2._2)

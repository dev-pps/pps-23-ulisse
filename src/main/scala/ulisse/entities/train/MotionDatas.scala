package ulisse.entities.train

object MotionDatas:
  case class MotionData(distanceTravelled: Double, speed: Double, acceleration: Double):
    def withAcceleration(acc: Double): MotionData =
      copy(acceleration = acc)
    def withSpeed(v: Double): MotionData =
      copy(speed = if speed + v >= 0 then speed + v else 0)
    def withDistanceOffset(v: Double): MotionData =
      copy(distanceTravelled = if distanceTravelled + v >= 0 then distanceTravelled + v else 0)

  extension (md: MotionData)
    def updated(dt: Int): MotionData =
      val secondToHours: Double = 3600
      val speed                 = md.speed + md.acceleration * dt
      val dtInHour: Double      = dt / secondToHours
      val newDistanceTravelled  = md.distanceTravelled + speed * dtInHour + 0.5 * md.acceleration * Math.pow(dt, 2)
      md.copy(distanceTravelled = newDistanceTravelled, speed = speed)

  def emptyMotionData: MotionData = MotionData(0.0, 0.0, 0.0)

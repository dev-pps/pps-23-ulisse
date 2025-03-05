package ulisse.entities.train

object MotionDatas:
  case class MotionData(distanceTravelled: Double, speed: Double, acceleration: Double, elapsedSeconds: Int):
    def withAcceleration(acc: Double): MotionData =
      copy(acceleration = acc)
    def withSpeed(v: Double): MotionData =
      copy(speed = if speed + v >= 0 then speed + v else 0)
    def withDistanceOffset(v: Double): MotionData =
      copy(distanceTravelled = if distanceTravelled + v >= 0 then distanceTravelled + v else 0)

  extension (motionData: MotionData)
    def updated(dt: Int): MotionData =
      val elapsedSeconds: Int   = motionData.elapsedSeconds + dt
      val secondToHours: Double = 3600
      val speed                 = motionData.speed + motionData.acceleration * dt
      val dtInHour: Double      = elapsedSeconds / secondToHours
      val newDistanceTravelled  = speed * dtInHour + 0.5 * motionData.acceleration * Math.pow(dt, 2)
      motionData.copy(
        distanceTravelled = newDistanceTravelled,
        speed = speed,
        elapsedSeconds = elapsedSeconds
      )

  def emptyMotionData: MotionData = MotionData(0.0, 0.0, 0.0, 0)

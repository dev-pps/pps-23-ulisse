package ulisse.entities.train

/** Object tha groups entity MotionData and utility methods. */
object MotionDatas:

  /** Entity that represent motion state of train and distance travelled. */
  case class MotionData(distanceTravelled: Double, speed: Double, acceleration: Double):
    /** Returns updates MotionDate with updated `acc` acceleration. */
    def withAcceleration(acc: Double): MotionData =
      copy(acceleration = acc)

    /** Returns updates MotionDate with updated `newSpeed` speed. */
    def withSpeed(newSpeed: Double): MotionData =
      copy(speed = if speed + newSpeed >= 0 then speed + newSpeed else 0)

    /** Returns updates MotionDate with distance travelled plus some `offset`. */
    def withDistanceOffset(offset: Double): MotionData =
      copy(distanceTravelled = if distanceTravelled + offset >= 0 then distanceTravelled + offset else 0)

  extension (md: MotionData)
    /** Extension method that returns MotionData with updated travelled distance given `dt`. */
    def updated(dt: Int): MotionData =
      val secondToHours: Double = 3600
      val speed                 = md.speed + md.acceleration * dt
      val dtInHour: Double      = dt / secondToHours
      val newDistanceTravelled  = md.distanceTravelled + speed * dtInHour + 0.5 * md.acceleration * Math.pow(dt, 2)
      md.copy(distanceTravelled = newDistanceTravelled, speed = speed)

  /** Return MotionData with all values to zero. */
  def emptyMotionData: MotionData = MotionData(0.0, 0.0, 0.0)

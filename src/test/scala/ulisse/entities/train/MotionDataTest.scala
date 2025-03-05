package ulisse.entities.train

import org.scalatest.flatspec.AnyFlatSpec
import ulisse.Utils.MatchersUtils.shouldBe
import ulisse.entities.train.MotionDatas.MotionData

class MotionDataTest extends AnyFlatSpec:

  "MotionData" should "keep elapsed time after an update" in:
    val motionData =
      MotionData(distanceTravelled = 0.0, speed = 0.0, acceleration = 0.0, elapsedSeconds = 0)
    motionData.updated(1).updated(2).elapsedSeconds shouldBe 3
  it should "have empty constructor" in:
    MotionDatas.emptyMotionData shouldBe MotionData(
      distanceTravelled = 0.0,
      speed = 0.0,
      acceleration = 0.0,
      elapsedSeconds = 0
    )

  it should "update speed, acceleration and elapsed seconds" in:
    val fakeValue = 20
    val expectedMotionD = MotionData(
      distanceTravelled = fakeValue,
      speed = fakeValue,
      acceleration = fakeValue,
      elapsedSeconds = 0
    )
    MotionDatas.emptyMotionData
      .withDistanceOffset(fakeValue)
      .withSpeed(fakeValue)
      .withAcceleration(fakeValue) shouldBe expectedMotionD

  it should "update acceleration" in:
    MotionDatas.emptyMotionData.withAcceleration(100) shouldBe MotionData(
      distanceTravelled = 0.0,
      speed = 0.0,
      acceleration = 100.0,
      elapsedSeconds = 0
    )

  it should "update distance travelled using speed (km/h) and elapsed time (s)" in:
    val speedKm_h          = 150
    val dt_secs            = 24
    val dt_hours           = dt_secs / 3600
    val exceptedDistanceKm = 1
    MotionDatas.emptyMotionData
      .withSpeed(speedKm_h)
      .updated(dt_secs).distanceTravelled shouldBe exceptedDistanceKm

  it should "add some offset to distance travelled" in:
    val offset = 20
    MotionDatas.emptyMotionData
      .withDistanceOffset(offset)
      .withDistanceOffset(offset)
      .distanceTravelled shouldBe offset + offset
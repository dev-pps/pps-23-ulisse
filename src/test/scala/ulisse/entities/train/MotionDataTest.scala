package ulisse.entities.train

import org.scalatest.flatspec.AnyFlatSpec
import ulisse.Utils.MatchersUtils.shouldBe
import ulisse.entities.train.MotionDatas.MotionData

class MotionDataTest extends AnyFlatSpec:

  "MotionData" should "have empty constructor" in:
    MotionDatas.emptyMotionData shouldBe MotionData(distanceTravelled = 0.0, speed = 0.0, acceleration = 0.0)

  it should "set speed, acceleration and set an offset to distance" in:
    val fakeValue = 20
    val expectedMotionD = MotionData(distanceTravelled = fakeValue, speed = fakeValue, acceleration = fakeValue)
    MotionDatas.emptyMotionData
      .withDistanceOffset(fakeValue)
      .withSpeed(fakeValue)
      .withAcceleration(fakeValue) shouldBe expectedMotionD

  it should "update acceleration" in:
    MotionDatas.emptyMotionData.withAcceleration(100) shouldBe MotionData(distanceTravelled = 0.0, speed = 0.0, acceleration = 100.0)

  it should "update correctly distance travelled using speed (km/h) and dt (seconds)" in:
    val speedKm_h          = 150
    val dt_secs            = 10
    val dt_secs_2          = 14
    val exceptedDistanceKm = 1
    MotionDatas.emptyMotionData
      .withSpeed(speedKm_h)
      .updated(dt_secs)
      .updated(dt_secs_2).distanceTravelled shouldBe exceptedDistanceKm

  it should "add some offset to distance travelled" in:
    val offset = 20
    MotionDatas.emptyMotionData
      .withDistanceOffset(offset)
      .withDistanceOffset(offset)
      .distanceTravelled shouldBe offset + offset

package ulisse.infrastructures.view.components.images

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.ui.decorators.Images.Rotation

class RotationTest extends AnyFlatSpec with Matchers:
  val zeroAngle    = 0
  val rotationZero = Rotation(zeroAngle)

  "create rotation with 0 angle" should "return a rotation" in:
    rotationZero.angle mustBe zeroAngle
    rotationZero.toRadians mustBe math.toRadians(zeroAngle)

  "change angle" should "return a new rotation" in:
    val rightAngle  = 90
    val newRotation = rotationZero.withAngle(rightAngle)
    newRotation.angle mustBe rightAngle

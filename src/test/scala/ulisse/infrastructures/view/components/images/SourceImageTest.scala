package ulisse.infrastructures.view.components.images

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.styles.Images.SourceImage

object SourceImageTest:
  val rightPath = "icons/route.svg"
  val wrongPath = "icons/test"

  val rightSourceImage = SourceImage(rightPath)
  val wrongSourceImage = SourceImage(wrongPath)

class SourceImageTest extends AnyFlatSpec with Matchers:
  import SourceImageTest.*

  "create source with right path" should "return a source image" in:
    rightSourceImage.path mustBe rightPath
    rightSourceImage.bufferImage must not be None

  "create source with wrong path" should "return a empty image" in:
    wrongSourceImage.path mustBe wrongPath
    wrongSourceImage.bufferImage mustBe None

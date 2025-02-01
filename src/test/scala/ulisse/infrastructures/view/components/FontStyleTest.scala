package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import scala.swing.Font

class FontStyleTest extends AnyFlatSpec with Matchers:

  "default font style" should "be default parameters" in:
    defaultFont.nameFont mustBe defaultNameFont
    defaultFont.styleFont mustBe defaultStyleFont
    defaultFont.sizeFont mustBe defaultSizeFont

  "name font style" should "be SanSerif" in:
    val name = nameFont("SanSerif")
    name.nameFont mustBe "SanSerif"
    name.styleFont mustBe defaultStyleFont
    name.sizeFont mustBe defaultSizeFont

  "style font style" should "be Bold" in:
    val style = styleFont(Font.Bold)
    style.nameFont mustBe defaultNameFont
    style.styleFont mustBe Font.Bold
    style.sizeFont mustBe defaultSizeFont

  "size font style" should "be 20" in:
    val size = sizeFont(20)
    size.nameFont mustBe defaultNameFont
    size.styleFont mustBe defaultStyleFont
    size.sizeFont mustBe 20

  "new font style" should "be SanSerif, Bold, 20" in:
    val complete = font("SanSerif", Font.Bold, 20)
    complete.nameFont mustBe "SanSerif"
    complete.styleFont mustBe Font.Bold
    complete.sizeFont mustBe 20

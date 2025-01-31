package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import scala.swing.Font

class JFontStyleTest extends AnyFlatSpec with Matchers:
  val default = jFont()

  "default font style" should "be default parameters" in:
    default.name mustBe defaultNameFont
    default.style mustBe defaultStyleFont
    default.size mustBe defaultSizeFont

  "name font style" should "be SanSerif" in:
    val name = nameJFont("SanSerif")
    name.name mustBe "SanSerif"
    name.style mustBe defaultStyleFont
    name.size mustBe defaultSizeFont

  "style font style" should "be Bold" in:
    val style = styleJFont(Font.Bold)
    style.name mustBe defaultNameFont
    style.style mustBe Font.Bold
    style.size mustBe defaultSizeFont

  "size font style" should "be 20" in:
    val size = sizeJFont(20)
    size.name mustBe defaultNameFont
    size.style mustBe defaultStyleFont
    size.size mustBe 20

  "complete font style" should "be SanSerif, Bold, 20" in:
    val complete = completeJFont("SanSerif", Font.Bold, 20)
    complete.name mustBe "SanSerif"
    complete.style mustBe Font.Bold
    complete.size mustBe 20

  "change name font style" should "be SanSerif" in:
    val name = default.withName("SanSerif")
    name.name mustBe "SanSerif"
    name.style mustBe defaultStyleFont
    name.size mustBe defaultSizeFont

  "change style font style" should "be Bold" in:
    val style = default.withStyle(Font.Bold)
    style.name mustBe defaultNameFont
    style.style mustBe Font.Bold
    style.size mustBe defaultSizeFont

  "change size font style" should "be 20" in:
    val size = default.withSize(20)
    size.name mustBe defaultNameFont
    size.style mustBe defaultStyleFont
    size.size mustBe 20

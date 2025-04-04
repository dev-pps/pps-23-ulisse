package ulisse.infrastructures.view.components.styles

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import Styles.*

import java.awt.Color
import scala.swing.Font

class FontStyleTest extends AnyFlatSpec with Matchers:

  "default font style" should "be default parameters" in:
    defaultFont.name mustBe defaultNameFont
    defaultFont.style mustBe defaultStyleFont
    defaultFont.size mustBe defaultSizeFont
    defaultFont.palette mustBe defaultPaletteFont

  "font transform in swing font" should "be the same" in:
    val transformInSwingFont = defaultFont.swingFont
    val swingFont            = new Font(defaultNameFont, defaultStyleFont.id, defaultSizeFont)

    transformInSwingFont mustBe swingFont

  "font with name" should "change name" in:
    val fontName = "SanSerif"
    val newFont  = defaultFont.withName(fontName)

    newFont must not be defaultFont
    newFont.name mustBe fontName

  "font with style" should "change style" in:
    val fontStyle = Font.Bold
    val newFont   = defaultFont.withStyle(fontStyle)

    newFont must not be defaultFont
    newFont.style mustBe fontStyle

  "font with size" should "change size" in:
    val fontSize = 20
    val newFont  = defaultFont.withSize(fontSize)

    newFont must not be defaultFont
    newFont.size mustBe fontSize

  "create a new font" should "have new name, style, color and size" in:
    val fontName  = "SanSerif"
    val fontStyle = Font.Bold
    val fontColor = Color.red
    val fontSize  = 20
    val newFont   = createFont(fontName, fontStyle, fontColor, fontSize)

    newFont.name mustBe fontName
    newFont.style mustBe fontStyle
    newFont.size mustBe fontSize

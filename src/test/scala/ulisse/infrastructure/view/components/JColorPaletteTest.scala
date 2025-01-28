package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JColorPalette

import java.awt.Color

class JColorPaletteTest extends AnyFlatSpec with Matchers:
  val paletteColor = JColorPalette(Color.BLACK, Color.RED, Color.BLUE)

  "palette color" should "have background, click and hover color" in:
    paletteColor.background must be(Color.BLACK)
    paletteColor.click must be(Color.RED)
    paletteColor.hover must be(Color.BLUE)

  "change background color" should "have new background color" in:
    val newBackground   = Color.GREEN
    val newPaletteColor = paletteColor.setBackground(newBackground)
    newPaletteColor.background must be(newBackground)
    newPaletteColor.click must be(Color.RED)
    newPaletteColor.hover must be(Color.BLUE)

  "change click color" should "have new click color" in:
    val newClick        = Color.GREEN
    val newPaletteColor = paletteColor.setClick(newClick)
    newPaletteColor.background must be(Color.BLACK)
    newPaletteColor.click must be(newClick)
    newPaletteColor.hover must be(Color.BLUE)

  "change hover color" should "have new hover color" in:
    val newHover        = Color.GREEN
    val newPaletteColor = paletteColor.setHover(newHover)
    newPaletteColor.background must be(Color.BLACK)
    newPaletteColor.click must be(Color.RED)
    newPaletteColor.hover must be(newHover)

  "change all colors" should "have new colors" in:
    val newBackground   = Color.GREEN
    val newClick        = Color.YELLOW
    val newHover        = Color.CYAN
    val newPaletteColor = paletteColor.setAll(newBackground, newClick, newHover)
    newPaletteColor.background must be(newBackground)
    newPaletteColor.click must be(newClick)
    newPaletteColor.hover must be(newHover)

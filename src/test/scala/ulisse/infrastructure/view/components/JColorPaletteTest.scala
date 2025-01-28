package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JColorPalette

import java.awt.Color

class JColorPaletteTest extends AnyFlatSpec with Matchers:

  "palette color" should "have background, click and hover color" in:
    val paletteColor = JColorPalette(Color.BLACK, Color.RED, Color.BLUE)
    paletteColor.background must be(Color.BLACK)
    paletteColor.click must be(Color.RED)
    paletteColor.hover must be(Color.BLUE)

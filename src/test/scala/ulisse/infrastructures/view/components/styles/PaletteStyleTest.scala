package ulisse.infrastructures.view.components.styles

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import Styles.*

import java.awt.Color

class PaletteStyleTest extends AnyFlatSpec with Matchers:
  "default palette " should "have only background" in:
    defaultPalette.background must be(defaultColor)
    defaultPalette.clickColor must be(withOutColor)
    defaultPalette.hoverColor must be(withOutColor)

  "palette with background" should "change background color" in:
    val background      = Color.green
    val newPaletteColor = defaultPalette.withBackground(background)

    newPaletteColor must not be defaultPalette
    newPaletteColor.background must be(background)

  "palette with click" should "change background color" in:
    val clickColor      = Color.blue
    val newPaletteColor = defaultPalette.withClick(clickColor)

    newPaletteColor.background must be(defaultColor)
    newPaletteColor.clickColor must be(Some(clickColor))

  "palette with hover" should "change hover color" in:
    val hover           = Color.red
    val newPaletteColor = defaultPalette.withHover(hover)

    newPaletteColor.background must be(defaultColor)
    newPaletteColor.hoverColor must be(Some(hover))

  "create a new palette" should "have a new background, click and hover" in:
    val background      = Color.green
    val clickColor      = Color.blue
    val hover           = Color.red
    val newPaletteColor = createPalette(background, clickColor, hover)

    newPaletteColor.background must be(background)
    newPaletteColor.clickColor must be(Some(clickColor))
    newPaletteColor.hoverColor must be(Some(hover))

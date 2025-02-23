package ulisse.infrastructures.view.components.styles

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.ui.decorators.Styles.*

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

  "check current color" should "be the background color" in:
    val background      = Color.green
    val newPaletteColor = defaultPalette.withBackground(background)

    newPaletteColor.currentColor must be(background)

  "check current color after hover" should "be the hover color" in:
    val background      = Color.green
    val hover           = Color.red
    val newPaletteColor = defaultPalette.withBackground(background).withHover(hover)

    newPaletteColor.hoverAction()
    newPaletteColor.currentColor must be(hover)
    defaultPalette.currentColor must not be hover

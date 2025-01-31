package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import java.awt.Color

class PaletteStyleTest extends AnyFlatSpec with Matchers:
  "default palette " should "have only background" in:
    defaultPalette.background must be(defaultColor)
    defaultPalette.clickColor must be(withOutColor)
    defaultPalette.hoverColor must be(withOutColor)

  "background palette" should "have background color" in:
    val background      = Color.green
    val newPaletteColor = backgroundPalette(background)
    newPaletteColor.background must be(background)
    newPaletteColor.clickColor must be(withOutColor)
    newPaletteColor.hoverColor must be(withOutColor)

  "click palette" should "have background color" in:
    val clickColor      = Color.blue
    val newPaletteColor = clickPalette(clickColor)
    newPaletteColor.background must be(defaultColor)
    newPaletteColor.clickColor must be(Some(clickColor))
    newPaletteColor.hoverColor must be(withOutColor)

  "hover palette" should "have background and hover color" in:
    val hover           = Color.red
    val newPaletteColor = hoverPalette(hover)
    newPaletteColor.background must be(defaultColor)
    newPaletteColor.clickColor must be(withOutColor)
    newPaletteColor.hoverColor must be(Some(hover))

  "new palette" should "have all colors" in:
    val background      = Color.green
    val clickColor      = Color.blue
    val hover           = Color.red
    val newPaletteColor = palette(background, clickColor, hover)
    newPaletteColor.background must be(background)
    newPaletteColor.clickColor must be(Some(clickColor))
    newPaletteColor.hoverColor must be(Some(hover))

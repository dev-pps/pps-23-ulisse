package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import java.awt.Color

class PaletteStyle extends AnyFlatSpec with Matchers:
  val defaultPalette = JStyle.palette()

  "default palette " should "have only background" in:
    defaultPalette.background must be(JStyle.defaultBackgroundColor)
    defaultPalette.click must be(JStyle.withOutColor)
    defaultPalette.hover must be(JStyle.withOutColor)

  "background palette" should "have background color" in:
    val background      = Color.green
    val newPaletteColor = JStyle.backgroundPalette(background)
    newPaletteColor.background must be(background)
    newPaletteColor.click must be(JStyle.withOutColor)
    newPaletteColor.hover must be(JStyle.withOutColor)

  "click palette" should "have background color" in:
    val background      = Color.green
    val click           = Color.blue
    val newPaletteColor = JStyle.clickPalette(background, click)
    newPaletteColor.background must be(background)
    newPaletteColor.click must be(Some(click))
    newPaletteColor.hover must be(JStyle.withOutColor)

  "hover palette" should "have background and hover color" in:
    val background      = Color.green
    val click           = Color.blue
    val hover           = Color.red
    val newPaletteColor = JStyle.hoverPalette(background, click, hover)
    newPaletteColor.background must be(background)
    newPaletteColor.click must be(Some(click))
    newPaletteColor.hover must be(Some(hover))

  "change background color" should "have new background color" in:
    val newBackground   = Color.GREEN
    val newPaletteColor = defaultPalette.withBackground(newBackground)
    newPaletteColor.background must be(newBackground)
    newPaletteColor.click must be(JStyle.withOutColor)
    newPaletteColor.hover must be(JStyle.withOutColor)

  "change click color" should "have new click color" in:
    val newClick        = Color.GREEN
    val newPaletteColor = defaultPalette.withClick(newClick)
    newPaletteColor.background must be(JStyle.defaultBackgroundColor)
    newPaletteColor.click must be(Some(newClick))
    newPaletteColor.hover must be(JStyle.withOutColor)

  "change hover color" should "have new hover color" in:
    val newHover        = Color.GREEN
    val newPaletteColor = defaultPalette.withHover(newHover)
    newPaletteColor.background must be(JStyle.defaultBackgroundColor)
    newPaletteColor.click must be(JStyle.withOutColor)
    newPaletteColor.hover must be(Some(newHover))

  "change all colors" should "have new colors" in:
    val newBackground   = Color.GREEN
    val newClick        = Color.YELLOW
    val newHover        = Color.CYAN
    val newPaletteColor = defaultPalette.withAll(newBackground, newClick, newHover)
    newPaletteColor.background must be(newBackground)
    newPaletteColor.click must be(Some(newClick))
    newPaletteColor.hover must be(Some(newHover))

package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import java.awt.Color

class JStylerTest extends AnyFlatSpec with Matchers:
  "default styler" should "have default style" in:
    defaultStyler.rect must be(defaultRect)
    defaultStyler.palette must be(defaultPalette)
    defaultStyler.font must be(defaultFont)
    defaultStyler.all must be(List(defaultRect, defaultPalette, defaultFont))

  "rect styler" should "have rect style" in:
    val newRect = roundRect(10)
    val styler  = rectStyler(newRect)

    styler.rect must be(newRect)
    styler.palette must be(defaultPalette)
    styler.font must be(defaultFont)
    styler.border must be(withoutBorder)
    styler.all must be(Seq(newRect, defaultPalette, defaultFont))

  "palette styler" should "have palette style" in:
    val newPalette = backgroundPalette(Color.green)
    val styler     = paletteStyler(newPalette)

    styler.rect must be(defaultRect)
    styler.palette must be(newPalette)
    styler.font must be(defaultFont)
    styler.border must be(withoutBorder)
    styler.all must be(Seq(defaultRect, newPalette, defaultFont))

  "font styler" should "have font style" in:
    val newFont = sizeFont(20)
    val styler  = fontStyler(newFont)

    styler.rect must be(defaultRect)
    styler.palette must be(defaultPalette)
    styler.font must be(newFont)
    styler.all must be(Seq(defaultRect, defaultPalette, newFont))

  "border styler" should "have border style" in:
    val newBorder = colorBorder(Color.green)
    val styler    = borderStyler(newBorder)

    styler.rect must be(defaultRect)
    styler.palette must be(defaultPalette)
    styler.all must be(Seq(defaultRect, defaultPalette, defaultFont, newBorder))

  "new styler" should "have complete style" in:
    val newRect    = roundRect(10)
    val newPalette = backgroundPalette(Color.green)
    val font       = sizeFont(20)
    val newBorder  = colorBorder(Color.green)
    val newStyler  = styler(newRect, newPalette, font, newBorder)

    newStyler.rect must be(newRect)
    newStyler.palette must be(newPalette)
    newStyler.font must be(font)
    newStyler.border must be(Some(newBorder))
    newStyler.all must be(Seq(newRect, newPalette, font, newBorder))

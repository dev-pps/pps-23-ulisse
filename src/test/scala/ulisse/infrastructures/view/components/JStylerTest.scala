package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler.*

import java.awt.Color

class JStylerTest extends AnyFlatSpec with Matchers:
  val defaultStyler = styler()

  "default styler" should "have default style" in:
    defaultStyler.rect must be(defaultRect)
    defaultStyler.palette must be(defaultPalette)
    defaultStyler.jFont must be(defaultFont)
    defaultStyler.all must be(Seq(defaultRect, defaultPalette, defaultFont))

  "rect styler" should "have rect style" in:
    val newRect = roundRect(10)
    val styler  = rectStyler(newRect)

    styler.rect must be(newRect)
    styler.palette must be(defaultPalette)
    styler.jFont must be(defaultFont)
    styler.border must be(withoutBorder)
    styler.all must be(Seq(newRect, defaultPalette, defaultFont))

  "palette styler" should "have palette style" in:
    val newPalette = backgroundPalette(Color.green)
    val styler     = paletteStyler(newPalette)

    styler.rect must be(defaultRect)
    styler.palette must be(newPalette)
    styler.jFont must be(defaultFont)
    styler.border must be(withoutBorder)
    styler.all must be(Seq(defaultRect, newPalette, defaultFont))

  "font styler" should "have font style" in:
    val newFont = sizeJFont(20)
    val styler  = fontStyler(newFont)

    styler.rect must be(defaultRect)
    styler.palette must be(defaultPalette)
    styler.jFont must be(newFont)
    styler.all must be(Seq(defaultRect, defaultPalette, newFont))

  "border styler" should "have border style" in:
    val newBorder = colorBorder(Color.green)
    val styler    = borderStyler(newBorder)

    styler.rect must be(defaultRect)
    styler.palette must be(defaultPalette)
    styler.all must be(Seq(defaultRect, defaultPalette, defaultFont, newBorder))

  "complete styler" should "have complete style" in:
    val newRect    = roundRect(10)
    val newPalette = backgroundPalette(Color.green)
    val font       = sizeJFont(20)
    val newBorder  = colorBorder(Color.green)
    val styler     = completeStyler(newRect, newPalette, font, newBorder)

    styler.rect must be(newRect)
    styler.palette must be(newPalette)
    styler.jFont must be(font)
    styler.border must be(Some(newBorder))
    styler.all must be(Seq(newRect, newPalette, font, newBorder))

  "change rect styler" should "have new rect" in:
    val newRect    = roundRect(10)
    val rectStyler = defaultStyler.withRect(newRect)

    rectStyler.rect must be(newRect)
    rectStyler.palette must be(defaultPalette)
    rectStyler.all must be(Seq(newRect, defaultPalette, defaultFont))

  "change palette styler" should "have new palette" in:
    val newPalette    = backgroundPalette(Color.green)
    val paletteStyler = defaultStyler.withPalette(newPalette)

    paletteStyler.rect must be(defaultRect)
    paletteStyler.palette must be(newPalette)
    paletteStyler.all must be(Seq(defaultRect, newPalette, defaultFont))

  "change font styler" should "have new font" in:
    val newFont    = sizeJFont(20)
    val fontStyler = defaultStyler.withFont(newFont)

    fontStyler.rect must be(defaultRect)
    fontStyler.palette must be(defaultPalette)
    fontStyler.jFont must be(newFont)
    fontStyler.all must be(Seq(defaultRect, defaultPalette, newFont))

  "change border styler" should "have new border" in:
    val newBorder    = colorBorder(Color.green)
    val borderStyler = defaultStyler.withBorder(newBorder)

    borderStyler.rect must be(defaultRect)
    borderStyler.palette must be(defaultPalette)
    borderStyler.all must be(Seq(defaultRect, defaultPalette, defaultFont, newBorder))

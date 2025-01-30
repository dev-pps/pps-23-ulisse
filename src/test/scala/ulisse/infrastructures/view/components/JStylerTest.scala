package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyler._

import java.awt.Color

class JStylerTest extends AnyFlatSpec with Matchers:
  val defaultManager = apply()

  "default manager" should "have default style" in:
    defaultManager.rect must be(defaultRect)
    defaultManager.palette must be(defaultPalette)
    defaultManager.all must be(Seq(defaultRect, defaultPalette))

  "rect manager" should "have rect style" in:
    val newRect     = roundRect(10)
    val rectManager = rectStyler(newRect)

    rectManager.rect must be(newRect)
    rectManager.palette must be(defaultPalette)
    rectManager.border must be(withoutBorder)
    rectManager.all must be(Seq(newRect, defaultPalette))

  "palette manager" should "have palette style" in:
    val newPalette     = backgroundPalette(Color.green)
    val paletteManager = paletteStyler(newPalette)

    paletteManager.rect must be(defaultRect)
    paletteManager.palette must be(newPalette)
    paletteManager.border must be(withoutBorder)
    paletteManager.all must be(Seq(defaultRect, newPalette))

  "border manager" should "have border style" in:
    val newBorder     = colorBorder(Color.green)
    val borderManager = borderStyler(newBorder)

    borderManager.rect must be(defaultRect)
    borderManager.palette must be(defaultPalette)
    borderManager.all must be(Seq(defaultRect, defaultPalette, newBorder))

  "complete manager" should "have complete style" in:
    val newRect         = roundRect(10)
    val newPalette      = backgroundPalette(Color.green)
    val newBorder       = colorBorder(Color.green)
    val completeManager = completeStyler(newRect, newPalette, newBorder)

    completeManager.rect must be(newRect)
    completeManager.palette must be(newPalette)
    completeManager.border must be(Some(newBorder))
    completeManager.all must be(Seq(newRect, newPalette, newBorder))

  "change rect manager" should "have new rect" in:
    val newRect     = roundRect(10)
    val rectManager = defaultManager.withRect(newRect)

    rectManager.rect must be(newRect)
    rectManager.palette must be(defaultPalette)
    rectManager.all must be(Seq(newRect, defaultPalette))

  "change palette manager" should "have new palette" in:
    val newPalette     = backgroundPalette(Color.green)
    val paletteManager = defaultManager.withPalette(newPalette)

    paletteManager.rect must be(defaultRect)
    paletteManager.palette must be(newPalette)
    paletteManager.all must be(Seq(defaultRect, newPalette))

  "change border manager" should "have new border" in:
    val newBorder     = colorBorder(Color.green)
    val borderManager = defaultManager.withBorder(newBorder)

    borderManager.rect must be(defaultRect)
    borderManager.palette must be(defaultPalette)
    borderManager.all must be(Seq(defaultRect, defaultPalette, newBorder))

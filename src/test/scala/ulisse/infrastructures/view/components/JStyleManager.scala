package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import java.awt.Color

class JStyleManager extends AnyFlatSpec with Matchers:
  val defaultManager = JStyle()

  "default manager" should "have default style" in:
    defaultManager.rect must be(JStyle.defaultRect)
    defaultManager.palette must be(JStyle.defaultPalette)
    defaultManager.all must be(Seq(JStyle.defaultRect, JStyle.defaultPalette))

  "rect manager" should "have rect style" in:
    val newRect     = JStyle.roundRect(10)
    val rectManager = JStyle.rectStyle(newRect)

    rectManager.rect must be(newRect)
    rectManager.palette must be(JStyle.defaultPalette)
    rectManager.border must be(JStyle.withoutBorder)
    rectManager.all must be(Seq(newRect, JStyle.defaultPalette))

  "palette manager" should "have palette style" in:
    val newPalette     = JStyle.backgroundPalette(Color.green)
    val paletteManager = JStyle.paletteStyle(newPalette)

    paletteManager.rect must be(JStyle.defaultRect)
    paletteManager.palette must be(newPalette)
    paletteManager.border must be(JStyle.withoutBorder)
    paletteManager.all must be(Seq(JStyle.defaultRect, newPalette))

  "border manager" should "have border style" in:
    val newBorder     = JStyle.colorBorder(Color.green)
    val borderManager = JStyle.borderStyle(newBorder)

    borderManager.rect must be(JStyle.defaultRect)
    borderManager.palette must be(JStyle.defaultPalette)
    borderManager.all must be(Seq(JStyle.defaultRect, JStyle.defaultPalette, newBorder))

  "complete manager" should "have complete style" in:
    val newRect         = JStyle.roundRect(10)
    val newPalette      = JStyle.backgroundPalette(Color.green)
    val newBorder       = JStyle.colorBorder(Color.green)
    val completeManager = JStyle.completeStyle(newRect, newPalette, newBorder)

    completeManager.rect must be(newRect)
    completeManager.palette must be(newPalette)
    completeManager.border must be(Some(newBorder))
    completeManager.all must be(Seq(newRect, newPalette, newBorder))

  "change rect manager" should "have new rect" in:
    val newRect     = JStyle.roundRect(10)
    val rectManager = defaultManager.withRect(newRect)

    rectManager.rect must be(newRect)
    rectManager.palette must be(JStyle.defaultPalette)
    rectManager.all must be(Seq(newRect, JStyle.defaultPalette))

  "change palette manager" should "have new palette" in:
    val newPalette     = JStyle.backgroundPalette(Color.green)
    val paletteManager = defaultManager.withPalette(newPalette)

    paletteManager.rect must be(JStyle.defaultRect)
    paletteManager.palette must be(newPalette)
    paletteManager.all must be(Seq(JStyle.defaultRect, newPalette))

  "change border manager" should "have new border" in:
    val newBorder     = JStyle.colorBorder(Color.green)
    val borderManager = defaultManager.withBorder(newBorder)

    borderManager.rect must be(JStyle.defaultRect)
    borderManager.palette must be(JStyle.defaultPalette)
    borderManager.all must be(Seq(JStyle.defaultRect, JStyle.defaultPalette, newBorder))

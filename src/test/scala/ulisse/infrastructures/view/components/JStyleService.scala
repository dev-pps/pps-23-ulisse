package ulisse.infrastructures.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyle.*

import java.awt.Color

class JStyleService extends AnyFlatSpec with Matchers:
  val serviceEmpty       = JStyle.empty()
  val serviceWithPalette = serviceEmpty.change(orangePalette)
  val serviceWithBorder  = serviceEmpty.change(orangeBorder)
  val serviceWithAll     = JStyle(orangePalette, orangeBorder)

  "create empty style service" should "have no style" in:
    serviceEmpty.all must be(Seq(defaultPalette, defaultBorder))
    serviceEmpty.colorPalette must be(defaultPalette)
    serviceEmpty.border must be(defaultBorder)

  "create style service with color palette" should "have color palette" in:
    serviceWithPalette.all must be(Seq(orangePalette, JStyle.defaultBorder))
    serviceWithPalette.colorPalette must be(orangePalette)
    serviceWithPalette.border must be(JStyle.defaultBorder)

  "create style service with border" should "have border" in:
    serviceWithBorder.all must be(Seq(JStyle.defaultPalette, orangeBorder))
    serviceWithBorder.colorPalette must be(JStyle.defaultPalette)
    serviceWithBorder.border must be(orangeBorder)

  "create style service whit color palette and border" should "have color palette and border" in:
    serviceWithAll.all must be(Seq(orangePalette, orangeBorder))
    serviceWithAll.colorPalette must be(orangePalette)
    serviceWithAll.border must be(orangeBorder)

  "change style service with color palette" should "have new color palette" in:
    val newPalette      = JStyle.Palette(Color.RED, Color.GREEN, Color.BLUE)
    val newStyleService = serviceWithPalette.change(newPalette)

    newStyleService.all must be(Seq(newPalette, defaultBorder))
    newStyleService.colorPalette must be(newPalette)

  "change style service with border" should "have new border" in:
    val newBorder       = JStyle.Border(Color.RED, 4, 40)
    val newStyleService = serviceWithBorder.change(newBorder)

    newStyleService.all must be(Seq(defaultPalette, newBorder))
    newStyleService.border must be(newBorder)

  "change style service with color palette and border" should "have new color palette and border" in:
    val newPalette      = JStyle.Palette(Color.RED, Color.GREEN, Color.BLUE)
    val newBorder       = JStyle.Border(Color.RED, 4, 40)
    val newStyleService = serviceWithAll.change(newPalette, newBorder)

    newStyleService.all must be(Seq(newPalette, newBorder))
    newStyleService.colorPalette must be(newPalette)
    newStyleService.border must be(newBorder)

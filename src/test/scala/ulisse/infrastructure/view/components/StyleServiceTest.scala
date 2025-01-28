package ulisse.infrastructure.view.components

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import ulisse.infrastructures.view.components.JStyle

import java.awt.Color

class StyleServiceTest extends AnyFlatSpec with Matchers:
  val defaultPalette = JStyle.ColorPalette(Color.BLACK, Color.RED, Color.GREEN)
  val defaultBorder  = JStyle.Border(Color.BLACK, 2, 20)

  val serviceEmpty       = JStyle.empty()
  val serviceWithPalette = JStyle(defaultPalette)
  val serviceWithBorder  = JStyle(defaultBorder)
  val serviceWithAll     = JStyle(defaultPalette, defaultBorder)

  "create empty style service" should "have no style" in:
    serviceEmpty.all must be(Seq.empty[JStyle])
    serviceEmpty.colorPalette must be(None)
    serviceEmpty.border must be(None)

  "create style service with color palette" should "have color palette" in:
    serviceWithPalette.all must be(Seq(defaultPalette))
    serviceWithPalette.colorPalette must be(Some(defaultPalette))
    serviceWithPalette.border must be(None)

  "create style service with border" should "have border" in:
    serviceWithBorder.all must be(Seq(defaultBorder))
    serviceWithBorder.colorPalette must be(None)
    serviceWithBorder.border must be(Some(defaultBorder))

  "create style service whit color palette and border" should "have color palette and border" in:
    serviceWithAll.all must be(Seq(defaultPalette, defaultBorder))
    serviceWithAll.colorPalette must be(Some(defaultPalette))
    serviceWithAll.border must be(Some(defaultBorder))

  "change style service with color palette" should "have new color palette" in:
    val newPalette      = JStyle.ColorPalette(Color.RED, Color.GREEN, Color.BLUE)
    val newStyleService = serviceWithPalette.change(newPalette)

    newStyleService.all must be(Seq(newPalette))
    newStyleService.colorPalette must be(Some(newPalette))
    newStyleService.border must be(None)

  "change style service with border" should "have new border" in:
    val newBorder       = JStyle.Border(Color.RED, 4, 40)
    val newStyleService = serviceWithBorder.change(newBorder)

    newStyleService.all must be(Seq(newBorder))
    newStyleService.colorPalette must be(None)
    newStyleService.border must be(Some(newBorder))

  "change style service with color palette and border" should "have new color palette and border" in:
    val newPalette      = JStyle.ColorPalette(Color.RED, Color.GREEN, Color.BLUE)
    val newBorder       = JStyle.Border(Color.RED, 4, 40)
    val newStyleService = serviceWithAll.change(newPalette, newBorder)

    newStyleService.all must be(Seq(newPalette, newBorder))
    newStyleService.colorPalette must be(Some(newPalette))
    newStyleService.border must be(Some(newBorder))

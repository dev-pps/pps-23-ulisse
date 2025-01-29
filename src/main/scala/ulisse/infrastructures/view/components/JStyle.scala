package ulisse.infrastructures.view.components

import ulisse.utils.TypeCheckers.identicalClassIs

import java.awt.Color

trait JStyle

object JStyle:
  def palette(background: Color, click: Color, hover: Color): ColorPalette = ColorPalette(background, click, hover)
  def border(color: Color, width: Int, arc: Int): Border                   = Border(color, width, arc)

  def empty(): JStyleService                = JStyleService()
  def apply(styles: JStyle*): JStyleService = JStyleService(styles: _*)

  extension (service: JStyleService)
    def change[T <: JStyle](styles: T*): JStyleService =
      val newStyle = service.all.filterNot(style => styles.exists(_ identicalClassIs style)) ++ styles
      JStyleService(newStyle: _*)

  val defaultPalette: ColorPalette = palette(Color.WHITE, Color.WHITE, Color.WHITE)
  val orangePalette: ColorPalette  = palette(Color.decode("#FFA07A"), Color.decode("#FF4500"), Color.decode("#FF6347"))

  val defaultBorder: Border = border(Color.BLACK, 1, 10)
  val orangeBorder: Border  = border(Color.decode("#FF4500"), 2, 20)

  case class JStyleService(style: JStyle*):
    val colorPalette: ColorPalette = style.collectFirst { case c: ColorPalette => c }.getOrElse(defaultPalette)
    val border: Border             = style.collectFirst { case b: Border => b }.getOrElse(defaultBorder)
    val all: Seq[JStyle]           = Seq(colorPalette, border)

  case class ColorPalette(background: Color, click: Color, hover: Color) extends JStyle:
    def setAll(newBackground: Color, newClick: Color, newHover: Color): ColorPalette =
      copy(background = newBackground, click = newClick, hover = newHover)
    def setBackground(newBackground: Color): ColorPalette = copy(background = newBackground)
    def setClick(newClick: Color): ColorPalette           = copy(click = newClick)
    def setHover(newHover: Color): ColorPalette           = copy(hover = newHover)

  case class Border(color: Color, width: Int, arc: Int) extends JStyle:
    def setAll(newColor: Color, newWidth: Int, newArc: Int): Border =
      copy(color = newColor, width = newWidth, arc = newArc)
    def setColor(newColor: Color): Border = copy(color = newColor)
    def setWidth(newWidth: Int): Border   = copy(width = newWidth)
    def setArc(newArc: Int): Border       = copy(arc = newArc)

package ulisse.infrastructures.view.components

import ulisse.utils.TypeCheckers.identicalClassIs

import java.awt.Color

trait JStyle

object JStyle:
  val defaultArcShape: Int = 0

  val defaultPalette: Palette = palette(Color.WHITE, Color.WHITE, Color.WHITE)
  val orangePalette: Palette  = palette(Color.decode("#FFA07A"), Color.decode("#FF4500"), Color.decode("#FF6347"))

  val defaultBorder: Border = border(Color.BLACK, 1, 10)
  val orangeBorder: Border  = border(Color.decode("#FF4500"), 2, 20)

  def rect(): Rect              = new Rect(defaultArcShape)
  def roundRect(arc: Int): Rect = new Rect(arc)

  def palette(background: Color, click: Color, hover: Color): Palette = Palette(background, click, hover)
  def border(color: Color, width: Int, arc: Int): Border              = Border(color, width, arc)

  def empty(): JStyleService                = JStyleService()
  def apply(styles: JStyle*): JStyleService = JStyleService(styles: _*)

  case class JStyleService(style: JStyle*):
    val colorPalette: Palette = style.collectFirst { case c: Palette => c }.getOrElse(defaultPalette)
    val border: Border        = style.collectFirst { case b: Border => b }.getOrElse(defaultBorder)
    val all: Seq[JStyle]      = Seq(colorPalette, border)

    def change[T <: JStyle](styles: T*): JStyleService =
      JStyleService(all.filterNot(style => styles.exists(_ identicalClassIs style)) ++ styles: _*)

  case class Rect(arcTopSx: Int, arcTopDx: Int, arcBottomSx: Int, arcBottomDx: Int) extends JStyle:
    def this(arc: Int) = this(arc, arc, arc, arc)
    def setArc(arc: Int): Rect = copy(arcTopSx = arc, arcTopDx = arc, arcBottomSx = arc, arcBottomDx = arc)

  case class Palette(background: Color, click: Color, hover: Color) extends JStyle:
    def setAll(newBackground: Color, newClick: Color, newHover: Color): Palette =
      copy(background = newBackground, click = newClick, hover = newHover)
    def setBackground(newBackground: Color): Palette = copy(background = newBackground)
    def setClick(newClick: Color): Palette           = copy(click = newClick)
    def setHover(newHover: Color): Palette           = copy(hover = newHover)

  case class Border(color: Color, width: Int, arc: Int) extends JStyle:
    def setAll(newColor: Color, newWidth: Int, newArc: Int): Border =
      copy(color = newColor, width = newWidth, arc = newArc)
    def setColor(newColor: Color): Border = copy(color = newColor)
    def setWidth(newWidth: Int): Border   = copy(width = newWidth)
    def setArc(newArc: Int): Border       = copy(arc = newArc)

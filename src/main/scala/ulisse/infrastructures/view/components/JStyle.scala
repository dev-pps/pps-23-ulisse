package ulisse.infrastructures.view.components

import ulisse.utils.TypeCheckers.identicalClassIs

import java.awt.Color

trait JStyle

object JStyle:
  val defaultArcShape: Int = 0

  val defaultBackgroundColor: Color = Color.white
  val withOutColor                  = Option.empty[Color]

  val defaultBorder: Border = border(Color.BLACK, 1, 10)
  val orangeBorder: Border  = border(Color.decode("#FF4500"), 2, 20)

  def rect(): Rect              = new Rect(defaultArcShape)
  def roundRect(arc: Int): Rect = new Rect(arc)

  def backgroundPalette(background: Color): Palette          = new Palette(background)
  def clickPalette(background: Color, click: Color): Palette = Palette(background, Some(click), Option.empty)
  def hoverPalette(background: Color, click: Color, hover: Color): Palette =
    Palette(background, Some(click), Some(hover))
  def palette(): Palette = backgroundPalette(defaultBackgroundColor)

  def border(color: Color, width: Int, arc: Int): Border = Border(color, width, arc)

  def empty(): JStyleService                = JStyleService()
  def apply(styles: JStyle*): JStyleService = JStyleService(styles: _*)

  case class JStyleService(style: JStyle*):
    val colorPalette: Palette = style.collectFirst { case c: Palette => c }.getOrElse(palette())
    val border: Border        = style.collectFirst { case b: Border => b }.getOrElse(defaultBorder)
    val all: Seq[JStyle]      = Seq(colorPalette, border)

    def change[T <: JStyle](styles: T*): JStyleService =
      JStyleService(all.filterNot(style => styles.exists(_ identicalClassIs style)) ++ styles: _*)

  case class Rect(arcTopSx: Int, arcTopDx: Int, arcBottomSx: Int, arcBottomDx: Int) extends JStyle:
    def this(arc: Int) = this(arc, arc, arc, arc)

    def setArc(arc: Int): Rect = copy(arcTopSx = arc, arcTopDx = arc, arcBottomSx = arc, arcBottomDx = arc)

  case class Palette(background: Color, click: Option[Color], hover: Option[Color]) extends JStyle:
    def this(background: Color) = this(background, Option.empty, Option.empty)

    def withAll(newBackground: Color, newClick: Color, newHover: Color): Palette =
      copy(background = newBackground, click = Some(newClick), hover = Some(newHover))
    def withBackground(newBackground: Color): Palette = copy(background = newBackground)
    def withClick(newClick: Color): Palette           = copy(click = Some(newClick))
    def withHover(newHover: Color): Palette           = copy(hover = Some(newHover))

  case class Border(color: Color, width: Int, arc: Int) extends JStyle:
    def setAll(newColor: Color, newWidth: Int, newArc: Int): Border =
      copy(color = newColor, width = newWidth, arc = newArc)
    def setColor(newColor: Color): Border = copy(color = newColor)
    def setWidth(newWidth: Int): Border   = copy(width = newWidth)
    def setArc(newArc: Int): Border       = copy(arc = newArc)

package ulisse.infrastructures.view.components

import java.awt.Color

trait JStyle

object JStyle:
  val defaultArcShape: Int = 0

  val defaultBackgroundColor: Color = Color.white
  val withOutColor: Option[Color]   = Option.empty

  val defaultBorderColor: Color = defaultBackgroundColor
  val defaultStroke: Int        = 1

  val defaultRect: Rect             = rect()
  val defaultPalette: Palette       = palette()
  val withoutBorder: Option[Border] = Option.empty

  def rect(): Rect              = new Rect(defaultArcShape)
  def roundRect(arc: Int): Rect = new Rect(arc)

  def hoverPalette(background: Color, click: Color, hover: Color): Palette =
    Palette(background, Some(click), Some(hover))
  def clickPalette(background: Color, click: Color): Palette = Palette(background, Some(click), Option.empty)
  def backgroundPalette(background: Color): Palette          = new Palette(background)
  def palette(): Palette                                     = backgroundPalette(defaultBackgroundColor)

  def completeBorder(color: Color, stroke: Int): Border = Border(color, stroke)
  def colorBorder(color: Color): Border                 = completeBorder(color, defaultStroke)
  def border(): Border                                  = completeBorder(defaultBorderColor, defaultStroke)

  def completeStyle(rect: Rect, palette: Palette, border: Border): JStyleManager =
    JStyleManager(rect, palette, Some(border))
  def borderStyle(border: Border): JStyleManager    = JStyleManager(rect(), palette(), Some(border))
  def paletteStyle(palette: Palette): JStyleManager = JStyleManager(rect(), palette, withoutBorder)
  def rectStyle(rect: Rect): JStyleManager          = JStyleManager(rect, palette(), withoutBorder)
  def apply(): JStyleManager                        = JStyleManager(rect(), palette(), withoutBorder)

  case class JStyleManager(rect: Rect, palette: Palette, border: Option[Border]):
    val all: Seq[JStyle] = border.map(style => Seq(rect, palette, style)).getOrElse(Seq(rect, palette))

    def withRect(newRect: Rect): JStyleManager          = copy(rect = newRect)
    def withPalette(newPalette: Palette): JStyleManager = copy(palette = newPalette)
    def withBorder(newBorder: Border): JStyleManager    = copy(border = Some(newBorder))

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

  case class Border(color: Color, stroke: Int) extends JStyle:
    def withAll(newColor: Color, newStroke: Int): Border = copy(color = newColor, stroke = newStroke)
    def withColor(newColor: Color): Border               = copy(color = newColor)
    def withStroke(newWidth: Int): Border                = copy(stroke = newWidth)

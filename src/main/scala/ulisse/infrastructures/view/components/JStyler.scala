package ulisse.infrastructures.view.components

import java.awt.Color

object JStyler:
  export JStyle.*

  val defaultRect: Rect             = rect()
  val defaultPalette: Palette       = palette()
  val withoutBorder: Option[Border] = Option.empty

  def completeStyler(rect: Rect, palette: Palette, border: Border): JStyler =
    JStyler(rect, palette, Some(border))
  def borderStyler(border: Border): JStyler    = JStyler(rect(), palette(), Some(border))
  def paletteStyler(palette: Palette): JStyler = JStyler(rect(), palette, withoutBorder)
  def rectStyler(rect: Rect): JStyler          = JStyler(rect, palette(), withoutBorder)
  def apply(): JStyler                         = JStyler(rect(), palette(), withoutBorder)

  def puzzleStyler(using rect: Rect)(using palette: Palette)(using border: Border): JStyler =
    completeStyler(rect, palette, border)
  def puzzleRectStyler(using rect: Rect)(palette: Palette, border: Border): JStyler =
    completeStyler(rect, palette, border)
  def puzzlePaletteStyler(using palette: Palette)(rect: Rect, border: Border): JStyler =
    completeStyler(rect, palette, border)
  def puzzleBorderStyler(using border: Border)(rect: Rect, palette: Palette): JStyler =
    completeStyler(rect, palette, border)

  case class JStyler(rect: Rect, palette: Palette, border: Option[Border]):
    export rect._, palette._
    val all: Seq[JStyle] = border.map(style => Seq(rect, palette, style)).getOrElse(Seq(rect, palette))

    def withRect(newRect: Rect): JStyler          = copy(rect = newRect)
    def withPalette(newPalette: Palette): JStyler = copy(palette = newPalette)
    def withBorder(newBorder: Border): JStyler    = copy(border = Some(newBorder))

  trait JStyle
  private object JStyle:
    val defaultRoundRect: Int = 0

    val defaultColor: Color         = Color.white
    val withOutColor: Option[Color] = Option.empty

    val defaultStroke: Int = 1

    def rect(): Rect              = new Rect(defaultRoundRect)
    def roundRect(arc: Int): Rect = new Rect(arc)

    def puzzleRect(using arc: Int): Rect = new Rect(arc)

    def completePalette(background: Color, click: Color, hover: Color): Palette =
      Palette(background, Some(click), Some(hover))
    def hoverPalette(hover: Color): Palette           = Palette(defaultColor, withOutColor, Some(hover))
    def clickPalette(click: Color): Palette           = Palette(defaultColor, Some(click), withOutColor)
    def backgroundPalette(background: Color): Palette = new Palette(background)
    def palette(): Palette                            = backgroundPalette(defaultColor)

    def puzzlePalette(using background: Color)(using click: Color)(using hover: Color): Palette =
      completePalette(background, click, hover)
    def puzzleHoverPalette(using hover: Color)(background: Color, click: Color): Palette =
      completePalette(background, click, hover)
    def puzzleClickPalette(using click: Color)(background: Color, hover: Color): Palette =
      completePalette(background, click, hover)
    def puzzleBackgroundPalette(using background: Color)(click: Color, hover: Color): Palette =
      completePalette(background, click, hover)

    def completeBorder(color: Color, stroke: Int): Border = Border(color, stroke)
    def strokeBorder(stroke: Int): Border                 = completeBorder(defaultColor, stroke)
    def colorBorder(color: Color): Border                 = completeBorder(color, defaultStroke)
    def border(): Border                                  = completeBorder(defaultColor, defaultStroke)

    def puzzleBorder(using color: Color)(using stroke: Int) = completeBorder(color, stroke)
    def puzzleColorBorder(using color: Color)(stroke: Int)  = completeBorder(color, stroke)
    def puzzleStrokeBorder(using stroke: Int)(color: Color) = completeBorder(color, stroke)

    case class Rect(arcWidth: Int, arcHeight: Int) extends JStyle:
      def this(arc: Int) = this(arc, arc)

      def setArc(arc: Int): Rect = copy(arcWidth = arc, arcHeight = arc)

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

package ulisse.infrastructures.view.components

import java.awt.Color
import scala.swing.Font

object JStyler:
  export JStyle._

  val defaultRect: Rect             = rect()
  val defaultPalette: Palette       = palette()
  val defaultFont: JFont            = jFont()
  val withoutBorder: Option[Border] = Option.empty

  def completeStyler(rect: Rect, palette: Palette, jFont: JFont, border: Border): JStyler =
    JStyler(rect, palette, jFont, Some(border))
  def borderStyler(border: Border): JStyler    = JStyler(rect(), palette(), defaultFont, Some(border))
  def fontStyler(jFont: JFont): JStyler        = JStyler(rect(), palette(), jFont, withoutBorder)
  def paletteStyler(palette: Palette): JStyler = JStyler(rect(), palette, defaultFont, withoutBorder)
  def rectStyler(rect: Rect): JStyler          = JStyler(rect, palette(), defaultFont, withoutBorder)
  def styler(): JStyler                        = JStyler(rect(), palette(), defaultFont, withoutBorder)

  def puzzleStyler(using rect: Rect)(using palette: Palette)(using jFont: JFont)(using border: Border): JStyler =
    completeStyler(rect, palette, jFont, border)
  def puzzleRectStyler(using rect: Rect)(palette: Palette, jFont: JFont, border: Border): JStyler =
    completeStyler(rect, palette, jFont, border)
  def puzzlePaletteStyler(using palette: Palette)(rect: Rect, jFont: JFont, border: Border): JStyler =
    completeStyler(rect, palette, jFont, border)
  def puzzleFontStyler(using jFont: JFont)(rect: Rect, palette: Palette, border: Border): JStyler =
    completeStyler(rect, palette, jFont, border)
  def puzzleBorderStyler(using border: Border)(rect: Rect, palette: Palette, jFont: JFont): JStyler =
    completeStyler(rect, palette, jFont, border)

  case class JStyler(rect: Rect, palette: Palette, private val jFont: JFont, border: Option[Border]):
    export rect._, palette._, jFont._
    val all: Seq[JStyle] = border.map(style => Seq(rect, palette, jFont, style)).getOrElse(Seq(rect, palette, jFont))

    def withRect(newRect: Rect): JStyler          = copy(rect = newRect)
    def withPalette(newPalette: Palette): JStyler = copy(palette = newPalette)
    def withFont(newFont: JFont): JStyler         = copy(jFont = newFont)
    def withBorder(newBorder: Border): JStyler    = copy(border = Some(newBorder))

  trait JStyle
  private object JStyle:
    val defaultRoundRect: Int = 0

    val transparentColor: Color     = new Color(0, 0, 0, 0)
    val defaultColor: Color         = Color.white
    val withOutColor: Option[Color] = Option.empty

    val defaultStroke: Int = 1

    val defaultNameFont: String            = "Lucida Grande"
    val defaultStyleFont: Font.Style.Value = Font.Plain
    val defaultSizeFont: Int               = 13

    def rect(): Rect              = new Rect(defaultRoundRect)
    def roundRect(arc: Int): Rect = new Rect(arc)

    def puzzleRect(using arc: Int): Rect = new Rect(arc)

    case class Rect(arcWidth: Int, arcHeight: Int) extends JStyle:
      def this(arc: Int) = this(arc, arc)

      def setArc(arc: Int): Rect = copy(arcWidth = arc, arcHeight = arc)

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

    case class Palette(background: Color, click: Option[Color], hover: Option[Color]) extends JStyle:
      def this(background: Color) = this(background, Option.empty, Option.empty)

      def withAll(newBackground: Color, newClick: Color, newHover: Color): Palette =
        copy(background = newBackground, click = Some(newClick), hover = Some(newHover))
      def withBackground(newBackground: Color): Palette = copy(background = newBackground)
      def withClick(newClick: Color): Palette           = copy(click = Some(newClick))
      def withHover(newHover: Color): Palette           = copy(hover = Some(newHover))

    def completeBorder(color: Color, stroke: Int): Border = Border(color, stroke)
    def strokeBorder(stroke: Int): Border                 = completeBorder(defaultColor, stroke)
    def colorBorder(color: Color): Border                 = completeBorder(color, defaultStroke)
    def border(): Border                                  = completeBorder(defaultColor, defaultStroke)

    def puzzleBorder(using color: Color)(using stroke: Int) = completeBorder(color, stroke)
    def puzzleColorBorder(using color: Color)(stroke: Int)  = completeBorder(color, stroke)
    def puzzleStrokeBorder(using stroke: Int)(color: Color) = completeBorder(color, stroke)

    case class Border(color: Color, stroke: Int) extends JStyle:
      def withAll(newColor: Color, newStroke: Int): Border = copy(color = newColor, stroke = newStroke)
      def withColor(newColor: Color): Border               = copy(color = newColor)
      def withStroke(newWidth: Int): Border                = copy(stroke = newWidth)

    def completeJFont(name: String, style: Font.Style.Value, size: Int): JFont = JFont(name, style, size)
    def sizeJFont(size: Int): JFont                = JFont(defaultNameFont, defaultStyleFont, size)
    def styleJFont(style: Font.Style.Value): JFont = JFont(defaultNameFont, style, defaultSizeFont)
    def nameJFont(name: String): JFont             = JFont(name, defaultStyleFont, defaultSizeFont)
    def jFont(): JFont                             = JFont(defaultNameFont, defaultStyleFont, defaultSizeFont)

    def puzzleFont(using name: String)(using style: Font.Style.Value)(size: Int): JFont =
      completeJFont(name, style, size)
    def puzzleSizeFont(using size: Int)(name: String, style: Font.Style.Value): JFont =
      completeJFont(name, style, size)
    def puzzleStyleFont(using style: Font.Style.Value)(name: String, size: Int): JFont =
      completeJFont(name, style, size)
    def puzzleNameFont(using name: String)(style: Font.Style.Value, size: Int): JFont =
      completeJFont(name, style, size)

    case class JFont(name: String, style: Font.Style.Value, size: Int) extends JStyle:
      val font: Font = Font(name, style, size)
      def withAll(newName: String, newStyle: Font.Style.Value, newSize: Int): JFont =
        copy(name = newName, style = newStyle, size = newSize)
      def withName(newName: String): JFont             = copy(name = newName)
      def withStyle(newStyle: Font.Style.Value): JFont = copy(style = newStyle)
      def withSize(newSize: Int): JFont                = copy(size = newSize)

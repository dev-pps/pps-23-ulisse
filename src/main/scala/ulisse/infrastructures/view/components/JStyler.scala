package ulisse.infrastructures.view.components

import java.awt.Color
import scala.swing.Font as SwingFont
import scala.swing.Font.Style.Value as StyleFont

object JStyler:
  export JStyles._

  val withoutBorder: Option[Border] = Option.empty

  val default: JStyler     = JStyler(defaultRect, defaultPalette, defaultFont, withoutBorder)
  val transparent: JStyler = paletteStyler(transparentPalette)

  def styler(rect: Rect, palette: Palette, font: Font, border: Border): JStyler =
    JStyler(rect, palette, font, Some(border))
  def borderStyler(border: Border): JStyler    = JStyler(defaultRect, defaultPalette, defaultFont, Some(border))
  def fontStyler(font: Font): JStyler          = JStyler(defaultRect, defaultPalette, font, withoutBorder)
  def paletteStyler(palette: Palette): JStyler = JStyler(defaultRect, palette, defaultFont, withoutBorder)
  def rectStyler(rect: Rect): JStyler          = JStyler(rect, defaultPalette, defaultFont, withoutBorder)

  def rectPaletteFontStyler(rect: Rect, palette: Palette, font: Font): JStyler =
    JStyler(rect, palette, font, withoutBorder)
  def rectPaletteStyler(rect: Rect, palette: Palette): JStyler =
    JStyler(rect, palette, defaultFont, withoutBorder)

  def modularStyler(using rect: Rect)(using palette: Palette)(using font: Font)(using border: Border): JStyler =
    styler(rect, palette, font, border)
  def modularRectStyler(palette: Palette, font: Font, border: Border)(using rect: Rect): JStyler =
    styler(rect, palette, font, border)
  def modularRectStylerWithoutBorder(palette: Palette, font: Font)(using rect: Rect): JStyler =
    JStyler(rect, palette, font, withoutBorder)
  def modularPaletteStyler(rect: Rect, font: Font, border: Border)(using palette: Palette): JStyler =
    styler(rect, palette, font, border)
  def modularPaletteStylerWithoutBorder(rect: Rect, font: Font)(using palette: Palette): JStyler =
    JStyler(rect, palette, font, withoutBorder)
  def modularFontStyler(rect: Rect, palette: Palette, border: Border)(using font: Font): JStyler =
    styler(rect, palette, font, border)
  def modularBorderStyler(rect: Rect, palette: Palette, font: Font)(using border: Border): JStyler =
    styler(rect, palette, font, border)

  case class JStyler(rect: Rect, palette: Palette, font: Font, border: Option[Border]):
    export rect._, palette._, font._
    val all: List[JStyle] = List(rect, palette, font) ++ border.toList
    val swingFont         = new SwingFont(font.nameFont, font.styleFont.id, font.sizeFont)

  trait JStyle
  private object JStyles:
    val defaultSizeRect: Size       = Dimension2D(Option.empty, Option.empty)
    val defaultPaddingRect: Padding = Dimension2D(0, 0)
    val defaultRoundRect: Int       = 0

    val transparentColor: Color     = new Color(0, 0, 0, 0)
    val defaultColor: Color         = Color.white
    val withOutColor: Option[Color] = Option.empty

    val defaultStroke: Int = 0

    val defaultNameFont: String     = "Lucida Grande"
    val defaultStyleFont: StyleFont = SwingFont.Plain
    val defaultColorFont: Color     = Color.black
    val defaultSizeFont: Int        = 13

    val defaultRect: Rect       = rect(defaultSizeRect, defaultPaddingRect, defaultRoundRect)
    val defaultPalette: Palette = backgroundPalette(defaultColor)
    val defaultFont: Font       = font(defaultNameFont, defaultStyleFont, defaultColorFont, defaultSizeFont)
    val defaultBorder: Border   = border(defaultColor, defaultStroke)

    val transparentPalette: Palette = Palette(transparentColor, withOutColor, withOutColor)

    case class Dimension2D[T](width: T, height: T)
    type Size    = Dimension2D[Option[Int]]
    type Padding = Dimension2D[Int]

    case class Rect(size: Size, padding: Padding, arc: Int)                                     extends JStyle
    case class Palette(background: Color, clickColor: Option[Color], hoverColor: Option[Color]) extends JStyle
    case class Font(nameFont: String, styleFont: StyleFont, colorFont: Color, sizeFont: Int)    extends JStyle
    case class Border(color: Color, stroke: Int)                                                extends JStyle

    def createSize(width: Int, height: Int): Size = Dimension2D(Some(width), Some(height))
    def createWidthSize(width: Int): Size         = Dimension2D(Some(width), defaultSizeRect.height)
    def createHeightSize(height: Int): Size       = Dimension2D(defaultSizeRect.width, Some(height))

    def createPadding(width: Int, height: Int): Padding = Dimension2D(width, height)
    def widthPadding(width: Int): Padding               = createPadding(width, defaultPaddingRect.height)
    def heightPadding(height: Int): Padding             = createPadding(defaultPaddingRect.width, height)

    def rect(size: Size, padding: Padding, arc: Int): Rect = Rect(size, padding, arc)

    def palette(background: Color, click: Color, hover: Color): Palette = Palette(background, Some(click), Some(hover))
    def hoverPalette(hover: Color): Palette           = Palette(defaultColor, withOutColor, Some(hover))
    def clickPalette(click: Color): Palette           = Palette(defaultColor, Some(click), withOutColor)
    def backgroundPalette(background: Color): Palette = Palette(background, withOutColor, withOutColor)

    def backgroundHoverPalette(background: Color, hover: Color): Palette =
      Palette(background, withOutColor, Some(hover))

    def font(name: String, style: StyleFont, color: Color, size: Int): Font = Font(name, style, color, size)
    def sizeFont(size: Int): Font         = font(defaultNameFont, defaultStyleFont, defaultColorFont, size)
    def colorFont(color: Color): Font     = font(defaultNameFont, defaultStyleFont, color, defaultSizeFont)
    def styleFont(style: StyleFont): Font = font(defaultNameFont, style, defaultColorFont, defaultSizeFont)
    def nameFont(name: String): Font      = font(name, defaultStyleFont, defaultColorFont, defaultSizeFont)

    def border(color: Color, stroke: Int): Border = Border(color, stroke)
    def strokeBorder(stroke: Int): Border         = border(defaultColor, stroke)
    def colorBorder(color: Color): Border         = border(color, defaultStroke)

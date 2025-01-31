package ulisse.infrastructures.view.components

import java.awt.Color
import scala.swing.Font as SwingFont
import scala.swing.Font.Style.Value as StyleFont

object JStyler:
  export JStyles._

  val defaultRect: Rect       = rect(defaultRoundRect)
  val defaultPalette: Palette = backgroundPalette(defaultColor)
  val defaultFont: Font       = font(defaultNameFont, defaultStyleFont, defaultSizeFont)

  val defaultBorder: Border         = border(defaultColor, defaultStroke)
  val withoutBorder: Option[Border] = Option.empty

  val defaultStyler: JStyler = JStyler(defaultRect, defaultPalette, defaultFont, withoutBorder)

  def styler(rect: Rect, palette: Palette, font: Font, border: Border): JStyler =
    JStyler(rect, palette, font, Some(border))
  def borderStyler(border: Border): JStyler    = JStyler(defaultRect, defaultPalette, defaultFont, Some(border))
  def fontStyler(font: Font): JStyler          = JStyler(defaultRect, defaultPalette, font, withoutBorder)
  def paletteStyler(palette: Palette): JStyler = JStyler(defaultRect, palette, defaultFont, withoutBorder)
  def rectStyler(rect: Rect): JStyler          = JStyler(rect, defaultPalette, defaultFont, withoutBorder)

  def modularStyler(using rect: Rect)(using palette: Palette)(using font: Font)(using border: Border): JStyler =
    styler(rect, palette, font, border)
  def modularRectStyler(palette: Palette, font: Font, border: Border)(using rect: Rect): JStyler =
    styler(rect, palette, font, border)
  def modularPaletteStyler(rect: Rect, font: Font, border: Border)(using palette: Palette): JStyler =
    styler(rect, palette, font, border)
  def modularFontStyler(rect: Rect, palette: Palette, border: Border)(using font: Font): JStyler =
    styler(rect, palette, font, border)
  def modularBorderStyler(rect: Rect, palette: Palette, font: Font)(using border: Border): JStyler =
    styler(rect, palette, font, border)

  case class JStyler(rect: Rect, palette: Palette, font: Font, border: Option[Border]):
    export rect._, palette._, font._
    val swingFont         = new SwingFont(font.nameFont, font.styleFont.id, font.sizeFont)
    val all: List[JStyle] = List(rect, palette, font) ++ border.toList

    def withBorder(newBorder: Border): JStyler = copy(border = Some(newBorder))

  trait JStyle
  private object JStyles:
    val defaultRoundRect: Int = 0

    val transparentColor: Color     = new Color(0, 0, 0, 0)
    val defaultColor: Color         = Color.white
    val withOutColor: Option[Color] = Option.empty

    val defaultStroke: Int = 1

    val defaultNameFont: String     = "Lucida Grande"
    val defaultStyleFont: StyleFont = SwingFont.Plain
    val defaultSizeFont: Int        = 13

    case class Rect(arcWidth: Int, arcHeight: Int)                                              extends JStyle
    case class Palette(background: Color, clickColor: Option[Color], hoverColor: Option[Color]) extends JStyle
    case class Font(nameFont: String, styleFont: StyleFont, sizeFont: Int)                      extends JStyle
    case class Border(color: Color, stroke: Int)                                                extends JStyle

    def rect(arc: Int): Rect = Rect(arc, arc)

    def modularRect(using arc: Int): Rect = rect(arc)

    def palette(background: Color, click: Color, hover: Color): Palette =
      Palette(background, Some(click), Some(hover))
    def hoverPalette(hover: Color): Palette           = Palette(defaultColor, withOutColor, Some(hover))
    def clickPalette(click: Color): Palette           = Palette(defaultColor, Some(click), withOutColor)
    def backgroundPalette(background: Color): Palette = Palette(background, withOutColor, withOutColor)

    def modularPalette(using background: Color)(using click: Color)(using hover: Color): Palette =
      palette(background, click, hover)
    def modularBackgroundPalette(click: Color, hover: Color)(using background: Color): Palette =
      palette(background, click, hover)
    def modularClickPalette(background: Color, hover: Color)(using click: Color): Palette =
      palette(background, click, hover)
    def modularHoverPalette(background: Color, click: Color)(using hover: Color): Palette =
      palette(background, click, hover)

    def font(name: String, style: StyleFont, size: Int): Font = Font(name, style, size)
    def sizeFont(size: Int): Font                             = font(defaultNameFont, defaultStyleFont, size)
    def styleFont(style: StyleFont): Font                     = font(defaultNameFont, style, defaultSizeFont)
    def nameFont(name: String): Font                          = font(name, defaultStyleFont, defaultSizeFont)

    def modularFont(using name: String)(using style: StyleFont)(size: Int): Font = font(name, style, size)
    def modularNameFont(using name: String)(style: StyleFont, size: Int): Font   = font(name, style, size)
    def modularStyleFont(using style: StyleFont)(name: String, size: Int): Font  = font(name, style, size)
    def modularSizeFont(using size: Int)(name: String, style: StyleFont): Font   = font(name, style, size)

    def border(color: Color, stroke: Int): Border = Border(color, stroke)
    def strokeBorder(stroke: Int): Border         = border(defaultColor, stroke)
    def colorBorder(color: Color): Border         = border(color, defaultStroke)

    def modularBorder(using color: Color)(using stroke: Int): Border = border(color, stroke)
    def modularColorBorder(stroke: Int)(using color: Color): Border  = border(color, stroke)
    def modularStrokeBorder(color: Color)(using stroke: Int): Border = border(color, stroke)

package ulisse.infrastructures.view.components

import java.awt.Color
import scala.swing.Font as SwingFont
import scala.swing.Font.Style.Value as StyleFont

object JStyler:
  export JStyles._
  val withoutBorder: Option[Border] = Option.empty
  val defaultStyler: JStyler        = JStyler(defaultRect, defaultPalette, defaultFont, withoutBorder)

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
    val defaultSizeRect: Option[Size] = Option.empty
    val defaultRoundRect: Int         = 0

    val transparentColor: Color     = new Color(0, 0, 0, 0)
    val defaultColor: Color         = Color.white
    val withOutColor: Option[Color] = Option.empty

    val defaultStroke: Int = 1

    val defaultNameFont: String     = "Lucida Grande"
    val defaultStyleFont: StyleFont = SwingFont.Plain
    val defaultSizeFont: Int        = 13

    val defaultRect: Rect       = roundRect(defaultRoundRect)
    val defaultPalette: Palette = backgroundPalette(defaultColor)
    val defaultFont: Font       = font(defaultNameFont, defaultStyleFont, defaultSizeFont)
    val defaultBorder: Border   = border(defaultColor, defaultStroke)

    case class Size(width: Int, height: Int)
    case class Rect(size: Option[Size], arcWidth: Int, arcHeight: Int)                          extends JStyle
    case class Palette(background: Color, clickColor: Option[Color], hoverColor: Option[Color]) extends JStyle
    case class Font(nameFont: String, styleFont: StyleFont, sizeFont: Int)                      extends JStyle
    case class Border(color: Color, stroke: Int)                                                extends JStyle

    def rect(size: Size, arc: Int): Rect = Rect(Some(size), arc, arc)
    def sizeRect(size: Size): Rect       = rect(size, defaultRoundRect)
    def roundRect(arc: Int): Rect        = Rect(defaultSizeRect, arc, arc)

    def palette(background: Color, click: Color, hover: Color): Palette = Palette(background, Some(click), Some(hover))
    def hoverPalette(hover: Color): Palette           = Palette(defaultColor, withOutColor, Some(hover))
    def clickPalette(click: Color): Palette           = Palette(defaultColor, Some(click), withOutColor)
    def backgroundPalette(background: Color): Palette = Palette(background, withOutColor, withOutColor)

    def font(name: String, style: StyleFont, size: Int): Font = Font(name, style, size)
    def sizeFont(size: Int): Font                             = font(defaultNameFont, defaultStyleFont, size)
    def styleFont(style: StyleFont): Font                     = font(defaultNameFont, style, defaultSizeFont)
    def nameFont(name: String): Font                          = font(name, defaultStyleFont, defaultSizeFont)

    def border(color: Color, stroke: Int): Border = Border(color, stroke)
    def strokeBorder(stroke: Int): Border         = border(defaultColor, stroke)
    def colorBorder(color: Color): Border         = border(color, defaultStroke)

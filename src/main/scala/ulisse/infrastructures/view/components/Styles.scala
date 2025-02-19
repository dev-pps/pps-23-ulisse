package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Themes
import ulisse.utils.{Pair, Swings}

import java.awt.Color
import javax.swing.border.Border as SwingBorder
import scala.swing.Font as SwingFont
import scala.swing.Font.Style.Value as StyleFont

object Styles:
  /** Represent transparent color. */
  val transparentColor: Color = new Color(0, 0, 0, 0)

  /** Default [[Rect]] values. */
  val defaultSizeRect: Size       = Pair(Option.empty, Option.empty)
  val defaultPaddingRect: Padding = Pair(0, 0)
  val defaultRoundRect: Int       = 10

  /** Default [[Palette]] values. */
  val defaultColor: Color         = Themes.Theme.light.element
  val withOutColor: Option[Color] = Option.empty

  /** Default [[Font]] values. */
  val defaultBorderColor: Color = transparentColor
  val defaultStroke: Int        = 0

  /** Default [[Font]] values. */
  val defaultNameFont: String     = "Lucida Grande"
  val defaultStyleFont: StyleFont = SwingFont.Plain
  val defaultColorFont: Color     = Themes.Theme.light.text
  val defaultSizeFont: Int        = 13

  /** Default [[Rect]]. */
  val defaultRect: Rect = Rect(defaultSizeRect, defaultPaddingRect, defaultRoundRect)

  /** Default [[Palette]]. */
  val defaultPalette: Palette = Palette(defaultColor, withOutColor, withOutColor)

  /** Default [[Font]]. */
  val defaultFont: Font = Font(defaultNameFont, defaultStyleFont, defaultColorFont, defaultSizeFont)

  /** Default [[Border]]. */
  val defaultBorder: Border = Border(defaultBorderColor, defaultStroke)

  /** Create a [[Rect]] with the given [[width]], [[height]], [[padding]] and [[arc]]. */
  def createRect(width: Int, height: Int, padding: Int, arc: Int): Rect =
    Rect(Pair(Some(width), Some(height)), Pair(padding, padding), arc)

  /** Create a [[Size]] with the given [[width]] and [[height]]. */
  def createPalette(background: Color, click: Color, hover: Color): Palette =
    Palette(background, Some(click), Some(hover))

  /** Create a [[Font]] with the given [[name]], [[style]], [[color]] and [[size]]. */
  def createFont(name: String, style: StyleFont, color: Color, size: Int): Font = Font(name, style, color, size)

  /** Create a [[Border]] with the given [[color]] and [[stroke]]. */
  def createBorder(color: Color, stroke: Int): Border = Border(color, stroke)

  /** Common trait to represent a style. */
  trait JStyle

  /** [[Size]] represent a dimension with a possible width and height. */
  private type Size = Pair[Option[Int]]

  /** [[Padding]] represent a padding with a width and height. */
  private type Padding = Pair[Int]

  /** Methods to create a [[Size]] with the given [[width]] and [[height]]. */
  def createSize(width: Int, height: Int): Size = Pair(Some(width), Some(height))

  /** Methods to create a [[Padding]] with the given [[width]] and [[height]]. */
  def createPadding(width: Int, height: Int): Padding = Pair(width, height)

  /** Create a [[Rect]] to represent a rounded rectangle with the given [[size]], [[padding]] and [[arc]]. */
  case class Rect(size: Size, padding: Padding, arc: Int) extends JStyle:
    export size.{a as width, b as height}, padding.{withA as withWidthPadding, withB as withHeightPadding}
    val swingPadding: SwingBorder                                = Swings.createEmptyBorder(padding.a, padding.b)
    def withSize(size: Size): Rect                               = copy(size = size)
    def withWidthAndHeight(width: Int, height: Int): Rect        = withSize(createSize(width, height))
    def withWidth(width: Int): Rect                              = copy(size = Pair(Some(width), size.b))
    def withHeight(height: Int): Rect                            = copy(size = Pair(size.a, Some(height)))
    def withPadding(padding: Padding): Rect                      = copy(padding = padding)
    def withPaddingWidthAndHeight(width: Int, height: Int): Rect = withPadding(createPadding(width, height))
    def withArc(arc: Int): Rect                                  = copy(arc = arc)

  /** Create a [[Palette]] to represent a color with the given [[background]], [[click]] and [[hover]]. */
  case class Palette(background: Color, click: Option[Color], hover: Option[Color]) extends JStyle:
    def withBackground(color: Color): Palette = copy(background = color)
    def withClick(color: Color): Palette      = copy(click = Some(color))
    def withHover(color: Color): Palette      = copy(hover = Some(color))

  /** Create a [[Font]] to represent a font with the given [[name]], [[style]], [[color]] and [[size]]. */
  case class Font(name: String, style: StyleFont, color: Color, size: Int) extends JStyle:
    val swingFont: SwingFont              = new SwingFont(name, style.id, size)
    def withName(name: String): Font      = copy(name = name)
    def withStyle(style: StyleFont): Font = copy(style = style)
    def withColor(color: Color): Font     = copy(color = color)
    def withSize(size: Int): Font         = copy(size = size)

  /** Create a [[Border]] to represent a border with the given [[color]] and [[stroke]]. */
  case class Border(color: Color, stroke: Int) extends JStyle:
    val swingBorder: Rect => SwingBorder =
      rect => Swings.createEmptyBorder(rect.padding.a + stroke, rect.padding.b + stroke)
    def withColor(color: Color): Border = copy(color = color)
    def withStroke(stroke: Int): Border = copy(stroke = stroke)

package ulisse.infrastructures.view.components.styles

import ulisse.infrastructures.view.common.Themes
import ulisse.infrastructures.view.common.Themes.{withAlpha, Theme}
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.{CurrentColor, EnhancedLook}
import ulisse.infrastructures.view.utils.Swings
import ulisse.utils.Pair

import java.awt.Color
import javax.swing.border.Border as SwingBorder
import scala.swing.Font as SwingFont
import scala.swing.Font.Style.Value as StyleFont

@SuppressWarnings(Array("org.wartremover.warts.DefaultArguments"))
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

  /** Default [[Border]] values. */
  val defaultStroke: Int = 0

  /** Default [[Font]] values. */
  val defaultNameFont: String     = "Lucida Grande"
  val defaultStyleFont: StyleFont = SwingFont.Plain
  val defaultSizeFont: Int        = 18

  /** Default [[Palette]]. */
  val defaultPalette: Palette         = Palette(defaultColor, withOutColor, withOutColor)
  val transparentPalette: Palette     = defaultPalette.withBackground(transparentColor)
  val defaultPaletteFont: Palette     = defaultPalette.withBackground(Themes.Theme.light.text)
  val closeLabelPalette: Palette      = createPalette(Theme.light.overlay, Theme.light.click, Theme.light.click)
  val openLabelPalette: Palette       = closeLabelPalette.withBackground(Theme.light.background.withAlpha(50))
  val iconClosePalette: Palette       = defaultPalette.withBackground(Theme.light.background)
  val iconOpenPalette: Palette        = createEqualPalette(Theme.light.background).withBackground(Theme.light.overlay)
  val formButtonPalette: Palette      = defaultPalette.withBackground(Theme.light.text).withHover(Theme.light.click)
  val formTrueButtonPalette: Palette  = formButtonPalette.withClick(Theme.light.trueClick)
  val formFalseButtonPalette: Palette = formButtonPalette.withClick(Theme.light.falseClick)

  /** Default [[Rect]]. */
  val defaultRect: Rect         = Rect(defaultSizeRect, defaultPaddingRect, defaultRoundRect)
  val iconButtonRect: Rect      = defaultRect.withPaddingWidthAndHeight(5, 5)
  val panelRect: Rect           = defaultRect.withPaddingWidthAndHeight(15, 15)
  val formTrueButtonRect: Rect  = defaultRect.withPaddingWidthAndHeight(20, 10).withPalette(formTrueButtonPalette)
  val formFalseButtonRect: Rect = defaultRect.withPaddingWidthAndHeight(20, 10).withPalette(formFalseButtonPalette)

  /** Default [[Font]]. */
  val defaultFont: Font        = Font(defaultNameFont, defaultStyleFont, defaultSizeFont, defaultPaletteFont)
  val titleDashboardFont: Font = defaultFont.withSize(19).withStyle(SwingFont.Bold)
  val titleFormFont: Font      = titleDashboardFont.withSize(36)
  val whiteFont: Font          = defaultFont.withPalette(defaultPalette.withBackground(Theme.light.background))

  /** Default [[Border]]. */
  val defaultBorder: Border = Border(defaultStroke)

  /** Create a [[Rect]] with the given [[width]], [[height]], [[padding]] and [[arc]]. */
  def createRect(width: Int, height: Int, padding: Int, arc: Int): Rect =
    Rect(Pair(Some(width), Some(height)), Pair(padding, padding), arc)

  /** Create a [[Palette]] with the given [[background]], [[click]] and [[hover]]. */
  def createPalette(background: Color, click: Color, hover: Color): Palette =
    Palette(background, Some(click), Some(hover))

  /** Create a [[Palette]] with the given [[background]]. */
  def createBackgroundPalette(background: Color): Palette = Palette(background, withOutColor, withOutColor)

  /** Create a [[Palette]] with the given [[color]] for all the colors. */
  def createEqualPalette(color: Color): Palette = Palette(color, Some(color), Some(color))

  /** Create a [[Font]] with the given [[name]], [[style]] and [[size]]. */
  def createFont(name: String, style: StyleFont, color: Color, size: Int): Font = Font(name, style, size)

  /** Create a [[Border]] with the given [[color]] and [[stroke]]. */
  def createBorder(stroke: Int): Border = Border(stroke)

  /** Common trait to represent a colorable style. */
  trait Colorable:
    val palette: Palette

  /** Common trait to represent a style. */
  trait Style

  /** [[Size]] represent a dimension with a possible width and height. */
  private type Size = Pair[Option[Int]]

  /** [[Padding]] represent a padding with a width and height. */
  type Padding = Pair[Int]

  /** Methods to create a [[Size]] with the given [[width]] and [[height]]. */
  def createSize(width: Int, height: Int): Size = Pair(Some(width), Some(height))

  /** Methods to create a [[Padding]] with the given [[width]] and [[height]]. */
  def createPadding(width: Int, height: Int): Padding = Pair(width, height)

  /** Create a [[Palette]] to represent a color with the given [[background]], [[clickColor]] and [[hoverColor]]. */
  case class Palette(background: Color, clickColor: Option[Color], hoverColor: Option[Color]) extends Style:
    /** Return a new [[Palette]] with the given [[background]]. */
    def withBackground(color: Color): Palette = copy(background = color)

    /** Return a new [[Palette]] with the given [[clickColor]]. */
    def withClick(color: Color): Palette = copy(clickColor = Some(color))

    /** Return a new [[Palette]] with the given [[hoverColor]]. */
    def withHover(color: Color): Palette = copy(hoverColor = Some(color))

  /** Create a [[Rect]] to represent a rounded rectangle with the given [[size]], [[padding]], [[arc]] and [[palette]]. */
  case class Rect(size: Size, padding: Padding, arc: Int, palette: Palette = defaultPalette) extends Style
      with Colorable:
    export size.{a as width, b as height}, padding.{withA as withWidthPadding, withB as withHeightPadding}, palette._

    /** Swing padding of the rect. */
    val swingPadding: SwingBorder = Swings.createEmptyBorder(padding.a, padding.b)

    /** Set the [[size]] of the rect with the given [[size]]. */
    def withSize(size: Size): Rect = copy(size = size)

    /** Set the [[size]] of the rect with the given [[width]] and [[height]]. */
    def withWidthAndHeight(width: Int, height: Int): Rect = withSize(createSize(width, height))

    /** Set the [[size]] of the rect with the given [[width]] and maintain the [[height]]. */
    def withWidth(width: Int): Rect = copy(size = Pair(Some(width), size.b))

    /** Set the [[size]] of the rect with the given [[height]] and maintain the [[width]]. */
    def withHeight(height: Int): Rect = copy(size = Pair(size.a, Some(height)))

    /** Set the [[padding]] of the rect with the given [[padding]]. */
    def withPadding(padding: Padding): Rect = copy(padding = padding)

    /** Set the [[padding]] of the rect with the given [[width]] and [[height]]. */
    def withPaddingWidthAndHeight(width: Int, height: Int): Rect = withPadding(createPadding(width, height))

    /** Set the [[arc]] of the rect with the given [[arc]]. */
    def withArc(arc: Int): Rect = copy(arc = arc)

    /** Set the [[palette]] of the rect with the given [[palette]]. */
    def withPalette(palette: Palette): Rect = copy(palette = palette)

  /** Create a [[Font]] to represent a font with the given [[name]], [[style]], [[size]] and [[palette]]. */
  case class Font(name: String, style: StyleFont, size: Int, palette: Palette = defaultPalette) extends Style
      with Colorable:
    export palette._

    /** Swing font of the font. */
    val swingFont: SwingFont = new SwingFont(name, style.id, size)

    /** Set the [[name]] of the font with the given [[name]]. */
    def withName(name: String): Font = copy(name = name)

    /** Set the [[style]] of the font with the given [[style]]. */
    def withStyle(style: StyleFont): Font = copy(style = style)

    /** Set the [[size]] of the font with the given [[size]]. */
    def withSize(size: Int): Font = copy(size = size)

    /** Set the [[palette]] of the font with the given [[palette]]. */
    def withPalette(palette: Palette): Font = copy(palette = palette)

  /** Create a [[Border]] to represent a border with the given [[stroke]] and [[palette]]. */
  case class Border(stroke: Int, palette: Palette = defaultPalette) extends Style with Colorable:
    export palette._

    /** Swing border of the border. */
    val swingBorder: Rect => SwingBorder =
      rect => Swings.createEmptyBorder(rect.padding.a + stroke, rect.padding.b + stroke)

    /** Set the [[stroke]] of the border with the given [[stroke]]. */
    def withStroke(stroke: Int): Border = copy(stroke = stroke)

    /** Set the [[palette]] of the border with the given [[palette]]. */
    def withPalette(palette: Palette): Border = copy(palette = palette)

  /** Utility object to set up [[EnhancedLook]] from [[Style]] values. */
  object EnhancedLookExtensions:

    /** Extension methods for [[EnhancedLook]] to update the style with the given [[style]]. */
    extension (component: EnhancedLook)

      /** Update the current color of the component with the given [[colorable]]. */
      def updateCurrentColor(colorable: Colorable, current: CurrentColor): Unit =
        current.current = colorable.palette.background
        component.updateGraphics()

      /** Update the shape of the component with the given [[rect]]. */
      def updateRect(rect: Rect): Unit =
        val width  = rect.width.getOrElse(component.size.width)
        val height = rect.height.getOrElse(component.size.height)
        component.size.setSize(width, height)
        component.border = rect.swingPadding
        component.updateGraphics()

      /** Update the font of the component with the given [[font]]. */
      def updateFont(font: Font): Unit =
        component.font = font.swingFont
        component.foreground = font.palette.background
        component.updateGraphics()

      /** Update the border of the component with the given [[border]]. */
      def updateBorder(rect: Rect, border: Border): Unit =
        component.border = border.swingBorder(rect)
        component.updateGraphics()

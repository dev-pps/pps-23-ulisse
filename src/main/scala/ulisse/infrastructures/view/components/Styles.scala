package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Themes
import ulisse.infrastructures.view.components.SwingEnhancements.EnhancedLook
import ulisse.utils.{Pair, Swings}

import java.awt.Color
import javax.swing.border.Border as SwingBorder
import scala.swing.Font.Style.Value as StyleFont
import scala.swing.{event, Font as SwingFont, Reactions, Swing}

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
  val defaultSizeFont: Int        = 13

  /** Default [[Palette]]. */
  val defaultPalette: Palette     = Palette(defaultColor, withOutColor, withOutColor)
  val defaultPaletteFont: Palette = defaultPalette.withBackground(Themes.Theme.light.text)

  /** Default [[Rect]]. */
  val defaultRect: Rect = Rect(defaultSizeRect, defaultPaddingRect, defaultRoundRect)

  /** Default [[Font]]. */
  val defaultFont: Font = Font(defaultNameFont, defaultStyleFont, defaultSizeFont, defaultPaletteFont)

  /** Default [[Border]]. */
  val defaultBorder: Border = Border(defaultStroke)

  /** Create a [[Rect]] with the given [[width]], [[height]], [[padding]] and [[arc]]. */
  def createRect(width: Int, height: Int, padding: Int, arc: Int): Rect =
    Rect(Pair(Some(width), Some(height)), Pair(padding, padding), arc)

  /** Create a [[Size]] with the given [[width]] and [[height]]. */
  def createPalette(background: Color, click: Color, hover: Color): Palette =
    Palette(background, Some(click), Some(hover))

  /** Create a [[Font]] with the given [[name]], [[style]] and [[size]]. */
  def createFont(name: String, style: StyleFont, color: Color, size: Int): Font = Font(name, style, size)

  /** Create a [[Border]] with the given [[color]] and [[stroke]]. */
  def createBorder(stroke: Int): Border = Border(stroke)

  /** Common trait to represent a style. */
  trait Style

  /** [[Size]] represent a dimension with a possible width and height. */
  private type Size = Pair[Option[Int]]

  /** [[Padding]] represent a padding with a width and height. */
  private type Padding = Pair[Int]

  /** Methods to create a [[Size]] with the given [[width]] and [[height]]. */
  def createSize(width: Int, height: Int): Size = Pair(Some(width), Some(height))

  /** Methods to create a [[Padding]] with the given [[width]] and [[height]]. */
  def createPadding(width: Int, height: Int): Padding = Pair(width, height)

  /** Create a [[Palette]] to represent a color with the given [[background]], [[clickColor]] and [[hoverColor]]. */
  case class Palette(background: Color, clickColor: Option[Color], hoverColor: Option[Color]) extends Style:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _currentColor: Color = background

    def currentColor: Color                        = _currentColor
    private def currentColor_=(color: Color): Unit = _currentColor = color
    def withBackground(color: Color): Palette      = copy(background = color)
    def withClickColor(color: Color): Palette      = copy(clickColor = Some(color))
    def withHoverColor(color: Color): Palette      = copy(hoverColor = Some(color))
    def hoverAction(): Unit                        = hoverColor.foreach(currentColor = _)
    def exitAction(): Unit                         = hoverColor.foreach(_ => currentColor = background)
    def clickAction(): Unit                        = clickColor.foreach(currentColor = _)
    def releaseAction(): Unit                      = clickColor.foreach(_ => currentColor = background)

  /** Create a [[Rect]] to represent a rounded rectangle with the given [[size]], [[padding]], [[arc]] and [[palette]]. */
  case class Rect(size: Size, padding: Padding, arc: Int, palette: Palette = defaultPalette) extends Style:
    export size.{a as width, b as height}, padding.{withA as withWidthPadding, withB as withHeightPadding}, palette._
    val swingPadding: SwingBorder                                = Swings.createEmptyBorder(padding.a, padding.b)
    def withSize(size: Size): Rect                               = copy(size = size)
    def withWidthAndHeight(width: Int, height: Int): Rect        = withSize(createSize(width, height))
    def withWidth(width: Int): Rect                              = copy(size = Pair(Some(width), size.b))
    def withHeight(height: Int): Rect                            = copy(size = Pair(size.a, Some(height)))
    def withPadding(padding: Padding): Rect                      = copy(padding = padding)
    def withPaddingWidthAndHeight(width: Int, height: Int): Rect = withPadding(createPadding(width, height))
    def withArc(arc: Int): Rect                                  = copy(arc = arc)
    def withPalette(palette: Palette): Rect                      = copy(palette = palette)

  /** Create a [[Font]] to represent a font with the given [[name]], [[style]], [[size]] and [[palette]]. */
  case class Font(name: String, style: StyleFont, size: Int, palette: Palette = defaultPalette) extends Style:
    export palette._
    val swingFont: SwingFont                = new SwingFont(name, style.id, size)
    def withName(name: String): Font        = copy(name = name)
    def withStyle(style: StyleFont): Font   = copy(style = style)
    def withSize(size: Int): Font           = copy(size = size)
    def withPalette(palette: Palette): Font = copy(palette = palette)

  /** Create a [[Border]] to represent a border with the given [[stroke]] and [[palette]]. */
  case class Border(stroke: Int, palette: Palette = defaultPalette) extends Style:
    export palette._
    val swingBorder: Rect => SwingBorder =
      rect => Swings.createEmptyBorder(rect.padding.a + stroke, rect.padding.b + stroke)
    def withStroke(stroke: Int): Border       = copy(stroke = stroke)
    def withPalette(palette: Palette): Border = copy(palette = palette)

  /** Utility object to set up [[EnhancedLook]] from [[Style]] values. */
  object EnhancedLookExtension:
    extension (component: EnhancedLook)

      /** Initialize the color reactions of the component with the given [[palette]]. */
      def initColorReactions(palette: () => Palette): Reactions.Reaction =
        case _: event.MouseEntered  => { palette().hoverAction(); component.updateGraphics() }
        case _: event.MouseExited   => { palette().exitAction(); component.updateGraphics() }
        case _: event.MousePressed  => { palette().clickAction(); component.updateGraphics() }
        case _: event.MouseReleased => { palette().releaseAction(); component.updateGraphics() }

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
        component.updateGraphics()

      /** Update the border of the component with the given [[border]]. */
      def updateBorder(rect: Rect, border: Border): Unit =
        component.border = border.swingBorder(rect)
        component.updateGraphics()

package ulisse.infrastructures.view.components.decorators

import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.components.styles.Styles.EnhancedLookExtensions.*
import ulisse.infrastructures.view.components.styles.Styles.Palette

import java.awt.geom.RoundRectangle2D
import java.awt.{BasicStroke, Color, RenderingHints}
import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object SwingEnhancements:

  /** Represent the current color of the component. */
  final case class CurrentColor(private var _current: Color):
    def current: Color                                    = _current
    def current_=(color: Color): Unit                     = _current = color
    private def hoverColor(palette: Styles.Palette): Unit = palette.hoverColor.foreach(current = _)
    private def clickColor(palette: Styles.Palette): Unit = palette.clickColor.foreach(current = _)
    private def exitColor(palette: Styles.Palette): Unit =
      palette.hoverColor.foreach(_ => current = palette.background)
    private def releaseColor(palette: Styles.Palette): Unit =
      palette.clickColor.foreach(_ => current = palette.background)

    /** Initialize the color reactions of the component. */
    def initColorReactions(component: EnhancedLook, palette: () => Palette): Reactions.Reaction =
      case _: event.MousePressed  => clickColor(palette()); component.updateGraphics()
      case _: event.MouseReleased => releaseColor(palette()); component.updateGraphics()
      case _: event.MouseEntered  => hoverColor(palette()); component.updateGraphics()
      case _: event.MouseExited   => exitColor(palette()); component.updateGraphics()

  /** Base trait decorator to enhanced look of swing [[Component]] */
  trait EnhancedLook extends Component:
    opaque = false
    listenTo(mouseEvents: _*)

    /** Read-only property to get the mouse events of the component. */
    def mouseEvents: List[Publisher] = List(mouse.moves, mouse.clicks)

    /** Update the graphic component. */
    def updateGraphics(): Unit =
      revalidate()
      repaint()

    /** Paint the custom appearance of the component. */
    protected def paintLook(g: Graphics2D): Unit = ()

    /** Paint the component with antialiasing, then paint the custom appearance and finally paint the component. */
    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
      paintLook(g)
      super.paintComponent(g)

  /** Trait to enhance the shape of swing component and [[_rect]] control shape params. */
  trait ShapeEffect extends EnhancedLook:
    private var _rect: Styles.Rect         = Styles.defaultRect
    private val currentColor: CurrentColor = CurrentColor(rectPalette.background)

    this.updateRect(rect)
    reactions += currentColor.initColorReactions(this, () => rectPalette)

    /** Read-only property to get the shape of the component. */
    def rect: Styles.Rect = _rect

    /** Change the shape of the component. */
    def rect_=(newRect: Styles.Rect): Unit = { _rect = newRect; this.updateRect(rect) }

    /** Read-only property to get the shape palette of the component. */
    def rectPalette: Styles.Palette = rect.palette

    /** Change the shape palette of the component. */
    def rectPalette_=(palette: Styles.Palette): Unit =
      rect = rect.withPalette(palette)
      this.updateCurrentColor(rect, currentColor)

    /** Read-only property to get the shape padding of the component. */
    def rectPadding: Styles.Padding = rect.padding

    /** Change the shape padding of the component. */
    def rectPadding_=(padding: Styles.Padding): Unit = rect = rect.withPadding(padding)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      g.setColor(currentColor.current)
      val clipShape =
        new RoundRectangle2D.Float(0, 0, size.width.toFloat, size.height.toFloat, rect.arc.toFloat, rect.arc.toFloat)
      g.setClip(clipShape)
      g.fillRoundRect(0, 0, size.width, size.height, rect.arc, rect.arc)

  /** Trait to enhance the font of swing component and [[_font]] control font params. */
  trait FontEffect extends EnhancedLook:
    private var _font: Styles.Font         = Styles.defaultFont
    private val currentColor: CurrentColor = CurrentColor(fontEffect.background)

    this.updateFont(fontEffect)
    reactions += currentColor.initColorReactions(this, () => fontPalette)

    /** Read-only property to get the font effect of the component. */
    def fontEffect: Styles.Font = _font

    /** Change the font effect of the component. */
    def fontEffect_=(newFont: Styles.Font): Unit = { (_font = newFont); this.updateFont(fontEffect) }

    /** Read-only property to get the font palette of the component. */
    def fontPalette: Styles.Palette = fontEffect.palette

    /** Change the font palette of the component. */
    def fontPalette_=(palette: Styles.Palette): Unit =
      fontEffect = fontEffect.withPalette(palette)
      this.updateCurrentColor(fontEffect, currentColor)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      foreground = currentColor.current

  /** Trait to enhance the border of swing component and [[_border]] control border params. */
  trait BorderEffect extends EnhancedLook:
    self: ShapeEffect =>
    private var _border: Styles.Border     = Styles.defaultBorder
    private val currentColor: CurrentColor = CurrentColor(borderEffect.background)

    this.updateBorder(rect, Styles.defaultBorder)
    reactions += currentColor.initColorReactions(this, () => borderPalette)

    /** Read-only property to get the border of the component. */
    def borderEffect: Styles.Border = _border

    /** Change the border of the component. */
    def borderEffect_=(border: Styles.Border): Unit = { _border = border; this.updateBorder(rect, borderEffect) }

    /** Read-only property to get the border palette of the component. */
    def borderPalette: Styles.Palette = borderEffect.palette

    /** Change the border palette of the component. */
    def borderPalette_=(palette: Styles.Palette): Unit =
      borderEffect = borderEffect.withPalette(palette)
      this.updateCurrentColor(borderEffect, currentColor)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      val position   = borderEffect.stroke / 2
      val borderSize = (size.width - borderEffect.stroke, size.height - borderEffect.stroke)
      g.setColor(currentColor.current)
      g.setStroke(BasicStroke(borderEffect.stroke.toFloat))
      g.drawRoundRect(position, position, borderSize._1, borderSize._2, rect.arc, rect.arc)

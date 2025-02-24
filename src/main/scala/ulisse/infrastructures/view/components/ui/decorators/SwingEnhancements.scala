package ulisse.infrastructures.view.components.ui.decorators

import ulisse.infrastructures.view.components.ui.decorators.Styles.EnhancedLookExtension.*

import java.awt.geom.RoundRectangle2D
import java.awt.{BasicStroke, RenderingHints}
import scala.swing.{Component, Graphics2D, Publisher}

object SwingEnhancements:
  /** Base trait decorator to enhanced look of swing [[Component]] */
  trait EnhancedLook extends Component:
    opaque = false

    /** Read-only property to get the mouse events of the component. */
    def mouseEvents: List[Publisher] = List(mouse.moves, mouse.clicks)

    /** Update the graphic component. */
    def updateGraphics(): Unit =
//      revalidate()
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
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _rect: Styles.Rect = Styles.defaultRect

    this.updateRect(rect)
    listenTo(mouseEvents: _*)
    reactions += this.initColorReactions(() => rectPalette)

    /** Read-only property to get the shape of the component. */
    def rect: Styles.Rect = _rect

    /** Change the shape of the component. */
    def rect_=(newRect: Styles.Rect): Unit = { _rect = newRect; this.updateRect(rect) }

    def rectPalette: Styles.Palette                  = rect.palette
    def rectPalette_=(palette: Styles.Palette): Unit = rect = rect.withPalette(palette)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      g.setColor(rect.currentColor)
      val clipShape =
        new RoundRectangle2D.Float(0, 0, size.width.toFloat, size.height.toFloat, rect.arc.toFloat, rect.arc.toFloat)
      g.setClip(clipShape)
      g.fillRoundRect(0, 0, size.width, size.height, rect.arc, rect.arc)

  /** Trait to enhance the font of swing component and [[_font]] control font params. */
  trait FontEffect extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _font: Styles.Font = Styles.defaultFont

    this.updateFont(fontEffect)
    listenTo(mouseEvents: _*)
    reactions += this.initColorReactions(() => fontPalette)

    /** Read-only property to get the font effect of the component. */
    def fontEffect: Styles.Font = _font

    /** Change the font effect of the component. */
    def fontEffect_=(newFont: Styles.Font): Unit = { (_font = newFont); this.updateFont(fontEffect) }

    def fontPalette: Styles.Palette                  = fontEffect.palette
    def fontPalette_=(palette: Styles.Palette): Unit = fontEffect = fontEffect.withPalette(palette)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      foreground = fontEffect.currentColor

  /** Trait to enhance the border of swing component and [[_border]] control border params. */
  trait BorderEffect extends EnhancedLook:
    self: ShapeEffect =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _border: Styles.Border = Styles.defaultBorder

    this.updateBorder(rect, Styles.defaultBorder)
    listenTo(mouseEvents: _*)
    reactions += this.initColorReactions(() => rectPalette)

    /** Read-only property to get the border of the component. */
    def borderEffect: Styles.Border = _border

    /** Change the border of the component. */
    def borderEffect_=(border: Styles.Border): Unit = { (_border = border); this.updateBorder(rect, borderEffect) }

    def borderPalette: Styles.Palette                  = borderEffect.palette
    def borderPalette_=(palette: Styles.Palette): Unit = borderEffect = borderEffect.withPalette(palette)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      val position   = borderEffect.stroke / 2
      val borderSize = (size.width - borderEffect.stroke, size.height - borderEffect.stroke)
      g.setColor(borderEffect.currentColor)
      g.setStroke(BasicStroke(borderEffect.stroke.toFloat))
      g.drawRoundRect(position, position, borderSize._1, borderSize._2, rect.arc, rect.arc)

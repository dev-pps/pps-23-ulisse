package ulisse.infrastructures.view.components

import java.awt.{BasicStroke, RenderingHints}
import scala.swing.{event, Component, Graphics2D}

object SwingEnhancements:
  /** Base trait decorator to enhanced look of swing [[Component]] */
  trait EnhancedLook extends Component:
    opaque = false

    /** Paint the custom appearance of the component. */
    protected def paintLook(g: Graphics2D): Unit = ()

    /** Paint the component with antialiasing, then paint the custom appearance and finally paint the component. */
    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintLook(g)
      super.paintComponent(g)

  /** Trait to enhance the shape of swing component and [[_rect]] control shape params. */
  trait ShapeEffect extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _rect: Styles.Rect = initRect(Styles.defaultRect)

    private def initRect(newRect: Styles.Rect): Styles.Rect =
      val width  = newRect.width.getOrElse(size.width)
      val height = newRect.height.getOrElse(size.height)
      size.setSize(width, height)
      border = newRect.swingPadding
      revalidate()
      repaint()
      newRect

    /** Read-only property to get the shape of the component. */
    def rect: Styles.Rect = _rect

    /** Change the shape of the component. */
    def rect_=(newRect: Styles.Rect): Unit = _rect = initRect(newRect)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      g.setColor(background)
      g.fillRoundRect(0, 0, size.width, size.height, rect.arc, rect.arc)

  /** Trait to enhance the color of swing component and [[_palette]] control color params. */
  trait ColorEffect extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _palette: Styles.Palette = initColor(Styles.defaultPalette)
    listenTo(mouse.moves, mouse.clicks)

    private def initColor(newPalette: Styles.Palette): Styles.Palette =
      background = newPalette.background
      revalidate()
      repaint()
      newPalette

    /** Read-only property to get the color of the component. */
    def palette: Styles.Palette = _palette

    /** Change the color of the component. */
    def palette_=(newPalette: Styles.Palette): Unit = _palette = initColor(newPalette)

    reactions += {
      case event.MouseEntered(_, _, _)        => palette.hover.foreach(background = _)
      case event.MouseExited(_, _, _)         => palette.hover.foreach(_ => background = palette.background)
      case event.MousePressed(_, _, _, _, _)  => palette.click.foreach(background = _)
      case event.MouseReleased(_, _, _, _, _) => palette.click.foreach(_ => background = palette.background)
    }

    override protected def paintLook(g: Graphics2D): Unit =
      g.setColor(background)
      super.paintLook(g)

  /** Trait to enhance the font of swing component and [[_font]] control font params. */
  trait FontEffect extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _font: Styles.Font = initFont(Styles.defaultFont)

    private def initFont(newFont: Styles.Font): Styles.Font =
      font = newFont.swingFont
      foreground = newFont.color
      revalidate()
      repaint()
      newFont

    def fontEffect: Styles.Font                  = _font
    def fontEffect_=(newFont: Styles.Font): Unit = _font = initFont(newFont)

  /** Trait to enhance the border of swing component and [[_border]] control border params. */
  trait BorderEffect extends EnhancedLook:
    self: ShapeEffect =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _border: Styles.Border = initBorder(Styles.defaultBorder)

    private def initBorder(newBorder: Styles.Border): Styles.Border =
      border = newBorder.swingBorder(rect)
      revalidate()
      repaint()
      newBorder

    /** Read-only property to get the border of the component. */
    def borderEffect: Styles.Border = _border

    /** Change the border of the component. */
    def borderEffect_=(newBorder: Styles.Border): Unit = _border = initBorder(newBorder)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      val position   = borderEffect.stroke / 2
      val borderSize = (size.width - borderEffect.stroke, size.height - borderEffect.stroke)
      g.setColor(borderEffect.color)
      g.setStroke(BasicStroke(borderEffect.stroke.toFloat))
      g.drawRoundRect(position, position, borderSize._1, borderSize._2, rect.arc, rect.arc)

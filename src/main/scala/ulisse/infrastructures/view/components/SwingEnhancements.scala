package ulisse.infrastructures.view.components

import java.awt.{BasicStroke, RenderingHints}
import scala.swing.{event, Component, Graphics2D}

object SwingEnhancements:
  /** Base trait decorator to enhanced look of swing [[Component]] */
  trait EnhancedLook extends Component:
    self: Component =>
    opaque = false

    /** Paint the custom appearance of the component. */
    protected def paintLook(g: Graphics2D): Unit = ()

    /** Paint the custom appearance of the border. */
    protected def paintBorderLook(g: Graphics2D): Unit = ()

    /** Paint the component with antialiasing, then paint the custom appearance and finally paint the component. */
    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintLook(g)
      super.paintComponent(g)

    /** Paint the border with antialiasing, then paint the custom appearance and finally paint the border. */
    override protected def paintBorder(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBorderLook(g)
      super.paintBorder(g)

  /** Trait to enhance the shape of swing component and [[_rect]] control shape params. */
  trait ShapeEffect extends EnhancedLook:
    self: EnhancedLook =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _rect: JStyler.Rect = initRect(JStyler.defaultRect)

    private def initRect(newRect: JStyler.Rect): JStyler.Rect =
      val width  = newRect.size.width.getOrElse(size.width)
      val height = newRect.size.height.getOrElse(size.height)
      size.setSize(width, height)
      border = newRect.swingPadding
      revalidate()
      repaint()
      newRect.withSize(width, height)

    /** Read-only property to get the shape of the component. */
    def rect: JStyler.Rect = _rect

    /** Change the shape of the component. */
    def rect_=(newRect: JStyler.Rect): Unit = _rect = initRect(newRect)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      g.setColor(background)
      g.fillRoundRect(0, 0, size.width, size.height, rect.arc, rect.arc)

  /** Trait to enhance the color of swing component and [[_palette]] control color params. */
  trait ColorEffect extends EnhancedLook:
    self: EnhancedLook =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _palette: JStyler.Palette = initColor(JStyler.transparentPalette)
    listenTo(mouse.moves, mouse.clicks)

    private def initColor(newPalette: JStyler.Palette): JStyler.Palette =
      background = newPalette.background
      revalidate()
      repaint()
      newPalette

    /** Read-only property to get the color of the component. */
    def palette: JStyler.Palette = _palette

    /** Change the color of the component. */
    def palette_=(newPalette: JStyler.Palette): Unit = _palette = initColor(newPalette)

    reactions += {
      case event.MouseEntered(_, _, _)        => palette.hoverColor.foreach(background = _)
      case event.MouseExited(_, _, _)         => palette.hoverColor.foreach(_ => background = palette.background)
      case event.MousePressed(_, _, _, _, _)  => palette.clickColor.foreach(background = _)
      case event.MouseReleased(_, _, _, _, _) => palette.clickColor.foreach(_ => background = palette.background)
    }

    override protected def paintLook(g: Graphics2D): Unit =
      g.setColor(background)
      super.paintLook(g)

  /** Trait to enhance the font of swing component and [[_font]] control font params. */
  trait FontEffect extends EnhancedLook:
    self: EnhancedLook =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _font: JStyler.Font = initFont(JStyler.defaultFont)

    private def initFont(newFont: JStyler.Font): JStyler.Font =
      font = newFont.swingFont
      foreground = newFont.colorFont
      revalidate()
      repaint()
      newFont

    def fontEffect: JStyler.Font                  = _font
    def fontEffect_=(newFont: JStyler.Font): Unit = _font = initFont(newFont)

  /** Trait to enhance the border of swing component and [[_border]] control border params. */
  trait BorderEffect extends EnhancedLook:
    self: EnhancedLook with ShapeEffect =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _border: JStyler.Border = initBorder(JStyler.defaultBorder)

    private def initBorder(newBorder: JStyler.Border): JStyler.Border =
      border = newBorder.swingBorder(rect)
      revalidate()
      repaint()
      newBorder

    /** Read-only property to get the border of the component. */
    def borderEffect: JStyler.Border = _border

    /** Change the border of the component. */
    def borderEffect_=(newBorder: JStyler.Border): Unit = _border = initBorder(newBorder)

    override protected def paintBorderLook(g: Graphics2D): Unit =
      super.paintBorderLook(g)
      val position   = borderEffect.stroke / 2
      val borderSize = (size.width - borderEffect.stroke, size.height - borderEffect.stroke)
      g.setColor(borderEffect.color)
      g.setStroke(BasicStroke(borderEffect.stroke.toFloat))
      g.drawRoundRect(position, position, borderSize._1, borderSize._2, rect.arc, rect.arc)

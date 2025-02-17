package ulisse.infrastructures.view.components

import java.awt.{BasicStroke, RenderingHints}
import javax.swing.BorderFactory
import scala.swing.{event, Color, Component, Graphics2D}

object SwingEnhancements:

  trait EnhancedEffect[A]

  trait RectEffect:
    component: Component =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _rect: JStyler.Rect = JStyler.defaultRect

    private def initRect(): Unit =
      val width  = rect.size.width.getOrElse(size.width)
      val height = rect.size.height.getOrElse(size.height)
      size.setSize(width, height)

    def rect: JStyler.Rect = _rect
    def rect_=(newRect: JStyler.Rect): Unit =
      _rect = newRect
      initRect()
      revalidate()
      repaint()

    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.fillRoundRect(0, 0, size.width, size.height, rect.arc, rect.arc)
      component.paintComponent(g)

  trait ColorEffect:
    component: Component =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _palette: JStyler.Palette = JStyler.defaultPalette
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _currentColor = JStyler.transparentColor
    listenTo(mouse.moves, mouse.clicks)
    currentColor = palette.background

    def palette: JStyler.Palette = _palette
    def palette_=(newPalette: JStyler.Palette): Unit =
      _palette = newPalette
      _currentColor = _palette.background

    private def currentColor: Color = _currentColor
    private def currentColor_=(color: Color): Unit =
      _currentColor = color
      repaint()

    reactions += {
      case event.MouseEntered(_, _, _)        => _palette.hoverColor.foreach(currentColor = _)
      case event.MouseExited(_, _, _)         => _palette.hoverColor.foreach(_ => currentColor = _palette.background)
      case event.MousePressed(_, _, _, _, _)  => _palette.clickColor.foreach(currentColor = _)
      case event.MouseReleased(_, _, _, _, _) => _palette.clickColor.foreach(_ => currentColor = _palette.background)
    }

    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g.setColor(currentColor)
      component.paintComponent(g)

  trait FontEffect:
    component: Component =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _font: JStyler.Font = JStyler.defaultFont

    def font: JStyler.Font = _font
    def font_=(newFont: JStyler.Font): Unit =
      _font = newFont
      component.font = font.swingFont
      component.foreground = font.colorFont
      repaint()

  trait BorderEffect:
    component: Component with RectEffect =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _borderEffect: JStyler.Border = JStyler.defaultBorder

    private def initBorder(): Unit =
      border = BorderFactory.createEmptyBorder(
        rect.padding.height,
        rect.padding.width,
        rect.padding.height,
        rect.padding.width
      )

    def borderEffect: JStyler.Border = _borderEffect
    def borderEffect_=(newBorder: JStyler.Border): Unit =
      _borderEffect = newBorder
      repaint()

    def paintBorder(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      val borderPosition = borderEffect.stroke / 2
      val borderSize     = (size.width - borderEffect.stroke, size.height - borderEffect.stroke)
      g.setColor(borderEffect.color)
      g.setStroke(new BasicStroke(borderEffect.stroke))
      g.drawRoundRect(borderPosition, borderPosition, borderSize._1, borderSize._2, rect.arc, rect.arc)
      component.paintBorder(g)

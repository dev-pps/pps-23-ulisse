package ulisse.infrastructures.view.components

import java.awt.{BasicStroke, RenderingHints}
import javax.swing.BorderFactory
import scala.swing.{event, Component, Graphics2D}

object SwingEnhancements:
  // usato decorator + mixin:
  // 1- idea iniziale usare classico incapsulamento del decorator, non funziona perche
  // nel momento della composizione della grafica, il componente non viene aggiornato, anche se estendo da component
  // perdo il riferimento dell'oggetto che sto decorando
  // 2- idea mixin con self-type però non posso overridare i metodi di paintComponent e paintBorder,
  // non basta solo il mixin e estendere da component, devo riuscire ad avere un unica estensione da component
  // 3- devo estendere per forza da component per riuscire a lavorare con la grafica di swing, dato che le
  // due funzioni sono protected, quindi dentro il trait che estende da component, faccio code injection per
  // modificare la grafica mantendendo il riferimento e logica della grafica del componente
  // decorator pattern con mixin, grazie ai trait posso comporre la grafica del componente in modo custom

  trait Enhanced extends Component with EnhancedLook:
    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintLook(g)
      super.paintComponent(g)

    override protected def paintBorder(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintBorderLook(g)
      super.paintBorder(g)

  trait EnhancedLook:
    self: Component =>
    opaque = false
    def paintLook(g: Graphics2D): Unit       = ()
    def paintBorderLook(g: Graphics2D): Unit = ()

  trait BaseEnhancedLook[T]:
    self: Component =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    protected var _effect: T
    protected def init(newEffect: T): T =
      revalidate()
      repaint()
      newEffect

    protected def effect: T                    = _effect
    protected def effect_=(newEffect: T): Unit = _effect = init(newEffect)

  trait RectEffect extends EnhancedLook:
    self: Enhanced =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _rect: JStyler.Rect = JStyler.defaultRect
    initRect(rect)

    private def initRect(newRect: JStyler.Rect): JStyler.Rect =
      val width  = newRect.size.width.getOrElse(size.width)
      val height = newRect.size.height.getOrElse(size.height)
      size.setSize(width, height)
      border = newRect.swingPadding
      revalidate()
      repaint()
      newRect

    def rect: JStyler.Rect                  = _rect
    def rect_=(newRect: JStyler.Rect): Unit = _rect = initRect(newRect)

    override def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      g.setColor(background)
      g.fillRoundRect(0, 0, size.width, size.height, rect.arc, rect.arc)

  trait ColorEffect extends EnhancedLook:
    self: Enhanced =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _palette: JStyler.Palette = JStyler.transparentPalette
    listenTo(mouse.moves, mouse.clicks)
    initColor(palette)

    private def initColor(newPalette: JStyler.Palette): JStyler.Palette =
      background = newPalette.background
      revalidate()
      repaint()
      newPalette

    def palette: JStyler.Palette                     = _palette
    def palette_=(newPalette: JStyler.Palette): Unit = _palette = initColor(newPalette)

    reactions += {
      case event.MouseEntered(_, _, _)        => palette.hoverColor.foreach(background = _)
      case event.MouseExited(_, _, _)         => palette.hoverColor.foreach(_ => background = palette.background)
      case event.MousePressed(_, _, _, _, _)  => palette.clickColor.foreach(background = _)
      case event.MouseReleased(_, _, _, _, _) => palette.clickColor.foreach(_ => background = palette.background)
    }

    override def paintLook(g: Graphics2D): Unit =
      g.setColor(background)
      super.paintLook(g)

  trait FontEffect extends EnhancedLook:
    self: Enhanced =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _font: JStyler.Font = JStyler.defaultFont
    initFont(fontEffect)

    private def initFont(newFont: JStyler.Font): JStyler.Font =
      font = newFont.swingFont
      foreground = newFont.colorFont
      revalidate()
      repaint()
      newFont

    def fontEffect: JStyler.Font                  = _font
    def fontEffect_=(newFont: JStyler.Font): Unit = _font = initFont(newFont)

  trait BorderEffect extends EnhancedLook:
    self: Enhanced with RectEffect =>
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _border: JStyler.Border = JStyler.defaultBorder
    initBorder(borderEffect)

    private def initBorder(newBorder: JStyler.Border): JStyler.Border =
      border = BorderFactory.createEmptyBorder(
        rect.padding.height,
        rect.padding.width,
        rect.padding.height,
        rect.padding.width
      )
      revalidate()
      repaint()
      newBorder

    def borderEffect: JStyler.Border                    = _border
    def borderEffect_=(newBorder: JStyler.Border): Unit = _border = initBorder(newBorder)

    override def paintBorderLook(g: Graphics2D): Unit =
      super.paintBorderLook(g)
      val position   = borderEffect.stroke / 2
      val borderSize = (size.width - borderEffect.stroke, size.height - borderEffect.stroke)
      g.setColor(borderEffect.color)
      g.setStroke(BasicStroke(borderEffect.stroke))
      g.drawRoundRect(position, position, borderSize._1, borderSize._2, rect.arc, rect.arc)

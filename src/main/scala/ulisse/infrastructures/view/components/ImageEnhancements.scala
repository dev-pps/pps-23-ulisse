package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import ulisse.infrastructures.view.components.ui.decorators.Styles

import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import scala.swing.{event, Component, Graphics2D}

object ImageEnhancements:

  trait ImageEnhanced extends Component:
    self: Component =>
    opaque = false

    protected def paintImage(g: Graphics2D): Unit = ()

    override protected def paintComponent(g: Graphics2D): Unit =
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      paintImage(g)
      super.paintComponent(g)

  trait ImageEffect() extends ImageEnhanced:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _image: Option[BufferedImage] = Option.empty

    private def initImage(path: String): Option[BufferedImage] =
      try
        val buffer = ImageIO.read(ClassLoader.getSystemResource(path))
        revalidate()
        repaint()
        Some(buffer)
      catch case _: Exception => Option.empty

    def image: Option[BufferedImage] = _image
    def image_=(path: String): Unit  = _image = initImage(path)

    override protected def paintImage(g: Graphics2D): Unit =
      super.paintImage(g)
      // tornare indietro basta l'affine transform
      // g.rotate(math.toRadians(rotation), size.width / 2, size.getHeight / 2)
      image.foreach(image =>
        val imgSize = math.min(size.width, size.height)
        g.drawImage(image, (size.width - imgSize) / 2, (size.height - imgSize) / 2, imgSize, imgSize, peer)
      )

  trait SVGEffect extends ImageEnhanced:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _svgIcon: Option[FlatSVGIcon] = Option.empty
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _palette: Styles.Palette = Styles.defaultPalette

    listenTo(mouse.moves, mouse.clicks)

    private def initSVG(path: String): Option[FlatSVGIcon] =
      try
        ImageIO.read(ClassLoader.getSystemResource(path))
        revalidate()
        repaint()
        Some(new FlatSVGIcon(path))
      catch case _: Exception => Option.empty

    private def initColor(newPalette: Styles.Palette): Styles.Palette =
      background = newPalette.background
      svgIcon.foreach(_.setColorFilter(FlatSVGIcon.ColorFilter(_ => background)))
      revalidate()
      repaint()
      newPalette

    def svgIcon: Option[FlatSVGIcon]                = _svgIcon
    def svgIcon_=(path: String): Unit               = _svgIcon = initSVG(path)
    def palette: Styles.Palette                     = _palette
    def palette_=(newPalette: Styles.Palette): Unit = _palette = initColor(newPalette)

    reactions += {
      case _: event.MouseEntered  => palette.hoverColor.foreach(background = _)
      case _: event.MouseExited   => palette.hoverColor.foreach(_ => background = palette.background)
      case _: event.MousePressed  => palette.clickColor.foreach(background = _)
      case _: event.MouseReleased => palette.clickColor.foreach(_ => background = palette.background)
    }

    override protected def paintImage(g: Graphics2D): Unit =
      super.paintImage(g)

//      svgIcon.foreach(icon =>
//
//        val imgSize = math.min(size.width, size.height)
//        val icon    = icon.derive(imgSize, imgSize)
//        icon.paintIcon(peer, g, (size.width - icon.getWidth) / 2, (size.height - icon.getHeight) / 2)
//      )

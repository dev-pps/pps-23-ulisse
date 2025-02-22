package ulisse.infrastructures.view.components.ui.decorators

import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ui.decorators.Images.{Picture, SVGIcon}
import ulisse.infrastructures.view.components.ui.decorators.Styles.EnhancedLookExtension.*
import ulisse.infrastructures.view.components.ui.decorators.SwingEnhancements.EnhancedLook

import scala.swing.{Component, Graphics2D}

object ImageEffects:

  trait ImageEffect() extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _picture: Picture = Images.defaultPicture

    updateGraphics()

    def picture: Picture              = _picture
    def picture_=(path: String): Unit = { _picture = picture.withPath(path); updateGraphics() }

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      picture.bufferImage.foreach(image =>
        val imgSize = math.min(size.width, size.height)
        g.drawImage(image, (size.width - imgSize) / 2, (size.height - imgSize) / 2, imgSize, imgSize, peer)
      )

  trait SVGEffect extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _svgIcon: SVGIcon = Images.defaultSVGIcon

    updateGraphics()
    listenTo(mouse.moves, mouse.clicks)
    reactions += this.initColorReactions(() => svgIconPalette)

    def svgIcon: SVGIcon              = _svgIcon
    def svgIcon_=(path: String): Unit = _svgIcon = svgIcon.withPath(path)

    def svgIconPalette: Styles.Palette                  = svgIcon.palette
    def svgIconPalette_=(palette: Styles.Palette): Unit = { _svgIcon = svgIcon.withPalette(palette); updateGraphics() }

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      svgIcon.icon.foreach(svg =>
        svg.setColorFilter(ColorFilter(_ => svgIconPalette.currentColor))
        val imgSize = math.min(size.width, size.height)
        val icon    = svg.derive(imgSize, imgSize)
        icon.paintIcon(peer, g, (size.width - icon.getWidth) / 2, (size.height - icon.getHeight) / 2)
      )

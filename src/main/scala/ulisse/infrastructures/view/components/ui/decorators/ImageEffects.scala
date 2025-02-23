package ulisse.infrastructures.view.components.ui.decorators

import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ui.decorators.Images.{Picture, SVGIcon}
import ulisse.infrastructures.view.components.ui.decorators.Styles.EnhancedLookExtension.*
import ulisse.infrastructures.view.components.ui.decorators.SwingEnhancements.EnhancedLook

import java.awt.geom.AffineTransform
import scala.swing.{Component, Graphics2D}

object ImageEffects:
  private val identityTransform: AffineTransform = new AffineTransform()

  trait ImageEffect() extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _picture: Picture = Images.defaultPicture

    updateGraphics()

    def picture: Picture               = _picture
    def picture_=(path: String): Unit  = { _picture = picture.withPath(path); updateGraphics() }
    def withRotation(angle: Int): Unit = { _picture = picture.withRotation(angle); updateGraphics() }

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      picture.bufferImage.foreach(image =>
        val imgSize = math.min(size.width, size.height)
        g.rotate(picture.toRadians, size.width / 2, size.height / 2)
        g.drawImage(image, (size.width - imgSize) / 2, (size.height - imgSize) / 2, imgSize, imgSize, peer)
        g.setTransform(identityTransform)
      )

  trait SVGEffect extends EnhancedLook:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var _svgIcon: SVGIcon = Images.defaultSVGIcon

    updateGraphics()
    listenTo(mouse.moves, mouse.clicks)
    reactions += this.initColorReactions(() => svgIconPalette)

    def svgIcon: SVGIcon               = _svgIcon
    def svgIcon_=(path: String): Unit  = _svgIcon = svgIcon.withPath(path)
    def withRotation(angle: Int): Unit = { _svgIcon = svgIcon.withRotation(angle); updateGraphics() }

    def svgIconPalette: Styles.Palette                  = svgIcon.palette
    def svgIconPalette_=(palette: Styles.Palette): Unit = { _svgIcon = svgIcon.withPalette(palette); updateGraphics() }

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      svgIcon.icon.foreach(svg =>
        svg.setColorFilter(ColorFilter(_ => svgIconPalette.currentColor))
        val imgSize = math.min(size.width, size.height)
        val icon    = svg.derive(imgSize, imgSize)
        g.rotate(svgIcon.toRadians, size.width / 2, size.height / 2)
        icon.paintIcon(peer, g, (size.width - icon.getWidth) / 2, (size.height - icon.getHeight) / 2)
        g.setTransform(identityTransform)
      )

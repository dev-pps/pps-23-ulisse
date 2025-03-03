package ulisse.infrastructures.view.components.decorators

import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook
import ulisse.infrastructures.view.components.styles.Images.{Picture, SVGIcon}
import ulisse.infrastructures.view.components.styles.Styles.EnhancedLookExtensions.*
import ulisse.infrastructures.view.components.styles.{CurrentColor, Images, Styles}

import java.awt.geom.{AffineTransform, RoundRectangle2D}
import scala.swing.{Component, Graphics2D}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object ImageEffects:
  private val identityTransform: AffineTransform = new AffineTransform()

  /** Represent an image effect. */
  trait ImageEffect extends EnhancedLook:
    /** Represent angle of the image. */
    def angle: Int

    /** Represent a picture effect. */
    def withRotation(angle: Int): Unit

  /** Represent a picture effect. */
  trait PictureEffect() extends ImageEffect:
    private var _picture: Picture = Images.defaultPicture

    updateGraphics()

    override def angle: Int                     = picture.rotation.angle
    override def withRotation(angle: Int): Unit = { _picture = picture.withRotation(angle); updateGraphics() }

    /** Represent a picture. */
    def picture: Picture = _picture

    /** Represent the arc of the picture. */
    def arc: Int = picture.arc

    /** Set the path of the picture. */
    def picture_=(path: String): Unit = { _picture = picture.withPath(path); updateGraphics() }

    /** Set the arc of the picture. */
    def withArc(arc: Int): Unit = { _picture = picture.withArc(arc); updateGraphics() }

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      picture.bufferImage.foreach(image =>
        val imgSize = math.min(size.width, size.height)
        g.rotate(picture.toRadians, size.width / 2, size.height / 2)
        val clipShape =
          new RoundRectangle2D.Float(0, 0, size.width.toFloat, size.height.toFloat, arc.toFloat, arc.toFloat)
        g.setClip(clipShape)
        g.drawImage(image, (size.width - imgSize) / 2, (size.height - imgSize) / 2, imgSize, imgSize, peer)
        g.setTransform(identityTransform)
      )

  /** Represent an SVG effect. */
  trait SVGEffect extends ImageEffect:
    private var _svgIcon: SVGIcon       = Images.defaultSVGIcon
    private val colorable: CurrentColor = CurrentColor(svgIconPalette.background)

    updateGraphics()
    listenTo(mouse.moves, mouse.clicks)
    reactions += colorable.initColorReactions(this, () => svgIconPalette)

    override def angle: Int                     = svgIcon.rotation.angle
    override def withRotation(angle: Int): Unit = { _svgIcon = svgIcon.withRotation(angle); updateGraphics() }

    /** Represent an SVG icon. */
    def svgIcon: SVGIcon = _svgIcon

    /** Set the path of the SVG icon. */
    def svgIcon_=(path: String): Unit = { _svgIcon = svgIcon.withPath(path); updateGraphics() }

    /** Represent the palette of the SVG icon. */
    def svgIconPalette: Styles.Palette = svgIcon.palette

    /** Set the palette of the SVG icon. */
    def svgIconPalette_=(palette: Styles.Palette): Unit =
      _svgIcon = svgIcon.withPalette(palette)
      this.updateCurrentColor(svgIcon, colorable)

    override protected def paintLook(g: Graphics2D): Unit =
      super.paintLook(g)
      svgIcon.icon.foreach(svg =>
        svg.setColorFilter(ColorFilter(_ => colorable.current))
        val imgSize = math.min(size.width, size.height)
        val icon    = svg.derive(imgSize, imgSize)
        g.rotate(svgIcon.toRadians, size.width / 2, size.height / 2)
        icon.paintIcon(peer, g, (size.width - icon.getWidth) / 2, (size.height - icon.getHeight) / 2)
        g.setTransform(identityTransform)
      )

package ulisse.infrastructures.view.components.ui.decorators

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ui.decorators.Styles.Palette

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Images:
  val defaultAngle: Int = 0

  val defaultPicture: Picture = new Picture("", defaultAngle)
  val defaultSVGIcon: SVGIcon = new SVGIcon("", defaultAngle, Styles.defaultPalette)

  /** Represent a generic image. */
  trait Image:
    val source: SourceImage
    val rotation: Rotation

  /** Represent a source image with [[path]]. */
  case class SourceImage(path: String):
    val bufferImage: Option[BufferedImage] =
      try Some(ImageIO.read(ClassLoader.getSystemResource(path)))
      catch case _: Exception => None

    def withPath(newPath: String): SourceImage = copy(path = newPath)

  /** Represent a rotation with [[angle]]. */
  case class Rotation(angle: Int):
    def toRadians: Double                  = math.toRadians(angle)
    def withAngle(newAngle: Int): Rotation = copy(angle = newAngle)

  /** Represent a generic image with a [[SourceImage]]. */
  case class Picture(source: SourceImage, rotation: Rotation) extends Image:
    def this(path: String, angle: Int) = this(SourceImage(path), Rotation(angle))
    export source.bufferImage, rotation.toRadians

    def withPath(path: String): Picture   = copy(source = source.withPath(path))
    def withRotation(angle: Int): Picture = copy(rotation = Rotation(angle))

  /** Represent an SVG image with [[SourceImage]] and [[Palette]]. */
  case class SVGIcon(source: SourceImage, rotation: Rotation, palette: Palette) extends Image:
    def this(path: String, angle: Int, palette: Palette) = this(SourceImage(path), Rotation(angle), palette)
    val icon: Option[FlatSVGIcon] = source.bufferImage.map(_ => new FlatSVGIcon(source.path))

    export source.bufferImage, rotation.toRadians
    icon.foreach(_.setColorFilter(ColorFilter(_ => palette.currentColor)))

    def withPath(path: String): SVGIcon           = copy(source = source.withPath(path))
    def withRotation(angle: Int): SVGIcon         = copy(rotation = Rotation(angle))
    def withPalette(newPalette: Palette): SVGIcon = copy(palette = newPalette)

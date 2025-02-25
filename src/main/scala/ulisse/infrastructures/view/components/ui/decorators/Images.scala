package ulisse.infrastructures.view.components.ui.decorators

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ui.decorators.Styles.Palette

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import scala.util.Try

object Images:
  /** Default angle for images. */
  val defaultAngle: Int = 0

  /** Default picture with [[defaultAngle]] and [[Styles.defaultRoundRect]]. */
  val defaultPicture: Picture = new Picture("", defaultAngle, Styles.defaultRoundRect)

  /** Default SVG icon with [[defaultAngle]] and [[Styles.defaultPalette]]. */
  val defaultSVGIcon: SVGIcon = new SVGIcon("", defaultAngle, Styles.defaultPalette)

  /** Represent a generic image. */
  trait Image:
    /** Source image of the image. */
    val source: SourceImage

    /** Rotation of the image. */
    val rotation: Rotation

  /** Represent a source image with [[path]]. */
  case class SourceImage(path: String):
    /** Value of [[path]] as a [[BufferedImage]]. */
    val bufferImage: Option[BufferedImage] = Try(ImageIO.read(ClassLoader.getSystemResource(path))).toOption

    /** Return a new [[SourceImage]] with [[newPath]]. */
    def withPath(newPath: String): SourceImage = copy(path = newPath)

  /** Represent a rotation with [[angle]]. */
  case class Rotation(angle: Int):
    /** Value of [[angle]] in radians. */
    def toRadians: Double = math.toRadians(angle)

    /** Return a new [[Rotation]] with [[newAngle]]. */
    def withAngle(newAngle: Int): Rotation = copy(angle = newAngle)

  /** Represent a generic image with a [[SourceImage]], [[Rotation]] and [[arc]]. */
  case class Picture(source: SourceImage, rotation: Rotation, arc: Int) extends Image:
    def this(path: String, angle: Int, arc: Int) = this(SourceImage(path), Rotation(angle), arc)
    export source.bufferImage, rotation.toRadians

    /** Return a new [[Picture]] with [[path]]. */
    def withPath(path: String): Picture = copy(source = source.withPath(path))

    /** Return a new [[Picture]] with [[angle]]. */
    def withRotation(angle: Int): Picture = copy(rotation = Rotation(angle))

    /** Return a new [[Picture]] with [[arc]]. */
    def withArc(arc: Int): Picture = copy(arc = arc)

  /** Represent an SVG image with [[SourceImage]], [[Rotation]] and [[Palette]]. */
  case class SVGIcon(source: SourceImage, rotation: Rotation, palette: Palette) extends Image:
    def this(path: String, angle: Int, palette: Palette) = this(SourceImage(path), Rotation(angle), palette)

    /** Icon of the svg image. */
    val icon: Option[FlatSVGIcon] = source.bufferImage.map(_ => new FlatSVGIcon(source.path))
    icon.foreach(_.setColorFilter(ColorFilter(_ => palette.currentColor)))

    export source.bufferImage, rotation.toRadians

    /** Return a new [[SVGIcon]] with [[path]]. */
    def withPath(path: String): SVGIcon = copy(source = source.withPath(path))

    /** Return a new [[SVGIcon]] with [[angle]]. */
    def withRotation(angle: Int): SVGIcon = copy(rotation = Rotation(angle))

    /** Return a new [[SVGIcon]] with [[palette]]. */
    def withPalette(newPalette: Palette): SVGIcon = copy(palette = newPalette)

package ulisse.infrastructures.view.components.ui.decorators

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ui.decorators.Styles.Palette

import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object Images:

  val defaultPicture: Picture = new Picture("")
  val defaultSVGIcon: SVGIcon = new SVGIcon("", Styles.defaultPalette)

  /** Represent a generic image. */
  trait Image:
    val source: SourceImage

  /** Represent a source image with [[path]]. */
  case class SourceImage(path: String):
    val bufferImage: Option[BufferedImage] =
      try Some(ImageIO.read(ClassLoader.getSystemResource(path)))
      catch case _: Exception => None

    def withPath(newPath: String): SourceImage = copy(path = newPath)

  /** Represent a generic image with a [[SourceImage]]. */
  case class Picture(source: SourceImage) extends Image:
    def this(path: String) = this(SourceImage(path))
    export source.bufferImage

    def withPath(path: String): Picture = copy(source = source.withPath(path))

  /** Represent an SVG image with [[SourceImage]] and [[Palette]]. */
  case class SVGIcon(source: SourceImage, palette: Palette) extends Image:
    def this(path: String, palette: Palette) = this(SourceImage(path), palette)
    val icon: Option[FlatSVGIcon] = source.bufferImage.map(_ => new FlatSVGIcon(source.path))

    export source.bufferImage
    icon.foreach(_.setColorFilter(ColorFilter(_ => palette.currentColor)))

    def withPath(path: String): SVGIcon           = copy(source = source.withPath(path))
    def withPalette(newPalette: Palette): SVGIcon = copy(palette = newPalette)

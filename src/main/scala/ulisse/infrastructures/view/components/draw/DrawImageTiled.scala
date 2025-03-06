package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.components.draw.DrawImages.defaultDimension
import ulisse.infrastructures.view.components.styles.Images
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.geom.AffineTransform
import java.awt.image.ImageObserver
import scala.math.{abs, sqrt}
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images tiled on the screen. */
trait DrawImageTiled:

  /** Draw the tiled image on the screen. */
  def drawTiledImage(g: Graphics2D, scale: Double, observer: ImageObserver): Unit

/** Companion object for [[DrawImageTiled]]. */
object DrawImageTiled:

  /** Create a new [[DrawImageTiled]]. */
  def apply(path: String, start: Point, end: Point, dimension: Dimension): DrawImageTiled =
    new DrawImageTiledImpl(path, start, end, dimension)

  /** Create a new [[DrawImageTiled]] with the default dimension. */
  def createAt(path: String, start: Point, end: Point): DrawImageTiled =
    DrawImageTiled(path, start, end, defaultDimension)

  private case class DrawImageTiledImpl(source: SourceImage, start: Point, end: Point, dimension: Dimension)
      extends DrawImageTiled:

    def this(path: String, start: Point, end: Point, dimension: Dimension) =
      this(SourceImage(path), start, end, dimension)

    def drawTiledImage(g: Graphics2D, scale: Double, observer: ImageObserver): Unit =
      for img <- source.bufferImage
      yield
        val scaleDim = (img.getWidth(observer) * scale, img.getHeight(observer) * scale)
        val rotate   = start angle end
        val diagonal = sqrt(scaleDim._1 * scaleDim._1 + scaleDim._2 * scaleDim._2)

        val positions: Seq[(Double, Double)] =
          val dx       = end.x - start.x
          val dy       = end.y - start.y
          val distance = start distance end

          val correctedStep = diagonal - abs(diagonal - scaleDim._1)
          val stepX         = (dx / distance) * correctedStep
          val stepY         = (dy / distance) * correctedStep

          val x = start.x until end.x by stepX.toInt
          val y = start.y until end.y by stepY.toInt
          (x zip y).map((x, y) => (x.toDouble, y.toDouble))

        positions.foreach((x, y) =>
          val transform = new AffineTransform()
          transform translate (x, y)
          transform scale (scale, scale)
          transform rotate rotate
          transform translate (-scaleDim._1 / 2, -scaleDim._2 / 2)
          g drawImage (img, transform, observer)
        )

package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages.{defaultDimension, DrawImage}
import ulisse.infrastructures.view.components.styles.Images
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.ImageObserver
import scala.math.{abs, sqrt}
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images tiled on the screen. */
trait DrawImageTiled extends DrawImage:

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

    private val image: DrawImageSimple              = DrawImageSimple(source.path, start, dimension)
    override val center: Point                      = start plus end times 0.5
    override val observable: Observable[MouseEvent] = image.observable

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var width = 0

    scale = 0.05

    export image.{center => _, dimension => _, draw => _, observable => _, _}

    override def onMove(data: MouseEvent): Unit =
      if data.point.isPointInRotatedRectangle(start, end, width) then
        println("CIAO")

    override def draw(g: Graphics2D, observer: ImageObserver): Unit =
      drawTiledImage(g, scale, observer)

    def drawTiledImage(g: Graphics2D, scale: Double, observer: ImageObserver): Unit =
      for img <- source.bufferImage
      yield
        val scaleDim = new Dimension((img.getWidth(observer) * scale).toInt, (img.getHeight(observer) * scale).toInt)
        val rotate   = start angle end
        val diagonal = sqrt(scaleDim.width * scaleDim.width + scaleDim.height * scaleDim.height)

        width = scaleDim.width

        g.setColor(Color.red)
        g.fillOval(center.x, center.y, 5, 5)

        g.setColor(Color.blue)
        g.fillOval(start.x, start.y, 5, 5)

        g.setColor(Color.green)
        g.fillOval(end.x, end.y, 5, 5)

        val positions: Seq[(Double, Double)] =
          val dx       = end.x - start.x
          val dy       = end.y - start.y
          val distance = start distance end

          val correctedStep = diagonal - abs(diagonal - scaleDim.width)
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
          transform translate (-scaleDim.width, -scaleDim.height)
          g drawImage (img, transform, observer)
        )

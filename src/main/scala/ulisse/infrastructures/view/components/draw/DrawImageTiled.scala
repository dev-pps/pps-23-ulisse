package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages.{defaultDimension, defaultScaleSilhouette, DrawImage}
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.components.styles.{CurrentColor, Images, Styles}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.{BufferedImage, ImageObserver}
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

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class DrawImageTiledImpl(source: SourceImage, start: Point, end: Point, dimension: Dimension)
      extends DrawImageTiled:
    def this(path: String, start: Point, end: Point, dimension: Dimension) =
      this(SourceImage(path), start, end, dimension)

    private val silhouette: Option[BufferedImage] =
      source.bufferImage.map(image => BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB))

    override val center: Point                      = start plus end times 0.5
    override val observable: Observable[MouseEvent] = Observers.createObservable[MouseEvent]

    private var _scale: Float                      = defaultScaleSilhouette
    private var _silhouettePalette: Styles.Palette = Styles.silhouettePalette
    private val silhouetteColor: CurrentColor      = CurrentColor(_silhouettePalette.background)

    private var width = 0
    scale = 0.05

    export observable._

    override def scale: Float = _scale

    override def scale_=(value: Float): Unit = _scale = value

    override def silhouettePalette: Styles.Palette = _silhouettePalette

    override def silhouettePalette_=(palette: Styles.Palette): Unit =
      _silhouettePalette = palette
      silhouetteColor.current = palette.background

    private def isCollide(point: Point): Boolean =
      val a = new Point(this.start.x - (width / 2), this.start.y)
      val b = new Point(this.end.x - (width / 2), this.end.y)
      point.isPointInRotatedRectangle(a, b, width)

    override def onMove(data: MouseEvent): Unit =
      if isCollide(data.point) then
        silhouetteColor.hoverColor(silhouettePalette)
        observable.notifyHover(data)
        observable.notifyMove(data)
      else
        silhouetteColor.exitColor(silhouettePalette)
        observable.notifyExit(data)

    override def onClick(data: MouseEvent): Unit =
      if isCollide(data.point) then
        silhouetteColor.clickColor(silhouettePalette)
        observable.notifyClick(data)

    override def onRelease(data: MouseEvent): Unit =
      if isCollide(data.point) then
        silhouetteColor.releaseColor(silhouettePalette)
        observable.notifyRelease(data)

    override def draw(g: Graphics2D, observer: ImageObserver): Unit =
      drawTiledImage(g, scale, observer)

    def drawTiledImage(g: Graphics2D, scale: Double, observer: ImageObserver): Unit =
      for img <- source.bufferImage
      yield
        val silhouetteScale = scale + 0.01
        val silhouetteDim   = new Dimension(img.getWidth(observer), img.getHeight(observer)) plus silhouetteScale
        val dimImage        = new Dimension(img.getWidth(observer), img.getHeight(observer))
        val scaleDim        = dimImage times scale

        val start = new Point(this.start.x - (scaleDim.width / 2), this.start.y)
        val end   = new Point(this.end.x - (scaleDim.width / 2), this.end.y)

        val rotate   = start angle end
        val diagonal = sqrt(scaleDim.width * scaleDim.width + scaleDim.height * scaleDim.height)

        width = scaleDim.width

        g.setColor(Color.red)
        g.fillOval(center.x, center.y, 5, 5)

        g.setColor(Color.blue)
//        g.fillOval(a.x, a.y, 5, 5)
        g.setColor(Color.magenta)
        g.fillOval(this.start.x, this.start.y, 5, 5)

        g.setColor(Color.green)
//        g.fillOval(b.x, b.y, 5, 5)
        g.setColor(Color.cyan)
        g.fillOval(this.end.x, this.end.y, 5, 5)

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
          val transformSilhouette = new AffineTransform()
          transformSilhouette translate (x, y)
          transformSilhouette scale (silhouetteScale, silhouetteScale)
          transformSilhouette rotate rotate
          val dimS = silhouetteDim divide 2
          transformSilhouette translate (-scaleDim.width, -scaleDim.height)

          if silhouetteColor.current != Styles.transparentColor then
            silhouette.foreach(silhouette =>
              setupSilhouette(observer)
              g.drawImage(silhouette, transformSilhouette, observer)
            )

          val transform = new AffineTransform()
          transform translate (x, y)
          transform scale (scale, scale)
          transform rotate rotate
          val dim = scaleDim divide 2
          transform translate (-dim.width, 0)
          g drawImage (img, transform, observer)
        )

    private def setupSilhouette(observer: ImageObserver): Unit =
      source.bufferImage.foreach(image =>
        silhouette.foreach(silhouette =>
          val graphics = silhouette.createGraphics()
          graphics.drawImage(image, 0, 0, observer)
          graphics.setComposite(java.awt.AlphaComposite.SrcAtop)
          graphics.setColor(silhouetteColor.current)
          graphics.fillRect(0, 0, image.getWidth, image.getHeight)
          graphics.dispose()
        )
      )

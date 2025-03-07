package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages.{defaultDimension, defaultScaleSilhouette, DrawImage}
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.components.styles.{CurrentColor, Images, Styles}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.Color
import java.awt.image.ImageObserver
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images tiled on the screen. */
trait DrawImageTiled extends DrawImage

/** Companion object for [[DrawImageTiled]]. */
object DrawImageTiled:

  /** Default dimension for the image. */
  private val defaultRouteDimension: Dimension = new Dimension(12, 12)

  /** Create a new [[DrawImageTiled]]. */
  def apply(start: Point, end: Point, dimension: Dimension, color: Color): DrawImageTiled =
    new DrawImageTiledImpl(start, end, dimension, color)

  /** Create a new [[DrawImageTiled]] with the default dimension. */
  def createAt(start: Point, end: Point, color: Color): DrawImageTiled =
    DrawImageTiled(start, end, defaultRouteDimension, color)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class DrawImageTiledImpl(
      source: SourceImage,
      start: Point,
      end: Point,
      dimension: Dimension,
      color: Color
  ) extends DrawImageTiled:
    def this(start: Point, end: Point, dimension: Dimension, color: Color) =
      this(SourceImage(""), start, end, dimension, color)

    override val center: Point                      = start plus end times 0.5
    override val observable: Observable[MouseEvent] = Observers.createObservable[MouseEvent]

    private var _scale: Float                      = defaultScaleSilhouette
    private var _silhouettePalette: Styles.Palette = Styles.silhouettePalette
    private val silhouetteColor: CurrentColor      = CurrentColor(_silhouettePalette.background)

    export observable._

    override def scale: Float = _scale

    override def scale_=(value: Float): Unit = _scale = value

    override def silhouettePalette: Styles.Palette = _silhouettePalette

    override def silhouettePalette_=(palette: Styles.Palette): Unit =
      _silhouettePalette = palette
      silhouetteColor.current = palette.background

    private def isCollide(point: Point): Boolean =
      point.isPointInRotatedRectangle(start, end, dimension.width)

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
      val dir   = (end minus start).toPointDouble.normalize
      val width = dimension.width

      val positions =
        val distance = start distance end
        val nStep    = distance / width
        val i        = 0 until (nStep.toInt + 1)
        i.map(i =>
          val step = (dir times (width * i)).toPoint
          start plus step
        )

      positions.foreach(point =>
        val (x, y)              = (point.x, point.y)
        val silhouetteWidth     = width + 4
        val halfWidth           = width / 2
        val silhouetteHalfWidth = silhouetteWidth / 2

        if silhouetteColor.current != Styles.transparentColor then
          g.setColor(silhouetteColor.current)
          g.fillOval(x - silhouetteHalfWidth, y - silhouetteHalfWidth, silhouetteWidth, silhouetteWidth)

        g.setColor(color)
        g.fillOval(x - halfWidth, y - halfWidth, width, width)
      )

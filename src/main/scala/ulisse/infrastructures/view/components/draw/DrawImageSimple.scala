package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages.{defaultDimension, defaultScaleSilhouette, DrawImage}
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.components.styles.{CurrentColor, Styles}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.image.{BufferedImage, ImageObserver}
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw an image with a silhouette. */
trait DrawImageSimple extends DrawImage

/** Companion object for [[DrawImage]]. */
object DrawImageSimple:

  /** Create a new [[DrawImage]] with path, position and dimension. */
  def apply(path: String, position: Point, dimension: Dimension): DrawImageSimple =
    new DrawImageImpl(path, position, dimension)

  /** Create a new [[DrawImage]] with path and point. */
  def createAt(path: String, point: Point): DrawImageSimple = DrawImageSimple(path, point, defaultDimension)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private final case class DrawImageImpl(source: SourceImage, position: Point, dimension: Dimension)
      extends DrawImageSimple:
    def this(path: String, position: Point, dimension: Dimension) = this(SourceImage(path), position, dimension)

    private val silhouette: Option[BufferedImage] =
      source.bufferImage.map(image => BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB))

    override val center: Point                      = position minus (dimension.toPoint times 0.5)
    override val observable: Observable[MouseEvent] = Observers.createObservable[MouseEvent]
    private var _scale: Float                       = defaultScaleSilhouette
    private var _silhouettePalette: Styles.Palette  = Styles.silhouettePalette
    private val silhouetteColor: CurrentColor       = CurrentColor(_silhouettePalette.background)

    export observable._

    override def scale: Float = _scale

    override def scale_=(value: Float): Unit = _scale = value

    override def silhouettePalette: Styles.Palette = _silhouettePalette

    override def silhouettePalette_=(palette: Styles.Palette): Unit =
      _silhouettePalette = palette
      silhouetteColor.current = palette.background

    override def onMove(data: MouseEvent): Unit =
      if data.point.hasCollided(this) then
        silhouetteColor.hoverColor(silhouettePalette)
        observable.notifyHover(data)
        observable.notifyMove(data)
      else
        silhouetteColor.exitColor(silhouettePalette)
        observable.notifyExit(data)

    override def onClick(data: MouseEvent): Unit =
      if data.point.hasCollided(this) then
        silhouetteColor.clickColor(silhouettePalette)
        observable.notifyClick(data)

    override def onRelease(data: MouseEvent): Unit =
      if data.point.hasCollided(this) then
        silhouetteColor.releaseColor(silhouettePalette)
        observable.notifyRelease(data)

    override def draw(g: Graphics2D, observer: ImageObserver): Unit =
      if silhouetteColor.current != Styles.transparentColor then
        silhouette.foreach(silhouette =>
          setupSilhouette(observer)
          val (pos, size) = (center, dimension).scaleOf(scale)
          g.drawImage(silhouette, pos.x, pos.y, size.width, size.height, observer)
        )
      source.bufferImage.foreach(g.drawImage(_, center.x, center.y, dimension.width, dimension.height, observer))

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

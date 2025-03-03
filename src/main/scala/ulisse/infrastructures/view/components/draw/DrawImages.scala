package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.components.styles.{CurrentColor, Images, Styles}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.image.{BufferedImage, ImageObserver}
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images on the screen. */
object DrawImages:
  export DrawImage._

  /** Default dimension for images. */
  val defaultDimension: Dimension = new Dimension(30, 30)

  /** Default scale for silhouette. */
  val defaultScaleSilhouette: Float = 1.4f

  /** Represent a generic image. */
  trait DrawImage extends Observable[MouseEvent]:
    /** Center of the image. */
    val center: Point

    /** Dimension of the image. */
    val dimension: Dimension

    /** Observe the mouse event. */
    val observable: Observable[MouseEvent]

    /** Scale of the image. */
    def scale: Float

    /** Set the scale of the image. */
    def scale_=(value: Float): Unit

    /** Silhouette of the image. */
    def silhouettePalette: Styles.Palette

    /** Set the silhouette palette of the image. */
    def silhouettePalette_=(palette: Styles.Palette): Unit

    /** Draw the image on the screen. */
    def draw(g: Graphics2D, observer: ImageObserver): Unit

  /** Companion object for [[DrawImage]]. */
  object DrawImage:

    /** Create a new [[DrawImage]] with path, position and dimension. */
    def apply(path: String, position: Point, dimension: Dimension): DrawImage =
      new DrawImageImpl(path, position, dimension)

    /** Create a new [[DrawImage]] with path and point. */
    def createAt(path: String, point: Point): DrawImage = DrawImage(path, point, defaultDimension)

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private final case class DrawImageImpl(source: SourceImage, position: Point, dimension: Dimension)
        extends DrawImage:
      def this(path: String, position: Point, dimension: Dimension) = this(SourceImage(path), position, dimension)

      private val silhouette: Option[BufferedImage] =
        source.bufferImage.map(image => BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB))

      private var _scale: Float                      = defaultScaleSilhouette
      private var isSilhouetteShown: Boolean         = false
      private var _silhouettePalette: Styles.Palette = Styles.silhouettePalette
      private val silhouetteColor: CurrentColor      = CurrentColor(_silhouettePalette.background)
      override val center: Point = new Point(position.x - (dimension.width / 2), position.y - (dimension.height / 2))
      override val observable: Observable[MouseEvent] = Observers.createObservable[MouseEvent]

      export observable.{notifyClick => _, notifyExit => _, notifyHover => _, notifyRelease => _, _}

      override def scale: Float = _scale

      override def scale_=(value: Float): Unit = _scale = value

      override def silhouettePalette: Styles.Palette = _silhouettePalette

      override def silhouettePalette_=(palette: Styles.Palette): Unit =
        _silhouettePalette = palette
        silhouetteColor.current = palette.background

      override def notifyClick(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then
          silhouetteColor.clickColor(silhouettePalette)
          observable.notifyClick(data)

      override def notifyHover(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then
          silhouetteColor.hoverColor(silhouettePalette)
          observable.notifyHover(data)

      override def notifyRelease(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then
          silhouetteColor.releaseColor(silhouettePalette)
          observable.notifyRelease(data)

      override def notifyExit(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then
          silhouetteColor.exitColor(silhouettePalette)
          observable.notifyExit(data)

      override def draw(g: Graphics2D, observer: ImageObserver): Unit =
        if silhouetteColor.current != Styles.transparentColor then
          silhouette.foreach(silhouette =>
            println(s"${silhouetteColor.current}")
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

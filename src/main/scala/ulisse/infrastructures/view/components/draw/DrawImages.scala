package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.styles.Images.SourceImage
import ulisse.infrastructures.view.components.styles.{Images, Styles}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.Color
import java.awt.image.{BufferedImage, ImageObserver}
import scala.swing.event.MouseEvent
import scala.swing.{Dimension, Graphics2D, Point}

/** Draw images on the screen. */
object DrawImages:
  export DrawImage._

  /** Default point for images. */
  val defaultPoint: Point = new Point(0, 0)

  /** Default dimension for images. */
  val defaultDimension: Dimension = new Dimension(30, 30)

  /** Represent a generic image. */
  trait DrawImage extends Observable[MouseEvent]:
    /** Center of the image. */
    val center: Point

    /** Dimension of the image. */
    val dimension: Dimension

    /** Observe the mouse event. */
    val observable: Observable[MouseEvent]

    /** Draw the image on the screen. */
    def draw(g: Graphics2D, observer: ImageObserver): Unit

    /** Draw the silhouette of the image on the screen. */
    def drawSilhouette(g: Graphics2D, scale: Float, color: Color, observer: ImageObserver): Unit

  /** Companion object for [[DrawImage]]. */
  object DrawImage:

    /** Create a new [[DrawImage]] with path, position and dimension. */
    def apply(path: String, position: Point, dimension: Dimension): DrawImage =
      new DrawImageImpl(path, position, dimension)

    /** Create a new [[DrawImage]] with path and point. */
    def createAt(path: String, point: Point): DrawImage = DrawImage(path, point, defaultDimension)

    sealed private case class DrawImageImpl(picture: SourceImage, position: Point, dimension: Dimension)
        extends DrawImage:
      def this(path: String, position: Point, dimension: Dimension) = this(SourceImage(path), position, dimension)

      private val silhouette: Option[BufferedImage] =
        picture.bufferImage.map(image => BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB))

      private val palette: Styles.Palette = Styles.silhouettePalette

      override val center: Point = new Point(position.x - (dimension.width / 2), position.y - (dimension.height / 2))
      override val observable: Observable[MouseEvent] = Observers.createObservable[MouseEvent]

      export observable.{notifyClick => _, notifyExit => _, notifyHover => _, notifyRelease => _, _}

      override def notifyClick(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then observable.notifyClick(data)

      override def notifyHover(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then observable.notifyHover(data)

      override def notifyRelease(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then observable.notifyRelease(data)

      override def notifyExit(data: MouseEvent): Unit =
        if data.point.hasCollided(this) then observable.notifyExit(data)

      private def setupSilhouette(color: Color, observer: ImageObserver): Unit =
        for
          image      <- picture.bufferImage
          silhouette <- silhouette
        yield
          val graphics = silhouette.createGraphics()
          graphics.drawImage(image, 0, 0, observer)
          graphics.setComposite(java.awt.AlphaComposite.SrcAtop)
          graphics.setColor(color)
          graphics.fillRect(0, 0, image.getWidth, image.getHeight)
          graphics.dispose()

      override def draw(g: Graphics2D, observer: ImageObserver): Unit =
        picture.bufferImage.foreach(g.drawImage(_, center.x, center.y, dimension.width, dimension.height, observer))

      override def drawSilhouette(g: Graphics2D, scale: Float, color: Color, observer: ImageObserver): Unit =
        setupSilhouette(color, observer)
        silhouette.foreach(silhouette =>
          val (pos, size) = (center, dimension).scaleOf(scale)
          g.drawImage(silhouette, pos.x, pos.y, size.width, size.height, observer)
        )

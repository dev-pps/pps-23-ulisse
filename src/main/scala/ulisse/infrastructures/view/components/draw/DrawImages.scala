package ulisse.infrastructures.view.components.draw

import ulisse.infrastructures.view.components.styles.Images.{Picture, Rotation, SourceImage}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt.Color
import java.awt.image.{BufferedImage, ImageObserver}
import scala.swing.{Dimension, Graphics2D, Point}

object DrawImages:
  export DrawImage._

  val defaultPosition: Point = new Point(0, 0)
  val defaultSize: Dimension = new Dimension(30, 30)

  trait DrawImage:
    val center: Point
    val dimension: Dimension

    def draw(g: Graphics2D, observer: ImageObserver): Unit
    def drawSilhouette(g: Graphics2D, scale: Float, color: Color, observer: ImageObserver): Unit

  object DrawImage:

    def create(path: String, position: Point, dimension: Dimension): DrawImage =
      new DrawImageImpl(path, position, dimension)

    sealed private case class DrawImageImpl(picture: Picture, position: Point, dimension: Dimension) extends DrawImage:
      def this(path: String, position: Point, dimension: Dimension) =
        this(Picture(SourceImage(path), Rotation(0), 0), position, dimension)

      private val silhouette: Option[BufferedImage] =
        picture.bufferImage.map(image => BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB))

      override val center: Point = new Point(position.x - (dimension.width / 2), position.y - (dimension.height / 2))

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
          val scaleSize     = new Dimension((dimension.width * scale).toInt, (dimension.height * scale).toInt)
          val evenSize      = scaleSize.plus(new Dimension(scaleSize.width % 2, scaleSize.height % 2))
          val differentSize = evenSize.minus(dimension)
          val pos           = center.minus(new Point(differentSize.width / 2, differentSize.height / 2))
          g.drawImage(silhouette, pos.x, pos.y, evenSize.width, evenSize.height, observer)
        )

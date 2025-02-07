package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.JStyler.JStyles

import java.awt.image.{BufferedImage, ImageObserver}
import java.awt.{Color, RenderingHints}
import javax.imageio.ImageIO
import scala.swing.Graphics2D

object JImages:
  export JImage._

  type Position  = JStyler.Dimension2D[Int]
  type Dimension = JStyler.Dimension2D[Int]

  val defaultPosition: Position = JStyler.Dimension2D(0, 0)
  val defaultSize: Dimension    = JStyler.Dimension2D(30, 30)

  def createPosition(x: Int, y: Int): Position                    = JStyler.Dimension2D(x, y)
  def createScale(x: Float, y: Float): JStyler.Dimension2D[Float] = JStyler.Dimension2D(x, y)
  def createDimension(width: Int, height: Int): Dimension         = JStyler.Dimension2D(width, height)

  extension (g: Graphics2D)
    def drawImage(image: JImage, observer: ImageObserver): Unit =
      image.draw(g, observer)

    def drawSilhouette(image: JImage, scale: Float, color: Color, observer: ImageObserver): Unit =
      image.drawSilhouette(g, scale, color, observer)

  extension (point: java.awt.Point)
    def hasCollided(item: JImage): Boolean =
      val x          = point.x
      val y          = point.y
      val itemX      = item.center.width
      val itemY      = item.center.height
      val itemWidth  = item.dimension.width
      val itemHeight = item.dimension.height
      x >= itemX && x <= itemX + itemWidth && y >= itemY && y <= itemY + itemHeight

  trait JImage:
    val center: Position
    val dimension: Dimension

    def draw(g: Graphics2D, observer: ImageObserver): Unit
    def drawSilhouette(g: Graphics2D, scale: Float, color: Color, observer: ImageObserver): Unit

  object JImage:

    def create(path: String, position: Position, dimension: Dimension): JImage = JImageImpl(path, position, dimension)
    def createWithPosition(path: String, position: Position, dimension: Dimension): JImage =
      create(path, position, dimension)

    sealed private case class JImageImpl(path: String, position: Position, dimension: Dimension) extends JImage:
      private val url        = ClassLoader.getSystemResource(path)
      private val image      = ImageIO.read(url)
      private val silhouette = BufferedImage(image.getWidth, image.getHeight, BufferedImage.TYPE_INT_ARGB)

      override val center: Position =
        JStyler.Dimension2D(position.width - (dimension.width / 2), position.height - (dimension.height / 2))

      private def setupSilhouette(color: Color, observer: ImageObserver): Unit =
        val graphics = silhouette.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(image, 0, 0, observer)
        graphics.setComposite(java.awt.AlphaComposite.SrcAtop)
        graphics.setColor(color)
        graphics.fillRect(0, 0, image.getWidth, image.getHeight)
        graphics.dispose()

      override def draw(g: Graphics2D, observer: ImageObserver): Unit =
        g.drawImage(image, center.width, center.height, dimension.width, dimension.height, observer)

      override def drawSilhouette(g: Graphics2D, scale: Float, color: Color, observer: ImageObserver): Unit =
        setupSilhouette(color, observer)

        val scaleSize     = createDimension((dimension.width * scale).toInt, (dimension.height * scale).toInt)
        val evenSize      = scaleSize.plus(createDimension(scaleSize.width % 2, scaleSize.height % 2))
        val differentSize = evenSize.minus(dimension)
        val pos           = center.minus(createPosition(differentSize.width / 2, differentSize.height / 2))

        g.drawImage(silhouette, pos.width, pos.height, evenSize.width, evenSize.height, observer)

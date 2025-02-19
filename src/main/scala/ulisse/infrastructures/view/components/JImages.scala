package ulisse.infrastructures.view.components

import java.awt.image.{BufferedImage, ImageObserver}
import java.awt.{Color, RenderingHints}
import javax.imageio.ImageIO
import scala.swing.Graphics2D

object JImages:
  export JImage._

  type Position  = JStyles.Pair[Int]
  type Dimension = JStyles.Pair[Int]

  val defaultPosition: Position = JStyles.Pair(0, 0)
  val defaultSize: Dimension    = JStyles.Pair(30, 30)

  def createPosition(x: Int, y: Int): Position             = JStyles.Pair(x, y)
  def createScale(x: Float, y: Float): JStyles.Pair[Float] = JStyles.Pair(x, y)
  def createDimension(width: Int, height: Int): Dimension  = JStyles.Pair(width, height)

  extension (g: Graphics2D)
    def drawImage(image: JImage, observer: ImageObserver): Unit =
      image.draw(g, observer)

    def drawSilhouette(image: JImage, scale: Float, color: Color, observer: ImageObserver): Unit =
      image.drawSilhouette(g, scale, color, observer)

  extension (point: java.awt.Point)
    def hasCollided(item: JImage): Boolean =
      val x          = point.x
      val y          = point.y
      val itemX      = item.center.a
      val itemY      = item.center.b
      val itemWidth  = item.dimension.a
      val itemHeight = item.dimension.b
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
        JStyles.Pair(position.a - (dimension.a / 2), position.b - (dimension.b / 2))

      private def setupSilhouette(color: Color, observer: ImageObserver): Unit =
        val graphics = silhouette.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        graphics.drawImage(image, 0, 0, observer)
        graphics.setComposite(java.awt.AlphaComposite.SrcAtop)
        graphics.setColor(color)
        graphics.fillRect(0, 0, image.getWidth, image.getHeight)
        graphics.dispose()

      override def draw(g: Graphics2D, observer: ImageObserver): Unit =
        g.drawImage(image, center.a, center.b, dimension.a, dimension.b, observer)

      override def drawSilhouette(g: Graphics2D, scale: Float, color: Color, observer: ImageObserver): Unit =
        setupSilhouette(color, observer)

        val scaleSize     = createDimension((dimension.a * scale).toInt, (dimension.b * scale).toInt)
        val evenSize      = scaleSize.plus(createDimension(scaleSize.a % 2, scaleSize.b % 2))
        val differentSize = evenSize.minus(dimension)
        val pos           = center.minus(createPosition(differentSize.a / 2, differentSize.b / 2))

        g.drawImage(silhouette, pos.a, pos.b, evenSize.a, evenSize.b, observer)

package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.components.JImages.*
import ulisse.infrastructures.view.components.JStyler
import ulisse.infrastructures.view.components.JStyler.JStyles

import java.awt.Color
import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

trait MapItem:
  val center: Position
  val dimension: Dimension

  def drawItem(g: Graphics2D, observer: ImageObserver): Unit
  def onHover(mousePoint: Point): Unit
  def onClick(mousePoint: Point): Unit

object MapItem:
  def createSingleItem(imagePath: String, x: Int, y: Int): SingleItem =
    SingleItem(imagePath, JStyler.Dimension2D(x, y), defaultSize)

  extension (point: Point)
    def hasCollide(item: MapItem): Option[Position] =
      val x          = point.x
      val y          = point.y
      val itemX      = item.center.width
      val itemY      = item.center.height
      val itemWidth  = item.dimension.width
      val itemHeight = item.dimension.height
      val isCollide  = x >= itemX && x <= itemX + itemWidth && y >= itemY && y <= itemY + itemHeight
      if isCollide then
        Some(JStyler.Dimension2D(x, y))
      else
        None

  sealed case class SingleItem(imagePath: String, pos: Position, dim: Dimension) extends MapItem:
    private val image = JImage.createWithPosition(imagePath, pos, dim)

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var hovered = false
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var clicked = false

    override val center: Position     = image.center
    override val dimension: Dimension = image.dimension
    export image.{center as _, dimension as _, _}

    override def drawItem(g: Graphics2D, observer: ImageObserver): Unit =
      if (hovered) g.drawSilhouette(image, 1.2f, Color.RED, observer)
      g.drawImage(image, observer)

    override def onHover(mousePoint: Point): Unit =
      hovered = mousePoint.hasCollide(this).isDefined

    override def onClick(mousePoint: Point): Unit =
      clicked = mousePoint.hasCollide(this).isDefined

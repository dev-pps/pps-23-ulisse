package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages
import ulisse.infrastructures.view.utils.Swings.*

import java.awt
import java.awt.image.ImageObserver
import scala.swing.{Dimension, Graphics2D, Point}

trait MapItem:
  def drawItem(g: Graphics2D, observer: ImageObserver): Unit
//  def onHover(mousePoint: Point): Unit
//  def onClick(mousePoint: Point): Unit
//  def onRelease(mousePoint: Point): Unit

object MapItem:
  def createSingleItem(imagePath: String, x: Int, y: Int): SingleItem =
    SingleItem(imagePath, new Point(x, y), DrawImages.defaultDimension)

  sealed case class SingleItem(imagePath: String, pos: Point, dim: Dimension) extends MapItem:
    private val image          = DrawImages.createAt(imagePath, pos)
    private val itemObservable = Observers.createObservable[Point]

    export image._

    override def drawItem(g: Graphics2D, observer: ImageObserver): Unit = g.drawImage(image, observer)

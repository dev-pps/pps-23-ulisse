package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.components.draw.JImages.*
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.utils.Pair

import java.awt
import java.awt.Color
import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

trait MapItem extends Observable[Point]:
  def center: Position
  def dimension: Dimension
  def hasCollided(point: Point): Boolean

  def drawItem(g: Graphics2D, observer: ImageObserver): Unit

  def onHover(mousePoint: Point): Unit
  def onClick(mousePoint: Point): Unit
  def onRelease(mousePoint: Point): Unit

object MapItem:
  def createSingleItem(imagePath: String, x: Int, y: Int): SingleItem =
    SingleItem(imagePath, Pair(x, y), defaultSize)

  sealed case class SingleItem(imagePath: String, pos: Position, dim: Dimension) extends MapItem:
    private val image          = JImage.createWithPosition(imagePath, pos, dim)
    private val itemObservable = Observers.createObservable[Point]

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var currentColorSilhouette: Color = Styles.transparentColor
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var isSilhouetteShown: Boolean = false

    export image._, itemObservable._

    override def hasCollided(point: Point): Boolean = point.hasCollided(image)

    override def drawItem(g: Graphics2D, observer: ImageObserver): Unit =
      if (isSilhouetteShown) g.drawSilhouette(image, 1.4f, currentColorSilhouette, observer)
      g.drawImage(image, observer)

    override def onHover(mousePoint: Point): Unit =
      if hasCollided(mousePoint) then
        isSilhouetteShown = true
        currentColorSilhouette = Theme.light.overlay
      else
        isSilhouetteShown = false

    override def onClick(mousePoint: Point): Unit =
      if hasCollided(mousePoint) then
        isSilhouetteShown = true
        currentColorSilhouette = Theme.light.click
        itemObservable.notifyClick(mousePoint)

    override def onRelease(mousePoint: Point): Unit =
      if hasCollided(mousePoint) then isSilhouetteShown = false

package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.JImages.*
import ulisse.infrastructures.view.components.JStyler
import ulisse.infrastructures.view.components.JStyler.JStyles

import java.awt
import java.awt.Color
import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

trait MapItem:
  def center: Position
  def dimension: Dimension
  def hasCollided(point: Point): Boolean

  def drawItem(g: Graphics2D, observer: ImageObserver): Unit

  def onHover(mousePoint: Point): Unit
  def onClick(mousePoint: Point): Unit
  def onRelease(mousePoint: Point): Unit

object MapItem:
  def createSingleItem(imagePath: String, x: Int, y: Int): SingleItem =
    SingleItem(imagePath, JStyler.Dimension2D(x, y), defaultSize)

  sealed case class SingleItem(imagePath: String, pos: Position, dim: Dimension) extends MapItem:
    private val image = JImage.createWithPosition(imagePath, pos, dim)

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var currentColorSilhouette: Color = JStyler.transparentColor
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var isSilhouetteShown: Boolean = false

    export image._

    override def hasCollided(point: Point): Boolean = point.hasCollided(image)

    override def drawItem(g: Graphics2D, observer: ImageObserver): Unit =
      if (isSilhouetteShown) g.drawSilhouette(image, 1.2f, currentColorSilhouette, observer)
      g.drawImage(image, observer)

    override def onHover(mousePoint: Point): Unit =
      if hasCollided(mousePoint) then
        isSilhouetteShown = true
        currentColorSilhouette = Theme.light.overlayElement
      else
        isSilhouetteShown = false

    override def onClick(mousePoint: Point): Unit =
      if hasCollided(mousePoint) then
        isSilhouetteShown = true
        currentColorSilhouette = Theme.light.forwardClick

    override def onRelease(mousePoint: Point): Unit =
      if hasCollided(mousePoint) then isSilhouetteShown = false

package ulisse.infrastructures.view.map

import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

object MapItemsCollection:

  def apply(): MapItemsCollection = MapItemsCollection(List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  case class MapItemsCollection(var mapItems: List[MapItem]):

    def onClick(mousePoint: Point): Unit =
      if mapItems.exists(_.hasCollided(mousePoint)) then
        mapItems.foreach(_.onClick(mousePoint))
      else
        mapItems = mapItems.appended(MapItem.createSingleItem("station.png", mousePoint.x, mousePoint.y))

    def onHover(mousePoint: Point): Unit   = mapItems.foreach(_.onHover(mousePoint))
    def onRelease(mousePoint: Point): Unit = mapItems.foreach(_.onRelease(mousePoint))

    def draw(g: Graphics2D, observer: ImageObserver): Unit = mapItems.foreach(_.drawItem(g, observer))

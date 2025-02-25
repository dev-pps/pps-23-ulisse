package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.ViewObservers
import ulisse.infrastructures.view.common.ViewObservers.{ViewObservable, ViewObserver}

import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

object MapItemsCollection:

  def apply(): MapItemsCollection = MapItemsCollection(List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  case class MapItemsCollection(var mapItems: List[MapItem]) extends ViewObservable[Point]:
    private val observable                            = ViewObservers.createObservable[Point]
    override val observers: List[ViewObserver[Point]] = observable.observers

    def onClick(mousePoint: Point): Unit =
      if mapItems.exists(_.hasCollided(mousePoint)) then
        mapItems.foreach(_.onClick(mousePoint))
      else
        val item = MapItem.createSingleItem("station.png", mousePoint.x, mousePoint.y)
        observable.observers.foreach(item.attach)
        mapItems = mapItems.appended(item)

    def onHover(mousePoint: Point): Unit   = mapItems.foreach(_.onHover(mousePoint))
    def onRelease(mousePoint: Point): Unit = mapItems.foreach(_.onRelease(mousePoint))

    def draw(g: Graphics2D, observer: ImageObserver): Unit = mapItems.foreach(_.drawItem(g, observer))

    def refreshObserver(): Unit = ()

    override def attach(observer: ViewObserver[Point]): Unit =
      observable.attach(observer)
      mapItems.foreach(_.attach(observer))
    override def detach(observer: ViewObserver[Point]): Unit =
      observable.detach(observer)
      mapItems.foreach(_.detach(observer))

    override def notifyOnClick(data: Point): Unit   = mapItems.foreach(_.notifyOnClick(data))
    override def notifyOnHover(data: Point): Unit   = mapItems.foreach(_.notifyOnHover(data))
    override def notifyOnRelease(data: Point): Unit = mapItems.foreach(_.notifyOnRelease(data))

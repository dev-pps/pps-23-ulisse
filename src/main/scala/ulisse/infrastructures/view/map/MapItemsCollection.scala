package ulisse.infrastructures.view.map

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.{Observable, Observer}

import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

object MapItemsCollection:

  def apply(): MapItemsCollection = MapItemsCollection(List.empty)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  case class MapItemsCollection(var mapItems: List[MapItem]) extends Observable[Point]:
    private val observable                        = Observers.createObservable[Point]
    override val observers: List[Observer[Point]] = observable.observers

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

    override def attach(observer: Observer[Point]): Unit =
      observable.attach(observer)
      mapItems.foreach(_.attach(observer))
    override def detach(observer: Observer[Point]): Unit =
      observable.detach(observer)
      mapItems.foreach(_.detach(observer))

    override def notifyClick(data: Point): Unit   = mapItems.foreach(_.notifyClick(data))
    override def notifyHover(data: Point): Unit   = mapItems.foreach(_.notifyHover(data))
    override def notifyRelease(data: Point): Unit = mapItems.foreach(_.notifyRelease(data))
    override def notifyExit(data: Point): Unit    = mapItems.foreach(_.notifyExit(data))

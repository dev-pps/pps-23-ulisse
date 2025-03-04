package ulisse.infrastructures.view.map

import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.components.draw.DrawImages
import ulisse.infrastructures.view.components.draw.DrawImages.DrawImage
import ulisse.infrastructures.view.utils.Swings.*

import java.awt
import java.awt.image.ImageObserver
import scala.swing.{Graphics2D, Point}

/** Represent a generic element of the map. */
trait MapElement[T] extends Observable[MapElement[T]]:
  /** The element of the map. */
  val element: T

  /** The image of the element. */
  val image: DrawImage

  /** Draw the item on the screen. */
  def drawItem(g: Graphics2D, observer: ImageObserver): Unit

/** Companion object for [[MapElement]]. */
object MapElement:
  /** Create a new [[MapElement]] with the given [[Station]], [[String]] and [[Point]]. */
  def createStation(station: Station, imagePath: String): MapElement[Station] =
    new MapElementImpl(station, imagePath, station.coordinate.toPoint)

  /** Create a new [[MapElement]] with the given [[Route]] and [[String]]. */
  def createRoute(route: Route, imagePath: String): MapElement[Route] =
    new MapElementImpl(route, imagePath, new Point(0, 0))

  private case class MapElementImpl[T](element: T, image: DrawImage) extends MapElement[T]:
    def this(element: T, imagePath: String, pos: Point) = this(element, DrawImages.createAt(imagePath, pos))
    private val observable = Observers.createObservable[MapElement[T]]
    image.attach(observable.toObserver(_ => this))
    export observable._

    override def drawItem(g: Graphics2D, observer: ImageObserver): Unit = g.drawImage(image, observer)

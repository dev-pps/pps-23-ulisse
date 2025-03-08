package ulisse.infrastructures.view.map

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.{Route, RouteType}
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.infrastructures.view.common.Observers.Observable
import ulisse.infrastructures.view.common.{ImagePath, Observers}
import ulisse.infrastructures.view.components.draw.DrawImages.DrawImage
import ulisse.infrastructures.view.components.draw.{DrawImageSimple, DrawImageTiled, DrawImages}
import ulisse.infrastructures.view.utils.Swings.*

import java.awt
import java.awt.Color
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
  def createStation(station: Station): MapElement[Station] =
    val imagePath = ImagePath.station
    MapElementSimple(station, DrawImageSimple.createAt(imagePath, station.coordinate.toPoint))

  /** Create a new [[MapElement]] with the given [[StationEnvironmentElement]]. */
  def createStationEnvironmentElement(station: StationEnvironmentElement): MapElement[StationEnvironmentElement] =
    val imagePath = ImagePath.station
    MapElementSimple(station, DrawImageSimple.createAt(imagePath, station.coordinate.toPoint))

  /** Create a new [[MapElement]] with the given [[Route]] and [[String]]. */
  def createRoute[R <: Route](route: R, findPath: Boolean): MapElement[R] =
    val offset        = new Point(-10, -10)
    val offsetWithOld = if findPath then offset else new Point(0, 0)
    val start         = route.departure.coordinate.toPoint plus offsetWithOld
    val end           = route.arrival.coordinate.toPoint plus offsetWithOld
    val color = route.typology match
      case RouteType.Normal => Color.black
      case RouteType.AV     => Color.red
    MapElementSimple(route, DrawImageTiled.createAt(start, end, color))

  private case class MapElementSimple[T](element: T, image: DrawImage) extends MapElement[T]:
    private val observable = Observers.createObservable[MapElement[T]]
    image.attach(observable.toObserver(_ => this))
    export observable._

    override def drawItem(g: Graphics2D, observer: ImageObserver): Unit = g.drawImage(image, observer)

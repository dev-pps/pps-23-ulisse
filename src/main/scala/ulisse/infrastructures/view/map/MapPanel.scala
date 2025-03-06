package ulisse.infrastructures.view.map

import ulisse.applications.ports.StationPorts
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.ImagePath
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.EnhancedLook
import ulisse.infrastructures.view.components.draw.DrawImageTiled

import scala.swing.*

/** Represent the map panel. */
trait MapPanel extends Panel with EnhancedLook:
  /** Attach the station form to the map panel. */
  def attachClickStation(event: ClickObserver[MapElement[Station]]): Unit

  /** Draw the station on the screen. */
  def uploadStation(newStations: StationPorts.Input#SM): Unit

/** Companion object for [[MapPanel]]. */
object MapPanel:
  /** Create a new [[MapPanel]]. */
  def apply(): MapPanel = MapPanelImpl()

  private case class MapPanelImpl() extends MapPanel:
    private val stations = MapElements[Station](observable)

    private val route = DrawImageTiled.createAt("route.png", new Point(500, 500), new Point(400, 400))

    def attachClickStation(event: ClickObserver[MapElement[Station]]): Unit =
      stations.attachClick(event)

    override def uploadStation(newStations: StationPorts.Input#SM): Unit =
      stations.update(newStations.map(MapElement.createStation(_, ImagePath.station)))
      updateGraphics()

    override protected def paintLook(g: Graphics2D): Unit =
      stations.draw(g, peer)
      route.drawTiledImage(g, 0.1, peer)
      super.paintLook(g)

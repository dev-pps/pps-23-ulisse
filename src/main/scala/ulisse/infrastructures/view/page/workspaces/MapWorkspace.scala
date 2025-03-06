package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.InputAdapterManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.forms.StationForm.{CreationStationEvent, DeletionStationEvent}
import ulisse.infrastructures.view.page.forms.{RouteForm, StationForm}
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace

import scala.swing.BorderPanel.Position

/** Represents the map workspace of the application. */
trait MapWorkspace extends Workspace:
  /** The selected station of the form. */
  def selectedStation: Option[Station]

  /** Set the selected station of the form. */
  def selectedStation_=(station: Option[Station]): Unit

  /** Reset selected station. */
  def resetSelectedStation(): Unit = selectedStation = Option.empty

  /** Compile station form. */
  def compileStationForm(station: Station): Unit

  /** Updates the stations of the map. */
  def updateStations(stations: StationPorts.Input#SM): Unit

  /** Updates the routes of the map. */
  def updateRoutes(routes: List[Route]): Unit

  /** Updates the stations and routes of the map. */
  def update(stations: StationPorts.Input#SM, routes: List[Route]): Unit =
    updateStations(stations)
    updateRoutes(routes)

/** Companion object of the [[MapWorkspace]]. */
object MapWorkspace:

  /** Creates a new instance of map workspace. */
  def apply(adapterManager: InputAdapterManager): MapWorkspace = MapWorkspaceImpl(adapterManager)

  /** Represents the map workspace of the application. */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private final case class MapWorkspaceImpl(adapterManager: InputAdapterManager) extends MapWorkspace:
    private val workspace   = BaseWorkspace()
    private val formManager = FormManager.createMap()

    private val mapPanel: MapPanel       = MapPanel()
    private val stationForm: StationForm = formManager.stationForm
    private val routeForm: RouteForm     = formManager.routeForm

    private var _selectedStation: Option[Station] = Option.empty
    private var _selectedRoute: Option[Route]     = Option.empty

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    formManager.stationForm attachCreation CreationStationEvent(adapterManager.station, this)
    formManager.stationForm attachDeletion DeletionStationEvent(adapterManager.station, this)

    formManager.routeForm attachCreation RouteForm.CreationRouteEvent(adapterManager.route, this)

    mapPanel attachClick StationForm.TakePointFomMapEvent(stationForm)

    export workspace.{component, revalidate}, stationForm.compileForm as compileStationForm

    override def selectedStation: Option[Station] = _selectedStation

    override def selectedStation_=(station: Option[Station]): Unit = _selectedStation = station

    override def updateStations(stations: StationPorts.Input#SM): Unit =
      mapPanel uploadStation stations
      mapPanel attachClickStation StationForm.TakeStationFromMapEvent(this)
      mapPanel attachClickStation RouteForm.TakeStationFromMapEvent(routeForm)

    override def updateRoutes(routes: List[Route]): Unit =
      mapPanel updateRoutes routes

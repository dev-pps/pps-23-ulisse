package ulisse.infrastructures.view.page.workspaces

import cats.syntax.option.*
import ulisse.adapters.InputAdapterManager
import ulisse.applications.ports.StationPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.forms.Form.TimetableForm
import ulisse.infrastructures.view.page.forms.StationForm.{CreationStationEvent, DeletionStationEvent}
import ulisse.infrastructures.view.page.forms.{RouteForm, StationForm}
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext

import scala.swing.BorderPanel.Position
import scala.swing.Point

/** Represents the map workspace of the application. */
trait MapWorkspace extends Workspace:
  /** Initialize the map. */
  def initMap(): Unit

  /** The selected station of the form. */
  def selectedStation: Option[Station]

  /** Set the selected station of the form. */
  def selectedStation_=(station: Station): Unit

  /** The selected route of the form. */
  def selectedRoute: Option[Route]

  /** Set the selected route of the form. */
  def selectedRoute_=(route: Route): Unit

  /** Compile station form. */
  def compileStationForm(station: Station): Unit

  /** Compile station coordinates form. */
  def compileStationCoordinatesForm(point: Point): Unit

  /** Compile route form. */
  def compileRouteForm(route: Route): Unit

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
    private val formManager = FormManager.createMap(adapterManager.timetable)

    private val mapPanel: MapPanel           = MapPanel()
    private val stationForm: StationForm     = formManager.stationForm
    private val routeForm: RouteForm         = formManager.routeForm
    private val timetableForm: TimetableForm = formManager.timetableForm

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    formManager.stationForm attachCreation CreationStationEvent(adapterManager.station, this, stationForm)
    formManager.stationForm attachDeletion DeletionStationEvent(adapterManager.station, this, stationForm)

    formManager.routeForm attachCreation RouteForm.CreationRouteEvent(adapterManager.route, this, routeForm)
    formManager.routeForm attachDeletion RouteForm.DeletionRouteEvent(adapterManager.route, this, routeForm)

    mapPanel attachClick StationForm.TakePointFomMapEvent(this)

    export workspace.{component, revalidate}, stationForm.compileForm as compileStationForm,
      routeForm.compileForm as compileRouteForm, stationForm.selectedStation, stationForm.selectedStation_=,
      stationForm.resetSelectedStation, routeForm.selectedRoute, routeForm.selectedRoute_=, routeForm.resetSelectedRoute

    override def initMap(): Unit =
      adapterManager.station.stationMap onComplete (_ fold (a => a, updateStations))
      adapterManager.route.routes onComplete (_ fold (a => a, updateRoutes))

    override def compileStationCoordinatesForm(point: Point): Unit =
      stationForm.xField.text = point.x.toString
      stationForm.yField.text = point.y.toString

    override def updateStations(stations: StationPorts.Input#SM): Unit =
      mapPanel uploadStation stations
      mapPanel attachClickStation StationForm.TakeStationFromMapEvent(this)
      mapPanel attachClickStation RouteForm.TakeStationFromMapEvent(routeForm)
      stationForm.cleanForm()
      resetSelectedStation()

    override def updateRoutes(routes: List[Route]): Unit =
      mapPanel updateRoutes routes
      mapPanel attachClickRoute RouteForm.TakeRouteFromMapEvent(this)
      routeForm.cleanForm()
      resetSelectedRoute()

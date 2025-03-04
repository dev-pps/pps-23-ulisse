package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.InputAdapterManager
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.forms.StationForm.{CreationStationEvent, DeletionStationEvent}
import ulisse.infrastructures.view.page.forms.{RouteForm, StationForm}
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace

import scala.swing.BorderPanel.Position

/** Represents the map workspace of the application. */
trait MapWorkspace extends Workspace:
  /** The map panel of the workspace. */
  val mapPanel: MapPanel

  /** The station form of the workspace. */
  val stationForm: StationForm

  /** The route form of the workspace. */
  val routeForm: RouteForm

/** Companion object of the [[MapWorkspace]]. */
object MapWorkspace:

  /** Creates a new instance of map workspace. */
  def apply(adapterManager: InputAdapterManager): MapWorkspace =
    MapWorkspaceImpl(adapterManager)

  /** Represents the map workspace of the application. */
  private final case class MapWorkspaceImpl(adapterManager: InputAdapterManager) extends MapWorkspace:
    private val workspace   = BaseWorkspace()
    private val formManager = FormManager.createMap()

    override val mapPanel: MapPanel       = MapPanel()
    override val stationForm: StationForm = formManager.stationForm
    override val routeForm: RouteForm     = formManager.routeForm

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    formManager.stationForm.attachCreation(CreationStationEvent(adapterManager.stationAdapter, formManager, mapPanel))
    formManager.stationForm.attachDeletion(DeletionStationEvent(adapterManager.stationAdapter, stationForm, mapPanel))

    mapPanel.attachClick(StationForm.TakePointFomMapEvent(stationForm))

    export workspace.{component, revalidate}

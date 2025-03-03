package ulisse.infrastructures.view.page.workspaces

import ulisse.adapters.InputAdapterManager
import ulisse.adapters.input.StationEditorAdapter
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.MapPanel
import ulisse.infrastructures.view.page.forms.StationForm.StationFormData
import ulisse.infrastructures.view.page.workspaces.Workspace.BaseWorkspace

import scala.concurrent.ExecutionContext
import scala.swing.Swing

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

import scala.swing.BorderPanel.Position

/** Represents the map workspace of the application. */
trait MapWorkspace extends Workspace

/** Companion object of the [[MapWorkspace]]. */
object MapWorkspace:

  /** Creates a new instance of map workspace. */
  def apply(adapterManager: InputAdapterManager): MapWorkspace =
    MapWorkspaceImpl(adapterManager)

  /** Represents the creation station event. */
  private final case class CreationStationEvent(adapter: StationEditorAdapter, workspace: MapWorkspace)
      extends ClickObserver[StationFormData]:
    override def onClick(data: StationFormData): Unit =
      val future = adapter.onOkClick(data.name, data.x, data.y, data.tracks, Option.empty)
      future.onComplete(_ map:
        case Left(error)     => println(error)
        case Right(stations) => println(stations)
      )

  /** Represents the deletion station event. */
  private final case class DeletionStationEvent(adapter: StationEditorAdapter, workspace: MapWorkspace)
      extends ClickObserver[StationFormData]:
    override def onClick(data: StationFormData): Unit = ()

  /** Represents the map workspace of the application. */
  private final case class MapWorkspaceImpl(adapterManager: InputAdapterManager) extends MapWorkspace:
    private val workspace   = BaseWorkspace()
    private val mapPanel    = MapPanel.empty()
    private val formManager = FormManager.createMap()

    workspace.workPanel.layout(mapPanel) = Position.Center
    workspace.menuPanel.layout(formManager.component) = Position.East
    workspace.revalidate()

    formManager.stationForm.attachCreation(CreationStationEvent(adapterManager.stationAdapter, this))
    formManager.stationForm.attachDeletion(DeletionStationEvent(adapterManager.stationAdapter, this))

    export workspace.{component, revalidate}

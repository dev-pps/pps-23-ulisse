package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.StationEditorAdapter
import ulisse.adapters.input.StationEditorAdapter.StationCreationInfo
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.workspaces.MapWorkspace
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext

import scala.swing.event.MouseEvent

/** Represents the station form of the application. */
trait StationForm extends Form:
  /** The name field of the form. */
  val name: ComposedSwing.InfoTextField

  /** The x field of the form. */
  val xField: ComposedSwing.InfoTextField

  /** The y field of the form. */
  val yField: ComposedSwing.InfoTextField

  /** The tracks field of the form. */
  val tracks: ComposedSwing.InfoTextField

  /** Attach the creation observer to the form of type [[StationCreationInfo]]. */
  def attachCreation(observer: ClickObserver[StationCreationInfo]): Unit

  /** Attach the deletion observer to the form of type [[StationCreationInfo]]. */
  def attachDeletion(observer: ClickObserver[StationCreationInfo]): Unit

  /** Compiles the form. */
  def compileForm(station: Station): Unit =
    name.text = station.name
    xField.text = station.coordinate.x.toString
    yField.text = station.coordinate.y.toString
    tracks.text = station.numberOfPlatforms.toString

/** Companion object of the [[StationForm]]. */
object StationForm:

  /** Creates a new instance of station form. */
  def apply(): StationForm = StationFormImpl()

  /** Represents the creation station event. */
  final case class CreationStationEvent(adapter: StationEditorAdapter, workspace: MapWorkspace)
      extends ClickObserver[StationCreationInfo]:

    private def createStation(data: StationCreationInfo): Unit =
      adapter addStation data onComplete (_ fold (println, _ fold (println, workspace.updateStations)))

    private def updateStation(data: StationCreationInfo, oldStation: Station): Unit =
      adapter updateStation (data, oldStation) onComplete (_ fold (println, _ fold (println, workspace.update)))
      workspace.resetSelectedStation()

    override def onClick(data: StationCreationInfo): Unit =
      workspace.selectedStation.fold(createStation(data))(updateStation(data, _))

  /** Represents the deletion station event. */
  final case class DeletionStationEvent(adapter: StationEditorAdapter, workspace: MapWorkspace)
      extends ClickObserver[StationCreationInfo]:

    override def onClick(data: StationCreationInfo): Unit =
      workspace.selectedStation.fold(println("Error not found"))(
        adapter removeStation _ onComplete (_ fold (println, _ fold (println, workspace.update)))
      )
      workspace.resetSelectedStation()

  /** Represents the take point from map event. */
  final case class TakePointFomMapEvent(stationForm: StationForm) extends ClickObserver[MouseEvent]:
    override def onClick(data: MouseEvent): Unit =
      stationForm.xField.text = data.point.x.toString
      stationForm.yField.text = data.point.y.toString

  /** Represents the take station from map event. */
  final case class TakeStationFromMapEvent(workspace: MapWorkspace) extends ClickObserver[MapElement[Station]]:
    override def onClick(data: MapElement[Station]): Unit =
      workspace.selectedStation = data.element
      workspace.compileStationForm(data.element)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class StationFormImpl() extends StationForm:
    override val name: ComposedSwing.InfoTextField   = ComposedSwing createInfoTextField "Name"
    override val xField: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "x"
    override val yField: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "y"
    override val tracks: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Tracks"

    private val saveButton   = ExtendedSwing createFormButtonWith ("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing createFormButtonWith ("Delete", Styles.formFalseButtonRect)

    private val form = BaseForm("Station", name, xField, yField, tracks)

    private val creationObservable = Observers.createObservable[StationCreationInfo]
    private val deletionObservable = Observers.createObservable[StationCreationInfo]

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    saveButton.attach(creationObservable toObserver (_ =>
      StationCreationInfo(name.text, xField.text, yField.text, tracks.text)
    ))
    deleteButton.attach(deletionObservable toObserver (_ =>
      StationCreationInfo(name.text, xField.text, yField.text, tracks.text)
    ))

    export form._, creationObservable.attachClick as attachCreation, deletionObservable.attachClick as attachDeletion

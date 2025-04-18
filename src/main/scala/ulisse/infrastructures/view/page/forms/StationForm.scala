package ulisse.infrastructures.view.page.forms

import cats.syntax.option.*
import ulisse.adapters.input.StationEditorAdapter
import ulisse.adapters.input.StationEditorAdapter.StationCreationInfo
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.page.forms.Form.{BaseForm, CleanFormEvent}
import ulisse.infrastructures.view.page.workspaces.MapWorkspace
import ulisse.infrastructures.view.utils.ComponentUtils.*
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext
import ulisse.utils.ValidationUtils.mkErrors

import scala.swing.event.MouseEvent
import scala.swing.{Orientation, Swing}

/** Represents the station form of the application. */
trait StationForm extends Form:
  /** The name field of the form. */
  val name: ComposedSwing.InfoTextField

  /** The x field of the form. */
  val xField: ComposedSwing.InfoTextField

  /** The y field of the form. */
  val yField: ComposedSwing.InfoTextField

  /** The tracks field of the form. */
  val platforms: ComposedSwing.InfoTextField

  /** Attach the creation observer to the form of type [[StationCreationInfo]]. */
  def attachCreation(observer: ClickObserver[StationCreationInfo]): Unit

  /** Attach the deletion observer to the form of type [[StationCreationInfo]]. */
  def attachDeletion(observer: ClickObserver[StationCreationInfo]): Unit

  /** The selected station of the form. */
  def selectedStation: Option[Station]

  /** Set the selected station of the form. */
  def selectedStation_=(station: Station): Unit

  /** Reset the selected station of the form. */
  def resetSelectedStation(): Unit

  /** Cleans the form. */
  override def cleanForm(): Unit =
    name.text = ""
    xField.text = ""
    yField.text = ""
    platforms.text = ""
    resetSelectedStation()

  /** Compiles the form. */
  def compileForm(station: Station): Unit =
    name.text = station.name
    xField.text = station.coordinate.x.toString
    yField.text = station.coordinate.y.toString
    platforms.text = station.numberOfPlatforms.toString

/** Companion object of the [[StationForm]]. */
object StationForm:

  /** Creates a new instance of station form. */
  def apply(): StationForm = StationFormImpl()

  /** Represents the creation station event. */
  final case class CreationStationEvent(adapter: StationEditorAdapter, workspace: MapWorkspace, form: StationForm)
      extends ClickObserver[StationCreationInfo]:

    private def createStation(data: StationCreationInfo): Unit =
      adapter addStation data onComplete (_ fold (println, _ fold (error =>
        form.showError(s"${error.mkErrors}"), workspace.updateStations)))

    private def updateStation(data: StationCreationInfo, oldStation: Station): Unit =
      adapter updateStation (data, oldStation) onComplete (_ fold (println, _ fold (error =>
        form.showError(s"${error.mkErrors}"), workspace.update)))

    override def onClick(data: StationCreationInfo): Unit =
      form.resetError()
      workspace.selectedStation.fold(createStation(data))(updateStation(data, _))

  /** Represents the deletion station event. */
  final case class DeletionStationEvent(adapter: StationEditorAdapter, workspace: MapWorkspace, form: StationForm)
      extends ClickObserver[StationCreationInfo]:

    override def onClick(data: StationCreationInfo): Unit =
      form.resetError()
      workspace.selectedStation.fold(form.showError("Error not found"))(
        adapter removeStation _ onComplete (_ fold (println, _ fold (error =>
          form.showError(s"${error.mkErrors}"), workspace.update)))
      )

  /** Represents the take point from map event. */
  final case class TakePointFomMapEvent(workspace: MapWorkspace) extends ClickObserver[MouseEvent]:
    override def onClick(data: MouseEvent): Unit =
      workspace.compileStationCoordinatesForm(data.point)

  /** Represents the take station from map event. */
  final case class TakeStationFromMapEvent(workspace: MapWorkspace) extends ClickObserver[MapElement[Station]]:
    override def onClick(data: MapElement[Station]): Unit =
      workspace.selectedStation = data.element
      workspace compileStationForm data.element

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class StationFormImpl() extends StationForm:
    override val name: ComposedSwing.InfoTextField      = ComposedSwing createInfoTextField "Name"
    override val xField: ComposedSwing.InfoTextField    = ComposedSwing createInfoTextField "x"
    override val yField: ComposedSwing.InfoTextField    = ComposedSwing createInfoTextField "y"
    override val platforms: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Platforms"

    private val layoutButton = ExtendedSwing.SBoxPanel(Orientation.Vertical).transparent()
    private val controlPanel = ExtendedSwing.SFlowPanel().transparent()
    private val saveButton   = ExtendedSwing createFormButtonWith ("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing createFormButtonWith ("Delete", Styles.formFalseButtonRect)
    private val resetButton  = ExtendedSwing createFormButtonWith ("Reset", Styles.formButtonRect)
    private val form         = BaseForm("Station", name, xField, yField, platforms)

    private val creationObservable                = Observers.createObservable[StationCreationInfo]
    private val deletionObservable                = Observers.createObservable[StationCreationInfo]
    private var _selectedStation: Option[Station] = Option.empty

    controlPanel.hGap = form.space
    controlPanel.contents += saveButton
    controlPanel.contents += deleteButton
    layoutButton.contents += controlPanel
    layoutButton.contents += Swing.VStrut(form.space)
    layoutButton.contents += resetButton.centerHorizontally()
    buttonPanel.contents += layoutButton

    resetButton attachClick CleanFormEvent(this)

    saveButton attach (creationObservable toObserver (_ =>
      StationCreationInfo(name.text, xField.text, yField.text, platforms.text)
    ))
    deleteButton attach (deletionObservable toObserver (_ =>
      StationCreationInfo(name.text, xField.text, yField.text, platforms.text)
    ))

    export form._, creationObservable.attachClick as attachCreation, deletionObservable.attachClick as attachDeletion

    override def selectedStation: Option[Station] = _selectedStation

    override def selectedStation_=(station: Station): Unit = _selectedStation = station.some

    override def resetSelectedStation(): Unit = _selectedStation = Option.empty

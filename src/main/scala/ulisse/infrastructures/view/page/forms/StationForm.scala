package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.StationEditorAdapter
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.manager.FormManager
import ulisse.infrastructures.view.map.{MapElement, MapPanel}
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.forms.StationForm.StationFormData

import scala.concurrent.ExecutionContext
import scala.swing.Swing
import scala.swing.event.MouseEvent

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

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

  /** Attach the creation observer to the form. */
  def attachCreation(observer: ClickObserver[StationFormData]): Unit

  /** Attach the deletion observer to the form. */
  def attachDeletion(observer: ClickObserver[StationFormData]): Unit

/** Companion object of the [[StationForm]]. */
object StationForm:

  /** Creates a new instance of station form. */
  def apply(): StationForm = StationFormImpl()

  /** Represents the station form data. */
  final case class StationFormData(name: String, x: String, y: String, tracks: String)

  /** Represents the creation station event. */
  final case class CreationStationEvent(adapter: StationEditorAdapter, forms: FormManager, map: MapPanel)
      extends ClickObserver[StationFormData]:
    override def onClick(data: StationFormData): Unit =
      val future = adapter.onOkClick(data.name, data.x, data.y, data.tracks, Option.empty)
      future.onComplete(_ map:
        case Left(error) => println(error)
        case Right(stations) =>
          map.uploadStation(stations)
          map.attachClickStation(StationForm.TakeStationFromMapEvent(forms.stationForm))
          map.attachClickStation(RouteForm.TakeStationFromMapEvent(forms.routeForm))
      )

  /** Represents the deletion station event. */
  final case class DeletionStationEvent(adapter: StationEditorAdapter, form: StationForm, map: MapPanel)
      extends ClickObserver[StationFormData]:
    override def onClick(data: StationFormData): Unit = ()

  /** Represents the take point from map event. */
  final case class TakePointFomMapEvent(stationForm: StationForm) extends ClickObserver[MouseEvent]:
    override def onClick(data: MouseEvent): Unit =
      stationForm.xField.text = data.point.x.toString
      stationForm.yField.text = data.point.y.toString

  /** Represents the take station from map event. */
  final case class TakeStationFromMapEvent(stationForm: StationForm) extends ClickObserver[MapElement[Station]]:
    override def onClick(data: MapElement[Station]): Unit =
      stationForm.name.text = data.element.name
      stationForm.xField.text = data.element.coordinate.x.toString
      stationForm.yField.text = data.element.coordinate.y.toString
      stationForm.tracks.text = data.element.numberOfTracks.toString

  private case class StationFormImpl() extends StationForm:
    override val name: ComposedSwing.InfoTextField   = ComposedSwing.createInfoTextField("Name")
    override val xField: ComposedSwing.InfoTextField = ComposedSwing.createInfoTextField("x")
    override val yField: ComposedSwing.InfoTextField = ComposedSwing.createInfoTextField("y")
    override val tracks: ComposedSwing.InfoTextField = ComposedSwing.createInfoTextField("Tracks")

    private val saveButton   = ExtendedSwing.createFormButtonWith("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing.createFormButtonWith("Delete", Styles.formFalseButtonRect)

    private val form = BaseForm("Station", name, xField, yField, tracks)

    private val creationObservable = Observers.createObservable[StationFormData]
    private val deletionObservable = Observers.createObservable[StationFormData]

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    saveButton.attach(creationObservable.toObserver(_ =>
      StationFormData(name.text, xField.text, yField.text, tracks.text)
    ))
    deleteButton.attach(deletionObservable.toObserver(_ =>
      StationFormData(name.text, xField.text, yField.text, tracks.text)
    ))

    export form._, creationObservable.attachClick as attachCreation, deletionObservable.attachClick as attachDeletion

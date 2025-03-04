package ulisse.infrastructures.view.page.forms

import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.forms.StationForm.StationFormData

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

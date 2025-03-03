package ulisse.infrastructures.view.page.forms

import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.{ClickObserver, Observer}
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.forms.StationForm.StationFormData

/** Represents the station form of the application. */
trait StationForm extends Form:
  def attachCreation(observer: ClickObserver[StationFormData]): Unit
  def attachDeletion(observer: ClickObserver[StationFormData]): Unit

/** Companion object of the [[StationForm]]. */
object StationForm:

  /** Creates a new instance of station form. */
  def apply(): StationForm = StationFormImpl()

  /** Represents the station form data. */
  final case class StationFormData(name: String, x: String, y: String, tracks: String)

  private case class StationFormImpl() extends StationForm:
    private val name         = ComposedSwing.createInfoTextField("Name")
    private val x            = ComposedSwing.createInfoTextField("x")
    private val y            = ComposedSwing.createInfoTextField("y")
    private val tracks       = ComposedSwing.createInfoTextField("Tracks")
    private val saveButton   = ExtendedSwing.createFormButtonWith("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing.createFormButtonWith("Delete", Styles.formFalseButtonRect)
    private val form         = BaseForm("Station", name, x, y, tracks)

    private val creationObservable = Observers.createObservable[StationFormData]
    private val deletionObservable = Observers.createObservable[StationFormData]

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    saveButton.attach(creationObservable.toObserver(_ => StationFormData(name.text, x.text, y.text, tracks.text)))
    deleteButton.attach(deletionObservable.toObserver(_ => StationFormData(name.text, x.text, y.text, tracks.text)))

    export form._, creationObservable.attachClick as attachCreation, deletionObservable.attachClick as attachDeletion

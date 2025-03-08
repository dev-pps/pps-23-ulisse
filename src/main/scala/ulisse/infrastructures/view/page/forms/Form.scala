package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.event.MouseEvent
import scala.swing.{Component, Orientation, Swing}

/** Represents the form of the application. */
trait Form extends ComposedSwing:
  /** Cleans the form. */
  def cleanForm(): Unit = ()

  /** Shows the error message. */
  def showError(error: String): Unit

  /** Resets the error message. */
  def resetError(): Unit = showError("")

/** Companion object of the [[Form]]. */
object Form:

  /** Creates a new instance of the form. */
  def createRoute(): RouteForm = RouteForm()

  /** Creates a new instance of the form. */
  def createStation(): StationForm = StationForm()

  /** Creates a new instance of the form. */
  def createTimetable(adapter: TimetableViewAdapter): TimetableForm = TimetableForm(adapter)

  /** Represents the clean form event. */
  final case class CleanFormEvent(form: Form) extends ClickObserver[MouseEvent]:
    override def onClick(data: MouseEvent): Unit =
      form.cleanForm()
      form.resetError()

  final case class BaseForm(title: String, fields: ComposedSwing.InfoTextField*):
    private val mainPanel: ExtendedSwing.SBoxPanel    = ExtendedSwing SBoxPanel Orientation.Vertical
    private val insertForm: ComposedSwing.JInsertForm = ComposedSwing.createInsertForm(title, fields: _*)
    val space                                         = 10

    private val boxPanel                      = ExtendedSwing SBoxPanel Orientation.Vertical
    private val errorLabel                    = ExtendedSwing SLabel "---"
    val buttonPanel: ExtendedSwing.SFlowPanel = ExtendedSwing.SFlowPanel()

    boxPanel.contents += errorLabel.centerHorizontally()
    boxPanel.contents += Swing VStrut space
    boxPanel.contents += buttonPanel

    mainPanel.rect = Styles.panelRect
    insertForm.titleLabel.fontEffect = Styles.titleFormFont

    buttonPanel.hGap = space

    mainPanel.contents += insertForm.component
    mainPanel.contents += boxPanel

    def showError(error: String): Unit = errorLabel.text = s"ERROR: $error"

    def component[T >: Component]: T = mainPanel

  case class TimetableForm(adapter: TimetableViewAdapter) extends Form:
    import ulisse.infrastructures.view.timetable.TimetableView
    private val mainPanel: ExtendedSwing.SBoxPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    mainPanel.rect = Styles.panelRect
    mainPanel.contents += TimetableView(adapter)

    override def showError(message: String): Unit = ()

    override def component[T >: Component]: T = mainPanel

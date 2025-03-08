package ulisse.infrastructures.view.page.forms

import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.components.ExtendedSwing.SLabel

import scala.swing.event.MouseEvent
import scala.swing.{Component, Orientation}

/** Represents the form of the application. */
trait Form extends ComposedSwing:
  /** Cleans the form. */
  def cleanForm(): Unit = ()

  /** Shows the error message. */
  def showErrorMessage(message: String): Unit

/** Companion object of the [[Form]]. */
object Form:

  def createRoute(): RouteForm                                      = RouteForm()
  def createStation(): StationForm                                  = StationForm()
  def createTimetable(adapter: TimetableViewAdapter): TimetableForm = TimetableForm(adapter)

  /** Represents the clean form event. */
  final case class CleanFormEvent(form: Form) extends ClickObserver[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = form.cleanForm()

  final case class BaseForm(title: String, fields: ComposedSwing.InfoTextField*):
    private val mainPanel: ExtendedSwing.SBoxPanel    = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val insertForm: ComposedSwing.JInsertForm = ComposedSwing.createInsertForm(title, fields: _*)
    private val space                                 = 10

    private val boxPanel                      = ExtendedSwing SBoxPanel Orientation.Vertical
    private val errorLabel                    = ExtendedSwing SLabel ""
    val buttonPanel: ExtendedSwing.SFlowPanel = ExtendedSwing.SFlowPanel()

    boxPanel.contents += errorLabel
    boxPanel.contents += buttonPanel

    mainPanel.rect = Styles.panelRect
    insertForm.titleLabel.fontEffect = Styles.titleFormFont

    buttonPanel.hGap = space

    mainPanel.contents += insertForm.component
    mainPanel.contents += boxPanel

    def showErrorMessage(message: String): Unit = errorLabel.text = message

    def component[T >: Component]: T = mainPanel

  case class TimetableForm(adapter: TimetableViewAdapter) extends Form:
    import ulisse.infrastructures.view.timetable.TimetableView
    private val mainPanel: ExtendedSwing.SBoxPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    mainPanel.rect = Styles.panelRect
    mainPanel.contents += TimetableView(adapter)

    override def showErrorMessage(message: String): Unit = ()

    override def component[T >: Component]: T = mainPanel

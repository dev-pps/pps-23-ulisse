package ulisse.infrastructures.view.page.forms

import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.event.MouseEvent
import scala.swing.{Component, Orientation}

trait Form extends ComposedSwing:
  def cleanForm(): Unit = ()

object Form:

  def createRoute(): RouteForm       = RouteForm()
  def createStation(): StationForm   = StationForm()
  def createSchedule(): ScheduleForm = ScheduleForm()

  /** Represents the clean form event. */
  final case class CleanFormEvent(form: Form) extends ClickObserver[MouseEvent]:
    override def onClick(data: MouseEvent): Unit = form.cleanForm()

  final case class BaseForm(title: String, fields: ComposedSwing.InfoTextField*):
    private val mainPanel: ExtendedSwing.SBoxPanel    = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    private val insertForm: ComposedSwing.JInsertForm = ComposedSwing.createInsertForm(title, fields: _*)
    private val space                                 = 10

    val buttonPanel: ExtendedSwing.SFlowPanel = ExtendedSwing.SFlowPanel()

    mainPanel.rect = Styles.panelRect
    insertForm.titleLabel.fontEffect = Styles.titleFormFont

    buttonPanel.hGap = space

    mainPanel.contents += insertForm.component
    mainPanel.contents += buttonPanel

    def component[T >: Component]: T = mainPanel

  case class ScheduleForm() extends Form:

    import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
    import ulisse.infrastructures.view.timetable.TimetableView

    private val mainPanel: ExtendedSwing.SBoxPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    mainPanel.rect = Styles.panelRect
    mainPanel.contents += TimetableView(TimetableInputPortMocked())

    override def component[T >: Component]: T = mainPanel

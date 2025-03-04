package ulisse.infrastructures.view.page.forms

import ulisse.infrastructures.view.common.Observers.Observer
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.{Component, Orientation, Point}

trait Form extends ComposedSwing

object Form:

  def createRoute(): RouteForm       = RouteForm()
  def createStation(): StationForm   = StationForm()
  def createSchedule(): ScheduleForm = ScheduleForm()

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

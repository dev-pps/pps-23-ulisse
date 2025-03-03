package ulisse.infrastructures.view.page.forms

import ulisse.infrastructures.view.common.Observers.Observer
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.{Component, Orientation, Point}

trait Form extends ComposedSwing
//  def mapObserver: Observer[Point]

object Form:

  def createRoute(): RouteForm       = RouteForm()
  def createStation(): StationForm   = StationForm()
  def createSchedule(): ScheduleForm = ScheduleForm()

  final case class BaseForm(title: String, fields: ComposedSwing.JInfoTextField*):
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

  case class RouteForm() extends Form with Observer[Point]:
    private val departureStation = ComposedSwing.createInfoTextField("Departure Station")
    private val arrivalStation   = ComposedSwing.createInfoTextField("Arrival Station")
    private val routeType        = ComposedSwing.createInfoTextField("Type")
    private val rails            = ComposedSwing.createInfoTextField("Rails")
    private val length           = ComposedSwing.createInfoTextField("Length")

    private val form       = BaseForm("Route", departureStation, arrivalStation, routeType, rails, length)
    private val saveButton = ExtendedSwing.SButton("Save")
    saveButton.rect = Styles.formTrueButtonRect
    saveButton.fontEffect = Styles.whiteFont
    private val deleteButton = ExtendedSwing.SButton("Delete")
    deleteButton.rect = Styles.formFalseButtonRect
    deleteButton.fontEffect = Styles.whiteFont

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._

    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var lastClick = false
    override def onClick(data: Point): Unit =
      lastClick match
        case false => departureStation.text_=(s"position: ${data.x} - ${data.y}"); lastClick = true
        case true  => arrivalStation.text_=(s"position: ${data.x} - ${data.y}"); lastClick = false
    override def onHover(data: Point): Unit   = ()
    override def onRelease(data: Point): Unit = ()
    override def onExit(data: Point): Unit    = ()

  case class ScheduleForm() extends Form:
    import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
    import ulisse.infrastructures.view.timetable.TimetableView

    private val mainPanel: ExtendedSwing.SBoxPanel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
    mainPanel.rect = Styles.panelRect
    mainPanel.contents += TimetableView(TimetableInputPortMocked())

    override def component[T >: Component]: T = mainPanel

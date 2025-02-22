package ulisse.infrastructures.view.common

import ulisse.infrastructures.view.components.ui.ComposedSwing
import scala.swing.Component

trait CentralController:
  def stationForm: Form
  def routeForm: Form
  def scheduleForm: Form
  def component[T >: Component]: T

object CentralController:
  def createMap(): MapController = MapController()

  private case class BaseCentralController(iconLabels: ComposedSwing.JIconLabel*)(forms: Form*):
    private val page: Map[ComposedSwing.JIconLabel, Form] = iconLabels.zip(forms).toMap
    private val tabbedPane: ComposedSwing.JTabbedPane     = ComposedSwing.createTabbedPane(iconLabels: _*)

    page.foreach(tabbedPane.paneOf(_).contents += _.component)

    def pageOf(label: ComposedSwing.JIconLabel): Form = page(label)
    def component[T >: Component]: T                  = tabbedPane.component

  case class MapController() extends CentralController:
    private val station: ComposedSwing.JIconLabel  = ComposedSwing.createIconLabel("icons/station.svg", "station")
    private val route: ComposedSwing.JIconLabel    = ComposedSwing.createIconLabel("icons/route.svg", "route")
    private val schedule: ComposedSwing.JIconLabel = ComposedSwing.createIconLabel("icons/train.svg", "schedule")

    private val menu: BaseCentralController =
      BaseCentralController(station, route, schedule)(Form.createStation(), Form.createRoute(), Form.createSchedule())

    export menu._

    override def stationForm: Form  = menu.pageOf(station)
    override def routeForm: Form    = menu.pageOf(route)
    override def scheduleForm: Form = menu.pageOf(schedule)

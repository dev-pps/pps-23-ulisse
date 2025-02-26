package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}

import scala.swing.{Component, Orientation}

trait CentralController extends ComposedSwing:
  def stationForm: Form
  def routeForm: Form
  def scheduleForm: Form

object CentralController:
  def createMap(): MapController = MapController()

  private case class BaseCentralController(iconLabels: ComposedImageLabel*)(forms: Form*):
    private val page: Map[ComposedImageLabel, Form]   = iconLabels.zip(forms).toMap
    private val tabbedPane: ComposedSwing.JTabbedPane = ComposedSwing.createTabbedPane(iconLabels: _*)

    page.foreach(tabbedPane.paneOf(_).contents += _.component)

    def pageOf(label: ComposedImageLabel): Form = page(label)
    def component[T >: Component]: T            = tabbedPane.component

  case class MapController() extends CentralController:
    given orientation: Orientation.Value = Orientation.Horizontal
    private val station                  = ComposedImageLabel.createIcon("icons/station.svg", "station")
    private val route                    = ComposedImageLabel.createIcon("icons/route.svg", "route")
    private val schedule                 = ComposedImageLabel.createIcon("icons/menu/train.svg", "schedule")

    private val menu: BaseCentralController =
      BaseCentralController(station, route, schedule)(Form.createStation(), Form.createRoute(), Form.createSchedule())

    export menu._

    override def stationForm: Form  = menu.pageOf(station)
    override def routeForm: Form    = menu.pageOf(route)
    override def scheduleForm: Form = menu.pageOf(schedule)

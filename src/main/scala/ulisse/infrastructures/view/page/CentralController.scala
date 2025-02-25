package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.components.ui.composed.{ComposedImageLabel, ComposedSwing}

import scala.swing.{Component, Orientation}

trait CentralController extends ComposedSwing:
  def stationForm: Form
  def routeForm: Form
  def scheduleForm: Form

object CentralController:
  def createMap(): MapController = MapController()

  private case class BaseCentralController(iconLabels: ComposedImageLabel.SVGIconLabel*)(forms: Form*):
    private val page: Map[ComposedImageLabel.SVGIconLabel, Form] = iconLabels.zip(forms).toMap
    private val tabbedPane: ComposedSwing.JTabbedPane            = ComposedSwing.createTabbedPane(iconLabels: _*)

    page.foreach(tabbedPane.paneOf(_).contents += _.component)

    def pageOf(label: ComposedImageLabel.SVGIconLabel): Form = page(label)
    def component[T >: Component]: T                         = tabbedPane.component

  case class MapController() extends CentralController:
    given orientation: Orientation.Value = Orientation.Horizontal
    private val station                  = ComposedImageLabel.createIconLabel("icons/station.svg", "station")
    private val route                    = ComposedImageLabel.createIconLabel("icons/route.svg", "route")
    private val schedule                 = ComposedImageLabel.createIconLabel("icons/menu/train.svg", "schedule")

    private val menu: BaseCentralController =
      BaseCentralController(station, route, schedule)(Form.createStation(), Form.createRoute(), Form.createSchedule())

    export menu._

    override def stationForm: Form  = menu.pageOf(station)
    override def routeForm: Form    = menu.pageOf(route)
    override def scheduleForm: Form = menu.pageOf(schedule)

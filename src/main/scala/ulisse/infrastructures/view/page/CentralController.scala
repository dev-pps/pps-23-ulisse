package ulisse.infrastructures.view.page

import ulisse.infrastructures.view.components.ui.composed.ComposedImage.Direction
import ulisse.infrastructures.view.components.ui.composed.ComposedImage.Direction.{Horizontal, Vertical}
import ulisse.infrastructures.view.components.ui.composed.{ComposedImage, ComposedSwing}
import ulisse.infrastructures.view.page.Form

import scala.swing.Component

trait CentralController extends ComposedSwing:
  def stationForm: Form
  def routeForm: Form
  def scheduleForm: Form

object CentralController:
  def createMap(): MapController = MapController()

  private case class BaseCentralController(iconLabels: ComposedImage.SVGIconLabel*)(forms: Form*):
    private val page: Map[ComposedImage.SVGIconLabel, Form] = iconLabels.zip(forms).toMap
    private val tabbedPane: ComposedSwing.JTabbedPane       = ComposedSwing.createTabbedPane(iconLabels: _*)

    page.foreach(tabbedPane.paneOf(_).contents += _.component)

    def pageOf(label: ComposedImage.SVGIconLabel): Form = page(label)
    def component[T >: Component]: T                    = tabbedPane.component

  case class MapController() extends CentralController:
    given direction: Direction = Horizontal
    private val station        = ComposedImage.createIconLabel("icons/station.svg", "station")
    private val route          = ComposedImage.createIconLabel("icons/route.svg", "route")
    private val schedule       = ComposedImage.createIconLabel("icons/menu/train.svg", "schedule")

    private val menu: BaseCentralController =
      BaseCentralController(station, route, schedule)(Form.createStation(), Form.createRoute(), Form.createSchedule())

    export menu._

    override def stationForm: Form  = menu.pageOf(station)
    override def routeForm: Form    = menu.pageOf(route)
    override def scheduleForm: Form = menu.pageOf(schedule)

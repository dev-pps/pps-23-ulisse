package ulisse.infrastructures.view.form

import ulisse.infrastructures.view.components.JComponent

import scala.swing.Component

trait CentralController:
  def component[T >: Component]: T

object CentralController:
  def createMap(): MapController = MapController()

  private case class BaseCentralController(iconLabels: JComponent.JIconLabel*)(forms: Form*) extends CentralController:
    private val page: Map[JComponent.JIconLabel, Form] = iconLabels.zip(forms).toMap

    private val tabbedPane = JComponent.createTabbedPane(iconLabels: _*)

    page.foreach(tabbedPane.paneOf(_).contents += _.component)

    override def component[T >: Component]: T = tabbedPane.component

  case class MapController() extends CentralController:
    private val station: JComponent.JIconLabel  = JComponent.createIconLabel("icons/station.svg", "station")
    private val route: JComponent.JIconLabel    = JComponent.createIconLabel("icons/route.svg", "route")
    private val schedule: JComponent.JIconLabel = JComponent.createIconLabel("icons/train.svg", "schedule")

    private val menu: BaseCentralController =
      BaseCentralController(station, route, schedule)(Form.createStation(), Form.createRoute(), Form.createSchedule())

    export menu._

package ulisse.infrastructures.view.manager

import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.page.forms.{Form, StationForm}
import ulisse.infrastructures.view.page.forms.Form.{RouteForm, ScheduleForm}

import scala.swing.{Component, Orientation}

trait FormManager extends ComposedSwing:
  def stationForm: StationForm
  def routeForm: RouteForm
  def scheduleForm: Form

object FormManager:
  def createMap(): FormManager = new FormManagerImpl()

  private case class BaseFormManager(iconLabels: ComposedImageLabel*)(forms: Form*):
    private val page: Map[ComposedImageLabel, Form]   = iconLabels.zip(forms).toMap
    private val tabbedPane: ComposedSwing.JTabbedPane = ComposedSwing.createTabbedPane(iconLabels: _*)

    page.map(tabbedPane.paneOf(_).contents += _.component)

    def pageOf(label: ComposedImageLabel): Form = page(label)
    def component[T >: Component]: T            = tabbedPane.component

  private case class FormManagerImpl(stationForm: StationForm, routeForm: RouteForm, scheduleForm: ScheduleForm)
      extends FormManager:
    def this() = this(StationForm(), Form.createRoute(), Form.createSchedule())
    given orientation: Orientation.Value = Orientation.Horizontal
    private val station                  = ComposedImageLabel.createIcon("icons/station.svg", "station")
    private val route                    = ComposedImageLabel.createIcon("icons/route.svg", "route")
    private val schedule                 = ComposedImageLabel.createIcon("icons/menu/train.svg", "schedule")

    private val menu: BaseFormManager =
      BaseFormManager(station, route, schedule)(stationForm, routeForm, scheduleForm)

    export menu._

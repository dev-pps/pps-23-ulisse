package ulisse.infrastructures.view.manager

import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.page.forms.Form.TimetableForm
import ulisse.infrastructures.view.page.forms.{Form, RouteForm, StationForm}

import scala.swing.{Component, Orientation}

/** Manager of the forms. */
trait FormManager extends ComposedSwing:
  /** The station form. */
  def stationForm: StationForm

  /** The route form. */
  def routeForm: RouteForm

  /** The timetable form. */
  def timetableForm: TimetableForm

/** Factory of the form manager. */
object FormManager:
  /** Create a form manager. */
  def createMap(adapter: TimetableViewAdapter): FormManager = new FormManagerImpl(adapter)

  private case class BaseFormManager(iconLabels: ComposedImageLabel*)(forms: Form*):
    private val page: Map[ComposedImageLabel, Form]   = iconLabels.zip(forms).toMap
    private val tabbedPane: ComposedSwing.JTabbedPane = ComposedSwing.createTabbedPane(iconLabels: _*)

    page.map(tabbedPane.paneOf(_).contents += _.component)

    /** Return the form of the label. */
    def pageOf(label: ComposedImageLabel): Form = page(label)

    /** Return the component of the form manager. */
    def component[T >: Component]: T = tabbedPane.component

  private case class FormManagerImpl(stationForm: StationForm, routeForm: RouteForm, timetableForm: TimetableForm)
      extends FormManager:
    def this(adapter: TimetableViewAdapter) = this(StationForm(), Form.createRoute(), Form.createTimetable(adapter))
    given orientation: Orientation.Value = Orientation.Horizontal
    private val station                  = ComposedImageLabel.createIcon("icons/station.svg", "station")
    private val route                    = ComposedImageLabel.createIcon("icons/route.svg", "route")
    private val timetable                = ComposedImageLabel.createIcon("icons/menu/train.svg", "timetable")

    private val menu: BaseFormManager =
      BaseFormManager(station, route, timetable)(stationForm, routeForm, timetableForm)

    export menu._

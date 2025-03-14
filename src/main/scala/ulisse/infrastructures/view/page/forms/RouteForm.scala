package ulisse.infrastructures.view.page.forms

import cats.syntax.option.*
import ulisse.adapters.input.RouteAdapter
import ulisse.adapters.input.RouteAdapter.RouteCreationInfo
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.page.forms.Form.{BaseForm, CleanFormEvent}
import ulisse.infrastructures.view.page.workspaces.MapWorkspace
import ulisse.infrastructures.view.utils.ComponentUtils.*
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext
import ulisse.utils.ValidationUtils.mkMsgErrors

import scala.swing.{Orientation, Swing}

/** Represents the route form of the application. */
trait RouteForm extends Form:
  /** The departure station field of the form. */
  val departureStation: ComposedSwing.InfoTextField

  /** The arrival station field of the form. */
  val arrivalStation: ComposedSwing.InfoTextField

  /** The route type field of the form. */
  val routeType: ComposedSwing.InfoTextField

  /** The rails field of the form. */
  val tracks: ComposedSwing.InfoTextField

  /** The length field of the form. */
  val length: ComposedSwing.InfoTextField

  /** The departure station of the route. */
  def departure: Option[Station]

  /** The arrival station of the route. */
  def arrival: Option[Station]

  /** The departure station of the route. */
  def departure_=(value: Option[Station]): Unit

  /** The arrival station of the route. */
  def arrival_=(station: Option[Station]): Unit

  /** The selected route of the form. */
  def selectedRoute: Option[Route]

  /** Set the selected route of the form. */
  def selectedRoute_=(route: Route): Unit

  /** Reset the selected route of the form. */
  def resetSelectedRoute(): Unit

  /** Attach the creation observer to the form of type [[RouteCreationInfo]]. */
  def attachCreation(observer: ClickObserver[RouteCreationInfo]): Unit

  /** Attach the deletion observer to the form of type [[RouteCreationInfo]]. */
  def attachDeletion(observer: ClickObserver[RouteCreationInfo]): Unit

  /** Compile the form. */
  def compileForm(route: Route): Unit

  /** Compute the distance between the departure and arrival stations if both are present. */
  def computeDistance(): Unit = (departure, arrival) match
    case (Some(departure), Some(arrival)) => length.text = departure.coordinate.distance(arrival.coordinate).toString
    case _                                => ()

/** Companion object of the [[RouteForm]]. */
object RouteForm:
  /** Creates a new instance of route form. */
  def apply(): RouteForm = RouteFormImpl()

  /** Represents the creation route event. */
  final case class CreationRouteEvent(adapter: RouteAdapter, workspace: MapWorkspace, form: RouteForm)
      extends ClickObserver[RouteCreationInfo]:

    private def creationRoute(data: RouteCreationInfo): Unit =
      adapter save (Option.empty, data) onComplete (_ fold (println, _ fold (error =>
        form.showError(s"${error.mkMsgErrors}"), workspace.updateRoutes)))

    private def updateRoute(data: RouteCreationInfo, oldRoute: Route): Unit =
      adapter save (Option(oldRoute), data) onComplete (_ fold (println, _ fold (error =>
        form.showError(s"${error.mkMsgErrors}"), workspace.updateRoutes)))

    override def onClick(data: RouteCreationInfo): Unit =
      form.resetError()
      workspace.selectedRoute.fold(creationRoute(data))(updateRoute(data, _))

  /** Represents the deletion route event. */
  final case class DeletionRouteEvent(adapter: RouteAdapter, workspace: MapWorkspace, form: RouteForm)
      extends ClickObserver[RouteCreationInfo]:

    override def onClick(data: RouteCreationInfo): Unit =
      form.resetError()
      adapter delete data onComplete (_ fold (println, _ fold (error =>
        form.showError(s"${error.mkMsgErrors}"), workspace.updateRoutes)))

  /** Represents the take station from map event. */
  final case class TakeStationFromMapEvent(routeForm: RouteForm) extends ClickObserver[MapElement[Station]]:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var chosenStation = true

    override def onClick(data: MapElement[Station]): Unit =
      if chosenStation then routeForm.departure = Option(data.element)
      else routeForm.arrival = Option(data.element)
      chosenStation = !chosenStation

  /** Represents the take route from map event. */
  final case class TakeRouteFromMapEvent(workspace: MapWorkspace) extends ClickObserver[MapElement[Route]]:
    override def onClick(data: MapElement[Route]): Unit =
      workspace.selectedRoute = data.element
      workspace.compileRouteForm(data.element)

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class RouteFormImpl() extends RouteForm:
    override val departureStation: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Departure Station"
    override val arrivalStation: ComposedSwing.InfoTextField   = ComposedSwing createInfoTextField "Arrival Station"
    override val routeType: ComposedSwing.InfoTextField        = ComposedSwing createInfoTextField "Type"
    override val tracks: ComposedSwing.InfoTextField           = ComposedSwing createInfoTextField "Tracks"
    override val length: ComposedSwing.InfoTextField           = ComposedSwing createInfoTextField "Length"

    private val layoutButton = ExtendedSwing.SBoxPanel(Orientation.Vertical).transparent()
    private val controlPanel = ExtendedSwing.SFlowPanel().transparent()
    private val saveButton   = ExtendedSwing createFormButtonWith ("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing createFormButtonWith ("Delete", Styles.formFalseButtonRect)
    private val resetButton  = ExtendedSwing createFormButtonWith ("Reset", Styles.formButtonRect)

    private val form = BaseForm("Route", departureStation, arrivalStation, routeType, tracks, length)

    private var _departure: Option[Station]   = Option.empty
    private var _arrival: Option[Station]     = Option.empty
    private var _selectedRoute: Option[Route] = Option.empty

    private val creationObservable = Observers.createObservable[RouteCreationInfo]
    private val deletionObservable = Observers.createObservable[RouteCreationInfo]

    controlPanel.hGap = form.space
    controlPanel.contents += saveButton
    controlPanel.contents += deleteButton
    layoutButton.contents += controlPanel
    layoutButton.contents += Swing.VStrut(form.space)
    layoutButton.contents += resetButton.centerHorizontally()
    buttonPanel.contents += layoutButton

    resetButton attachClick CleanFormEvent(this)

    saveButton attach (creationObservable toObserver (_ =>
      RouteCreationInfo(departure, arrival, routeType.text, tracks.text, length.text)
    ))
    deleteButton attach (deletionObservable toObserver (_ =>
      RouteCreationInfo(departure, arrival, routeType.text, tracks.text, length.text)
    ))

    export form._, creationObservable.attachClick as attachCreation, deletionObservable.attachClick as attachDeletion

    override def departure: Option[Station] = _departure

    override def arrival: Option[Station] = _arrival

    override def departure_=(station: Option[Station]): Unit =
      _departure = station
      station.foreach(data => departureStation.text = data.name)
      computeDistance()

    override def arrival_=(station: Option[Station]): Unit =
      _arrival = station
      station.foreach(data => arrivalStation.text = data.name)
      computeDistance()

    override def selectedRoute: Option[Route] = _selectedRoute

    override def selectedRoute_=(route: Route): Unit = _selectedRoute = route.some

    override def resetSelectedRoute(): Unit = _selectedRoute = Option.empty

    override def compileForm(route: Route): Unit =
      departure = route.departure.some
      arrival = route.arrival.some
      routeType.text = route.typology.toString
      tracks.text = route.railsCount.toString
      length.text = route.length.toString

    override def cleanForm(): Unit =
      departure = Option.empty
      arrival = Option.empty
      resetSelectedRoute()
      departureStation.text = ""
      arrivalStation.text = ""
      routeType.text = ""
      tracks.text = ""
      length.text = ""

package ulisse.infrastructures.view.page.forms

import ulisse.adapters.input.RouteAdapter
import ulisse.adapters.input.RouteAdapter.RouteCreationInfo
import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.page.forms.Form.BaseForm
import ulisse.infrastructures.view.page.workspaces.MapWorkspace
import ulisse.infrastructures.view.utils.Swings.given_ExecutionContext

/** Represents the route form of the application. */
trait RouteForm extends Form:
  /** The departure station field of the form. */
  val departureStation: ComposedSwing.InfoTextField

  /** The arrival station field of the form. */
  val arrivalStation: ComposedSwing.InfoTextField

  /** The route type field of the form. */
  val routeType: ComposedSwing.InfoTextField

  /** The rails field of the form. */
  val rails: ComposedSwing.InfoTextField

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

  /** Attach the creation observer to the form of type [[RouteCreationInfo]]. */
  def attachCreation(observer: ClickObserver[RouteCreationInfo]): Unit

  /** Attach the deletion observer to the form of type [[RouteCreationInfo]]. */
  def attachDeletion(observer: ClickObserver[RouteCreationInfo]): Unit

/** Companion object of the [[RouteForm]]. */
object RouteForm:
  /** Creates a new instance of route form. */
  def apply(): RouteForm = RouteFormImpl()

  /** Represents the creation route event. */
  final case class CreationRouteEvent(adapter: RouteAdapter, workspace: MapWorkspace)
      extends ClickObserver[RouteCreationInfo]:

    override def onClick(data: RouteCreationInfo): Unit =
      adapter.save(Option.empty, data).onComplete(_ fold (println, _ fold (println, workspace.updateRoutes)))

  /** Represents the take station from map event. */
  final case class TakeStationFromMapEvent(routeForm: RouteForm) extends ClickObserver[MapElement[Station]]:
    override def onClick(data: MapElement[Station]): Unit =
      routeForm.departure match
        case Some(value) =>
          routeForm.arrivalStation.text = data.element.name
          routeForm.length.text = (value.coordinate distance data.element.coordinate).toString
          routeForm.departure = Option.empty
        case None =>
          routeForm.departure = Option(data.element)
          routeForm.departureStation.text = data.element.name

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private case class RouteFormImpl() extends RouteForm:
    override val departureStation: ComposedSwing.InfoTextField = ComposedSwing createInfoTextField "Departure Station"
    override val arrivalStation: ComposedSwing.InfoTextField   = ComposedSwing createInfoTextField "Arrival Station"
    override val routeType: ComposedSwing.InfoTextField        = ComposedSwing createInfoTextField "Type"
    override val rails: ComposedSwing.InfoTextField            = ComposedSwing createInfoTextField "Rails"
    override val length: ComposedSwing.InfoTextField           = ComposedSwing createInfoTextField "Length"
    private val saveButton   = ExtendedSwing createFormButtonWith ("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing createFormButtonWith ("Delete", Styles.formFalseButtonRect)
    private val form         = BaseForm("Route", departureStation, arrivalStation, routeType, rails, length)

    private var _departure: Option[Station] = Option.empty
    private var _arrival: Option[Station]   = Option.empty

    private val creationObservable = Observers.createObservable[RouteCreationInfo]
    private val deletionObservable = Observers.createObservable[RouteCreationInfo]

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

//    saveButton.attach(creationObservable toObserver (_ =>
//        RouteCreationInfo(depa
//    ))
//    deleteButton.attach(deletionObservable toObserver (_ =>
//      RouteCreationInfo(name.text, xField.text, yField.text, tracks.text)
//      ))

    export form._, creationObservable.attachClick as attachCreation, deletionObservable.attachClick as attachDeletion

    override def departure: Option[Station] = _departure

    override def arrival: Option[Station] = _arrival

    override def departure_=(station: Option[Station]): Unit = _departure = station

    override def arrival_=(station: Option[Station]): Unit = _arrival = station

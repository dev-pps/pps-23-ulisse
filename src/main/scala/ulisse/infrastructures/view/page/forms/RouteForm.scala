package ulisse.infrastructures.view.page.forms

import ulisse.entities.station.Station
import ulisse.infrastructures.view.common.Observers.ClickObserver
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.map.MapElement
import ulisse.infrastructures.view.page.forms.Form.BaseForm

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

/** Companion object of the [[RouteForm]]. */
object RouteForm:
  /** Creates a new instance of route form. */
  def apply(): RouteForm = RouteFormImpl()

  /** Represents the take station from map event. */
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class TakeStationFromMapEvent(routeForm: RouteForm) extends ClickObserver[MapElement[Station]]:
    // TODO: da spostate nella form quando si attacca l'adapter
    private var departureStation: Option[Station] = Option.empty
    private val arrivalStation: Option[Station]   = Option.empty

    override def onClick(data: MapElement[Station]): Unit =
      departureStation match
        case Some(value) =>
          routeForm.arrivalStation.text = data.element.name
          routeForm.length.text = value.coordinate.distance(data.element.coordinate).toString
          departureStation = Option.empty
        case None =>
          departureStation = Option(data.element)
          routeForm.departureStation.text = data.element.name

  private case class RouteFormImpl() extends RouteForm:
    override val departureStation: ComposedSwing.InfoTextField = ComposedSwing.createInfoTextField("Departure Station")
    override val arrivalStation: ComposedSwing.InfoTextField   = ComposedSwing.createInfoTextField("Arrival Station")
    override val routeType: ComposedSwing.InfoTextField        = ComposedSwing.createInfoTextField("Type")
    override val rails: ComposedSwing.InfoTextField            = ComposedSwing.createInfoTextField("Rails")
    override val length: ComposedSwing.InfoTextField           = ComposedSwing.createInfoTextField("Length")
    private val saveButton   = ExtendedSwing.createFormButtonWith("Save", Styles.formTrueButtonRect)
    private val deleteButton = ExtendedSwing.createFormButtonWith("Delete", Styles.formFalseButtonRect)
    private val form         = BaseForm("Route", departureStation, arrivalStation, routeType, rails, length)

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._

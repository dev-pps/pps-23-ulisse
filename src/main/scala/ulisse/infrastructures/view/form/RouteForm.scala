package ulisse.infrastructures.view.form

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route
import ulisse.entities.Route.TypeRoute
import ulisse.infrastructures.view.common.{FormPanel, KeyValuesPanel}

import scala.swing.*

trait RouteForm extends FormPanel[BorderPanel]:
  def routeType: Option[ComboBox[String]]
  def departureStation: Seq[TextField]
  def arrivalStation: Seq[TextField]
  def railsCount: Option[TextField]
  def length: Option[TextField]

  def setDepartureStation(name: String, x: Double, y: Double): Unit
  def setArrivalStation(name: String, x: Double, y: Double): Unit
  def create(): Option[Route]

object RouteForm:

  private def createForm(using opaque: Boolean): FormPanel[BorderPanel] =
    val typeRoute = KeyValuesPanel(FlowPanel())(Label("Route Type"), ComboBox(Seq("Normal", "AV")))
    val departureStation =
      KeyValuesPanel(FlowPanel())(Label("Departure Station"), TextField(5), TextField(3), TextField(3))
    val arrivalStation = KeyValuesPanel(FlowPanel())(Label("Arrival Station"), TextField(5), TextField(3), TextField(3))
    val railsCount     = KeyValuesPanel(FlowPanel())(Label("Rails Count"), TextField(10))
    val length         = KeyValuesPanel(FlowPanel())(Label("Length"), TextField(10))
    FormPanel(BorderPanel(), typeRoute, departureStation, arrivalStation, length, railsCount)

  def apply()(using opaque: Boolean): RouteForm = RouteFormImpl(createForm)

  private enum Fields(val index: Int):
    case RouteType        extends Fields(0)
    case DepartureStation extends Fields(1)
    case ArrivalStation   extends Fields(2)
    case RailsCount       extends Fields(3)
    case Length           extends Fields(4)

  private case class RouteFormImpl(form: FormPanel[BorderPanel]) extends RouteForm:
    export form.*

    override def routeType: Option[ComboBox[String]] =
      form.keyValuesPanel(Fields.RouteType.index).values[ComboBox[String]].headOption

    override def departureStation: Seq[TextField] = form.keyValuesPanel(Fields.DepartureStation.index).values[TextField]

    override def arrivalStation: Seq[TextField] = form.keyValuesPanel(2).values[TextField]

    override def railsCount: Option[TextField] = form.keyValuesPanel(3).values[TextField].headOption

    override def length: Option[TextField] = form.keyValuesPanel(4).values[TextField].headOption

    override def setDepartureStation(name: String, x: Double, y: Double): Unit =
      List(name, x.toString, y.toString).zip(departureStation).foreach((v, f) => f.text = v)

    override def setArrivalStation(name: String, x: Double, y: Double): Unit =
      List(name, x.toString, y.toString).zip(arrivalStation).foreach((v, f) => f.text = v)

    override def create(): Option[Route] =
      for {
        typeRoute  <- form.keyValuesPanel(0).values[ComboBox[String]].headOption
        length     <- form.keyValuesPanel(3).values[TextField].headOption
        railsCount <- form.keyValuesPanel(4).values[TextField].headOption
      } yield Route(
        TypeRoute.valueOf(typeRoute.selection.item),
        (
          (departureStation(0).text, Coordinate(departureStation(1).text.toDouble, departureStation(2).text.toDouble)),
          (arrivalStation(0).text, Coordinate(arrivalStation(1).text.toDouble, arrivalStation(2).text.toDouble))
        ),
        30.0d,
        2
      )

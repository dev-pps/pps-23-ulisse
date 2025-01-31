package ulisse.infrastructures.view.form

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route
import ulisse.entities.Route.TypeRoute
import ulisse.infrastructures.view.common.Theme.{light, Light}
import ulisse.infrastructures.view.common.{FormPanel, KeyValuesPanel, Theme}
import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JStyler.*

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
  private val buttonPickerStyler =
    rectPaletteStyler(rect(40, 20, 10), palette(light.element, light.forwardClick, light.hover))
  private val textFieldStyler =
    rectPaletteStyler(rect(40, 20, 10), backgroundHoverPalette(light.element, light.hover))

  private def createForm(using opaque: Boolean): FormPanel[BorderPanel] =
    val typeRoute = KeyValuesPanel(FlowPanel())(Label("Type"), ComboBox(Seq("Normal", "AV")))

    val departureStation =
      KeyValuesPanel(FlowPanel())(
        JComponent.label("Departure", paletteStyler(backgroundPalette(transparentColor))),
        JComponent.textField(5, textFieldStyler),
        JComponent.textField(4, textFieldStyler),
        JComponent.textField(4, textFieldStyler),
        JComponent.button("...", buttonPickerStyler)
      )
    val arrivalStation =
      KeyValuesPanel(FlowPanel())(
        JComponent.label("Arrival", paletteStyler(backgroundPalette(transparentColor))),
        JComponent.textField(5, textFieldStyler),
        JComponent.textField(4, textFieldStyler),
        JComponent.textField(4, textFieldStyler),
        JComponent.button("...", buttonPickerStyler)
      )
    val railsCount = KeyValuesPanel(FlowPanel())(
      Label("Rails Count"),
      JComponent.textField(3, textFieldStyler)
    )
    val length = KeyValuesPanel(FlowPanel())(
      Label("Length"),
      JComponent.textField(3, textFieldStyler)
    )
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
    form.panel().opaque = true
    form.panel().background = light.background

    private val buttonRect: Rect = rect(85, 20, 10)
    saveButton.setStyler(rectPaletteStyler(buttonRect, palette(light.element, light.trueClick, light.hover)))
    deleteButton.setStyler(rectPaletteStyler(buttonRect, palette(light.element, light.falseClick, light.hover)))
    exitButton.setStyler(rectPaletteStyler(buttonRect, palette(light.element, light.backwardClick, light.hover)))

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
          (
            departureStation(0).text,
            Coordinate.geo(departureStation(1).text.toDouble, departureStation(2).text.toDouble)
          ),
          (arrivalStation(0).text, Coordinate.geo(arrivalStation(1).text.toDouble, arrivalStation(2).text.toDouble))
        ),
        30.0d,
        2
      )

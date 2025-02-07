package ulisse.infrastructures.view.common

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.{JComponent, JItem, JStyler}
import ulisse.infrastructures.view.map.ViewObservers.ViewObserver

import scala.swing.Font.Style
import scala.swing.{Component, Orientation, Point}

trait Form:
  def mapObserver: ViewObserver[Point]
  def component[T >: Component]: T

object Form:

  def createRoute(): RouteForm       = RouteForm()
  def createStation(): StationForm   = StationForm()
  def createSchedule(): ScheduleForm = ScheduleForm()

  private val titleFont = JStyler.defaultFont.copy(styleFont = Style.Bold, colorFont = Theme.light.text, sizeFont = 36)

  private val formStyler =
    JStyler.rectPaletteStyler(
      JStyler.defaultRect.copy(padding = JStyler.createPadding(40, 0), arc = 15),
      JStyler.backgroundPalette(Theme.light.element)
    )

  private val buttonStyler =
    JStyler.rectPaletteFontStyler(
      JStyler.defaultRect.copy(padding = JStyler.createPadding(20, 10), arc = 10),
      JStyler.backgroundHoverPalette(Theme.light.text, Theme.light.forwardClick),
      JStyler.defaultFont.copy(colorFont = Theme.light.background)
    )

  private val trueButtonStyler =
    buttonStyler.copy(palette = buttonStyler.palette.copy(clickColor = Some(Theme.light.trueClick)))
  private val falseButtonStyler =
    buttonStyler.copy(palette = buttonStyler.palette.copy(clickColor = Some(Theme.light.falseClick)))

  private case class BaseForm(title: String, fields: JComponent.JInfoTextField*):
    private val mainPanel: JItem.JBoxPanelItem     = JItem.createBoxPanel(Orientation.Vertical, formStyler)
    private val insertForm: JComponent.JInsertForm = JComponent.createInsertForm(title, fields: _*)
    private val space                              = 10

    val buttonPanel: JItem.JFlowPanelItem = JItem.createFlowPanel(JStyler.transparent)

    insertForm.titleLabel.setStyler(insertForm.titleLabel.getStyler.copy(font = titleFont))

    buttonPanel.hGap = space

    mainPanel.contents += insertForm.component
    mainPanel.contents += buttonPanel

    def component[T >: Component]: T = mainPanel

  case class RouteForm() extends Form with ViewObserver[Point]:
    private val departureStation = JComponent.createInfoTextField("Departure Station")
    private val arrivalStation   = JComponent.createInfoTextField("Arrival Station")
    private val routeType        = JComponent.createInfoTextField("Type")
    private val rails            = JComponent.createInfoTextField("Rails")
    private val length           = JComponent.createInfoTextField("Length")

    private val form         = BaseForm("Route", departureStation, arrivalStation, routeType, rails, length)
    private val saveButton   = JItem.button("Save", trueButtonStyler)
    private val deleteButton = JItem.button("Delete", falseButtonStyler)

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._
    override def mapObserver: ViewObserver[Point] = this

    override def onClick(data: Point): Unit   = ()
    override def onHover(data: Point): Unit   = ()
    override def onRelease(data: Point): Unit = ()

  case class StationForm() extends Form with ViewObserver[Point]:
    private val name      = JComponent.createInfoTextField("Name")
    private val latitude  = JComponent.createInfoTextField("Latitude")
    private val longitude = JComponent.createInfoTextField("Longitude")
    private val tracks    = JComponent.createInfoTextField("Tracks")

    private val form = BaseForm("Station", name, latitude, longitude, tracks)

    private val saveButton   = JItem.button("Save", trueButtonStyler)
    private val deleteButton = JItem.button("Delete", falseButtonStyler)

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._
    override def mapObserver: ViewObserver[Point] = this

    override def onClick(data: Point): Unit =
      latitude.text_=(data.x.toString)
      longitude.text_=(data.y.toString)

    override def onHover(data: Point): Unit   = ()
    override def onRelease(data: Point): Unit = ()

  case class ScheduleForm() extends Form with ViewObserver[Point]:
    private val field  = JComponent.createInfoTextField("Field")
    private val field1 = JComponent.createInfoTextField("Field1")
    private val field2 = JComponent.createInfoTextField("Field2")

    private val form = BaseForm("Schedule", field, field1, field2)

    private val saveButton   = JItem.button("Save", trueButtonStyler)
    private val deleteButton = JItem.button("Delete", falseButtonStyler)

    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._
    override def mapObserver: ViewObserver[Point] = this
    override def onClick(data: Point): Unit       = ()
    override def onHover(data: Point): Unit       = ()
    override def onRelease(data: Point): Unit     = ()

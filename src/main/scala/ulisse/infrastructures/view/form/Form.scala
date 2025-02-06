package ulisse.infrastructures.view.form

import ulisse.infrastructures.view.common.Theme
import ulisse.infrastructures.view.components.{JComponent, JItem, JStyler}

import scala.swing.{Component, Orientation, Swing}

trait Form:
  def component[T >: Component]: T

object Form:

  def createRoute(): RouteForm     = RouteForm()
  def createStation(): StationForm = StationForm()

  private val elementStyler =
    JStyler.rectPaletteStyler(JStyler.roundRect(10), JStyler.backgroundPalette(Theme.light.element))
  private val buttonStyler =
    JStyler.rectPaletteStyler(JStyler.roundRect(10), JStyler.backgroundPalette(Theme.light.hover))

  private case class BaseForm(title: String, fields: JComponent.JInfoTextField*) extends Form:
    private val mainPanel: JItem.JBoxPanelItem     = JItem.createBoxPanel(Orientation.Vertical, elementStyler)
    private val insertForm: JComponent.JInsertForm = JComponent.createInsertForm(title, fields: _*)
    val buttonPanel: JItem.JFlowPanelItem          = JItem.createFlowPanel(JStyler.transparent)

    val space = 10

    mainPanel.contents += insertForm.component
    mainPanel.contents += buttonPanel
    mainPanel.contents += Swing.VStrut(space)

    override def component[T >: Component]: T = mainPanel

  case class RouteForm() extends Form:
    private val departureStation = JComponent.createInfoTextField("Departure Station")
    private val arrivalStation   = JComponent.createInfoTextField("Arrival Station")
    private val routeType        = JComponent.createInfoTextField("Type")
    private val rails            = JComponent.createInfoTextField("Rails")
    private val length           = JComponent.createInfoTextField("Length")

    private val form         = BaseForm("Route", departureStation, arrivalStation, routeType, rails, length)
    private val saveButton   = JItem.button("Save", buttonStyler)
    private val deleteButton = JItem.button("Delete", buttonStyler)

    buttonPanel.hGap = form.space
    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._

  case class StationForm() extends Form:
    private val name      = JComponent.createInfoTextField("Name")
    private val latitude  = JComponent.createInfoTextField("Latitude")
    private val longitude = JComponent.createInfoTextField("Longitude")
    private val tracks    = JComponent.createInfoTextField("Tracks")

    private val form = BaseForm("Route", name, latitude, longitude, tracks)

    private val saveButton   = JItem.button("Save", buttonStyler)
    private val deleteButton = JItem.button("Delete", buttonStyler)

    buttonPanel.hGap = space
    buttonPanel.contents += saveButton
    buttonPanel.contents += deleteButton

    export form._

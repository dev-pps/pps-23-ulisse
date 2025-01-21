package ulisse.infrastructures.view.common

import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Route
import ulisse.entities.Route.TypeRoute

import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.Font.Style
import scala.swing.*

trait FormPanel[+MP <: Panel] extends WrapPanel[MP]:
  def saveButton(): Button
  def deleteButton(): Button
  def exitButton(): Button
  def create(): Option[Route]

object FormPanel:
  def apply[MP <: BorderPanel, P <: Panel](panel: MP, pairs: KeyValuesPanel[P]*)(using opaque: Boolean): FormPanel[MP] =
    FormPanelImpl(panel, pairs: _*)

  private case class FormPanelImpl[+MP <: BorderPanel, +P <: Panel](mainPanel: MP, keyValuesPanel: KeyValuesPanel[P]*)(
      using opaque: Boolean
  ) extends FormPanel[MP]:
    private val title  = Label("Route")
    private val save   = new Button("save")
    private val delete = new Button("delete")
    private val exit   = new Button("exit")

    private val centralBox: BoxPanel    = BoxPanel(Orientation.Vertical)
    private val southBox: BoxPanel      = BoxPanel(Orientation.Vertical)
    private val managerPanel: FlowPanel = FlowPanel(save, delete)

    southBox.contents += managerPanel
    southBox.contents += FlowPanel(Swing.HGlue, exit, Swing.HGlue)

    title.font = Font("Arial", Style.Bold, 24)

    centralBox.contents += Swing.Glue
    centralBox.contents += title
    centralBox.contents ++= keyValuesPanel.map(_.panel())
    centralBox.contents += Swing.Glue

    mainPanel.layout(centralBox) = Center
    mainPanel.layout(southBox) = South

    private val wrapPanel: WrapPanel[MP] = WrapPanel(mainPanel)(centralBox, southBox)

    export wrapPanel.*
    override def saveButton(): Button   = save
    override def deleteButton(): Button = delete
    override def exitButton(): Button   = exit

    override def create(): Option[Route] =
      for {
        typeRoute        <- keyValuesPanel(0).values[ComboBox[String]].headOption
        departureStation <- keyValuesPanel(1).values[TextField].headOption
        arrivalStation   <- keyValuesPanel(2).values[TextField].headOption
        length           <- keyValuesPanel(3).values[TextField].headOption
        railsCount       <- keyValuesPanel(4).values[TextField].headOption
      } yield Route(
        TypeRoute.valueOf(typeRoute.selection.item),
        (("Rimini", Coordinate(10.0d, 10.0d)), ("Cesena", Coordinate(20.0d, 20.0d))),
        30.0d,
        2
      )

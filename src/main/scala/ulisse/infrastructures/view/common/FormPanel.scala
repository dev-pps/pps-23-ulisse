package ulisse.infrastructures.view.common

import scala.swing.*
import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.Font.Style

trait FormPanel[+MP <: Panel, +A <: Component, +B <: Component] extends WrapPanel[MP]:
  def saveButton(): Button
  def deleteButton(): Button
  def exitButton(): Button
  def form(): List[String]

object FormPanel:

  def apply[MP <: BorderPanel, P <: Panel, A <: Component, B <: Component](
      panel: MP,
      pairs: PairPanel[P, A, B]*
  )(using opaque: Boolean): FormPanel[MP, A, B] = FormPanelImpl(panel, pairs: _*)

  private case class FormPanelImpl[+MP <: BorderPanel, +P <: Panel, +A <: Component, +B <: Component](
      mainPanel: MP,
      pairPanels: PairPanel[P, A, B]*
  )(using opaque: Boolean) extends FormPanel[MP, A, B]:
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

    centralBox.contents += Swing.VGlue
    centralBox.contents += title
    centralBox.contents ++= pairPanels.map(_.panel())
    centralBox.contents += Swing.VGlue

    mainPanel.layout(centralBox) = Center
    mainPanel.layout(southBox) = South

    private val wrapPanel: WrapPanel[MP] = WrapPanel(mainPanel)(centralBox, southBox)

    export wrapPanel.*
    override def saveButton(): Button   = save
    override def deleteButton(): Button = delete
    override def exitButton(): Button   = exit

    override def form(): List[String] = pairPanels.map(_.value.toString).toList

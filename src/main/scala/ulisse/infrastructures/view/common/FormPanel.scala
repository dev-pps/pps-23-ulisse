package ulisse.infrastructures.view.common

import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JComponent.JButton
import ulisse.infrastructures.view.components.JStyler.*

import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.{Font as SwingFont, *}
import scala.swing.Font.Style

trait FormPanel[+MP <: Panel] extends WrapPanel[MP]:
  def keyValuesPanel: Seq[KeyValuesPanel[Panel]]
  def saveButton(): JButton
  def deleteButton(): JButton
  def exitButton(): JButton

object FormPanel:
  def apply[MP <: BorderPanel, P <: Panel](panel: MP, pairs: KeyValuesPanel[P]*)(using opaque: Boolean): FormPanel[MP] =
    FormPanelImpl(panel, pairs: _*)

  private case class FormPanelImpl[+MP <: BorderPanel, +P <: Panel](mainPanel: MP, keyValuesPanel: KeyValuesPanel[P]*)(
      using opaque: Boolean
  ) extends FormPanel[MP]:
    private val title  = Label("Route")
    private val save   = JComponent.button("save", defaultStyler)
    private val delete = JComponent.button("delete", defaultStyler)
    private val exit   = JComponent.button("exit", defaultStyler)

    private val centralBox: BoxPanel    = BoxPanel(Orientation.Vertical)
    private val northPanel: FlowPanel   = FlowPanel(title)
    private val southBox: BoxPanel      = BoxPanel(Orientation.Vertical)
    private val managerPanel: FlowPanel = FlowPanel(save, delete)

    southBox.contents += managerPanel
    southBox.contents += FlowPanel(Swing.HGlue, exit, Swing.HGlue)

    title.font = SwingFont("Arial", Style.Bold, 24)

    centralBox.contents += Swing.Glue
    centralBox.contents += northPanel
    centralBox.contents ++= keyValuesPanel.map(_.panel())
    centralBox.contents += Swing.Glue

    mainPanel.layout(centralBox) = Center
    mainPanel.layout(southBox) = South

    private val wrapPanel: WrapPanel[MP] = WrapPanel(mainPanel)(centralBox, southBox)

    export wrapPanel.*
    override def saveButton(): JButton   = save
    override def deleteButton(): JButton = delete
    override def exitButton(): JButton   = exit

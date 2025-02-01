package ulisse.infrastructures.view.common

import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JComponent.JButton
import ulisse.infrastructures.view.components.JStyler.*

import scala.swing.BorderPanel.Position.{Center, South}
import scala.swing.Font.Style
import scala.swing.{Font as SwingFont, *}

trait FormPanel[+MP <: Panel] extends WrapPanel[MP]:
  val saveButton: JButton
  val deleteButton: JButton
  val exitButton: JButton

  def keyValuesPanel: Seq[KeyValuesPanel[Panel]]

object FormPanel:
  def apply[MP <: BorderPanel, P <: Panel](panel: MP, pairs: KeyValuesPanel[P]*)(using opaque: Boolean): FormPanel[MP] =
    FormPanelImpl(panel, pairs: _*)

  private case class FormPanelImpl[+MP <: BorderPanel, +P <: Panel](mainPanel: MP, keyValuesPanel: KeyValuesPanel[P]*)(
      using opaque: Boolean
  ) extends FormPanel[MP]:
    private val title                  = Label("Route")
    override val saveButton: JButton   = JComponent.button("save", defaultStyler)
    override val deleteButton: JButton = JComponent.button("delete", defaultStyler)
    override val exitButton: JButton   = JComponent.button("exit", defaultStyler)

    private val centralBox: BoxPanel     = BoxPanel(Orientation.Vertical)
    private val northPanel: FlowPanel    = FlowPanel(title)
    private val southBox: BoxPanel       = BoxPanel(Orientation.Vertical)
    private val managerPanel: FlowPanel  = FlowPanel(saveButton, deleteButton)
    private val exitPanel: FlowPanel     = FlowPanel(Swing.HGlue, exitButton, Swing.HGlue)
    private val wrapPanel: WrapPanel[MP] = WrapPanel(mainPanel)(centralBox, southBox)

    northPanel.opaque = false
    centralBox.opaque = false
    southBox.opaque = false
    managerPanel.opaque = false
    exitPanel.opaque = false
    mainPanel.opaque = false

    southBox.contents += managerPanel
    southBox.contents += exitPanel

    title.font = SwingFont("Arial", Style.Bold, 24)

    centralBox.contents += Swing.Glue
    centralBox.contents += northPanel
    centralBox.contents ++= keyValuesPanel.map(_.panel())
    centralBox.contents += Swing.Glue

    mainPanel.layout(centralBox) = Center
    mainPanel.layout(southBox) = South

    export wrapPanel.*

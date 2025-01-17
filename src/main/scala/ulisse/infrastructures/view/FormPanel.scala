package ulisse.infrastructures.view

import scala.swing.*
import scala.swing.BorderPanel.Position.{Center, North, South}

trait FormPanel[
    +MP <: Panel,
    +P <: Panel,
    +A <: Component,
    +B <: Component
] extends WrapPanel[MP]:
  def buttonForm(): Button
  def form(): List[String]

object FormPanel:

  def apply[
      MP <: BorderPanel,
      P <: Panel,
      A <: Component,
      B <: Component
  ](
      panel: WrapPanel[MP],
      pairPanels: List[PairPanel[P, A, B]]
  ): FormPanel[_, _, _, _] = FormPanelImpl(panel, pairPanels)

  private case class FormPanelImpl[
      +MP <: BorderPanel,
      +P <: Panel,
      +A <: Component,
      +B <: Component,
      PP <: PairPanel[P, A, B]
  ](
      mainPanel: WrapPanel[MP],
      pairPanels: List[PairPanel[P, A, B]]
  ) extends FormPanel[MP, P, A, B]:
    private val submit             = new Button("Submit")
    private val boxPanel: BoxPanel = BoxPanel(Orientation.Vertical)

    boxPanel.contents += Swing.Glue
    boxPanel.contents ++= pairPanels.map(_.panel)
    boxPanel.contents += Swing.Glue

    mainPanel.panel.layout(boxPanel) = Center
    mainPanel.panel.layout(submit) = South

    export mainPanel.*

    override def buttonForm(): Button = submit

    override def form(): List[String] = pairPanels.map(_.value)

package view

import scala.swing.*

trait FormPanel[
    +MP <: Panel,
    +P <: Panel,
    +A <: Component,
    +B <: Component
] extends WrapPanel[MP]

object FormPanel:

  def apply[
      MP <: Panel,
      P <: Panel,
      A <: Component,
      B <: Component
  ](
      panel: WrapPanel[MP],
      pairPanels: List[PairPanel[P, A, B]]
  ): FormPanel[_, _, _, _] = FormPanelImpl(panel, pairPanels)

  private case class FormPanelImpl[
      +MP <: Panel,
      +P <: Panel,
      +A <: Component,
      +B <: Component,
      PP <: PairPanel[P, A, B]
  ](
      mainPanel: WrapPanel[MP],
      pairPanels: List[PairPanel[P, A, B]]
  ) extends FormPanel[MP, P, A, B]:
    private val submit = new Button("Submit")
    pairPanels.foreach(pair => mainPanel.addComponent(pair.panel))
    mainPanel.addComponent(submit)

    export mainPanel.panel, mainPanel.addComponent, mainPanel.setVisible

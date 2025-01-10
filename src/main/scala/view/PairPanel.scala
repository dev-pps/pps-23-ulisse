package view

import scala.swing.{Component, FlowPanel, Panel}

trait PairPanel[+P <: Panel, +A <: Component, +B <: Component]
    extends WrapPanel[P]

object PairPanel:
  def apply[P <: Panel, A <: Component, B <: Component](
      panel: WrapPanel[P],
      first: A,
      second: B
  ): PairPanel[P, A, B] = PairPanelImpl(panel, first, second)

  private case class PairPanelImpl[
      +P <: Panel,
      +A <: Component,
      +B <: Component
  ](
      mainPanel: WrapPanel[P],
      first: A,
      second: B
  ) extends PairPanel[P, A, B]:
    mainPanel.addComponent(first)
    mainPanel.addComponent(second)

    export mainPanel.panel, mainPanel.addComponent, mainPanel.setVisible

//    panel.contents += Seq(first, second)

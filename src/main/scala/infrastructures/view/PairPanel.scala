package infrastructures.view

import java.awt.Color
import javax.swing.BorderFactory
import scala.swing.{Component, Dimension, Panel}

trait PairPanel[+P <: Panel, +A <: Component, +B <: Component]
    extends WrapPanel[P]:
  def key: String
  def value: String

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
    mainPanel.panel.maximumSize = new Dimension(400, 40)

    export mainPanel.*

    override def key: String = first.toString

    override def value: String = second.toString

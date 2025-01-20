package ulisse.infrastructures.view.common

import scala.swing.{Component, Dimension, Panel}

trait PairPanel[+P <: Panel, +A <: Component, +B <: Component] extends WrapPanel[P]:
  def key: A
  def value: B

object PairPanel:
  def apply[P <: Panel, A <: Component, B <: Component](panel: P, first: A, second: B)(using
      opaque: Boolean
  ): PairPanel[P, A, B] = PairPanelImpl(panel, first, second)

  private case class PairPanelImpl[+P <: Panel, +A <: Component, +B <: Component](mainPanel: P, first: A, second: B)(
      using opaque: Boolean
  ) extends PairPanel[P, A, B]:
    private val wrapPanel: WrapPanel[P] = WrapPanel(mainPanel)(first, second)
    mainPanel.maximumSize = new Dimension(400, 40)

    export wrapPanel.*
    override def key: A   = first
    override def value: B = second

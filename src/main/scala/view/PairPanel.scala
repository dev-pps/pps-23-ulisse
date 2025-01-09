package scala.view

import scala.swing.{Component, FlowPanel}

trait PairPanel[A <: Component, B <: Component]

object PairPanel:
  def apply[A <: Component, B <: Component](
      first: A,
      second: B
  ): PairPanel[_, _] = PairPanelImpl(first, second)

  private case class PairPanelImpl[A <: Component, B <: Component](
      first: A,
      second: B
  ) extends FlowPanel, PairPanel[_, _]:
    contents += first
    contents += second

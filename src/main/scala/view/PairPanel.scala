package scala.view

import scala.swing.{Component, FlowPanel}

trait PairPanel[A, B]

object PairPanel:
  def apply[A <: Component, B <: Component](
      first: A,
      second: B
  ): PairPanel[A, B] = PairPanelImpl(first, second)

  private case class PairPanelImpl[A <: Component, B <: Component](
      first: A,
      second: B
  ) extends FlowPanel, PairPanel[A, B]:
    contents += first
    contents += second

package scala.view

import scala.swing.{Component, FlowPanel, Panel}

trait PairPanel[A <: Component, B <: Component] extends Panel

object PairPanel:
  def apply[A <: Component, B <: Component](
      first: A,
      second: B
  ): PairPanel[A, B] = PairPanelImpl(first, second)

  private case class PairPanelImpl[A <: Component, B <: Component](
      first: A,
      second: B
  ) extends FlowPanel, PairPanel[A, B]:
    this.opaque = false
    contents += first
    contents += second

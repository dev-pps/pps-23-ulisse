package scala.view

import scala.swing.{BoxPanel, Button, Component, Orientation}

trait FormPanel[PairPanel[A <: Component, B <: Component] <: Component]

object FormPanel:

  def apply[A <: Component, B <: Component, PairPanel[_, _] <: Component](
      orientation: Orientation.Value
  )(
      pairPanels: List[PairPanel[A, B]]
  ): FormPanel[_] = FormPanelImpl(orientation, pairPanels)

  private case class FormPanelImpl[A <: Component, B <: Component, PairPanel[
      _,
      _
  ] <: Component](
      orientation: Orientation.Value,
      pairPanels: List[PairPanel[A, B]]
  ) extends BoxPanel(orientation), FormPanel[_]:
    private val submit = new Button("Submit")
    contents ++= pairPanels
    contents += submit

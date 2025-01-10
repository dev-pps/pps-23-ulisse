package view

import scala.swing.{BoxPanel, Button, Component, Orientation, Panel}

trait FormPanel[PairPanel[A <: Component, B <: Component]] extends Panel

object FormPanel:

  def apply[A <: Component, B <: Component](
      orientation: Orientation.Value
  )(
      pairPanels: List[PairPanel[A, B]]
  ): FormPanel[_] = FormPanelImpl(orientation, pairPanels)

  private case class FormPanelImpl[A <: Component, B <: Component](
      orientation: Orientation.Value,
      pairPanels: List[PairPanel[A, B]]
  ) extends BoxPanel(orientation), FormPanel[_]:
    private val submit = new Button("Submit")
    contents ++= pairPanels
    contents += submit

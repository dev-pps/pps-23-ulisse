package ulisse.infrastructures.view.common

import scala.swing.*
import scala.swing.BorderPanel.Position.{Center, South}

trait FormPanel[+MP <: Panel, +A <: Component, +B <: Component] extends WrapPanel[MP]:
  def buttonForm(): Button
  def form(): List[String]

object FormPanel:

  def apply[MP <: BorderPanel, P <: Panel, A <: Component, B <: Component](
      panel: MP,
      pairs: PairPanel[P, A, B]*
  )(using opaque: Boolean): FormPanel[MP, A, B] = FormPanelImpl(panel, pairs: _*)

  private case class FormPanelImpl[+MP <: BorderPanel, +P <: Panel, +A <: Component, +B <: Component](
      mainPanel: MP,
      pairPanels: PairPanel[P, A, B]*
  )(using opaque: Boolean) extends FormPanel[MP, A, B]:
    private val submit             = new Button("Submit")
    private val boxPanel: BoxPanel = BoxPanel(Orientation.Vertical)

    boxPanel.contents += Swing.Glue
    boxPanel.contents ++= pairPanels.map(_.panel())
    boxPanel.contents += Swing.Glue

    mainPanel.layout(boxPanel) = Center
    mainPanel.layout(submit) = South

    private val wrapPanel: WrapPanel[MP] = WrapPanel(mainPanel)(boxPanel)

    export wrapPanel.*
    override def buttonForm(): Button = submit
    override def form(): List[String] = pairPanels.map(_.value.toString).toList

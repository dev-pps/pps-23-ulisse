package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Theme

import scala.swing.{Component, Orientation, Swing}

trait JLabelComponent:
  def component[T >: Component]: T

object JLabelComponent:
  def createTextField(text: String): JLabelTextField = JLabelTextField(text)

  private val labelStyler = JStyler.paletteStyler(JStyler.transparentPalette)
  private val textFieldStyler =
    JStyler.rectPaletteStyler(JStyler.roundRect(10), JStyler.backgroundPalette(Theme.light.element))

  sealed case class JLabelTextField(text: String) extends JLabelComponent:
    private val mainPanel = JPanel.createBox(Orientation.Vertical)
    private val label     = JComponent.label(text, labelStyler)
    private val textField = JComponent.textField(10, textFieldStyler)

    private val northPanel = JPanel.createFlow()
    northPanel.contents += label

    mainPanel.contents += northPanel
    mainPanel.contents += textField

    override def component[T >: Component]: T = mainPanel

package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.common.Theme
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel.createSVGPanel

import java.awt.Dimension
import scala.swing.{Component, Orientation}

trait JLabelComponent:
  def component[T >: Component]: T

object JLabelComponent:
  def createTextField(text: String): JLabelTextField           = JLabelTextField(text)
  def createButton(iconPath: String, text: String): JIconLabel = JIconLabel(iconPath, text)

  private val transparentStyler = JStyler.paletteStyler(JStyler.transparentPalette)
  private val labelStyler       = JStyler.paletteStyler(JStyler.transparentPalette)
  private val elementStyler =
    JStyler.rectPaletteStyler(JStyler.roundRect(10), JStyler.backgroundPalette(Theme.light.element))

  case class JLabelTextField(text: String) extends JLabelComponent:
    private val mainPanel = JComponent.createBoxPanel(Orientation.Vertical, transparentStyler)
    private val label     = JComponent.label(text, labelStyler)
    private val textField = JComponent.textField(10, elementStyler)

    private val northPanel = JComponent.createFlowPanel(transparentStyler)
    northPanel.contents += label

    mainPanel.contents += northPanel
    mainPanel.contents += textField

    override def component[T >: Component]: T = mainPanel

  case class JIconLabel(iconPath: String, text: String) extends JLabelComponent:
    private val sizeIcon = 40

    private val mainPanel = JComponent.createBoxPanel(Orientation.Horizontal, elementStyler)
    private val icon      = createSVGPanel(iconPath, Theme.light.background)
    private val label     = JComponent.label(text, labelStyler)

    icon.preferredSize = Dimension(sizeIcon, sizeIcon)
    label.preferredSize = Dimension(100, sizeIcon)

    mainPanel.contents += icon
    mainPanel.contents += label

    showIcon()

    def showIconAndText(): Unit = label.visible = true
    def showIcon(): Unit        = label.visible = false

    override def component[T >: Component]: T = mainPanel

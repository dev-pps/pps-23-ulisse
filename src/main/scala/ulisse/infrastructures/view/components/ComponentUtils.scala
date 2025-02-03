package ulisse.infrastructures.view.components

import java.awt
import java.awt.{BorderLayout, Color, Graphics}
import javax.swing.{DefaultButtonModel, Icon, JToggleButton}
import javax.swing.JToggleButton.ToggleButtonModel
import javax.swing.border.LineBorder
import javax.swing.plaf.basic.BasicRadioButtonUI
import scala.swing.event.MouseClicked
import scala.swing.{
  AbstractButton,
  ButtonGroup,
  Component,
  Dimension,
  Graphics2D,
  Publisher,
  RadioButton,
  ToggleButton,
  UIElement
}

object ComponentUtils:

  private val emptyIcon = new Icon:
    override def getIconWidth: Int                                              = 0
    override def getIconHeight: Int                                             = 0
    override def paintIcon(c: awt.Component, g: Graphics, x: Int, y: Int): Unit = ()

  extension [E <: UIElement](element: E)
    def fixedSize(width: Int, height: Int): E =
      element.preferredSize = new Dimension(width, height)
      element.maximumSize = new Dimension(width, height)
      element.minimumSize = new Dimension(width, height)
      element

  extension [C <: Component](component: C)
    def genericClickReaction(action: () => Unit): C =
      component.listenTo(component.mouse.clicks)
      component.reactions += {
        case _: MouseClicked => action()
      }
      component

    def opaque(value: Boolean): C =
      component.opaque = value
      component

    def defaultBorder(): C =
      component.border = new LineBorder(Color.BLACK, 2)
      component

    def fListenTo(ps: Publisher*): C =
      component.listenTo(ps*)
      component

    def makeSelectable(): RadioButton =
      new RadioButton():
        this.opaque = true
        peer.setUI(new BasicRadioButtonUI { override def getDefaultIcon: Icon = emptyIcon })
        peer.add(component.peer, BorderLayout.CENTER)
        peer.getModel.addChangeListener(_ => {
          background = if peer.getModel.isSelected then Color.LIGHT_GRAY else Color.WHITE
        })

  extension [B <: AbstractButton](button: B)
    def addToGroup(buttonGroup: ButtonGroup): B =
      buttonGroup.peer.add(button.peer)
      button

package ulisse.infrastructures.view.components

import scala.swing.event.MouseClicked
import scala.swing.{Component, Dimension, UIElement}

object ComponentUtils:
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

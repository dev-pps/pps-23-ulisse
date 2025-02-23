package ulisse.infrastructures.view.components

import java.awt.Color
import scala.swing.{Component, LayoutContainer, MainFrame}

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object ComponentMixins:

  trait UpdatableContainer:
    this: MainFrame | LayoutContainer =>
    def update(component: Component): Unit

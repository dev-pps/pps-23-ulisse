package ulisse.infrastructures.view.utils

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.decorators.SwingEnhancements.ShapeEffect
import ulisse.infrastructures.view.components.styles.Styles

import scala.swing.*

@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
object ComponentUtils:

  /** Utils method to create a panel */
  extension [T <: Panel](panel: T)
    /** Make the component transparent */
    def transparent(): T =
      panel.opaque = false
      panel.background = Styles.transparentColor
      panel

  /** Utils method to center a component */
  extension [T <: Component](component: T)

    /** Center the component horizontally */
    def centerHorizontally(): ExtendedSwing.SFlowPanel =
      val panel = ExtendedSwing.SFlowPanel()
      panel.rectPalette = Styles.transparentPalette
      panel.contents += component
      panel

    /** Center the component vertically */
    def centerVertically(): ExtendedSwing.SBoxPanel =
      val panel = ExtendedSwing.SBoxPanel(Orientation.Vertical)
      panel.rectPalette = Styles.transparentPalette
      panel.contents += Swing.VGlue
      panel.contents += component
      panel

    /** Center the component */
    def center(): Panel = centerHorizontally().centerVertically()

    /** Create a component with a left and right component */
    def createLeftRight(right: Component): ExtendedSwing.SBoxPanel =
      val panel = ExtendedSwing.SBoxPanel(Orientation.Horizontal)
      panel.rectPalette = Styles.transparentPalette
      panel.contents += component.centerVertically()
      panel.contents += Swing.HGlue
      panel.contents += right.centerVertically()
      panel

  /** Utils method to center a composed component */
  extension (composed: ComposedSwing)

    /** Center the component horizontally */
    def centerHorizontally(): ExtendedSwing.SFlowPanel = composed.component.centerHorizontally()

    /** Center the component vertically */
    def centerVertically(): ExtendedSwing.SBoxPanel = composed.component.centerVertically()

    /** Center the component */
    def center(): Panel = centerHorizontally().centerVertically()

    /** Create a component with a left and right component */
    def createLeftRight(right: ComposedSwing): ExtendedSwing.SBoxPanel =
      composed.component.createLeftRight(right.component)

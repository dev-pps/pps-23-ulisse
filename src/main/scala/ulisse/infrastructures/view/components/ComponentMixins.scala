package ulisse.infrastructures.view.components

import java.awt.Color
import scala.swing.Component

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object ComponentMixins:
  trait Colorable:
    component: Component =>
    private var _color: Color = Color.BLACK
    def color: Color          = _color
    def color_=(newColor: Color): Unit =
      _color = newColor
      repaint()

  trait Rotatable:
    component: Component =>
    private var _rotation: Int = 0
    def rotation: Int          = _rotation
    def rotation_=(newRotation: Int): Unit =
      _rotation = newRotation
      repaint()

  trait Selectable extends Component:
    private var _selected: Boolean = true
    def selected: Boolean          = _selected
    def selected_=(newSelected: Boolean): Unit =
      _selected = newSelected
      repaint()

package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.Component

@SuppressWarnings(Array("org.wartremover.warts.Var"))
/** Components that can be selected. */
object Selectables:

  // Behaves like a radio button
  trait Selectable extends Component:
    private var _selected: Boolean = false

    /** True if the component is selected. */
    def selected: Boolean = _selected

    /** Set the component as selected. */
    def selected_=(newSelected: Boolean): Unit =
      _selected = newSelected
      repaint()

  /** A group of selectable components. */
  final case class SelectableGroup(group: Selectable*):
    deselectAll()
    for h <- group.headOption do select(h)

    private def select(component: Selectable): Unit =
      deselectAll()
      component.selected = true

    private def deselectAll(): Unit =
      group.foreach(_.selected = false)

    private def selected: Option[Selectable] =
      group.find(_.selected)

package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.ComponentUtils.*

import scala.swing.Component

@SuppressWarnings(Array("org.wartremover.warts.Var"))
object Selectables:

  // Behaves like a radio button
  trait Selectable extends Component:
    private var _selected: Boolean = false
    def selected: Boolean          = _selected
    def selected_=(newSelected: Boolean): Unit =
      _selected = newSelected
      repaint()

  final case class SelectableGroup(group: Selectable*):
    group.foreach(comp =>
      comp.genericClickReaction(() => {
        deselectAll(); select(comp); comp.repaint()
      })
    )
    deselectAll()
    for h <- group.headOption do select(h)

    def select(component: Selectable): Unit =
      deselectAll()
      component.selected = true

    def deselectAll(): Unit =
      group.foreach(_.selected = false)

    def selected: Option[Selectable] =
      group.find(_.selected)

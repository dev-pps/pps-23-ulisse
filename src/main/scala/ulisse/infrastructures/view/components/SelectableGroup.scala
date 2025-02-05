package ulisse.infrastructures.view.components

import ulisse.infrastructures.view.components.ComponentMixins.Selectable
import ulisse.infrastructures.view.components.ComponentUtils.*

final case class SelectableGroup(group: Selectable*):
  group.foreach(comp => comp.genericClickReaction(() => { deselectAll(); select(comp); comp.repaint() }))
  deselectAll()
  for h <- group.headOption do select(h)
  def select(component: Selectable): Unit =
    deselectAll()
    component.selected = true

  def deselectAll(): Unit =
    group.foreach(_.selected = false)

  def selected: Option[Selectable] =
    group.find(_.selected)

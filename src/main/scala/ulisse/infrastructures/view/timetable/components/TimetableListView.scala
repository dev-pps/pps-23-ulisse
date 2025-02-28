package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.components.ExtendedSwing.createSVGPanel
import ulisse.infrastructures.view.timetable.components.Observers.Updatable
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.TimetableEntry
import ulisse.infrastructures.view.utils.SwingUtils.headerLabel
import ulisse.infrastructures.view.utils.SwingUtils

import java.awt.Color
import scala.swing.Swing.{EmptyBorder, HGlue}
import scala.swing.*

final case class TimetableListView(timeEntries: List[TimetableEntry]) extends ListView(timeEntries)
    with Updatable[List[TimetableEntry]]:
  import scala.swing.ListView.IntervalMode
  selection.intervalMode = IntervalMode.Single
  background = Color.white
  renderer = new ItemRenderer[TimetableEntry]
  visibleRowCount = 3

  private class ItemRenderer[T] extends ListView.Renderer[TimetableEntry] {
    override def componentFor(
        list: ListView[_ <: TimetableEntry],
        isSelected: Boolean,
        focused: Boolean,
        item: TimetableEntry,
        index: Int
    ): Component = {
      StationCard(item)
    }
  }

  private case class StationCard(data: TimetableEntry) extends BoxPanel(Orientation.Vertical):
    import java.awt.Dimension
    preferredSize = Dimension(180, 110)
    border = EmptyBorder(5, 5, 5, 5)
    private val heading = new BoxPanel(Orientation.Horizontal) {
      background = Color.WHITE
      border = EmptyBorder(5, 5, 0, 5)
      contents += headerLabel(data.name)
      contents += HGlue
    }
    import SwingUtils.defaultIntString
    private val infos = new GridPanel(3, 2) {
      background = Color.lightGray
      contents ++= Seq(
        Label("Arrive at:"),
        Label(data.arrivingTime.getOrElse("-")),
        Label("Waits:"),
        Label(s"${data.waitMinutes.defaultIntString} min"),
        Label("Depart at:"),
        Label(data.departureTime.getOrElse("-"))
      )
    }
    contents += heading
    contents += infos
    background = Color.white
    contents += new Label() {
      contents += createSVGPanel("icons/arrow_down.svg")
    }

  override def update(data: List[TimetableEntry]): Unit = listData = data

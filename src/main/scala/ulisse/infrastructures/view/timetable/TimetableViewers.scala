package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.components.ExtendedSwing.createSVGPanel
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.TimetableEntry
import ulisse.infrastructures.view.train.SwingUtils
import ulisse.infrastructures.view.train.SwingUtils.headerLabel

import java.awt.Color
import scala.swing.Swing.{EmptyBorder, HGlue}
import scala.swing.{BoxPanel, Component, GridPanel, Label, ListView, Orientation, ScrollPane}

object TimetableViewers:

  def timetableViewer(timeEntries: List[TimetableEntry], visibleRows: Int): ListView[TimetableEntry] =
    val listView = new ListView(timeEntries) {
      import scala.swing.ListView.IntervalMode
      selection.intervalMode = IntervalMode.Single
      background = Color.white
      renderer = new ItemRenderer[TimetableEntry]
      visibleRowCount = visibleRows
    }
    listView

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

  case class StationCard(data: TimetableEntry) extends BoxPanel(Orientation.Vertical):
    import java.awt.Dimension
    preferredSize = Dimension(180, 110)
    border = EmptyBorder(5, 5, 5, 5)
    private val heading = new BoxPanel(Orientation.Horizontal) {
      background = Color.WHITE
      border = EmptyBorder(5, 5, 0, 5)
      contents += headerLabel(data.name)
      contents += HGlue
    }
    import ulisse.infrastructures.view.train.SwingUtils.defaultIntString
    private val infos = new GridPanel(3, 2) {
      background = Color.WHITE
      contents ++= Seq(
        Label("Arrive at:"),
        Label(data.arrivingTime),
        Label("Waits:"),
        Label(s"${data.waitMinutes.defaultIntString} min"),
        Label("Depart at:"),
        Label(data.departureTime)
      )
    }
    contents += heading
    contents += infos
    contents += new Label() {
      contents += createSVGPanel("icons/arrow_down.svg")
    }

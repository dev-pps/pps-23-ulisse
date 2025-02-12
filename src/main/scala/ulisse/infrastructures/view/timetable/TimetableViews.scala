package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JComponent.{JIconLabel, JTabbedPane}
import ulisse.infrastructures.view.timetable.TimetableView.{mockedData, StationCard}

import java.awt.Dimension
import scala.swing.{BoxPanel, Component, FlowPanel, Label, MainFrame, Orientation, Panel, ScrollPane}

object TimetableViews:

  extension (panes: Map[JIconLabel, Component])
    def toTabbedPane: JTabbedPane =
      val tabbedPane = JComponent.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  def timetableTabbedPane(): JTabbedPane =
    val formIcon: JIconLabel  = JComponent.createIconLabel("icons/calendar_add_on.svg", "Create")
    val formPane              = FlowPanel(Label("Create timetable form"))
    val savedIcon: JIconLabel = JComponent.createIconLabel("icons/calendar_clock.svg", "Saved")
    val savedPane = FlowPanel(ScrollPane(TimetableView.timetableViewer(List(mockedData, mockedData, mockedData))))

    Map(
      formIcon  -> formPane,
      savedIcon -> savedPane
    ).toTabbedPane

@main def timetableDemoGUI(): Unit =
  new MainFrame() {
    title = "timetable preview"
    preferredSize = Dimension(400, 400)
    val mainPanel = FlowPanel()
    import ulisse.infrastructures.view.timetable.TimetableViews.timetableTabbedPane
    mainPanel.contents += timetableTabbedPane().component
//    mainPanel.contents += StationCard(TimetableView.mockedData)
    contents = mainPanel
    visible = true
  }

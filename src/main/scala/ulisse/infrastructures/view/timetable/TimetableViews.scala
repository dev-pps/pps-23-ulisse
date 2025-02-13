package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JComponent.{JIconLabel, JTabbedPane}
import ulisse.infrastructures.view.timetable.TimetableEditorTabView.EditorTab

import scala.swing.{Component, FlowPanel, Label, MainFrame, ScrollPane}

object TimetableViews:

  extension (panes: Map[JIconLabel, Component])
    def toTabbedPane: JTabbedPane =
      val tabbedPane = JComponent.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  def timetableTabbedPane(): JTabbedPane =
    val formIcon: JIconLabel  = JComponent.createIconLabel("icons/calendar_add_on.svg", "Create")
    val formPane              = EditorTab(TimetableViewController())
    val savedIcon: JIconLabel = JComponent.createIconLabel("icons/calendar_clock.svg", "Saved")
    val savedPane             = FlowPanel(Label("Create timetable form"))

    Map(
      formIcon  -> formPane,
      savedIcon -> savedPane
    ).toTabbedPane

@main def timetableDemoGUI(): Unit =
  new MainFrame() {
    title = "timetable preview"
    val mainPanel = FlowPanel()
    import ulisse.infrastructures.view.timetable.TimetableViews.timetableTabbedPane
    mainPanel.contents += timetableTabbedPane().component
//    mainPanel.contents += StationCard(TimetableView.mockedData)
    contents = mainPanel
    visible = true
  }

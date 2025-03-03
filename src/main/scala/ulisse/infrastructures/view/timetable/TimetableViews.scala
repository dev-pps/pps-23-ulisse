package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import ulisse.infrastructures.view.timetable.TimetableEditorTabView.EditorTab

import scala.swing.{Component, FlowPanel, Label, MainFrame, ScrollPane}

object TimetableViews:

  extension (panes: Map[ComposedImageLabel, Component])
    private def toTabbedPane: JTabbedPane =
      val tabbedPane = ComposedSwing.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  def timetableTabbedPane(): JTabbedPane =
    import scala.swing.Orientation
    given orientation: Orientation.Value = Orientation.Vertical
    val formIcon                         = ComposedImageLabel.createIcon("icons/calendar_add_on.svg", "Create")
    val formPane                         = EditorTab(TimetableViewController())
    val savedIcon                        = ComposedImageLabel.createIcon("icons/calendar_clock.svg", "Saved")
    val savedPane                        = FlowPanel(Label("Create timetable form"))

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

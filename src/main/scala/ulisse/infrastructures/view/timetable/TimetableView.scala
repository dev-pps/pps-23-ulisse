package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
import ulisse.infrastructures.view.components.ComposedSwing
import ulisse.infrastructures.view.components.ComposedSwing.{JIconLabel, JTabbedPane}
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.components.EditingTab.EditorTab

import scala.swing.{Component, FlowPanel, Label, MainFrame}

trait TimetableUpdateListener:
  def showError(errorMessage: String): Unit

object TimetableView:

  extension (panes: Map[JIconLabel, Component])
    private def toTabbedPane: JTabbedPane =
      val tabbedPane = ComposedSwing.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  def apply(port: TimetablePorts.Input): Component =
    val portAdapterController = TimetableViewController(port)
    timetableTabbedPane(portAdapterController).component

  def timetableTabbedPane(controller: TimetableViewController): JTabbedPane =
    val formIcon: JIconLabel = ComposedSwing.createIconLabel("icons/calendar_add_on.svg", "Create")
    import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
    val formPane              = EditorTab(controller)
    val savedIcon: JIconLabel = ComposedSwing.createIconLabel("icons/calendar_clock.svg", "Saved")
    val savedPane             = FlowPanel(Label("Create timetable form"))

    Map(
      formIcon  -> formPane,
      savedIcon -> savedPane
    ).toTabbedPane

@main def timetableViewDemoGUI(): Unit =
  new MainFrame() {
    title = "timetable preview"
    val mainPanel = FlowPanel()
    import ulisse.infrastructures.view.timetable.TimetableView.timetableTabbedPane
    mainPanel.contents += TimetableView(TimetableInputPortMocked())
    contents = mainPanel
    visible = true
  }

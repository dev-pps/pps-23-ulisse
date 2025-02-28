package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
import ulisse.infrastructures.view.common.ImagePath as ImgPath
import ulisse.infrastructures.view.components.ExtendedSwing.SFlowPanel
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.components.EditingTab.EditorTab
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import ulisse.infrastructures.view.timetable.components.SavedTab

trait TimetableUpdateListener:
  def showError(errorMessage: String): Unit

object TimetableView:

  extension (panes: Map[ComposedImageLabel, Component])
    private def toTabbedPane: JTabbedPane =
      val tabbedPane = ComposedSwing.createTabbedPane(panes.keys.toList: _*)
      panes.foreach((k, p) => tabbedPane.paneOf(k).contents += p)
      tabbedPane

  def apply(port: TimetablePorts.Input): Component =
    val portAdapterController = TimetableViewController(port)
    timetableTabbedPane(portAdapterController).component

  def timetableTabbedPane(controller: TimetableViewController): JTabbedPane =
    given orientation: Orientation.Value = Orientation.Horizontal
    val formIcon                         = ComposedImageLabel.createIcon("icons/calendar_add_on.svg", "Create")
    val savedIcon                        = ComposedImageLabel.createIcon("icons/calendar_clock.svg", "Saved")
    import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
    val savedIcon = ComposedImageLabel.createIcon("icons/calendar_clock.svg", "Saved")

    Map(
      formIcon  -> EditorTab(controller),
      savedIcon -> SavedTab(controller)
    ).toTabbedPane

@main def timetableViewDemoGUI(): Unit =
  new MainFrame() {
    title = "timetable preview"
    val mainPanel = SFlowPanel()
    mainPanel.contents += TimetableView(TimetableInputPortMocked())
    contents = mainPanel
    visible = true
  }

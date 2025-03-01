package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.infrastructures.view.common.ImagePath as ImgPath
import ulisse.infrastructures.view.components.ExtendedSwing.SFlowPanel
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.subviews.EditingTab.EditorTab
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import ulisse.infrastructures.view.timetable.subviews.TimetableViewerTab
import scala.swing.{Component, MainFrame, Orientation}

object TimetableView:

  def apply(port: TimetablePorts.Input): Component =
    val portAdapterController = TimetableViewController(port)
    timetableTabbedPane(portAdapterController).component

  private def timetableTabbedPane(controller: TimetableViewController): JTabbedPane =
    given orientation: Orientation.Value = Orientation.Horizontal
    val formIcon                         = ComposedImageLabel.createIcon("icons/calendar_add_on.svg", "Create")
    val savedIcon                        = ComposedImageLabel.createIcon("icons/calendar_clock.svg", "Saved")
    val formTab                          = EditorTab(controller)
    val timetableViewer                  = TimetableViewerTab(controller)
    controller.addTimetableViewListener(timetableViewer)
    controller.addPreviewListener(formTab)
//    controller.addErrorListener(this)
    import ulisse.infrastructures.view.utils.SwingUtils.toTabbedPane
    Map(
      formIcon  -> formTab,
      savedIcon -> timetableViewer
    ).toTabbedPane

@main def timetableViewDemoGUI(): Unit =
  import ulisse.infrastructures.view.utils.SwingUtils.showPreview
  import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
  TimetableView(TimetableInputPortMocked()).showPreview()

package ulisse.infrastructures.view.timetable

import ulisse.applications.ports.TimetablePorts
import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.timetable.subviews.EditingTab.EditorTab
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import TimetableAdapterObservers.ErrorObserver
import ulisse.infrastructures.view.timetable.subviews.TimetableViewerTab

import scala.swing.{Component, Orientation}

object TimetableView:
  /** Creates timetable root view with its controller given `port`.
    * Child views are configured to be notified from internal controller of updates.
    */
  def apply(adapter: TimetableViewAdapter): Component =
    TimetableTabbedPane(adapter).component

  private class TimetableTabbedPane(controller: TimetableViewAdapter) extends JTabbedPane with ErrorObserver:
    given orientation: Orientation.Value = Orientation.Horizontal
    private val formIcon                 = ComposedImageLabel.createIcon("icons/calendar_add_on.svg", "Create")
    private val savedIcon                = ComposedImageLabel.createIcon("icons/calendar_clock.svg", "Saved")
    private val formTab                  = EditorTab(controller)
    private val timetableViewer          = TimetableViewerTab(controller)
    import ulisse.infrastructures.view.utils.SwingUtils.toTabbedPane
    private val tabbedPane =
      Map(
        formIcon  -> formTab,
        savedIcon -> timetableViewer
      ).toTabbedPane
    controller.addTimetableViewListener(timetableViewer)
    controller.addPreviewListener(formTab)
    controller.addErrorObserver(this)

    override def component[T >: Component]: T = tabbedPane.component

    override def showError(title: String, descr: String): Unit =
      import scala.swing.Dialog
      import scala.swing.Dialog.Message
      Dialog.showMessage(
        this.component,
        descr,
        title,
        Message.Error
      )

@main def timetableViewDemoGUI(): Unit =
  import ulisse.infrastructures.view.utils.SwingUtils.showPreview
  import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
  val adapter = TimetableViewAdapter(TimetableInputPortMocked())
  TimetableView(adapter).showPreview()

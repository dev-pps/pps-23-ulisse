package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.components.composed.{ComposedImageLabel, ComposedSwing}
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.components.composed.ComposedSwing.JTabbedPane
import TimetableAdapterObservers.RequestResultObserver
import ulisse.infrastructures.view.timetable.subviews.{EditorTab, TimetableViewerTab}

import scala.swing.{Component, Orientation}

/** Root timetable view. */
object TimetableView:
  /** Creates timetable root view given its `adapter`. */
  def apply(adapter: TimetableViewAdapter): Component =
    TimetableTabbedPane(adapter).component

  private class TimetableTabbedPane(adapter: TimetableViewAdapter) extends JTabbedPane with RequestResultObserver:
    import ulisse.infrastructures.view.components.ExtendedSwing.toTabbedPane
    given orientation: Orientation.Value = Orientation.Horizontal
    private val formIcon                 = ComposedImageLabel.createIcon("icons/calendar_add_on.svg", "Create")
    private val savedIcon                = ComposedImageLabel.createIcon("icons/calendar_clock.svg", "Saved")
    private val editorTab                = EditorTab(adapter)
    private val timetableViewer          = TimetableViewerTab(adapter)
    private val tabbedPane =
      Map(
        formIcon  -> editorTab,
        savedIcon -> timetableViewer
      ).toTabbedPane

    adapter.addRequestResultObserver(this)

    override def component[T >: Component]: T = tabbedPane.component

    override def showRequestResult(title: String, descr: String): Unit =
      import scala.swing.Dialog
      import scala.swing.Dialog.Message
      Dialog.showMessage(
        this.component,
        descr,
        title,
        Message.Info
      )

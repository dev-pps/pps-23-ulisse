package ulisse.infrastructures.view.timetable.subviews

import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.subviews.Observers.UpdatableTimetableView
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel
import ulisse.infrastructures.view.timetable.model.TimetableGUIModel.TimetableEntry
import ulisse.infrastructures.view.utils.SwingUtils.SFieldLabel
import ulisse.utils.Times.Time
import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight

import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, ComboBox, Orientation, ScrollPane}

class TimetableViewerTab(controller: TimetableViewController) extends SBoxPanel(Orientation.Vertical)
    with UpdatableTimetableView:
  private val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
  private val trainField                   = SFieldLabel("Train")(trainCombo)
  // todo: update combo time when train is selected (request to service, through port, all timetables of train)
  private val departTimeCombo: ComboBox[Time] = ComboBox[Time](List(Time(9, 0, 0), Time(10, 45, 0)))
  private val timeField                       = SFieldLabel("Departure time")(departTimeCombo)
  private val timetables                      = TimetableGUIModel.generateMockTimetable(6)
  private val timetableView                   = TimetableListView(timetables)
  export timetableView.update
  private val deleteBtn = SButton("Delete timetable")
  deleteBtn.rect = Styles.formFalseButtonRect
  deleteBtn.fontEffect = Styles.whiteFont
  deleteBtn.reactions += {
    case ButtonClicked(_) =>
      val selectedTime = departTimeCombo.selection.item
      val trainName    = trainCombo.selection.item
      controller.deleteTimetable(trainName, selectedTime)
  }

  private val borderTableView = new BorderPanel {
    layout(ScrollPane(timetableView)) = BorderPanel.Position.Center
  }

  listenTo(trainCombo)
  import scala.swing.event.SelectionChanged
  trainCombo.reactions += {
    case SelectionChanged(`trainCombo`) =>
      val trainName = trainCombo.selection.item
      val time      = departTimeCombo.selection.item
      controller.requestTimetable(trainName, time)
  }

  contents += trainField.createLeftRight(timeField)
  contents += borderTableView
  import ulisse.infrastructures.view.utils.ComponentUtils.centerHorizontally
  contents += deleteBtn.centerHorizontally()

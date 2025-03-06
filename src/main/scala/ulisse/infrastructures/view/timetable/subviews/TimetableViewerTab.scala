package ulisse.infrastructures.view.timetable.subviews

import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton, SFieldLabel}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.timetable.TimetableViewModel
import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight

import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, ComboBox, Orientation, ScrollPane, Swing}

/** Timetable consulting tab view.
  * It gets `controller` and observes (adhering to [[UpdatableTimetableView]]) specific updates from controller to updates timetable preview.
  */
class TimetableViewerTab(controller: TimetableViewAdapter) extends SBoxPanel(Orientation.Vertical):
  private val trainCombo: ComboBox[String]        = ComboBox[String](controller.trainNames)
  private val trainField                          = SFieldLabel("Train")(trainCombo)
  private val timetableCombo: ComboBox[Timetable] = ComboBox[Timetable](List.empty)
  private val timetableField                      = SFieldLabel("Timetable")(timetableCombo)
  private val timetables                          = TimetableViewModel.generateMockTimetable(6)
  private val timetableView                       = TimetableListView(timetables)
  private val UNSELECTED                          = -1
  trainCombo.selection.index = UNSELECTED

  private val deleteBtn = SButton("Delete timetable")
  deleteBtn.rect = Styles.formFalseButtonRect
  deleteBtn.fontEffect = Styles.whiteFont
  deleteBtn.enabled = false

  deleteBtn.reactions += {
    case ButtonClicked(_) =>
      controller.deleteTimetable(trainCombo.selectedItemOption, timetableCombo.selectedItemOption.map(_.departureTime))
  }

  private val borderTableView = new BorderPanel {
    layout(ScrollPane(timetableView)) = BorderPanel.Position.Center
  }

  listenTo(trainCombo)
  import scala.swing.event.SelectionChanged
  trainCombo.reactions += {
    case SelectionChanged(`trainCombo`) =>
      val trainName = trainCombo.selection.item
      controller.requestTimetables(trainName)
  }
  listenTo(timetableCombo)
  timetableCombo.reactions += {
    case SelectionChanged(`timetableCombo`) =>
      import TimetableViewModel.toTimetableEntries
      timetableCombo.selectedItemOption.foreach: i =>
        timetableView.update(i.toTimetableEntries)
        deleteBtn.enabled = true

  }

  contents += trainField.createLeftRight(timetableField)
  contents += borderTableView
  import ulisse.infrastructures.view.utils.ComponentUtils.centerHorizontally
  contents += deleteBtn.centerHorizontally()

  def update(data: List[Timetables.Timetable]): Unit =
    Swing.onEDT:
      timetableCombo.peer.setModel(ComboBox.newConstantModel(data))
      timetableCombo.selection.index = UNSELECTED
      deleteBtn.enabled = false

  extension [T](comboBox: ComboBox[T])
    private def selectedItemOption: Option[T] =
      Option.when(comboBox.selection.item != null)(comboBox.selection.item)

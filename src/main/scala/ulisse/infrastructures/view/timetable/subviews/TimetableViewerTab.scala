package ulisse.infrastructures.view.timetable.subviews

import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton, SFieldLabel}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.entities.train.Trains.Train
import ulisse.infrastructures.view.timetable.TimetableAdapterObservers.{TimetablesUpdatable, TrainsUpdatable, Updatable}
import ulisse.infrastructures.view.timetable.TimetableViewModel
import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight
import ulisse.infrastructures.view.utils.SwingUtils.selectedItemOption
import ulisse.infrastructures.view.utils.SwingUtils.updateModel

import scala.swing.Swing.onEDT
import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, ComboBox, Orientation, ScrollPane, Swing}

/** Timetable consulting tab view: by selecting train and departure time, timetable is shown.
  * It gets `controller` ([[TimetableViewAdapter]]) and observes updates from controller.
  *
  *  Observes:
  *  - [[TimetablesUpdatable]] to get timetables of selected train
  *  - [[TrainsUpdatable]] to updates train selector
  */
class TimetableViewerTab(controller: TimetableViewAdapter) extends SBoxPanel(Orientation.Vertical)
    with TimetablesUpdatable with TrainsUpdatable:
  controller.addTimetablesObserver(this)
  controller.addTrainsObserver(this)
  private val trainCombo: ComboBox[String] = ComboBox[String](List.empty)
  controller.requestTrains()
  private val trainField                          = SFieldLabel("Train")(trainCombo)
  private val timetableCombo: ComboBox[Timetable] = ComboBox[Timetable](List.empty)
  private val timetableField                      = SFieldLabel("Timetable")(timetableCombo)
  private val timetableView                       = TimetableListView(List.empty)
  private val UNSELECTED                          = -1
  trainCombo.selection.index = UNSELECTED

  import ulisse.infrastructures.view.utils.SwingUtils.setDefaultFont
  List(trainCombo, timetableCombo).setDefaultFont()
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

  listenTo(trainCombo.selection, timetableCombo.selection)
  import scala.swing.event.SelectionChanged
  reactions += {
    case SelectionChanged(`trainCombo`) =>
      val trainName = trainCombo.selection.item
      controller.requestTimetables(trainName)

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

  override def updateTimetables(tables: List[Timetable]): Unit =
    onEDT:
      timetableCombo.updateModel(tables)

  override def updateNewTrains(trains: List[Train]): Unit =
    onEDT:
      trainCombo.updateModel(trains.map(_.name))

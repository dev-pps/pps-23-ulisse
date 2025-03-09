package ulisse.infrastructures.view.timetable.subviews

import ulisse.entities.timetable.Timetables
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton, SFieldLabel}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.entities.train.Trains.Train
import ulisse.infrastructures.view.timetable.TimetableAdapterObservers.{TimetablesUpdatable, TrainsUpdatable}
import ulisse.infrastructures.view.timetable.TimetableViewModel
import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight
import ulisse.infrastructures.view.utils.SwingUtils.{selectedItemOption, updateModel}

import scala.swing.Swing.onEDT
import scala.swing.event.ButtonClicked
import scala.swing.{BorderPanel, ComboBox, Label, ListView, Orientation, ScrollPane, Swing}

/** Timetable consulting tab view: by selecting train and departure time, timetable is shown.
  * It gets `adapter` ([[TimetableViewAdapter]]) and observes updates from controller.
  *
  *  Observes:
  *  - [[TimetablesUpdatable]] to get timetables of selected train
  *  - [[TrainsUpdatable]] to updates train selector
  */
class TimetableViewerTab(adapter: TimetableViewAdapter) extends SBoxPanel(Orientation.Vertical)
    with TimetablesUpdatable with TrainsUpdatable:
  adapter.addTimetablesObserver(this)
  adapter.addTrainsObserver(this)
  private val trainCombo: ComboBox[String] = ComboBox[String](List.empty)
  private val refreshTrainsBtn: SButton    = SButton("refresh")
  adapter.requestTrains()
  private val trainField = SFieldLabel("Train")(trainCombo)
  private val timetableCombo: ComboBox[Timetable] = new ComboBox[Timetable](List.empty) {
    renderer = (list: ListView[_ <: Timetable], isSelected: Boolean, focused: Boolean, item: Timetable, index: Int) => {
      if item != null then
        Label(s"${item.departureTime} - ${item.startStation.name} -> ${item.arrivingStation.name}")
      else Label("")
    }
  }

  private val timetableField = SFieldLabel("Timetable")(timetableCombo)
  private val timetableView  = TimetableListView(List.empty)
  private val UNSELECTED     = -1

  import ulisse.infrastructures.view.utils.SwingUtils.setDefaultFont
  List(trainCombo, timetableCombo).setDefaultFont()
  private val deleteBtn = SButton("Delete timetable")
  deleteBtn.rect = Styles.formFalseButtonRect
  deleteBtn.fontEffect = Styles.whiteFont
  deleteBtn.enabled = false

  refreshTrainsBtn.rect = Styles.formTrueButtonRect
  refreshTrainsBtn.fontEffect = Styles.whiteFont

  refreshTrainsBtn.reactions += {
    case ButtonClicked(_) => adapter.requestTrains()
  }

  deleteBtn.reactions += {
    case ButtonClicked(_) =>
      adapter.deleteTimetable(trainCombo.selectedItemOption, timetableCombo.selectedItemOption.map(_.departureTime))
  }

  private val borderTableView = new BorderPanel {
    layout(ScrollPane(timetableView)) = BorderPanel.Position.Center
  }

  listenTo(trainCombo.selection, timetableCombo.selection)
  import scala.swing.event.SelectionChanged
  reactions += {
    case SelectionChanged(`trainCombo`) =>
      val trainName = trainCombo.selection.item
      adapter.requestTimetables(trainName)

    case SelectionChanged(`timetableCombo`) =>
      import TimetableViewModel.toTimetableEntries
      timetableCombo.selectedItemOption.foreach: i =>
        timetableView.update(i.toTimetableEntries)
        deleteBtn.enabled = true
  }
  import ulisse.infrastructures.view.utils.ComponentUtils.centerHorizontally
  import ulisse.infrastructures.view.utils.SwingUtils.vSpaced
  contents ++= List(
    trainField.createLeftRight(timetableField).createLeftRight(refreshTrainsBtn),
    borderTableView,
    deleteBtn.centerHorizontally()
  ).vSpaced(15)

  override def updateTimetables(tables: List[Timetable]): Unit =
    onEDT:
      timetableCombo.updateModel(tables)

  override def updateNewTrains(trains: List[Train]): Unit =
    onEDT:
      trainCombo.updateModel(trains.map(_.name))

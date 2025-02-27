package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.components.ComposedSwing.createInfoTextField
import ulisse.infrastructures.view.components.ExtendedSwing.{JButtonItem, JLabelItem, JTextFieldItem}
import ulisse.infrastructures.view.components.Styles
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController

import javax.swing.{JSpinner, SpinnerNumberModel}
import scala.swing.Swing.EmptyBorder
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, ComboBox, Orientation}
import ulisse.infrastructures.view.components.ComponentUtils.{alignLeft, hSpaced}
import ulisse.utils.ValidationUtils.validateNonBlankString

import scala.util.Try

private def StyledButton(label: String): JButtonItem =
  val buttonRect = Styles.defaultRect.withPaddingWidthAndHeight(20, 10)
  val btn        = JButtonItem(label)
  btn.rect = buttonRect
  btn

class EditorTabPane(controller: TimetableViewController) extends BoxPanel(Orientation.Vertical):
  private val minutesSpinnerModel = SpinnerNumberModel()
  minutesSpinnerModel.setMinimum(0)
  minutesSpinnerModel.setStepSize(1)
  private val minutesSpinner               = new JSpinner(minutesSpinnerModel)
  private val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
  private val stationSelection             = JTextFieldItem(10)
  private val undoBtn                      = StyledButton("undo")
  private val insertBtn                    = StyledButton("insert")
  private val editButtonsPane              = undoBtn.hSpaced(insertBtn)
  undoBtn.reactions += {
    case ButtonClicked(_) => controller.undoLastInsert()
  }

  // Hours and Minutes ComboBox
  private val hourValues                     = (0 to 23).map[String](h => "%02d".format(h)).prepended("-")
  private val minuteValues                   = (0 to 59).map[String](m => "%02d".format(m)).prepended("-")
  private val hoursCombo: ComboBox[String]   = new ComboBox[String](hourValues)
  private val minutesCombo: ComboBox[String] = new ComboBox[String](minuteValues)
  import scala.swing.*
  import scala.swing.event.*
  listenTo(hoursCombo.selection, minutesCombo.selection, trainCombo.selection)
  reactions += {
    case SelectionChanged(`hoursCombo`) | SelectionChanged(`minutesCombo`) =>
      for
        h <- Try(hoursCombo.selection.item.toInt)
        m <- Try(minutesCombo.selection.item.toInt)
      yield controller.setDepartureTime(h, m)

    case SelectionChanged(`trainCombo`) => controller.selectTrain(trainCombo.selection.item)
  }

  insertBtn.reactions += {
    case ButtonClicked(_) =>
      minutesSpinner.getValue match
        case waitMin: Int =>
          val stationName = stationSelection.text
          val res         = controller.insertStation(stationName, Option.when(waitMin > 0)(waitMin))
          res match
            case Left(e)  => println(s"error: $e")
            case Right(l) => println(l)
  }

  import ulisse.infrastructures.view.components.ComponentUtils.onLeftOf
  contents += JLabelItem("Train: ").onLeftOf(trainCombo)
  contents += JLabelItem("Selected Time: ").onLeftOf(hoursCombo.onLeftOf(minutesCombo))
  contents += JLabelItem("Station: ").onLeftOf(stationSelection)
  contents += JLabelItem("Wait minutes: ").onLeftOf(Component.wrap(minutesSpinner))
  contents += editButtonsPane

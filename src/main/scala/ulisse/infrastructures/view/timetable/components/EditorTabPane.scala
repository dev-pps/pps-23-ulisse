package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.components.ComposedSwing.createInfoTextField
import ulisse.infrastructures.view.components.ExtendedSwing.{JButtonItem, JLabelItem, JTextFieldItem}
import ulisse.infrastructures.view.components.Styles
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController

import scala.swing.Swing.EmptyBorder
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, ComboBox, Orientation}
import ulisse.infrastructures.view.components.ComponentUtils.{alignLeft, hSpaced}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.utils.ValidationUtils.validateNonBlankString

import scala.util.Try

private def StyledButton(label: String): SButton =
  val buttonRect = Styles.defaultRect.withPaddingWidthAndHeight(20, 10)
  val btn        = SButton(label)
  btn.rect = buttonRect
  btn

class EditorTabPane(controller: TimetableViewController) extends BoxPanel(Orientation.Vertical):
  private val waitMinutesField             = SwingUtils.JNumberFieldItem(5)
  private val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
  private val stationSelection             = JTextFieldItem(10)
  private val clearBtn                     = StyledButton("reset")
  private val undoBtn                      = StyledButton("undo")
  private val insertBtn                    = StyledButton("insert")
  private val editButtonsPane              = undoBtn.hSpaced(insertBtn)
  undoBtn.reactions += {
    case ButtonClicked(_) => controller.undoLastInsert()
  }
  clearBtn.reactions += {
    case ButtonClicked(_) =>
      // Todo: reset also temporary timetable data
      clearFields()
  }

  trainCombo.font = new Font("Arial", Font.Plain.id, 15)

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
      val waitMin     = Try(waitMinutesField.text.toInt).toOption
      val stationName = stationSelection.text
      val res         = controller.insertStation(stationName, waitMin)
      res match
        case Left(e) => println(s"error: $e")
        case Right(l) =>
          stationSelection.text = ""
          waitMinutesField.text = ""
          println(l)
  }

  import ulisse.infrastructures.view.components.composed.onLeftOf
  contents += SLabel("Train: ").onLeftOf(trainCombo)
  contents += SLabel("Selected Time: ").onLeftOf(hoursCombo.onLeftOf(minutesCombo))
  contents += SLabel("Station: ").onLeftOf(stationSelection)
  contents += SLabel("Wait minutes: ").onLeftOf(Component.wrap(minutesSpinner))
  contents += editButtonsPane

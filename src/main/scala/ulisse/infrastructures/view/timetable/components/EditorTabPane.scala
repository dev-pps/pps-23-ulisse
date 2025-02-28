package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.components.ComposedSwing.createInfoTextField
import ulisse.infrastructures.view.components.ExtendedSwing.{JButtonItem, JLabelItem, JTextFieldItem}
import ulisse.infrastructures.view.components.Styles
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController

import scala.swing.Swing.EmptyBorder
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, ComboBox, Font, Orientation}
import ulisse.infrastructures.view.components.ComponentUtils.{alignLeft, centerHorizontally, hSpaced}
import ulisse.infrastructures.view.train.SwingUtils
import ulisse.infrastructures.view.train.SwingUtils.StyledButton
import ulisse.utils.ValidationUtils.validateNonBlankString

import java.awt.Color
import scala.util.Try

class EditorTabPane(controller: TimetableViewController) extends BoxPanel(Orientation.Vertical):
  private val waitMinutesField             = SwingUtils.JNumberFieldItem(5)
  private val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
  private val stationSelection             = JTextFieldItem(10)
  private val clearBtn                     = StyledButton("reset")
  private val undoBtn                      = StyledButton("undo")
  private val insertBtn                    = StyledButton("insert")
  private val formButtonsPane              = clearBtn.hSpaced(undoBtn.hSpaced(insertBtn))
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
  private val hoursCombo: ComboBox[String]   = ComboBox[String](hourValues)
  private val minutesCombo: ComboBox[String] = ComboBox[String](minuteValues)
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

  import ulisse.infrastructures.view.components.ComponentUtils.onLeftOf
  contents += JLabelItem("Train: ").onLeftOf(trainCombo)
  contents += JLabelItem("Departure time").centerHorizontally()
  contents += Label("h").onLeftOf(hoursCombo.onLeftOf(Label("m").onLeftOf(minutesCombo)))
  contents += JLabelItem("Station: ").onLeftOf(stationSelection)
  contents += JLabelItem("Wait minutes: ").onLeftOf(waitMinutesField)
  contents += formButtonsPane

  def clearFields(): Unit =
    waitMinutesField.text = ""
    trainCombo.selection.index = -1
    stationSelection.text = ""
    hoursCombo.selection.index = 0
    minutesCombo.selection.index = 0

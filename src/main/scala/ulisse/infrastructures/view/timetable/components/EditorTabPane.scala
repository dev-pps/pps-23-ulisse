package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton, SLabel, STextField}
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.components.ExtendedSwing.{SButton, SLabel, STextField}
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight

import scala.swing.Swing.EmptyBorder
import scala.swing.event.ButtonClicked
import scala.swing.{ComboBox, Font, Orientation}
import ulisse.infrastructures.view.train.SwingUtils
import ulisse.infrastructures.view.train.SwingUtils.StyledButton
import ulisse.utils.ValidationUtils.validateNonBlankString

import scala.util.Try

class EditorTabPane(controller: TimetableViewController) extends SBoxPanel(Orientation.Vertical):
  private val waitMinutesField             = SwingUtils.SNumberField(5)
  private val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
  private val stationSelection             = ComposedSwing.createInfoTextField("Station")
  private val resetBtn                     = SButton("reset")
  resetBtn.rect = Styles.formFalseButtonRect
  resetBtn.fontEffect = Styles.whiteFont

  private val undoBtn = SButton("undo")
  undoBtn.rect = Styles.formFalseButtonRect
  undoBtn.fontEffect = Styles.whiteFont

  private val insertBtn = SButton("insert")
  insertBtn.rect = Styles.formTrueButtonRect
  insertBtn.fontEffect = Styles.whiteFont

  private val formButtonsPane = resetBtn.createLeftRight(undoBtn.createLeftRight(insertBtn))
  undoBtn.reactions += {
    case ButtonClicked(_) => controller.undoLastInsert()
  }
  resetBtn.reactions += {
    case ButtonClicked(_) =>
      controller.reset()
      clearFields()
  }

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
  List(trainCombo, hoursCombo, minutesCombo).setDefaultFont()
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

  import ulisse.infrastructures.view.utils.ComponentUtils.centerHorizontally
  contents += SLabel("Train: ").createLeftRight(trainCombo)
  contents += SLabel("Departure time").centerHorizontally()
  contents += SLabel("h").createLeftRight(hoursCombo.createLeftRight(SLabel("m").createLeftRight(minutesCombo)))
  contents += stationSelection.component
  contents += SLabel("Wait minutes: ").createLeftRight(waitMinutesField)
  contents += formButtonsPane

  def clearFields(): Unit =
    waitMinutesField.text = ""
    trainCombo.selection.index = -1
    stationSelection.text = ""
    hoursCombo.selection.index = 0
    minutesCombo.selection.index = 0

  extension (components: Seq[Component])
    private def setDefaultFont(): Unit =
      components.foreach(_.font = Styles.defaultFont.swingFont)

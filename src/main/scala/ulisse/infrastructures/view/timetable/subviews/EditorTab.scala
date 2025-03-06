package ulisse.infrastructures.view.timetable.subviews

import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton, SLabel, SNumberField}
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.adapters.input.TimetableViewAdapters.TimetableViewAdapter
import ulisse.infrastructures.view.timetable.TimetableViewModel.TimetableEntry
import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight
import ulisse.infrastructures.view.utils.SwingUtils

import scala.swing.Swing.{EmptyBorder, HGlue, VStrut}
import scala.swing.event.ButtonClicked
import scala.swing.{BoxPanel, ComboBox, Component, Font, Label, Orientation, Panel, ScrollPane}
import scala.util.Try

/** Timetable creation form containing fields to select train, departing time and stations sequence and preview of timetable */
class EditorTab(controller: TimetableViewAdapter)
    extends SBoxPanel(Orientation.Vertical):
  private val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
  private val waitMinutesField             = SNumberField(5)
  private val stationField                 = ComposedSwing.createInfoTextField("Station")
  private val resetBtn                     = SButton("reset")
  private val undoBtn                      = SButton("undo")
  private val insertBtn                    = SButton("insert")
  private val listPreview                  = TimetableListView(List.empty)
  private val previewPane                  = ScrollPane(listPreview)
  private val saveBtn                      = SButton("Save")

  private val falseBtnStyle = List(resetBtn, undoBtn)
  private val trueBtnStyle  = List(saveBtn, insertBtn)
  falseBtnStyle.foreach(_.rect = Styles.formFalseButtonRect)
  trueBtnStyle.foreach(_.rect = Styles.formTrueButtonRect)
  trueBtnStyle.concat(falseBtnStyle).foreach(_.fontEffect = Styles.whiteFont)

  extension (l: List[TimetableEntry])
    private def andUpdatePreview(): Unit = listPreview.update(l)

  private val formButtonsPane = resetBtn.createLeftRight(undoBtn.createLeftRight(insertBtn))
  undoBtn.reactions += {
    case ButtonClicked(_) => controller.undoLastInsert().andUpdatePreview()
  }
  resetBtn.reactions += {
    case ButtonClicked(_) =>
      clearFields()
      controller.reset().andUpdatePreview()
  }
  insertBtn.reactions += {
    case ButtonClicked(_) =>
      val waitMin     = Try(waitMinutesField.text.toInt).toOption
      val stationName = stationField.text
      controller.insertStation(stationName, waitMin).andUpdatePreview()
      clearStationFields()
  }
  saveBtn.reactions += {
    case ButtonClicked(_) =>
      controller.save()
  }

  // Hours and Minutes ComboBox
  private val hourValues                     = (0 to 23).map[String](h => "%02d".format(h)).prepended("-")
  private val minuteValues                   = (0 to 59).map[String](m => "%02d".format(m)).prepended("-")
  private val hoursCombo: ComboBox[String]   = ComboBox[String](hourValues)
  private val minutesCombo: ComboBox[String] = ComboBox[String](minuteValues)

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

  import ulisse.infrastructures.view.utils.ComponentUtils.centerHorizontally
  private val fieldsSpace = 15
  private val spacedFields = List(
    SLabel("Train: ").createLeftRight(trainCombo),
    SLabel("Departure time").centerHorizontally(),
    SLabel("h").createLeftRight(hoursCombo.createLeftRight(SLabel("m").createLeftRight(minutesCombo))),
    stationField.component,
    SLabel("Waiting minutes:").createLeftRight(waitMinutesField).createLeftRight(SLabel("min.")),
    formButtonsPane
  ).flatMap(field => List(field, VStrut(fieldsSpace)))
  contents += header("Timetable creation")
  contents ++= spacedFields
  contents += previewPane.withHeader("Timetable Preview")
  contents += saveBtn.centerHorizontally()
  List(trainCombo, hoursCombo, minutesCombo).setDefaultFont()
  clearFields()

  private def clearStationFields(): Unit =
    waitMinutesField.text = ""
    stationField.text = ""

  def clearFields(): Unit =
    clearStationFields()
    trainCombo.selection.index = -1
    hoursCombo.selection.index = 0
    minutesCombo.selection.index = 0

  extension (components: Seq[Component])
    private def setDefaultFont(): Unit =
      components.foreach(_.font = Styles.defaultFont.swingFont)

  extension (c: Component)
    private def withHeader(headerText: String): Panel =
      new BoxPanel(Orientation.Vertical) {
        contents += header(headerText)
        contents += c
      }
  private def header(headerText: String): BoxPanel = new SBoxPanel(Orientation.Horizontal) {
    border = EmptyBorder(10, 5, 5, 0)
    contents += new Label(headerText) {
      font = new Font("Arial", java.awt.Font.BOLD, 15)
    }
    contents += HGlue
  }

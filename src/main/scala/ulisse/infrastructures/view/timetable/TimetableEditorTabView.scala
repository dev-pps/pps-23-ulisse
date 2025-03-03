package ulisse.infrastructures.view.timetable

import ulisse.infrastructures.view.components.composed.ComposedSwing.createInfoTextField
import ulisse.infrastructures.view.timetable.TimetableEditorTabView.EditorTab

import javax.swing.border.Border
import javax.swing.event.ChangeListener
import javax.swing.{JSpinner, SpinnerNumberModel}
import scala.swing.Swing.{EmptyBorder, HGlue}
import scala.swing.{
  BoxPanel,
  Button,
  ComboBox,
  Component,
  FlowPanel,
  Font,
  Label,
  MainFrame,
  Orientation,
  Panel,
  ScrollPane
}

object TimetableEditorTabView:
  val padding: Border = EmptyBorder(10, 20, 10, 20)
//  private val styler =
//    JStyler.rectPaletteStyler(JStyler.defaultRect.copy(arc = 10),
//      JStyler.palette(Theme.light.element, Theme.light.overlayElement, Theme.light.forwardClick))

  class EditorTab(controller: TimetableViewController) extends BoxPanel(Orientation.Vertical):
    val formPane = FormPane(controller)
    contents += formPane.withHeader("Timetable creation")
    contents += ScrollPane(TimetableViewers.timetableViewer(controller.insertedStations(), 4)).withHeader(
      "Timetable Preview"
    )

    contents += new BoxPanel(Orientation.Horizontal) {
      border = padding
      contents += new Button("clear")
      contents += HGlue
      contents += new Button("Save")
    }
  extension (c: Component)
    def withHeader(headerText: String): Panel =
      new BoxPanel(Orientation.Vertical) {
        contents += header(headerText)
        contents += c
      }

  private def header(headerText: String): BoxPanel = new BoxPanel(Orientation.Horizontal) {
    border = EmptyBorder(10, 5, 5, 0)
    contents += new Label(headerText) {
      font = new Font("Arial", java.awt.Font.BOLD, 15)
    }
    contents += HGlue
  }

  class FormPane(controller: TimetableViewController) extends BoxPanel(Orientation.Vertical):
    private val minutesSpinnerModel = SpinnerNumberModel()
    minutesSpinnerModel.setMinimum(0)
    minutesSpinnerModel.setStepSize(1)
    private val minutesSpinner = new JSpinner(minutesSpinnerModel)
    minutesSpinner.addChangeListener(_ =>
      minutesSpinner.getValue
      println(s"Changed ${minutesSpinner.getValue}")
    )
    import ulisse.infrastructures.view.train.SwingUtils.onLeftOf
    val trainCombo: ComboBox[String] = ComboBox[String](controller.trainNames)
    private val stationSelection     = createInfoTextField("Next Station")
    contents += Label("Train").onLeftOf(trainCombo)
    contents += stationSelection.component
    contents += Label("Wait minutes").onLeftOf(Component.wrap(minutesSpinner))

    private val editButtonsPane = new BoxPanel(Orientation.Horizontal) {
      border = padding
      contents += new Button("undo")   // JButtonItem("insert")(styler) //TODO: connect reactions
      contents += new Button("insert") // TODO: connect reactions
    }
    contents += editButtonsPane

@main def tryEditorTab(): Unit =
  new MainFrame() {
    title = "timetable preview"
    val mainPanel = FlowPanel()
    mainPanel.contents += EditorTab(TimetableViewController())
    contents = mainPanel
    visible = true
  }

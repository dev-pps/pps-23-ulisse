package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.components.EditingTab.EditorTab
import ulisse.infrastructures.view.timetable.components.{EditorTabPane, TimetableListViews}

import javax.swing.border.Border
import javax.swing.event.ChangeListener
import javax.swing.{JSpinner, SpinnerNumberModel}
import scala.swing.Swing.{unwrapIcon, EmptyBorder, HGlue}
import scala.swing.event.ButtonClicked
import scala.swing.*

object EditingTab:

  class EditorTab(controller: TimetableViewController) extends BoxPanel(Orientation.Vertical):
    import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight
    private val formPane      = EditorTabPane(controller)
    private val previewPane   = ScrollPane(TimetableListViews.tablesListView(controller.insertedStations(), 4))
    private val clearBtn      = new Button("clear")
    private val saveBtn       = new Button("Save")
    private val bottomButtons = clearBtn.createLeftRight(saveBtn)
    contents += formPane.withHeader("Timetable creation")
    contents += previewPane.withHeader("Timetable Preview")
    contents += bottomButtons

  extension (c: Component)
    private def withHeader(headerText: String): Panel =
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

@main def tryEditorTab(): Unit =
  new MainFrame() {
    title = "timetable preview"
    val mainPanel = FlowPanel()
    import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
    mainPanel.contents += EditorTab(TimetableViewController(TimetableInputPortMocked()))
    contents = mainPanel
    visible = true
  }

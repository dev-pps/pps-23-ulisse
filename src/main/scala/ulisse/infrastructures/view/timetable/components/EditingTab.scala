package ulisse.infrastructures.view.timetable.components

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.components.EditingTab.EditorTab
import ulisse.infrastructures.view.timetable.components.{EditorTabPane, TimetableListView}
import ulisse.infrastructures.view.train.SwingUtils.StyledButton

import javax.swing.border.Border
import javax.swing.event.ChangeListener
import javax.swing.{JSpinner, SpinnerNumberModel}
import scala.swing.Swing.{unwrapIcon, EmptyBorder, HGlue}
import scala.swing.event.ButtonClicked
import scala.swing.*

object EditingTab:

  class EditorTab(controller: TimetableViewController) extends SBoxPanel(Orientation.Vertical):
    private val formPane    = EditorTabPane(controller)
    private val previewPane = ScrollPane(TimetableListView(controller.insertedStations()))
    private val saveBtn     = SButton("Save")
    saveBtn.rect = Styles.formTrueButtonRect
    saveBtn.fontEffect = Styles.whiteFont
    contents += formPane.withHeader("Timetable creation")
    contents += previewPane.withHeader("Timetable Preview")

    import ulisse.infrastructures.view.utils.ComponentUtils.centerHorizontally
    contents += saveBtn.centerHorizontally()
    saveBtn.reactions += {
      case ButtonClicked(_) => controller.save()
    }

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

@main def tryEditorTab(): Unit =
  new MainFrame() {
    title = "timetable preview"
    val mainPanel = FlowPanel()
    import ulisse.entities.timetable.MockedEntities.TimetableInputPortMocked
    mainPanel.contents += EditorTab(TimetableViewController(TimetableInputPortMocked()))
    contents = mainPanel
    visible = true
  }

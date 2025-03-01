package ulisse.infrastructures.view.timetable.subviews

import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton}
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.timetable.TimetableViewControllers.TimetableViewController
import ulisse.infrastructures.view.timetable.subviews.EditingTab.EditorTab
import ulisse.infrastructures.view.timetable.subviews.Observers.UpdatablePreview
import scala.swing.Swing.{EmptyBorder, HGlue}
import scala.swing.event.ButtonClicked
import scala.swing.*

object EditingTab:

  class EditorTab(controller: TimetableViewController) extends SBoxPanel(Orientation.Vertical) with UpdatablePreview:
    private val listPreview = TimetableListView(controller.insertedStations())
    private val previewPane = ScrollPane(listPreview)
    private val formPane    = TimetableFormPane(controller)
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
    export listPreview.update

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

import javax.swing.JTextField
import scala.swing.*
import scala.swing.event.*

final case class StationEditorMenu(onCreateClick: () => Unit)
    extends GridBagPanel:
  private val c = new Constraints
  c.anchor = GridBagPanel.Anchor.Center
  c.gridx = 0
  c.weightx = 1

  c.gridy = 0
  c.weighty = 1
  layout(Swing.VGlue) = c

  c.gridy = 1
  c.weighty = 0.1
  layout(new Label("Select a Station")) = c

  c.gridy = 2
  c.weighty = 0.1
  layout(new Label("or")) = c

  c.gridy = 3
  c.weighty = 0.1
  layout(new Button {
    text = "Create"
    reactions += {
      case ButtonClicked(_) =>
        onCreateClick()
    }
  }) = c

  c.gridy = 4
  c.weighty = 1.0
  layout(Swing.VGlue) = c

final case class StationForm(
    controller: StationEditorController,
    onOkClick: () => Unit
) extends GridBagPanel:
  private val c = new Constraints
  c.anchor = GridBagPanel.Anchor.Center
  c.gridx = 0
  c.weightx = 1

  c.gridwidth = 2
  c.gridy = 0
  c.weighty = 1
  layout(Swing.VGlue) = c

  c.gridy = 1
  c.weighty = 0.1
  layout(new Label("Station")) = c

  c.gridwidth = 1
  c.gridx = 0
  c.gridy = 2
  c.weighty = 0.1
  layout(new Label("Name: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(new TextField(5)) = c

  c.gridx = 0
  c.gridy = 3
  c.weighty = 0.1
  layout(new Label("Latitude: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(new TextField(5)) = c

  c.gridx = 0
  c.gridy = 4
  c.weighty = 0.1
  layout(new Label("Longitude: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(new TextField(5)) = c

  c.gridx = 0
  c.gridy = 5
  c.weighty = 0.1
  layout(new Label("Station Tracks: ")) = c

  c.gridx = 1
  c.weighty = 0.1
  layout(new TextField(5)) = c

  c.gridwidth = 2
  c.gridx = 0
  c.gridy = 6
  c.weighty = 0.1
  layout(new Button {
    text = "Ok"
    reactions += {
      case ButtonClicked(_) =>
        onOkClick()
    }
  }) = c

  c.gridy = 7
  c.weighty = 1.0
  layout(Swing.VGlue) = c
final case class StationEditorContent(
    worldMap: Panel,
    stationEditorPanel: Panel
)

final case class StationEditorPage(controller: StationEditorController)
    extends BorderPanel:
  private val worldMap: Panel = new Panel:
    background = java.awt.Color.BLUE
  private val createMenu: StationForm = StationForm(
    controller,
    () =>
      updateContent(StationEditorContent(worldMap, editMenu))
  )
  private val editMenu: StationEditorMenu = StationEditorMenu: () =>
    updateContent(StationEditorContent(worldMap, createMenu))

  updateContent(StationEditorContent(worldMap, editMenu))

  private def makeContent(content: StationEditorContent): GridBagPanel =
    new GridBagPanel:
      private val c = new Constraints
      c.fill = GridBagPanel.Fill.Both
      c.gridx = 0
      c.gridy = 0
      c.weighty = 1
      c.weightx = 0.7
      layout(content.worldMap) = c
      c.gridx = 1
      c.weightx = 0.3
      layout(content.stationEditorPanel) = c

  private def updateContent(content: StationEditorContent): Unit =
    layout(makeContent(content)) = BorderPanel.Position.Center
    revalidate()
    repaint()

final case class AppFrame() extends MainFrame:
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  contents = StationEditorPage(StationEditorController())
  pack()
  centerOnScreen()

@main def run(): Unit =
  AppFrame().open()

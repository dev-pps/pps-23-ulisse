package scala.view

import scala.swing.*
import scala.swing.BorderPanel.Position.*
import scala.swing.event.*

trait MapView

object MapView:
  def apply(): MapView = MapViewImpl()

  private case class MapViewImpl() extends MainFrame, MapView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(400, 300)

    // Main content pane with BorderLayout
    val contentPane  = new BorderPanel
    val createButton = new Button("Create")

    contentPane.layout(createButton) = North

    // GlassPane setup with BorderLayout
    val glassPane = new BorderPanel
    glassPane.visible = false // Initially invisible

    // Panel to appear on glassPane with form fields
    val formPanel: GridBagPanel = new GridBagPanel {
      val c = new Constraints

      c.fill = GridBagPanel.Fill.Horizontal
      c.gridx = 0; c.gridy = 0
      layout(new Label("Tipo di Tratta:")) = c

      c.gridx = 1; c.gridy = 0
      val trattaField = new ComboBox(Seq("Normale", "AV"))
      layout(trattaField) = c

      c.gridx = 0; c.gridy = 1
      layout(new Label("Numero di Rotaie:")) = c

      c.gridx = 1; c.gridy = 1
      val rotaieField = new TextField(10)
      layout(rotaieField) = c

      c.gridx = 0; c.gridy = 2
      layout(new Label("Stazione di Partenza:")) = c

      c.gridx = 1; c.gridy = 2
      val partenzaField = new TextField(10)
      layout(partenzaField) = c

      c.gridx = 0; c.gridy = 3
      layout(new Label("Stazione di Arrivo:")) = c

      c.gridx = 1; c.gridy = 3
      val arrivoField = new TextField(10)
      layout(arrivoField) = c

      c.gridx = 0; c.gridy = 4; c.gridwidth = 2
      layout(new Button("Submit") {
        reactions += {
          case ButtonClicked(_) =>
            Dialog.showMessage(
              parent = this,
              message =
                s"Tratta: ${trattaField.selection.item}, Rotaie: ${rotaieField.text}, Partenza: ${partenzaField.text}, Arrivo: ${arrivoField.text}",
              title = "Form Data"
            )
        }
      }) = c
    }

    glassPane.layout(formPanel) = Center

    // Create button action
    listenTo(createButton)
    reactions += {
      case ButtonClicked(`createButton`) =>
        glassPane.visible = true
        contentPane.visible = false
    }

    contents = new BorderPanel {
      layout(contentPane) = Center
      layout(glassPane) = North // GlassPane appears above
    }

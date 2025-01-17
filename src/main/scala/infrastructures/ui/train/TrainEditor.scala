package infrastructures.ui.train

import applications.train.TrainManager
import applications.train.TrainsControllers.TrainEditorController

import scala.swing.*
import scala.swing.event.SelectionChanged

object TrainEditor:
  object ViewModel:
    case class TrainData(
        name: String,
        technologyName: String,
        technologyMaxSpeed: Int,
        wagonNameType: String,
        wagonCapacity: Int,
        wagonCount: Int
    )

  extension (c: Component)
    private def onLeftOf(c1: Component) =
      new FlowPanel() {
        contents += c
        contents += c1
      }

  def apply(controller: TrainEditorController): Window =
    TrainEditImpl(controller)

  private class TrainEditImpl(val controller: TrainEditorController)
      extends Frame:
    import scala.swing.{Dimension, FlowPanel, ListView, ScrollPane}

    // TRAIN LIST
    private val trainListView = new ListView(controller.trainsData) {
      preferredSize = new Dimension(200, 100)
      minimumSize = preferredSize
      import scala.swing.ListView.IntervalMode
      selection.intervalMode = IntervalMode.Single
    }
    private val trainTypes       = controller.technologyNames
    private val carriageTypes    = controller.wagonTypeNames
    private val trainsFleetPanel = new FlowPanel(new ScrollPane(trainListView))
    private val nameField: TextField      = new TextField(10)
    private val carriagesField: TextField = new TextField(3)
    private val trainTypeCombo            = new ComboBox(trainTypes)
    private val carriageTypeCombo         = new ComboBox(carriageTypes)
    private val saveButton: Button        = new Button("Save")
    private val deleteButton: Button      = new Button("Delete")

    import scala.swing.{BoxPanel, Orientation}

    private val editPane: BoxPanel = new BoxPanel(Orientation.Vertical) {
      preferredSize = new Dimension(400, 200)
      minimumSize = preferredSize
      contents += new Label("Name:").onLeftOf(nameField)
      contents += new Label("Type:").onLeftOf(trainTypeCombo)
      contents += new Label("Transport type:").onLeftOf(carriageTypeCombo)
      contents += new Label("Carriages amount:").onLeftOf(carriagesField)
      contents += deleteButton.onLeftOf(saveButton)
    }

    new Frame {
      title = "Train Fleet Editor"
      contents = new BorderPanel {
        layout(trainsFleetPanel) = BorderPanel.Position.Center
        layout(editPane) = BorderPanel.Position.East
      }
      listenTo(trainListView.selection)

      reactions += {
        case SelectionChanged(`trainListView`) =>
          val selectedTrain = trainListView.selection.items.headOption
          selectedTrain.foreach(t =>
            nameField.text = t.name
            val selIndex = trainTypes.indexOf(t.technologyName)
            trainTypeCombo.selection.index = selIndex
            carriagesField.text = t.wagonNameType
          )
        case SelectionChanged(trainTypeCombo) =>
          println("updated selected type")
      }
      pack()
      centerOnScreen()
      open()
    }

  @main def main(): Unit =
    val model      = TrainManager.TrainModel()
    val controller = TrainEditorController(model)
    TrainEditor(controller)

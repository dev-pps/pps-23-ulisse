package train.view

import scala.swing.event.SelectionChanged
import scala.swing.{
  BorderPanel,
  Button,
  ComboBox,
  Component,
  FlowPanel,
  Frame,
  Label,
  TextField,
  Window
}

//object TrainEditor:
//  // DOMAIN
//
//  extension (c: Component)
//    private def onLeftOf(c1: Component) =
//      new FlowPanel() {
//        contents += c
//        contents += c1
//      }
//
//  def apply(): Window =
//    import scala.swing.{FlowPanel, ScrollPane, ListView, Dimension}
//    // TRAIN LIST
//    val trainList = new ListView(items) {
//      preferredSize = new Dimension(200, 100)
//      minimumSize = preferredSize
//      import scala.swing.ListView.IntervalMode
//      selection.intervalMode = IntervalMode.Single
//    }
//    val trainsFleetPanel          = new FlowPanel(new ScrollPane(trainList))
//    val nameField: TextField      = new TextField(10)
//    val carriagesField: TextField = new TextField(3)
//    val trainTypeCombo            = new ComboBox(trainTypes)
//    val carriageTypeCombo         = new ComboBox(carriageTypes)
//    val saveButton: Button        = new Button("Save")
//    val deleteButton: Button      = new Button("Delete")
//
//    import scala.swing.{BoxPanel, Orientation}
//    val editPane: BoxPanel = new BoxPanel(Orientation.Vertical) {
//      preferredSize = new Dimension(400, 200)
//      minimumSize = preferredSize
//      contents += new Label("Name:").onLeftOf(nameField)
//      contents += new Label("Type:").onLeftOf(trainTypeCombo)
//      contents += new Label("Transport type:").onLeftOf(carriageTypeCombo)
//      contents += new Label("Carriages amount:").onLeftOf(carriagesField)
//      contents += deleteButton.onLeftOf(saveButton)
//    }
//
//    new Frame {
//      title = "Train Fleet Editor"
//      contents = new BorderPanel {
//        layout(trainsFleetPanel) = BorderPanel.Position.Center
//        layout(editPane) = BorderPanel.Position.East
//      }
//      listenTo(trainList.selection)
//
//      reactions += {
//        case SelectionChanged(`trainList`) =>
//          val selectedTrain = trainList.selection.items.headOption
//          selectedTrain.foreach(t =>
//            nameField.text = t.name
//            val selIndex = trainTypes.indexOf(t.trainType)
//            trainTypeCombo.selection.index = selIndex
//            carriagesField.text = t.carriages.toString
//          )
//        case SelectionChanged(trainTypeCombo) =>
//          println("updated selected type")
//      }
//      pack()
//      centerOnScreen()
//      open()
//    }
//
//object GuiEntry extends App:
//  TrainEditor()

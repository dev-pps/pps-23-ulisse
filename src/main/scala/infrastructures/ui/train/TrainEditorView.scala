package infrastructures.ui.train

import scala.swing.*
import scala.swing.event.*
import scala.swing.{Dimension, FlowPanel, ListView, ScrollPane}
import scala.util.Try
import model.{TrainViewModel, TrainViewModelAdapter}
import applications.train.TrainPorts

object TrainEditorView:
  private val DEFAULT_WAGON_COUNT = 4
  extension (c: Component)
    private def onLeftOf(c1: Component) =
      new FlowPanel() {
        contents += c
        contents += c1
      }

  def apply(inServicePort: TrainPorts.InBound): Window =
    TrainEditImpl(TrainViewModelAdapter(inServicePort))

  private class TrainEditImpl(val modelAdapter: TrainViewModelAdapter)
      extends Frame:

    private val trainTech  = modelAdapter.technologies
    private val wagonTypes = modelAdapter.wagonTypes
    private val trains     = modelAdapter.trains

    private val trainListView = new ListView(trains) {
      preferredSize = new Dimension(200, 100)
      minimumSize = preferredSize
      import scala.swing.ListView.IntervalMode
      selection.intervalMode = IntervalMode.Single
    }

    private val trainsFleetPanel = new FlowPanel(new ScrollPane(trainListView))
    private val nameField: TextField        = new TextField(10)
    private val trainTechCombo              = new ComboBox(trainTech)
    private val wagonTypeCombo              = new ComboBox(wagonTypes)
    private val wagonCountAmount: TextField = SwingUtils.NumberField(2)
    private val wagonCapacity: TextField    = SwingUtils.NumberField(4)
    private val saveButton: Button          = new Button("Save")
    private val deleteButton: Button        = new Button("Delete")

    private def trainDataFromFields(): TrainViewModel.TrainData =
      val selectedTech =
        trainTech.lift(trainTechCombo.selection.index)
      val selectedWagonType = wagonTypes.lift(wagonTypeCombo.selection.index)
      TrainViewModel.TrainData(
        Option(nameField.text).filter(_.nonEmpty),
        technologyName = selectedTech.map(_.name).filter(_.nonEmpty),
        wagonNameType = selectedWagonType.map(_.useName).filter(_.nonEmpty),
        wagonCount = Try(wagonCountAmount.text.toInt).toOption,
        wagonCapacity = Try(wagonCapacity.text.toInt).toOption,
        technologyMaxSpeed = selectedTech.map(_.maxSpeed)
      )

    import scala.swing.{BoxPanel, Orientation}
    private val editPane: BoxPanel = new BoxPanel(Orientation.Vertical) {
      preferredSize = new Dimension(400, 200)
      minimumSize = preferredSize
      contents += new Label("Name:").onLeftOf(nameField)
      contents += new Label("Type:").onLeftOf(trainTechCombo)
      contents += new Label("Transport type:").onLeftOf(wagonTypeCombo)
      contents += new Label("Wagon Capacity:").onLeftOf(wagonCapacity)
      contents += new Label("wagons amount:").onLeftOf(wagonCountAmount)
      contents += deleteButton.onLeftOf(saveButton)
    }

    deleteButton.reactions += {
      case ButtonClicked(_) =>
        println("delete clicked")
        for
          selectedTrain <- trainListView.selection.items.headOption
          trainName     <- selectedTrain.name
        yield modelAdapter.deleteTrain(trainName)
        trainListView.listData = modelAdapter.trains
    }

    saveButton.reactions += {
      case ButtonClicked(_) =>
        println("save clicked")
        modelAdapter.addTrain(trainDataFromFields())
        trainListView.listData = modelAdapter.trains
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
            nameField.text = t.name.getOrElse("")
            trainTechCombo.selection.index = trainTech.indexOf(t.technologyName)
            wagonCountAmount.text =
              t.wagonCount.getOrElse(DEFAULT_WAGON_COUNT).toString
            wagonTypeCombo.selection.index = wagonTypes.indexOf(t.wagonNameType)
          )
        case SelectionChanged(trainTypeCombo) =>
          println("updated selected type")
      }

      pack()
      centerOnScreen()
      open()
    }

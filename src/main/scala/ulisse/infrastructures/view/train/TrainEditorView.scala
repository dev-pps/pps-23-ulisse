package ulisse.infrastructures.view.train

import SwingUtils.onLeftOf
import ulisse.applications.ports.TrainPorts
import ulisse.infrastructures.view.train.model.TrainViewModel.{emptyTrainData, TechType, TrainData, WagonName}
import ulisse.infrastructures.view.train.model.{TrainViewModel, TrainViewModelAdapter}

import scala.swing.event.*
import scala.swing.*
import scala.swing.Dialog.Message
import scala.util.Try

trait TrainEditorView:
  def updateTrainList(trains: List[TrainData]): Unit
  def updateTechnology(techs: List[TechType]): Unit
  def updateWagons(wagons: List[WagonName]): Unit
  def showError(errorMessage: String): Unit

object TrainEditorView:

  def apply(inServicePort: TrainPorts.Input): Window =
    TrainEditImpl(inServicePort)

  private class TrainEditImpl(val port: TrainPorts.Input)
      extends Frame, TrainEditorView:
    private val modelAdapter = TrainViewModelAdapter(port, this)
    modelAdapter.requestTechnologies()
    modelAdapter.requestWagonTypes()
    modelAdapter.requestTrains()

    private val trainListView = TrainListView(List.empty)

    private val trainsFleetPanel                    = new FlowPanel(new ScrollPane(trainListView))
    private val nameField: TextField                = new TextField(10)
    private val trainTechCombo: ComboBox[TechType]  = new ComboBox(List.empty)
    private val wagonTypeCombo: ComboBox[WagonName] = new ComboBox(List.empty)
    private val wagonCountAmount: TextField         = SwingUtils.NumberField(2)
    private val wagonCapacity: TextField            = SwingUtils.NumberField(4)
    private val saveButton: Button                  = new Button("Save")
    private val updateButton: Button                = new Button("Update")
    private val deleteButton: Button                = new Button("Delete")
    private val clearButton: Button                 = new Button("Clear")

    updateButton.enabled = false
    deleteButton.enabled = false

    clearButton.reactions += {
      case ButtonClicked(_) => clearFields()
    }

    deleteButton.reactions += {
      case ButtonClicked(_) =>
        for
          selectedTrain <- trainListView.selection.items.headOption
          trainName     <- selectedTrain.name
        yield modelAdapter.deleteTrain(trainName)
        updateButton.enabled = true
        clearFields()
    }

    saveButton.reactions += {
      case ButtonClicked(_) =>
        modelAdapter.addTrain(getFormFields)
        clearFields()
    }

    updateButton.reactions += {
      case ButtonClicked(_) =>
        modelAdapter.updateTrain(getFormFields)
        clearFields()
    }

    import scala.swing.{BoxPanel, Orientation}
    private val editPane: BoxPanel = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Name:").onLeftOf(nameField)
      contents += new Label("Type:").onLeftOf(trainTechCombo)
      contents += new Label("Transport type:").onLeftOf(wagonTypeCombo)
      contents += new Label("Wagon Capacity:").onLeftOf(wagonCapacity)
      contents += new Label("wagons amount:").onLeftOf(wagonCountAmount)
      contents += deleteButton.onLeftOf(clearButton).onLeftOf(updateButton).onLeftOf(saveButton)
    }

    private val mainFrame = new Frame {
      title = "Train Fleet Editor"
      contents = new BorderPanel {
        layout(trainsFleetPanel) = BorderPanel.Position.Center
        layout(editPane) = BorderPanel.Position.East
      }
      listenTo(trainListView.selection)

      reactions += {
        case SelectionChanged(`trainListView`) =>
          val selectedTrain = trainListView.selection.items.headOption
          selectedTrain.map(loadFormFields)
      }

      pack()
      centerOnScreen()
      open()
    }

    private def clearFields(): Unit =
      loadFormFields(emptyTrainData)
      updateButton.enabled = false
      deleteButton.enabled = false

    private def loadFormFields(t: TrainViewModel.TrainData): Unit =
      for
        n  <- t.name
        tk <- t.technologyName
        ts <- t.technologyMaxSpeed
        ac <- t.technologyAcc
        de <- t.technologyDec
        wt <- t.wagonNameType
        c  <- t.wagonCapacity
        wc <- t.wagonCount
      yield
        nameField.text = n
        trainTechCombo.selection.item = TechType(tk, ts, ac, de)
        wagonTypeCombo.selection.item = WagonName(wt)
        wagonCapacity.text = c.toString
        wagonCountAmount.text = wc.toString
        updateButton.enabled = true
        deleteButton.enabled = true

    private def getFormFields: TrainViewModel.TrainData =
      TrainViewModel.TrainData(
        Option(nameField.text).filter(_.nonEmpty),
        technologyName = Some(getSelectedTechnology.name),
        technologyAcc = Some(getSelectedTechnology.acc),
        technologyDec = Some(getSelectedTechnology.dec),
        wagonNameType = Some(getSelectedWagonType.useName),
        wagonCount = Try(wagonCountAmount.text.toInt).toOption,
        wagonCapacity = Try(wagonCapacity.text.toInt).toOption,
        technologyMaxSpeed = Some(getSelectedTechnology.maxSpeed)
      )

    private def getSelectedTechnology: TechType = trainTechCombo.selection.item

    private def getSelectedWagonType: WagonName = wagonTypeCombo.selection.item

    override def updateTrainList(trains: List[TrainData]): Unit =
      Swing.onEDT(trainListView.listData = trains)

    override def updateTechnology(techs: List[TechType]): Unit =
      println("Update technology combobox")
      Swing.onEDT(trainTechCombo.peer.setModel(ComboBox.newConstantModel(techs)))

    override def updateWagons(wagons: List[WagonName]): Unit =
      println("Update wagon combobox")
      Swing.onEDT(wagonTypeCombo.peer.setModel(ComboBox.newConstantModel(wagons)))

    override def showError(errorMessage: String): Unit =
      Swing.onEDT(
        Dialog.showMessage(
          mainFrame,
          errorMessage,
          "Error !",
          Message.Error
        )
      )

package ulisse.infrastructures.view.train

import ulisse.applications.ports.TrainPorts
import ulisse.infrastructures.view.components.ExtendedSwing.{SButton, SFlowPanel, SLabel, SNumberField}
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import ulisse.infrastructures.view.train.model.TrainViewModel.{emptyTrainData, TechType, TrainData, WagonName}
import ulisse.infrastructures.view.train.model.{TrainViewAdapter, TrainViewModel}
import ulisse.infrastructures.view.utils.SwingUtils

import scala.swing.event.*
import scala.swing.*
import scala.swing.Dialog.Message

trait TrainEditorView:
  def updateTrainList(trains: List[TrainData]): Unit
  def updateTechnology(techs: List[TechType]): Unit
  def updateWagons(wagons: List[WagonName]): Unit
  def showError(errorMessage: String): Unit

object TrainEditorView:

  def apply(inServicePort: TrainPorts.Input): Panel =
    TrainEditImpl(inServicePort)

  private class TrainEditImpl(val port: TrainPorts.Input)
      extends SFlowPanel, TrainEditorView:
    private val modelAdapter = TrainViewAdapter(port, this)
    modelAdapter.requestTechnologies()
    modelAdapter.requestWagonTypes()
    modelAdapter.requestTrains()

    private val trainListView                       = TrainsViews.TrainListView(List.empty)
    private val trainsFleetPanel                    = new ScrollPane(trainListView)
    private val nameField                           = ComposedSwing.createInfoTextField("Name")
    private val trainTechCombo: ComboBox[TechType]  = ComboBox(List.empty)
    private val wagonTypeCombo: ComboBox[WagonName] = ComboBox(List.empty)
    private val wagonCountAmount: TextField         = SNumberField(2)
    private val wagonCapacity: TextField            = SNumberField(4)
    private val saveBtn                             = SButton("Save")
    private val updateBtn                           = SButton("Update")
    private val deleteBtn                           = SButton("Delete")
    private val clearBtn                            = SButton("Clear")
    List(saveBtn, updateBtn).foreach(_.rect = Styles.formTrueButtonRect)
    List(deleteBtn, clearBtn).foreach(_.rect = Styles.formFalseButtonRect)
    List(saveBtn, updateBtn, deleteBtn, clearBtn).foreach(_.fontEffect = Styles.whiteFont)

    updateBtn.enabled = false
    deleteBtn.enabled = false

    clearBtn.reactions += {
      case ButtonClicked(_) => clearFields()
    }

    deleteBtn.reactions += {
      case ButtonClicked(_) =>
        for
          selectedTrain <- trainListView.selection.items.headOption
          trainName     <- selectedTrain.name
        yield modelAdapter.deleteTrain(trainName)
        updateBtn.enabled = true
        clearFields()
    }

    saveBtn.reactions += {
      case ButtonClicked(_) =>
        modelAdapter.addTrain(getFormFields)
        clearFields()
    }

    updateBtn.reactions += {
      case ButtonClicked(_) =>
        modelAdapter.updateTrain(getFormFields)
        clearFields()
    }

    import scala.swing.{BoxPanel, Orientation}
    import ulisse.infrastructures.view.utils.ComponentUtils.createLeftRight
    private val editPane: BoxPanel = new BoxPanel(Orientation.Vertical) {
      contents += nameField.component
      contents += SLabel("Type:").createLeftRight(trainTechCombo)
      contents += Label("Transport type:").createLeftRight(wagonTypeCombo)
      contents += SLabel("Wagon Capacity:").createLeftRight(wagonCapacity)
      contents += SLabel("wagons amount:").createLeftRight(wagonCountAmount)
      contents += deleteBtn.createLeftRight(clearBtn).createLeftRight(updateBtn).createLeftRight(saveBtn)
    }

    private val mainPanel = new BorderPanel {
      preferredSize = new Dimension(800, 400)
      layout(trainsFleetPanel) = BorderPanel.Position.Center
      layout(editPane) = BorderPanel.Position.East
      listenTo(trainListView.selection)

      reactions += {
        case SelectionChanged(`trainListView`) =>
          val selectedTrain = trainListView.selection.items.headOption
          selectedTrain.map(loadFormFields)
      }
    }
    contents += mainPanel

    private def clearFields(): Unit =
      loadFormFields(emptyTrainData)
      trainListView.selectIndices(-1)
      updateBtn.enabled = false
      deleteBtn.enabled = false
      nameField.editable = true

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
        nameField.editable = false
        trainTechCombo.selection.item = TechType(tk, ts, ac, de)
        wagonTypeCombo.selection.item = WagonName(wt)
        wagonCapacity.text = c.toString
        wagonCountAmount.text = wc.toString
        updateBtn.enabled = true
        deleteBtn.enabled = true

    private def getFormFields: TrainViewModel.TrainData =
      TrainViewModel.TrainData(
        Option(nameField.text).filter(_.nonEmpty),
        technologyName = Some(getSelectedTechnology.name),
        technologyAcc = Some(getSelectedTechnology.acc),
        technologyDec = Some(getSelectedTechnology.dec),
        wagonNameType = Some(getSelectedWagonType.useName),
        wagonCount = wagonCountAmount.text.toIntOption,
        wagonCapacity = wagonCapacity.text.toIntOption,
        technologyMaxSpeed = Some(getSelectedTechnology.maxSpeed)
      )

    private def getSelectedTechnology: TechType = trainTechCombo.selection.item

    private def getSelectedWagonType: WagonName = wagonTypeCombo.selection.item

    override def updateTrainList(trains: List[TrainData]): Unit =
      Swing.onEDT({
        trainListView.updateDataModel(trains)
      })

    override def updateTechnology(techs: List[TechType]): Unit =
      Swing.onEDT(trainTechCombo.peer.setModel(ComboBox.newConstantModel(techs)))

    override def updateWagons(wagons: List[WagonName]): Unit =
      Swing.onEDT(wagonTypeCombo.peer.setModel(ComboBox.newConstantModel(wagons)))

    override def showError(errorMessage: String): Unit =
      Swing.onEDT(
        Dialog.showMessage(
          mainPanel,
          errorMessage,
          "Something goes wrong !",
          Message.Warning
        )
      )

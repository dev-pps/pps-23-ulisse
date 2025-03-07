package ulisse.infrastructures.view.train

import ulisse.adapters.input.TrainViewAdapter
import ulisse.infrastructures.view.components.ExtendedSwing.{SBoxPanel, SButton, SFieldLabel, SFlowPanel, SNumberField}
import ulisse.infrastructures.view.components.composed.ComposedSwing
import ulisse.infrastructures.view.components.styles.Styles
import TrainViewModel.{emptyTrainData, TechType, TrainData, WagonTypeName}
import ulisse.infrastructures.view.utils.SwingUtils

import scala.swing.event.*
import scala.swing.*
import scala.swing.Dialog.Message

/** Train editor view that can updates showed trains, technologies, wagon types and shows errors. */
trait TrainEditorView:
  /** Updates train list selector with given `trains` */
  def updateTrainList(trains: List[TrainData]): Unit

  /** Updates technology type selector with given `technologies` */
  def updateTechnology(technologies: List[TechType]): Unit

  /** Updates wagons type names selector with given `wagons` */
  def updateWagons(wagons: List[WagonTypeName]): Unit

  /** Shows an `errorMessage` */
  def showError(errorMessage: String): Unit

object TrainEditorView:
  /** Creates train editor view with its `adapter`. */
  def apply(adapter: TrainViewAdapter): Component =
    TrainEditImpl(adapter)

  private class TrainEditImpl(val modelAdapter: TrainViewAdapter)
      extends SFlowPanel, TrainEditorView:
    modelAdapter.setView(this)
    private val trainListView                           = TrainListView.TrainListView(List.empty)
    private val trainsFleetPanel                        = ScrollPane(trainListView)
    private val nameField                               = ComposedSwing.createInfoTextField("Name")
    private val trainTechCombo: ComboBox[TechType]      = ComboBox(List.empty)
    private val wagonTypeCombo: ComboBox[WagonTypeName] = ComboBox(List.empty)
    private val wagonCountAmount: TextField             = SNumberField(2)
    private val wagonCapacity: TextField                = SNumberField(4)
    private val saveBtn                                 = SButton("Save")
    private val updateBtn                               = SButton("Update")
    private val deleteBtn                               = SButton("Delete")
    private val clearBtn                                = SButton("Clear")
    List(saveBtn, updateBtn).foreach(_.rect = Styles.formTrueButtonRect)
    List(deleteBtn, clearBtn).foreach(_.rect = Styles.formFalseButtonRect)
    List(saveBtn, updateBtn, deleteBtn, clearBtn).foreach(_.fontEffect = Styles.whiteFont)
    modelAdapter.requestTechnologies()
    modelAdapter.requestWagonTypes()
    modelAdapter.requestTrains()
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
    import ulisse.infrastructures.view.utils.SwingUtils.vSpaced
    private val editPane: BoxPanel = SBoxPanel(Orientation.Vertical)
    editPane.contents ++=
      List(
        nameField.component,
        SFieldLabel("Type:")(trainTechCombo).component,
        SFieldLabel("Transport type:")(wagonTypeCombo).component,
        SFieldLabel("Wagon Capacity:")(wagonCapacity).component,
        SFieldLabel("wagons amount:")(wagonCountAmount).component,
        deleteBtn.createLeftRight(clearBtn).createLeftRight(updateBtn).createLeftRight(saveBtn)
      ).vSpaced(15)
    import ulisse.infrastructures.view.utils.SwingUtils.setDefaultFont
    List(trainTechCombo, wagonTypeCombo).setDefaultFont()

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
        wagonTypeCombo.selection.item = WagonTypeName(wt)
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

    private def getSelectedWagonType: WagonTypeName = wagonTypeCombo.selection.item

    override def updateTrainList(trains: List[TrainData]): Unit =
      Swing.onEDT({
        trainListView.updateDataModel(trains)
      })

    override def updateTechnology(technologies: List[TechType]): Unit =
      Swing.onEDT(trainTechCombo.peer.setModel(ComboBox.newConstantModel(technologies)))

    override def updateWagons(wagons: List[WagonTypeName]): Unit =
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

@main def trainViewDemoGUI(): Unit =
  import ulisse.infrastructures.view.utils.SwingUtils.showPreview
  import ulisse.adapters.MockedPorts.TrainServiceMock
  val adapter = TrainViewAdapter(TrainServiceMock())
  TrainEditorView(adapter).showPreview()

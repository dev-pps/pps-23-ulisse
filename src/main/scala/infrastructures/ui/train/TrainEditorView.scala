package infrastructures.ui.train

import scala.swing.*
import scala.swing.event.*
import scala.swing.{FlowPanel, ScrollPane}
import scala.util.Try
import model.{TrainViewModel, TrainViewModelAdapter}
import SwingUtils.onLeftOf
import applications.train.TrainPorts

object TrainEditorView:

  def apply(inServicePort: TrainPorts.InBound): Window =
    TrainEditImpl(TrainViewModelAdapter(inServicePort))

  private class TrainEditImpl(val modelAdapter: TrainViewModelAdapter)
      extends Frame:

    private val trainTech  = modelAdapter.technologies
    private val wagonTypes = modelAdapter.wagonTypes
    private val trains     = modelAdapter.trains

    private val trainListView = TrainListView(trains)

    private val trainsFleetPanel            = new FlowPanel(new ScrollPane(trainListView))
    private val nameField: TextField        = new TextField(10)
    private val trainTechCombo              = new ComboBox(trainTech)
    private val wagonTypeCombo              = new ComboBox(wagonTypes)
    private val wagonCountAmount: TextField = SwingUtils.NumberField(2)
    private val wagonCapacity: TextField    = SwingUtils.NumberField(4)
    private val saveButton: Button          = new Button("Save")
    private val updateButton: Button        = new Button("Update")
    private val deleteButton: Button        = new Button("Delete")
    private val clearButton: Button         = new Button("Clear")

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
        updateListAndClearFields()
    }

    saveButton.reactions += {
      case ButtonClicked(_) =>
        modelAdapter.addTrain(getFormFields)
        updateListAndClearFields()
    }

    updateButton.reactions += {
      case ButtonClicked(_) =>
        modelAdapter.updateTrain(getFormFields)
        updateListAndClearFields()
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
          selectedTrain.map(loadFormFields)
      }

      pack()
      centerOnScreen()
      open()
    }

    private def clearFields(): Unit =
      import TrainViewModel.TrainData
      val emptyString = Some("")
      val unsetValue  = Some(0)
      loadFormFields(TrainData(emptyString, emptyString, emptyString, unsetValue, unsetValue, unsetValue))
      updateButton.enabled = false
      deleteButton.enabled = false

    private def loadFormFields(t: TrainViewModel.TrainData): Unit =
      for
        n  <- t.name
        tk <- t.technologyName
        wt <- t.wagonNameType
        c  <- t.wagonCapacity
        wc <- t.wagonCount
      yield
        nameField.text = n
        trainTechCombo.selection.index = trainTech.map(_.name).indexOf(tk)
        wagonTypeCombo.selection.index = wagonTypes.map(_.useName).indexOf(wt)
        wagonCapacity.text = c.toString
        wagonCountAmount.text = wc.toString
        updateButton.enabled = true
        deleteButton.enabled = true

    private def getFormFields: TrainViewModel.TrainData =
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

    private def updateListAndClearFields(): Unit = {
      trainListView.listData = modelAdapter.trains
      clearFields()
    }

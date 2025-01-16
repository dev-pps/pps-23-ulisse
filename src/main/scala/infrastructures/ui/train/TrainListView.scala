package infrastructures.ui.train

import infrastructures.ui.train.model.TrainViewModel
import java.awt.Color
import scala.swing.{BoxPanel, Component, Dimension, Font, Label, ListView, Orientation, Swing}

object TrainListView:

  private val selectedColor = Color.decode("#fff3d0")
  private val labelFont     = new Font("Arial", java.awt.Font.BOLD, 14)
  private val valueFont     = new Font("Arial", java.awt.Font.PLAIN, 14)

  def apply(trains: List[TrainViewModel.TrainData]): ListView[TrainViewModel.TrainData] =
    new ListView(trains) {
      preferredSize = new Dimension(400, 100)
      import scala.swing.ListView.IntervalMode
      selection.intervalMode = IntervalMode.Single
      renderer = new ItemRenderer[TrainViewModel.TrainData]
    }

  private class ItemRenderer[T] extends ListView.Renderer[TrainViewModel.TrainData] {
    override def componentFor(
        list: ListView[_ <: TrainViewModel.TrainData],
        isSelected: Boolean,
        focused: Boolean,
        item: TrainViewModel.TrainData,
        index: Int
    ): Component = {
      new TrainDataPanel(item) {
        background = if isSelected then selectedColor else Color.WHITE
      }
    }
  }

  private class TrainDataPanel(trainData: TrainViewModel.TrainData) extends BoxPanel(Orientation.Vertical) {

    private def createRow(labelText: String, value: Option[String]): BoxPanel = {
      new BoxPanel(Orientation.Horizontal) {
        contents += new Label(labelText + ":") {
          font = labelFont
        }
        contents += Swing.HStrut(10)
        contents += new Label(value.getOrElse("N/A")) {
          font = valueFont
        }
        background = Color.white
      }
    }
    contents += new Label(trainData.name.getOrElse("N/A")) {
      font = new Font("Arial", java.awt.Font.BOLD, 18)
    }
    contents += createRow("Technology Name", trainData.technologyName)
    contents += createRow("Wagon Name Type", trainData.wagonNameType)
    contents += createRow("Wagon Count", trainData.wagonCount.map(_.toString))
    contents += createRow("Technology Max Speed", trainData.technologyMaxSpeed.map(_.toString))
    contents += createRow("Wagon Capacity", trainData.wagonCapacity.map(_.toString))
    border = Swing.EmptyBorder(10, 10, 10, 10) // Padding
  }

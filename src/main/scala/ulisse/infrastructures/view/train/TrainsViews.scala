package ulisse.infrastructures.view.train

import ulisse.infrastructures.view.train.model.TrainViewModel
import ulisse.infrastructures.view.utils.SwingUtils.{defaultString, headerLabel, nameFont, valueLabel}
import java.awt.Color
import scala.swing.Swing.{EmptyBorder, HGlue, HStrut}
import scala.swing.{BoxPanel, Component, Label, ListView, Orientation, Swing}

object TrainsViews:

  private val selectedColor = Color.decode("#fff3d0")

  case class TrainListView(trains: List[TrainViewModel.TrainData]) extends ListView(trains):
    import scala.swing.ListView.IntervalMode
    selection.intervalMode = IntervalMode.Single
    renderer = new ItemRenderer[TrainViewModel.TrainData]

    def updateDataModel(data: List[TrainViewModel.TrainData]): Unit =
      listData = data
      revalidate()

    private class ItemRenderer[T] extends ListView.Renderer[TrainViewModel.TrainData] {
      override def componentFor(
          list: ListView[_ <: TrainViewModel.TrainData],
          isSelected: Boolean,
          focused: Boolean,
          item: TrainViewModel.TrainData,
          index: Int
      ): Component = {
        new TrainInfoCard(item, isSelected) {
          background = if isSelected then selectedColor else Color.WHITE
        }
      }
    }

    private class TrainInfoCard(trainData: TrainViewModel.TrainData, isSelected: Boolean)
        extends BoxPanel(Orientation.Vertical) {
      border = EmptyBorder(10, 20, 10, 20)
      private def createRow(labelText: String, value: Option[String]): BoxPanel = {
        new BoxPanel(Orientation.Horizontal) {
          contents += s"$labelText:".headerLabel
          contents += HGlue
          contents += value.valueLabel
          background = if isSelected then selectedColor else Color.WHITE
        }
      }
      private val trainIcon = Label("icon train") // ImagePanel.createImagePanel("train-icon.png")
      private val titleLabel = new Label(trainData.name.getOrElse("N/A")) {
        font = nameFont
      }
      private val titlePane = new BoxPanel(Orientation.Horizontal) {
        contents += HStrut(20)
        contents += titleLabel
        contents += HGlue
        contents += trainIcon
        contents += HGlue
      }
      contents += titlePane
      contents += createRow("Technology Name", trainData.technologyName)
      contents += createRow("Wagon Name Type", trainData.wagonNameType)
      contents += createRow("Wagon Count", trainData.wagonCount.map(_.toString))
      contents += createRow("Technology Max Speed", trainData.technologyMaxSpeed.map(_.toString))
      contents += createRow("Wagon Capacity", trainData.wagonCapacity.map(_.toString))
    }

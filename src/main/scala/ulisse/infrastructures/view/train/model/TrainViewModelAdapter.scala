package ulisse.infrastructures.view.train.model

import ulisse.entities.train.Wagons.UseType
import ulisse.applications.train.TrainPorts
import ulisse.entities.train.{Technology, Wagons}
import TrainViewModel.*

trait TrainViewModelAdapter:
  def trains: List[TrainData]
  def technologies: List[TechType]
  def wagonTypes: List[Wagon]
  def addTrain(trainData: TrainData): Unit
  def deleteTrain(name: String): Unit
  def updateTrain(trainData: TrainData): Unit

object TrainViewModelAdapter:
  def apply(trainService: TrainPorts.InBound): TrainViewModelAdapter =
    BaseAdapter(trainService)

  private final case class BaseAdapter(trainService: TrainPorts.InBound)
      extends TrainViewModelAdapter:
    override def trains: List[TrainData] = trainService.trains.map(t =>
      TrainData(
        name = Some(t.name),
        technologyName = Some(t.techType.name),
        technologyMaxSpeed = Some(t.techType.maxSpeed),
        wagonNameType = Some(t.wagon.use.name),
        wagonCapacity = Some(t.wagon.capacity),
        wagonCount = Some(t.wagonCount)
      )
    )

    override def addTrain(trainData: TrainData): Unit =
      for
        n  <- trainData.name
        tk <- trainData.technologyName
        wc <- trainData.wagonCount
        wt <- trainData.wagonNameType
        wa <- trainData.wagonCapacity
      yield trainService.addTrain(n, tk, wt, wa, wc)

    override def deleteTrain(name: String): Unit = trainService.removeTrain(name)

    override def updateTrain(trainData: TrainData): Unit =
      for
        n  <- trainData.name
        tk <- trainData.technologyName
        ts <- trainData.technologyMaxSpeed
        wc <- trainData.wagonCount
        wt <- trainData.wagonNameType
        wa <- trainData.wagonCapacity
      yield trainService.updateTrain(n)(Technology(tk, ts), Wagons.Wagon(UseType.valueOf(wt), wa), wc)

    override def wagonTypes: List[Wagon] =
      trainService.wagonTypes.map(w => Wagon(w.name))

    override def technologies: List[TechType] =
      trainService.technologies.map(t => TechType(t.name, t.maxSpeed))

package infrastructures.ui.train.model

import applications.train.TrainPorts
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

  override def technologies: List[TechType] =
    trainService.technologies.map(t => TechType(t.name, t.maxSpeed))
  override def addTrain(trainData: TrainData): Unit =
    for
      name          <- trainData.name
      techName      <- trainData.technologyName
      wagonCount    <- trainData.wagonCount
      wagonType     <- trainData.wagonNameType
      wagonCapacity <- trainData.wagonCapacity
    yield trainService.addTrain(
      name,
      techName,
      wagonType,
      wagonCapacity,
      wagonCount
    )
  override def deleteTrain(name: String): Unit         = trainService.removeTrain(name)
  override def updateTrain(trainData: TrainData): Unit = None
  override def wagonTypes: List[Wagon] =
    trainService.wagonTypes.map(w => Wagon(w.name))

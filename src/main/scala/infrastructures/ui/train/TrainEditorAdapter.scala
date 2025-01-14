package infrastructures.ui.train

import applications.train.TrainPorts
import infrastructures.ui.train.TrainViewModel.*

trait TrainEditorAdapter:
  def trains: List[TrainData]
  def technologies: List[TechType]
  def wagonTypes: List[Wagon]
  def addTrain(trainData: TrainData): Unit
  def deleteTrain(name: String): Unit
  def updateTrain(trainData: TrainData): Unit

object TrainEditorAdapter:
  def apply(trainService: TrainPorts.InBound): TrainEditorAdapter =
    BaseAdapter(trainService)

private final case class BaseAdapter(trainService: TrainPorts.InBound)
    extends TrainEditorAdapter:
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
    import entities.train.Trains.Train
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
  override def deleteTrain(name: String): Unit = trainService.removeTrain(name)
  override def updateTrain(trainData: TrainData): Unit = None
  override def wagonTypes: List[Wagon] =
    trainService.wagonTypes.map(w => Wagon(w.name))

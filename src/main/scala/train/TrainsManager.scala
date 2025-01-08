package train

import train.Trains.{Train, TrainType}

object TrainsManager:
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  private var _trains: List[Train] = List.empty

  private def trainExist(name: String): Boolean =
    _trains.map(_.name).contains(name)

  def addTrain(train: Train): Unit = _trains =
    require(
      !trainExist(train.name),
      s"train called '${train.name}' already exist"
    )
    _trains.appended(train)

  def createTrain(
      name: String,
      trainType: Trains.TrainType,
      carriages: Int,
      capacity: Int
  ): Unit =
    require(!trainExist(name), s"train called '$name' already exist")
    val t = trainType match
      case TrainType.Normal =>
        Trains.makeRegionalTrain(name, carriages, capacity)
      case TrainType.HighSpeed =>
        Trains.makeHighSpeedTrain(name, carriages, capacity)
    _trains = _trains.appended(t)

  def listTrains: List[Train] = _trains

  def delete(name: String): Unit =
    require(trainExist(name), s"train called '$name' not exist")
    _trains = _trains.filterNot(t => t.name == name)

  def update(name: String)(
      trainType: TrainType,
      carriages: Int,
      capacity: Int
  ): Unit =
    delete(name)
    createTrain(name, trainType, carriages, capacity)

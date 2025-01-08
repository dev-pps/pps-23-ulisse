package train

import train.Trains.TrainType.{HighSpeed, Normal}

object Trains:

  enum TrainType(val name: String, val maxSpeed: Int):
    case Normal    extends TrainType("Normal", 130)
    case HighSpeed extends TrainType("HighSpeed", 300)

  trait Train:
    def trainType: TrainType
    def maxSpeed: Int
    val name: String
    val carriages: Int
    val capacity: Int

  private class TrainImpl(
      val name: String,
      val carriages: Int,
      val capacity: Int,
      val trainType: TrainType
  ) extends Train:
    def maxSpeed: Int = trainType.maxSpeed

  def makeTrain(
      name: String,
      trainType: TrainType,
      carriages: Int,
      capacity: Int
  ): Train =
    TrainImpl(name, carriages, capacity, trainType)

  def makeRegionalTrain(name: String, carriages: Int, capacity: Int): Train =
    TrainImpl(name, carriages, capacity, Normal)

  def makeHighSpeedTrain(name: String, carriages: Int, capacity: Int): Train =
    TrainImpl(name, carriages, capacity, HighSpeed)

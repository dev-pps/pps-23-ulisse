package train

import train.Trains.TrainType.{HIGH_SPEED, NORMAL}

object Trains:
  enum TrainType(val maxSpeed: Int):
    case NORMAL extends TrainType(130)
    case HIGH_SPEED extends TrainType(300)

  abstract class Train:
    def trainType: TrainType
    def maxSpeed: Int = trainType.maxSpeed
    val name: String
    val carriages: Int
    val capacity: Int

  trait NormalSpeed extends Train:
    override def trainType: TrainType = NORMAL

  trait HighSpeed extends Train:
    override def trainType: TrainType = HIGH_SPEED

  class RegionalTrain(override val name: String,
                      override val carriages: Int,
                      override val capacity: Int) extends Train with NormalSpeed

  class HighSpeedTrain(override val name: String,
                      override val carriages: Int,
                      override val capacity: Int) extends Train with HighSpeed
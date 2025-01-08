package train

import train.Trains.TrainType.{HIGH_SPEED, NORMAL}

object Trains:
  enum TrainType(val maxSpeed: Int):
    case NORMAL     extends TrainType(130)
    case HIGH_SPEED extends TrainType(300)

  trait Train:
    def trainType: TrainType
    def maxSpeed: Int
    val name: String
    val carriages: Int
    val capacity: Int

  private abstract class AbstractTrain extends Train:
    def trainType: TrainType
    def maxSpeed: Int = trainType.maxSpeed
    val name: String
    val carriages: Int
    val capacity: Int

  private trait NormalSpeed extends AbstractTrain:
    override def trainType: TrainType = NORMAL

  private trait HighSpeed extends AbstractTrain:
    override def trainType: TrainType = HIGH_SPEED

  private class RegionalTrain(
      override val name: String,
      override val carriages: Int,
      override val capacity: Int
  ) extends AbstractTrain with NormalSpeed

  private class HighSpeedTrain(
      override val name: String,
      override val carriages: Int,
      override val capacity: Int
  ) extends AbstractTrain with HighSpeed

  def makeRegionalTrain(name: String, carriages: Int, capacity: Int): Train =
    RegionalTrain(name, carriages, capacity)

  def makeHighSpeedTrain(name: String, carriages: Int, capacity: Int): Train =
    HighSpeedTrain(name, carriages, capacity)

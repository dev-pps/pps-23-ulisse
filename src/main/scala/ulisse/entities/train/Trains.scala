package ulisse.entities.train

import ulisse.entities.Technology
import ulisse.entities.train.Wagons.Wagon

/** Object containing [[Train]] and [[TrainTechnology]] */
object Trains:

  /** Technology used by train */
  trait TrainTechnology extends Technology:
    /** Returns value of acceleration in m/s */
    def acceleration: Double

    /** Returns value of deceleration in m/s */
    def deceleration: Double

  /** Companion object of [[TrainTechnology]] */
  object TrainTechnology:
    def apply(name: String, maxSpeed: Int, acceleration: Double, deceleration: Double): TrainTechnology =
      TrainTechnologyImpl(name, maxSpeed, acceleration, deceleration)

    private case class TrainTechnologyImpl(n: String, speed: Int, acc: Double, dec: Double) extends TrainTechnology:
      override def name: String  = n
      override def maxSpeed: Int = speed
      def acceleration: Double   = acc
      def deceleration: Double   = dec

  /** Train is characterized by [[Technology]], [[Wagons.Wagon]] types and total capacity and wagon amount. */
  trait Train:

    /** Train name */
    def name: String

    /** Technology used by train */
    def techType: TrainTechnology

    /** wagon which train is composed by */
    def wagon: Wagons.Wagon

    /** Amount of wagons */
    def length: Int

    /** Length size in meters */
    def lengthSize: Int

    /** Returns max speed reachable by train */
    def maxSpeed: Int

    /** Returns total transport capacity */
    def capacity: Int

    /** Defines equality for Trains */
    override def equals(that: Any): Boolean =
      that match
        case that: Train =>
          name == that.name
        case _ => false

  /** Factory for [[Trains.Train]] instances. */
  object Train:
    /** Creates Train with a given `name`, `techType` technology type, `wagon` information and `length` (count of wagons). */
    def apply(
        name: String,
        techType: TrainTechnology,
        wagon: Wagon,
        length: Int
    ): Train =
      TrainImpl(name, techType, wagon, length)

    /** Returns an `Option` containing all characteristic of train. */
    def unapply(train: Train): Option[(String, TrainTechnology, Wagon, Int)] =
      Some(train.name, train.techType, train.wagon, train.length)

    private case class TrainImpl(
        name: String,
        techType: TrainTechnology,
        wagon: Wagon,
        length: Int
    ) extends Train:
      override def maxSpeed: Int   = techType.maxSpeed
      override def capacity: Int   = wagon.capacity * length
      override def lengthSize: Int = length * wagon.lengthSize

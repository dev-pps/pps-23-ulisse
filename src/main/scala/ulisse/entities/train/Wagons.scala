package ulisse.entities.train

object Wagons:

  private object DefaultLength:
    val passenger: LengthMeter = 26
    val other: LengthMeter     = 18

  private type Capacity    = Int
  private type LengthMeter = Int
  private case class WagonImpl(use: UseType, capacity: Capacity)
      extends Wagon:
    override def lengthSize: LengthMeter = use.lengthSize

  enum UseType(val name: String, val lengthSize: LengthMeter):
    case Passenger extends UseType("Passenger", DefaultLength.passenger)
    case Other     extends UseType("Other", DefaultLength.other)

  /** A train wagon (a.k.a. train wagon, train car, railroad car) that is part of a train. */
  trait Wagon:
    /** @return
      *   [[UseType]] that defines the type of transport of the wagon
      */
    def use: UseType

    /** returns Transport capacity of wagon */
    def capacity: Capacity

    /** Size length (metric unit) */
    def lengthSize: LengthMeter

  /** Factory for [[train.model.Trains.Wagons.Wagon]] instances. */
  object Wagon:
    def apply(use: UseType, capacity: Capacity): Wagon =
      WagonImpl(use, capacity)

    /** @param capacity
      *   Transport capacity
      * @return
      *   Passenger transport wagon
      */
  def PassengerWagon(capacity: Capacity): Wagon =
    Wagon(UseType.Passenger, capacity)

  /** @param capacity
    *   Transport capacity
    * @return
    *   Other transport use wagon
    */
  def OtherWagon(capacity: Capacity): Wagon =
    Wagon(UseType.Other, capacity)

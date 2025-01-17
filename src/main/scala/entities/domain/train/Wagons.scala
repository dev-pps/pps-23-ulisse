package entities.domain.train

object Wagons:
  private type Capacity = Int
  private case class WagonImpl(use: UseType, capacity: Capacity)
      extends Wagon

  enum UseType(val name: String):
    case Passenger extends UseType("Passenger")
    case Other     extends UseType("Other")

  /** A train wagon (a.k.a. train wagon, train car, railroad car) that is part
    * of a train.
    */
  trait Wagon:
    /** @return
      *   [[UseType]] that defines the type of transport of the wagon
      */
    def use: UseType

    /** @return
      *   Transport capacity of wagon
      */
    def capacity: Capacity

  /** Factory for [[train.model.Trains.Wagons.Wagon]] instances.
    */
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

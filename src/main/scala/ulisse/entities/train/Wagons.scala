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

  /** Wagons use types with `name` and `lengthSize` (metric unit) of a wagon */
  enum UseType(val name: String, val lengthSize: LengthMeter):
    case Passenger extends UseType("Passenger", DefaultLength.passenger)
    case Other     extends UseType("Other", DefaultLength.other)

  /** A train wagon (a.k.a. train wagon, train car, railroad car) that is part of a train. */
  trait Wagon:
    /** Returns [[UseType]] that defines the type of transport of the wagon */
    def use: UseType

    /** Returns Transport capacity of wagon */
    def capacity: Capacity

    /** Size length (metric unit) */
    def lengthSize: LengthMeter

  /** Factory for [[train.model.Trains.Wagons.Wagon]] instances. */
  object Wagon:
    def apply(use: UseType, capacity: Capacity): Wagon =
      WagonImpl(use, capacity)

    /** Returns passenger transport wagon with some `capacity` */
  def PassengerWagon(capacity: Capacity): Wagon =
    Wagon(UseType.Passenger, capacity)

  /** Returns other transport use type wagon with specified `capacity`. */
  def OtherWagon(capacity: Capacity): Wagon =
    Wagon(UseType.Other, capacity)

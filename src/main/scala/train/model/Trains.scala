package train.model

import train.model.Trains.Carriages.UseType.{Other, Passenger}

object Trains:

  object Carriages:
    private type Capacity = Int
    private case class CarriageImpl(use: UseType, capacity: Capacity)
        extends Carriage

    enum UseType:
      case Passenger
      case Other

    /** A train carriage (a.k.a. train wagon, train car, railroad car) that is
      * part of a train.
      */
    trait Carriage:
      /** @return
        *   [[UseType]] that defines the type of transport of the carriage
        */
      def use: UseType

      /** @return
        *   Transport capacity of carriage
        */
      def capacity: Capacity

    /** Factory for [[train.model.Trains.Carriages.Carriage]] instances.
      */
    object Carriage:
      def apply(use: UseType, capacity: Capacity): Carriage =
        CarriageImpl(use, capacity)

      /** @param capacity
        *   Transport capacity
        * @return
        *   Passenger transport carriage
        */
      def PassengerCarriage(capacity: Capacity): Carriage =
        Carriage(Passenger, capacity)

      /** @param capacity
        *   Transport capacity
        * @return
        *   Other transport use carriage
        */
      def OtherCarriage(capacity: Capacity): Carriage =
        Carriage(Other, capacity)

  /** The technology used by the train that define max speed bound of train
    */
  trait TechnologyType:
    /** @return
      *   Name of technology
      */
    def name: String

    /** @return
      *   Max speed value
      */
    def maxSpeed: Int

  /** Factory for [[train.model.Trains.TechnologyType]] instances.
    */
  object TechnologyType:
    /** Creates new technology type,
      * @param name
      *   Name of technology
      * @param maxSpeed
      *   Max speed reachable by train with this technology
      * @return
      *   [[TechnologyType]] instance.
      */
    def apply(name: String, maxSpeed: Int): TechnologyType =
      Technology(name: String, maxSpeed: Int)

  private case class Technology(name: String, maxSpeed: Int)
      extends TechnologyType

  /** Train is characterized by [[TechnologyType]], [[Carriages.Carriage]] types
    * and total capacity and carriage amount.
    */
  trait Train:

    /** Train name */
    val name: String

    /** Technology used by train */
    val techType: TechnologyType

    /** Carriage which train is composed by */
    val carriage: Carriages.Carriage

    /** Amount of carriages */
    val carriageCount: Int

    /** @return
      *   max speed reachable by train
      */
    def maxSpeed: Int

    /** @return
      *   total transport capacity
      */
    def capacity: Int

  import Carriages.Carriage

  /** Factory for [[train.model.Trains.Train]] instances. */
  object Train:
    /** Creates train with a given name, technology type and carriages
      * information.
      * @param name
      *   Train name
      * @param techType
      *   [[TechnologyType]] technology used by train
      * @param carriage
      *   [[Carriages.Carriage]] carriage that compose train
      * @param carriageCount
      *   Amount of carriage
      * @return
      *   A new Train instance
      */
    def apply(
        name: String,
        techType: TechnologyType,
        carriage: Carriage,
        carriageCount: Int
    ): Train =
      TrainImpl(name, techType, carriage, carriageCount)

    /** @param train
      *   Train instance
      * @return
      *   An `Option` containing all characteristic of train.
      */
    def unapply(train: Train): Option[(String, TechnologyType, Carriage, Int)] =
      Some(train.name, train.techType, train.carriage, train.carriageCount)

  private class TrainImpl(
      val name: String,
      val techType: TechnologyType,
      val carriage: Carriage,
      val carriageCount: Int
  ) extends Train:
    def maxSpeed: Int = techType.maxSpeed
    def capacity: Int = carriage.capacity * carriageCount

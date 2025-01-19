package ulisse.entities.train

import ulisse.entities.train.Wagons.Wagon

object Trains:

  /** Train is characterized by [[Technology]], [[Wagons.Wagon]] types and total capacity and wagon amount.
    */
  trait Train:

    /** Train name */
    def name: String

    /** Technology used by train */
    def techType: Technology

    /** wagon which train is composed by */
    def wagon: Wagons.Wagon

    /** Amount of wagons */
    def wagonCount: Int

    /** @return
      *   max speed reachable by train
      */
    def maxSpeed: Int

    /** @return
      *   total transport capacity
      */
    def capacity: Int

  /** Factory for [[Trains.Train]] instances. */
  object Train:
    /** Creates train with a given name, technology type and wagons information.
      *
      * @param name
      *   Train name
      * @param techType
      *   [[Technology]] technology used by train
      * @param wagon
      *   [[Wagons.Wagon]] wagon that compose train
      * @param wagonCount
      *   Amount of wagon
      * @return
      *   A new Train instance
      */
    def apply(
        name: String,
        techType: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Train =
      TrainImpl(name, techType, wagon, wagonCount)

    /** @param train
      *   Train instance
      * @return
      *   An `Option` containing all characteristic of train.
      */
    def unapply(train: Train): Option[(String, Technology, Wagon, Int)] =
      Some(train.name, train.techType, train.wagon, train.wagonCount)

    private case class TrainImpl(
        name: String,
        techType: Technology,
        wagon: Wagon,
        wagonCount: Int
    ) extends Train:
      def maxSpeed: Int = techType.maxSpeed
      def capacity: Int = wagon.capacity * wagonCount

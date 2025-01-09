package train.model

object Trains:

  object Carriages:
    private type Capacity = Int
    private case class CarriageImpl(use: UseType, capacity: Capacity)
        extends Carriage

    enum UseType:
      case Passenger
      case Other

    trait Carriage:
      def use: UseType
      def capacity: Capacity

    object Carriage:
      def apply(use: UseType, capacity: Capacity): Carriage =
        CarriageImpl(use, capacity)

  trait TechnologyType:
    def name: String
    def maxSpeed: Int

  case class Technology(name: String, maxSpeed: Int) extends TechnologyType

  trait Train:
    val name: String
    val techType: TechnologyType
    val carriage: Carriages.Carriage
    val carriageCount: Int
    def maxSpeed: Int
    def capacity: Int

  import Carriages.Carriage
  object Train:
    def apply(
        name: String,
        techType: TechnologyType,
        carriage: Carriage,
        carriageCount: Int
    ): Train =
      TrainImpl(name, techType, carriage, carriageCount)

  private class TrainImpl(
      val name: String,
      val techType: TechnologyType,
      val carriage: Carriage,
      val carriageCount: Int
  ) extends Train:
    def maxSpeed: Int = techType.maxSpeed
    def capacity: Int = carriage.capacity * carriageCount

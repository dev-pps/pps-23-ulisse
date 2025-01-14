package infrastructures.ui.train

object TrainViewModel:

  case class TrainData(
      name: Option[String],
      technologyName: Option[String],
      wagonNameType: Option[String],
      wagonCount: Option[Int],
      technologyMaxSpeed: Option[Int],
      wagonCapacity: Option[Int]
  )

  case class TechType(name: String, maxSpeed: Int)
  case class Wagon(useName: String)

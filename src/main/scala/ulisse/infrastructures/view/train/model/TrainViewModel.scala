package ulisse.infrastructures.view.train.model

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
  case class WagonName(useName: String)

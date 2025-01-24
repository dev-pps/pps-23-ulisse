package ulisse.infrastructures.view.train.model

object TrainViewModel:

  case class TrainData(
      name: Option[String],
      technologyName: Option[String],
      technologyAcc: Option[Double],
      technologyDec: Option[Double],
      wagonNameType: Option[String],
      wagonCount: Option[Int],
      technologyMaxSpeed: Option[Int],
      wagonCapacity: Option[Int]
  )

  def emptyTrainData: TrainData =
    val emptyString      = Some("")
    val unsetIntValue    = Some(0)
    val unsetDoubleValue = Some(0.0)
    TrainData(
      emptyString,
      emptyString,
      unsetDoubleValue,
      unsetDoubleValue,
      emptyString,
      unsetIntValue,
      unsetIntValue,
      unsetIntValue
    )

  case class TechType(name: String, maxSpeed: Int, acc: Double, dec: Double)
  case class WagonName(useName: String)

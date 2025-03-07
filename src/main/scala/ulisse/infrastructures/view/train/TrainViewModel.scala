package ulisse.infrastructures.view.train

import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType

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

  extension (l: List[Train])
    def toTrainDatas: List[TrainData] =
      l.map(t =>
        TrainData(
          name = Some(t.name),
          technologyName = Some(t.techType.name),
          technologyMaxSpeed = Some(t.techType.maxSpeed),
          technologyAcc = Some(t.techType.acceleration),
          technologyDec = Some(t.techType.deceleration),
          wagonNameType = Some(t.wagon.use.name),
          wagonCapacity = Some(t.wagon.capacity),
          wagonCount = Some(t.length)
        )
      )

  extension (t: List[TrainTechnology])
    def toTechType: List[TechType] =
      t.map(tk => TechType(tk.name, tk.maxSpeed, tk.acceleration, tk.deceleration))

  extension (w: List[UseType])
    def toWagonNames: List[WagonName] = w.map(w => WagonName(w.name))

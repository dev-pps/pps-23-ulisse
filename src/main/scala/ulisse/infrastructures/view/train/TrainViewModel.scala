package ulisse.infrastructures.view.train

import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType

/** Contains all data model used by train views and utility method to
  * convert application entities into view model ones.
  */
object TrainViewModel:

  /** Train's core information. */
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

  /** Returns [[TrainData]] with all fields set to a default value:
    *
    * - name, technologyName, wagonNameType to `Some("")`
    *
    * - wagonCount, technologyMaxSpeed, wagonCapacity, technologyAcc, technologyDec to 0
    */
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

  /** Technology type infos. */
  case class TechType(name: String, maxSpeed: Int, acc: Double, dec: Double)

  /** Wagon type name */
  case class WagonTypeName(useName: String)

  extension (l: List[Train])
    /** Returns list of [[TrainData]] given a list `l` of [[Train]] */
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

  /** Returns list of [[TechType]] given a list `t` of [[TrainTechnology]] */
  extension (t: List[TrainTechnology])
    def toTechType: List[TechType] =
      t.map(tk => TechType(tk.name, tk.maxSpeed, tk.acceleration, tk.deceleration))

  /** Returns list of [[WagonTypeName]] given a list `w` of [[UseType]] */
  extension (w: List[UseType])
    def toWagonNames: List[WagonTypeName] = w.map(w => WagonTypeName(w.name))

package ulisse.entities.timetable

import ulisse.entities.simulation.environments.Environments.Environment
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.Station
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Times.{ClockTime, Time}
import ulisse.utils.CollectionUtils.swapWhenEq

/** Environment that contains DynamicTimetables for the Simulation */
trait DynamicTimetableEnvironment extends Environment[DynamicTimetable]:
  /** The dynamic timetables for a train */
  def dynamicTimetablesByTrain: Map[Train, Seq[DynamicTimetable]]

  /** Function to update the timetable */
  def updateTables(
      updateF: (DynamicTimetable, ClockTime) => Option[DynamicTimetable],
      routeInfo: DynamicTimetable => Option[(Station, Station)],
      agent: TrainAgent,
      time: Time
  ): Option[(DynamicTimetableEnvironment, (Station, Station))]

  /** Find the current timetable for a train */
  def findCurrentTimetableFor(agent: TrainAgent): Option[DynamicTimetable] =
    dynamicTimetablesByTrain.get(agent).flatMap(_.find(!_.completed))

  /** The Seq of all timetable in the environment */
  def timetables: Seq[DynamicTimetable] = dynamicTimetablesByTrain.values.flatten.toSeq

  /** The Seq of all environmentElements in the environment */
  override def environmentElements: Seq[DynamicTimetable] = timetables

/** Factory for [[DynamicTimetable]] instances */
object DynamicTimetableEnvironment:

  /** Create a new DynamicTimetableEnvironment from a configurationData */
  def apply(configurationData: ConfigurationData): DynamicTimetableEnvironment =
    DynamicTimetableEnvironmentImpl(configurationData.timetablesByTrain)

  private final case class DynamicTimetableEnvironmentImpl(dynamicTimetablesByTrain: Map[Train, Seq[DynamicTimetable]])
      extends DynamicTimetableEnvironment:
    override def updateTables(
        updateF: (DynamicTimetable, ClockTime) => Option[DynamicTimetable],
        routeInfo: DynamicTimetable => Option[(Station, Station)],
        agent: TrainAgent,
        time: Time
    ): Option[(DynamicTimetableEnvironment, (Station, Station))] =
      for
        currentTimetable <- findCurrentTimetableFor(agent)
        currentClockTime <- ClockTime(time.h, time.m).toOption
        updatedTimetable <- updateF(currentTimetable, currentClockTime)
        updatedTimetables =
          copy(dynamicTimetablesByTrain.view.mapValues(_.swapWhenEq(currentTimetable)(updatedTimetable)).toMap)
        info <- routeInfo(currentTimetable)
      yield (updatedTimetables, info)

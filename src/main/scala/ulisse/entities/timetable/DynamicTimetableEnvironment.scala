package ulisse.entities.timetable

import ulisse.entities.simulation.environments.Environments.Environment
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.station.Station
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Times.{ClockTime, Time}

trait DynamicTimetableEnvironment extends Environment[DynamicTimetable]:
  def dynamicTimetablesByTrain: Map[Train, Seq[DynamicTimetable]]
  def updateTables(
      updateF: (DynamicTimetable, ClockTime) => Option[DynamicTimetable],
      routeInfo: DynamicTimetable => Option[(Station, Station)],
      agent: TrainAgent,
      time: Time
  ): Option[(DynamicTimetableEnvironment, (Station, Station))]

  def findCurrentTimetableFor(agent: TrainAgent): Option[DynamicTimetable] =
    dynamicTimetablesByTrain.get(agent).flatMap(_.find(!_.completed))
  def timetables: Seq[DynamicTimetable] = dynamicTimetablesByTrain.values.flatten.toSeq
object DynamicTimetableEnvironment:
  def apply(configurationData: ConfigurationData): DynamicTimetableEnvironment =
    DynamicTimetableEnvironmentImpl(configurationData.timetablesByTrain)
  private final case class DynamicTimetableEnvironmentImpl(dynamicTimetablesByTrain: Map[Train, Seq[DynamicTimetable]])
      extends DynamicTimetableEnvironment:
    override def environmentElements: Seq[DynamicTimetable] = dynamicTimetablesByTrain.values.flatten.toSeq

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
        updatedTimetables = copy(dynamicTimetablesByTrain.view.mapValues(
          _.updateWhen(_ == currentTimetable)(_ => updatedTimetable)
        ).toMap)
        info <- routeInfo(currentTimetable)
      yield (updatedTimetables, info)

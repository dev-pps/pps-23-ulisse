package ulisse.entities.station

import ulisse.entities.simulation.environments.Environments.TrainAgentEnvironment
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationData
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.CollectionUtils.swapWhenEq

/** Environment that contains Stations for the Simulation */
trait StationEnvironment extends TrainAgentEnvironment[StationEnvironment, StationEnvironmentElement]:
  /** Try to put a train in a station */
  def putTrain(train: TrainAgent, station: Station): Option[StationEnvironment]

/** Factory for [[StationEnvironment]] instances */
object StationEnvironment:
  /** Create a new [[StationEnvironment]] from a configurationData */
  def apply(configurationData: ConfigurationData): StationEnvironment =
    StationEnvironmentImpl(configurationData.stations)

  private final case class StationEnvironmentImpl(environmentElements: Seq[StationEnvironmentElement])
      extends StationEnvironment:
    override protected def constructor(environmentElements: Seq[StationEnvironmentElement]): StationEnvironment =
      copy(environmentElements)

    override def putTrain(train: TrainAgent, station: Station): Option[StationEnvironment] =
      for
        station        <- environmentElements.find(_ == station)
        updatedStation <- station.putTrain(train.resetDistanceTravelled)
      yield constructor(environmentElements.swapWhenEq(station)(updatedStation))

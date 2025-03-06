package ulisse.entities.station

import ulisse.entities.simulation.environments.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}
import ulisse.utils.Times.Time

/** Defines a station for simulation. */
trait StationEnvironmentElement extends Station with TrainAgentEEWrapper[StationEnvironmentElement]:
  /** Type of the TrainAgentsContainer */
  type TAC = Platform

  /** Try to put train inside a platform given the desired direction */
  def putTrain(train: TrainAgent): Option[StationEnvironmentElement]

  /** Check if the station is available for a train to be put in */
  def isAvailable: Boolean = containers.exists(_.isAvailable)

/** Factory for [[StationEnvironmentElement]] instances. */
object StationEnvironmentElement:
  /** Represents the information of a station environment. */
  final case class StationEnvironmentInfo(
      stationEnvironmentElement: StationEnvironmentElement,
      cumulativeDelay: Time,
      averageDelay: Time
  )

  /** Creates a `StationEnvironmentElement` instance with sequential containers. */
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(
      station,
      TrainAgentsContainer.generateSequentialContainers(Platform.apply, station.numberOfPlatforms)
    )

  private final case class StationEnvironmentElementImpl(station: Station, containers: Seq[Platform])
      extends StationEnvironmentElement:
    export station.*

    override def putTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      (for
        firstAvailableContainer <- containers.find(_.isAvailable)
        updatedContainers       <- containers.updateWhenWithEffects(_ == firstAvailableContainer)(_.putTrain(train))
      yield constructor(updatedContainers)) when !contains(train)

    override protected def constructor(containers: Seq[Platform]): StationEnvironmentElement =
      copy(containers = containers)

package ulisse.entities.station

import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer}
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

trait StationEnvironmentElement extends Station with TrainAgentEEWrapper[StationEnvironmentElement]:
  type TAC = Platform
  def putTrain(train: TrainAgent): Option[StationEnvironmentElement]

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(
      station,
      TrainAgentsContainer.generateSequentialContainers(Platform.apply, station.numberOfTracks)
    )

  private final case class StationEnvironmentElementImpl(station: Station, containers: Seq[Platform])
      extends StationEnvironmentElement:
    export station.*

    def putTrain(train: TrainAgent): Option[StationEnvironmentElement] =
      (for
        firstAvailableContainer <- containers.find(_.isAvailable)
        updatedContainer        <- firstAvailableContainer.putTrain(train)
      yield copy(containers =
        containers.updateWhen(_ == firstAvailableContainer)(_ => updatedContainer)
      )) when !contains(train)

    override protected def buildNewEnvironmentElement(containers: Seq[Platform]): StationEnvironmentElement =
      copy(containers = containers)

package ulisse.entities.station

import ulisse.entities.simulation.EnvironmentElements.{TrainAgentEEWrapper, TrainAgentsContainer, TrainAgentsDirection}
import ulisse.entities.simulation.Environments
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.*
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

trait StationEnvironmentElement extends Station with TrainAgentEEWrapper[StationEnvironmentElement]

object StationEnvironmentElement:
  def apply(station: Station): StationEnvironmentElement =
    StationEnvironmentElementImpl(
      station,
      TrainAgentsContainer.generateSequentialContainers(Platform.apply, station.numberOfTracks)
    )

  private final case class StationEnvironmentElementImpl(station: Station, containers: Seq[TrainAgentsContainer])
      extends StationEnvironmentElement:
    export station.*

    def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[StationEnvironmentElement] =
      (for
        firstAvailableContainer <- containers.find(_.isAvailable)
        updatedContainer        <- firstAvailableContainer.putTrain(train, direction)
      yield copy(containers =
        containers.updateWhen(_ == firstAvailableContainer)(_ => updatedContainer)
      )) when !contains(train)

    override protected def buildNewEnvironmentElement(containers: Seq[TrainAgentsContainer])
        : StationEnvironmentElement = copy(containers = containers)

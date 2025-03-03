package ulisse.entities.simulation.environments

import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhenWithEffects
import ulisse.utils.OptionUtils.{given_Conversion_Option_Option, when}

/** Contains the EnvironmentElements objects used in the simulation */
object EnvironmentElements:

  /** Represents an element of the simulation environment */
  trait EnvironmentElement

  /** A data structure that contains trains */
  trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
    self: TAC =>
    def id: Int
    def trains: Seq[TrainAgent]
    def updateTrain(train: TrainAgent): Option[TAC]
    def removeTrain(train: TrainAgent): Option[TAC]
    def contains(train: TrainAgent): Boolean = trains.exists(train.matchId)
    def isEmpty: Boolean                     = trains.isEmpty

  /** Companion object for TrainAgentsContainer */
  object TrainAgentsContainer:
    /** Creates a List of `TrainAgentContainer[?]` instance. If the specified numberOfContainers is not positive an empty List is returned */
    def generateSequentialContainers[TAC <: TrainAgentsContainer[?]](
        constructor: Int => TAC,
        numberOfContainers: Int
    ): List[TAC] =
      val step: Int => Int = _ + 1
      List.tabulate(numberOfContainers)(i => constructor(step(i)))

  /** An Environment Element that contains multiple TrainAgentsContainer of the same type */
  trait TrainAgentEEWrapper[EE <: TrainAgentEEWrapper[EE]] extends EnvironmentElement:
    self: EE =>

    /** The type of the TrainAgentsContainer */
    type TAC <: TrainAgentsContainer[TAC]

    /** The list of TrainAgentsContainer */
    def containers: Seq[TAC]

    /** Try to update the train in the respective container if it's present */
    def updateTrain(train: TrainAgent): Option[EE] = updateEE(train, _.updateTrain(train), contains(train))

    /** Try to remove the train from the respective container if it's present */
    def removeTrain(train: TrainAgent): Option[EE] = updateEE(train, _.removeTrain(train), contains(train))

    /** Check if the provided train is present inside the element containers */
    def contains(train: TrainAgent): Boolean = containers.exists(_.contains(train))

    private def updateEE(
        trainAgent: TrainAgent,
        updateFunction: TAC => Option[TAC],
        condition: Boolean
    ): Option[EE] =
      containers.updateWhenWithEffects(_.contains(trainAgent))(updateFunction).map(tracks =>
        updateEEContainers(tracks)
      ) when condition

    /** Update the containers with the provided sequence */
    protected def updateEEContainers(containers: Seq[TAC]): EE

  /** Companion object for TrainAgentEEWrapper */
  object TrainAgentEEWrapper:

    /** Extension methods for train agent */
    extension [EE <: TrainAgentEEWrapper[EE]](train: TrainAgent)
      /** Try to remove the train from the provided environmentElement */
      def leave(ee: EE): Option[EE] =
        ee.removeTrain(train)

      /** Search the train in the provided list of environmentElement */
      def findIn(eeSeq: Seq[EE]): Option[EE] =
        eeSeq.find(_.contains(train))

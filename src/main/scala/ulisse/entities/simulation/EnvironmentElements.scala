package ulisse.entities.simulation

import ulisse.entities.station.{Platform, Platform2}
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.OptionUtils.when
import ulisse.utils.CollectionUtils.updateWhenWithEffects
import ulisse.utils.OptionUtils.given_Conversion_Option_Option

object EnvironmentElements:
  trait EnvironmentElement

  enum TrainAgentsDirection:
    case Forward, Backward
  trait TrainAgentsContainer:
    def id: Int
    def trains: Seq[TrainAgent]
    def isAvailable: Boolean =
      isEmpty || trains.forall(t => t.distanceTravelled - t.length >= minPermittedDistanceBetweenTrains)
    def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[TrainAgentsContainer]
    def updateTrain(train: TrainAgent): Option[TrainAgentsContainer]
    def removeTrain(train: TrainAgent): Option[TrainAgentsContainer]
    def contains(train: TrainAgent): Boolean = trains.exists(train.matchId)
    def isEmpty: Boolean                     = trains.isEmpty
    def currentDirection: Option[TrainAgentsDirection]
    def minPermittedDistanceBetweenTrains: Double

  object TrainAgentsContainer:
    /** Creates a List of `TrainAgentContainer[TAC]` instance. If the specified numberOfContainers is not positive an empty List is returned */
    def generateSequentialContainers[TAC <: TrainAgentsContainer](
        constructor: Int => TAC,
        numberOfContainers: Int
    ): List[TAC] =
      val step: Int => Int = _ + 1
      List.tabulate(numberOfContainers)(i => constructor(step(i)))

  trait TrainAgentEEWrapper[EE <: TrainAgentEEWrapper[EE]] extends EnvironmentElement:
    self: EE =>
    def containers: Seq[TrainAgentsContainer]
    def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[EE]
    def updateTrain(train: TrainAgent): Option[EE] = updaterTemplate(train, _.updateTrain(train), contains(train))
    def removeTrain(train: TrainAgent): Option[EE] = updaterTemplate(train, _.removeTrain(train), contains(train))
    def contains(train: TrainAgent): Boolean       = containers.exists(_.contains(train))

    private def updaterTemplate(
        trainAgent: TrainAgent,
        updateFunction: TrainAgentsContainer => Option[TrainAgentsContainer],
        condition: Boolean
    ): Option[EE] =
      containers.updateWhenWithEffects(_.contains(trainAgent))(updateFunction).map(tracks =>
        buildNewEnvironmentElement(tracks)
      ) when condition

    protected def buildNewEnvironmentElement(containers: Seq[TrainAgentsContainer]): EE

  object TrainAgentEEWrapper:
    extension [EE <: TrainAgentEEWrapper[EE]](train: TrainAgent)
      def leave(ee: EE): Option[EE] =
        ee.removeTrain(train)

      def findIn(eeSeq: Seq[EE]): Option[EE] =
        eeSeq.find(_.contains(train))

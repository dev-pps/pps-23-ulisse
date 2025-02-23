package ulisse.entities.simulation

import ulisse.entities.train.TrainAgent

object EnvironmentElements:
  trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
    self: TAC =>
    def isAvailable: Boolean
    def putTrain(train: TrainAgent): Option[TAC]
    def updateTrain(train: TrainAgent): Option[TAC]
    def removeTrain(train: TrainAgent): Option[TAC]
    def contains(train: TrainAgent): Boolean

  trait TrainAgentEEWrapper[EE <: TrainAgentEEWrapper[EE]]:
    self: EE =>
    type TAC <: TrainAgentsContainer[?]
    def putTrain(container: TAC, train: TrainAgent): Option[EE]
    def updateTrain(train: TrainAgent): Option[EE]
    def removeTrain(train: TrainAgent): Option[EE]
    def contains(train: TrainAgent): Boolean

  object TrainAgentEEWrapper:
    extension [EE <: TrainAgentEEWrapper[EE]](train: TrainAgent)
      def leave(ee: EE): Option[EE] =
        ee.removeTrain(train)

      def findIn(eeSeq: Seq[EE]): Option[EE] =
        eeSeq.find(_.contains(train))

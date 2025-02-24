package ulisse.entities.simulation

import ulisse.entities.train.TrainAgent

object EnvironmentElements:
  trait TrainAgentsContainer:
    def id: Int
    def trains: Seq[TrainAgent]
    def isAvailable: Boolean
    def putTrain(train: TrainAgent): Option[TrainAgentsContainer]
    def updateTrain(train: TrainAgent): Option[TrainAgentsContainer]
    def removeTrain(train: TrainAgent): Option[TrainAgentsContainer]
    def contains(train: TrainAgent): Boolean
    def isEmpty: Boolean = trains.isEmpty
    def minPermittedDistanceBetweenTrains: Double

  trait TrainAgentEEWrapper:
    def containers: Seq[TrainAgentsContainer]
    def containersIDs: Seq[Int]
    def trains: Seq[TrainAgent]
    def putTrain(container: TrainAgentsContainer, train: TrainAgent): Option[TrainAgentEEWrapper]
    def updateTrain(train: TrainAgent): Option[TrainAgentEEWrapper]
    def removeTrain(train: TrainAgent): Option[TrainAgentEEWrapper]
    def contains(train: TrainAgent): Boolean

  object TrainAgentEEWrapper:
    extension (train: TrainAgent)
      def leave(ee: TrainAgentEEWrapper): Option[TrainAgentEEWrapper] =
        ee.removeTrain(train)

      def findIn(eeSeq: Seq[TrainAgentEEWrapper]): Seq[TrainAgentEEWrapper] =
        eeSeq.filter(_.contains(train))

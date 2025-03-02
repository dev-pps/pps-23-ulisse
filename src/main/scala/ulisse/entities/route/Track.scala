package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.route.Track.TrainAgentsDirection
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.{validatePositive, validateRange, validateUniqueItems, validateUniqueItemsBy}

import scala.annotation.targetName

trait Track extends TrainAgentsContainer[Track]:
  def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[Track]
  def currentDirection: Option[TrainAgentsDirection]
  def minPermittedDistanceBetweenTrains: Double
  override def isAvailable: Boolean =
    trains.lastOption.forall(t => t.distanceTravelled - t.lengthSize >= minPermittedDistanceBetweenTrains)

object Track:
  enum TrainAgentsDirection:
    case Forward, Backward

  object TrainAgentsDirection:
    extension (direction: TrainAgentsDirection)
      def opposite: TrainAgentsDirection = direction match
        case TrainAgentsDirection.Forward  => TrainAgentsDirection.Backward
        case TrainAgentsDirection.Backward => TrainAgentsDirection.Forward

  def apply(trackNumber: Int)(using minPermittedDistanceBetweenTrains: Double): Track =
    TrackImpl(math.max(1, trackNumber), Seq(), None)
  def createCheckedTrack(
      trackNumber: Int
  )(using minPermittedDistanceBetweenTrains: Double): Either[NonEmptyChain[Track.Errors], Track] =
    validatePositive(trackNumber, Errors.InvalidTrackNumber).toValidatedNec.toEither.map(Track(_))

  enum Errors extends BaseError:
    case InvalidTrackNumber

  private final case class TrackImpl(id: Int, trains: Seq[TrainAgent], currentDirection: Option[TrainAgentsDirection])(
      using val minPermittedDistanceBetweenTrains: Double
  ) extends Track:

    private def securityDistanceIsMaintained(trains: Seq[TrainAgent]): Boolean =
      trains.zip(trains.drop(1)).forall((train1, train2) =>
        train1.distanceTravelled - train1.lengthSize - train2.distanceTravelled >= minPermittedDistanceBetweenTrains
      )

    override def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[Track] =
      (
        currentDirection,
        copy(trains = trains :+ train) when isAvailable && !contains(train) && train.distanceTravelled == 0
      ) match
        case (Some(`direction`), Some(updatedTrack)) => Some(updatedTrack)
        case (None, Some(updatedTrack))              => Some(updatedTrack.copy(currentDirection = Some(direction)))
        case (_, _)                                  => None

    override def updateTrain(train: TrainAgent): Option[Track] =
      val updatedTrains = trains.updateWhen(train.matchId)(_ => train)
      copy(trains = updatedTrains) when contains(train) && securityDistanceIsMaintained(updatedTrains)

    override def removeTrain(train: TrainAgent): Option[Track] =
      (copy(trains = trains.drop(1)) when trains.headOption.exists(train.matchId)) match
        case Some(updatedTrack) if updatedTrack.isEmpty => Some(updatedTrack.copy(currentDirection = None))
        case Some(updatedTrack)                         => Some(updatedTrack)
        case _                                          => None

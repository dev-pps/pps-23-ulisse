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

/** A track is a TrainAgentsContainer that can hold multiple train at a times */
trait Track extends TrainAgentsContainer[Track]:
  /** Try to put train inside a track given the desired direction */
  def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[Track]

  /** Return the current direction of the track */
  def currentDirection: Option[TrainAgentsDirection]

  /** Return the minimum distance allowed between trains in the track */
  def minPermittedDistanceBetweenTrains: Double

  /** Check if the track is available for a train to be put in */
  def isAvailable(direction: TrainAgentsDirection): Boolean =
    trains.lastOption.forall(t =>
      t.distanceTravelled - t.lengthSize >= minPermittedDistanceBetweenTrains
    ) && currentDirection.getOrElse(direction) == direction

/** Check if the track contains the train */
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
        copy(trains = trains :+ train) when isAvailable(direction) && !contains(train) && train.distanceTravelled == 0
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

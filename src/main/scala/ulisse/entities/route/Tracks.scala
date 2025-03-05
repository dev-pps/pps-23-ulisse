package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.{validatePositive, validateRange, validateUniqueItems, validateUniqueItemsBy}

object Tracks:

  /** The possible directions in witch a Track can be used */
  enum TrackDirection:
    case Forward, Backward

  /** TrackDirection utility object */
  object TrackDirection:
    /** Extension methods for TrackDirection */
    extension (direction: TrackDirection)
      /** Return the opposite direction */
      def opposite: TrackDirection = direction match
        case TrackDirection.Forward  => TrackDirection.Backward
        case TrackDirection.Backward => TrackDirection.Forward

  /** Errors for Track */
  enum Errors extends BaseError:
    case InvalidTrackId

  /** Defines a rail in a Route. */
  trait Track extends TrainAgentsContainer[Track]:
    /** Try to put train inside the track given the desired direction */
    def putTrain(train: TrainAgent, direction: TrackDirection): Option[Track]

    /** Return the current direction of the track */
    def currentDirection: Option[TrackDirection]

    /** Return the minimum distance allowed between trains in the track */
    def minPermittedDistanceBetweenTrains: Double

    /** Check if the track is available for a train to be put in */
    def isAvailable(direction: TrackDirection): Boolean =
      trains.lastOption.forall(t =>
        t.distanceTravelled - t.lengthSize >= minPermittedDistanceBetweenTrains
      ) && currentDirection.getOrElse(direction) == direction

  /** Factory for [[Track]] instances. */
  object Track:

    /** Creates a `Track` instance. If track id is not positive it is set to 1 */
    def apply(id: Int)(using minPermittedDistanceBetweenTrains: Double): Track =
      TrackImpl(math.max(1, id), Seq(), None)

    /** Creates a `Track` instance with validation. If track id is not positive an error is returned */
    def createCheckedTrack(
        id: Int
    )(using minPermittedDistanceBetweenTrains: Double): Either[NonEmptyChain[Tracks.Errors], Track] =
      validatePositive(id, Errors.InvalidTrackId).toValidatedNec.toEither.map(Track(_))

    private final case class TrackImpl(id: Int, trains: Seq[TrainAgent], currentDirection: Option[TrackDirection])(
        using val minPermittedDistanceBetweenTrains: Double
    ) extends Track:

      private def securityDistanceIsMaintained(trains: Seq[TrainAgent]): Boolean =
        trains.zip(trains.drop(1)).forall((train1, train2) =>
          train1.distanceTravelled - train1.lengthSize - train2.distanceTravelled >= minPermittedDistanceBetweenTrains
        )

      override def putTrain(train: TrainAgent, direction: TrackDirection): Option[Track] =
        (
          currentDirection,
          copy(trains = trains :+ train) when isAvailable(direction) && !contains(
            train
          ) && train.distanceTravelled == 0
        ) match
          case (Some(`direction`), Some(updatedTrack)) => Some(updatedTrack)
          case (None, Some(updatedTrack))              => Some(updatedTrack.copy(currentDirection = Some(direction)))
          case _                                       => None

      override def updateTrain(train: TrainAgent): Option[Track] =
        val updatedTrains = trains.updateWhen(_ == train)(_ => train)
        copy(trains = updatedTrains) when contains(train) && securityDistanceIsMaintained(updatedTrains)

      override def removeTrain(train: TrainAgent): Option[Track] =
        (copy(trains = trains.drop(1)) when trains.headOption.contains(train)) match
          case Some(updatedTrack) if updatedTrack.isEmpty => Some(updatedTrack.copy(currentDirection = None))
          case other                                      => other

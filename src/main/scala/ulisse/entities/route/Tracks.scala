package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.swapWhenEq
import ulisse.utils.Errors.BaseError
import ulisse.utils.OptionUtils.when
import ulisse.utils.ValidationUtils.validatePositive

object Tracks:
  /** The possible directions in witch a Track can be used. */
  enum TrackDirection:
    case Forward, Backward

  /** TrackDirection utility object. */
  object TrackDirection:
    extension (direction: TrackDirection)
      /** Return the opposite direction. */
      def opposite: TrackDirection = direction match
        case TrackDirection.Forward  => TrackDirection.Backward
        case TrackDirection.Backward => TrackDirection.Forward

  /** Defines a rail in a Route. */
  trait Track extends TrainAgentsContainer[Track]:
    /** Try to put train inside the track given the desired direction. */
    def putTrain(train: TrainAgent, direction: TrackDirection): Option[Track]

    /** Return the current direction of the track. */
    def currentDirection: Option[TrackDirection]

    /** Return the minimum distance allowed between trains in the track. */
    def minPermittedDistanceBetweenTrains: Double

    /** Check if the track is available for a train to be put in. */
    def isAvailable(direction: TrackDirection): Boolean =
      trains.lastOption.forall(t =>
        t.motionData.distanceTravelled - t.lengthSize >= minPermittedDistanceBetweenTrains
      ) && currentDirection.getOrElse(direction) == direction

  /** Factory for [[Track]] instances. */
  object Track:
    /** Minimum track id. */
    val minTrackId: Int = 1

    /** Creates a `Track` instance. If platform id is lower than minTrackId it's set to that value. */
    def apply(id: Int)(using minPermittedDistanceBetweenTrains: Double): Track =
      TrackImpl(math.max(minTrackId, id), Seq(), None)

    /** Creates a `Track` instance with validation. If track id is lower than minTrackId an error is returned. */
    def createCheckedTrack(
        id: Int
    )(using minPermittedDistanceBetweenTrains: Double): Either[NonEmptyChain[Track.Error], Track] =
      validatePositive(id, Error.InvalidTrackId).toValidatedNec.toEither.map(Track(_))

    /** Represents errors that can occur during `Tracks` creation. */
    enum Error extends BaseError:
      case InvalidTrackId

    private final case class TrackImpl(id: Int, trains: Seq[TrainAgent], currentDirection: Option[TrackDirection])(
        using val minPermittedDistanceBetweenTrains: Double
    ) extends Track:

      private def securityDistanceIsMaintained(trains: Seq[TrainAgent]): Boolean =
        trains.zip(trains.drop(1)).forall((train1, train2) =>
          train1.motionData.distanceTravelled - train1.lengthSize - train2.motionData.distanceTravelled >= minPermittedDistanceBetweenTrains
        )

      override def putTrain(train: TrainAgent, direction: TrackDirection): Option[Track] =
        (
          currentDirection,
          copy(trains = trains :+ train) when isAvailable(direction) && !contains(
            train
          ) && train.motionData.distanceTravelled == 0
        ) match
          case (Some(`direction`), Some(updatedTrack)) => Some(updatedTrack)
          case (None, Some(updatedTrack))              => Some(updatedTrack.copy(currentDirection = Some(direction)))
          case _                                       => None

      override def updateTrain(train: TrainAgent): Option[Track] =
        val updatedTrains = trains.swapWhenEq(train)(train)
        copy(trains = updatedTrains) when contains(train) && securityDistanceIsMaintained(updatedTrains)

      override def removeTrain(train: TrainAgent): Option[Track] =
        (copy(trains = trains.drop(1)) when trains.headOption.contains(train)) match
          case Some(updatedTrack) if updatedTrack.isEmpty => Some(updatedTrack.copy(currentDirection = None))
          case other                                      => other

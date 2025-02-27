package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.route.Track.TrainAgentsDirection
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validatePositive, validateRange, validateUniqueItems}

import scala.annotation.targetName

trait Track extends TrainAgentsContainer[Track]:
  def currentDirection: Option[TrainAgentsDirection]
  def putTrain(train: TrainAgent, direction: TrainAgentsDirection): Option[Track] =
    :+(train, direction).toOption
  @targetName("appendedTrain")
  def :+(train: TrainAgent, direction: TrainAgentsDirection): Either[NonEmptyChain[Track.Errors], Track]
  def minPermittedDistanceBetweenTrains: Double
  override def isAvailable: Boolean = isEmpty || trains.forall(t => t.distanceTravelled - t.lengthSize >= minPermittedDistanceBetweenTrains)

object Track:
  enum TrainAgentsDirection:
    case Forward, Backward
  // TODO evaluate if leave there minPermittedDistanceBetweenTrains or pass as parameter for isAvailable
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
    @targetName("appendedTrain")
    override def :+(train: TrainAgent, direction: TrainAgentsDirection): Either[NonEmptyChain[Track.Errors], Track] =
      // TODO evaluate if could be nice to introduce a control to check if the train is entering the track or is already moved
//      for
//        _ <- validateUniqueItems(trains :+ train, _.matchId, Errors.DuplicateTrains).toValidatedNec
//      (
//        validateRange(train.distanceTravelled, 0.0, 0.0, Errors.TrainAlreadyMoved).toValidatedNec,
//        Track.createCheckedTrack(id, trains :+ train*).toValidated
//      ).mapN((_, t) => t).toEither
        Right(copy(trains = trains :+ train))

    override def updateTrain(train: TrainAgent): Option[Track] = // updateWhen(train.matchId)(_ => train).toOption
//      if trains.exists(train.matchId) then
//        Track.createCheckedTrack(id, trains.updateWhen(train.matchId)(_ => train)*).toOption
//      else None
      None  

    override def removeTrain(train: TrainAgent): Option[Track] =
      trains.find(train.matchId).map(_ => copy(trains = trains.filterNot(train.matchId)))


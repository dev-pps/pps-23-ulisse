package ulisse.entities.route

import cats.data.NonEmptyChain
import cats.syntax.all.*
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsContainer
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateRange, validateUniqueItems}

import scala.annotation.targetName

trait Track extends TrainAgentsContainer:
  val trains: Seq[TrainAgent]
  override def putTrain(train: TrainAgent): Option[Track] = :+(train).toOption
  @targetName("appendedTrain")
  def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track]

object Track:
  // TODO evaluate if leave there minPermittedDistanceBetweenTrains or pass as parameter for isAvailable
  def apply(id: Int, trains: TrainAgent*)(using minPermittedDistanceBetweenTrains: Double): Track =
    TrackImpl(math.max(1, id), trains.distinctBy(_.name))
  def createCheckedTrack(
      id: Int,
      trains: TrainAgent*
  )(using minPermittedDistanceBetweenTrains: Double): Either[NonEmptyChain[Track.Errors], Track] =
    validateUniqueItems(trains.map(_.name), Errors.DuplicateTrains)
      .toValidatedNec
      .map(_ => Track(id, trains*))
      .toEither

  private final case class TrackImpl(id: Int, trains: Seq[TrainAgent])(using
      val minPermittedDistanceBetweenTrains: Double
  ) extends Track:
    @targetName("appendedTrain")
    override def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track] =
      // TODO evaluate if is needed and in case improve validate function
      (
        validateRange(train.distanceTravelled, 0.0, 0.0, Errors.TrainAlreadyMoved).toValidatedNec,
        Track.createCheckedTrack(id, trains :+ train*).toValidated
      ).mapN((_, t) => t).toEither

    override def updateTrain(train: TrainAgent): Option[Track] = // updateWhen(train.matchId)(_ => train).toOption
      if trains.exists(train.matchId) then
        Track.createCheckedTrack(id, trains.updateWhen(train.matchId)(_ => train)*).toOption
      else None

    override def removeTrain(train: TrainAgent): Option[Track] =
      trains.find(train.matchId).map(_ => copy(trains = trains.filterNot(train.matchId)))

  enum Errors extends BaseError:
    case DuplicateTrains, TrainAlreadyMoved

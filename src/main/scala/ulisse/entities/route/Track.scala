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
  val container: Seq[TrainAgent]
  override def trains: Seq[TrainAgent] = container
  override def isAvailable: Boolean =
    container.forall(t => t.distanceTravelled - t.length >= minPermittedDistanceBetweenTrains)
  override def putTrain(train: TrainAgent): Option[Track] = :+(train).toOption
  @targetName("appendedTrain")
  def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  override def contains(train: TrainAgent): Boolean = container.exists(train.matchId)

object Track:
  // TODO evaluate if leave there minPermittedDistanceBetweenTrains or pass as parameter for isAvailable
  def apply(trains: TrainAgent*)(using minPermittedDistanceBetweenTrains: Double): Track =
    TrackImpl(trains.distinctBy(_.name))
  def createCheckedTrack(
      trains: TrainAgent*
  )(using minPermittedDistanceBetweenTrains: Double): Either[NonEmptyChain[Track.Errors], Track] =
    validateUniqueItems(trains.map(_.name), Errors.DuplicateTrains)
      .toValidatedNec
      .map(_ => Track(trains*))
      .toEither

  private final case class TrackImpl(container: Seq[TrainAgent])(using val minPermittedDistanceBetweenTrains: Double)
      extends Track:
    override def id: Int = 1

    @targetName("appendedTrain")
    override def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track] =
      // TODO evaluate if is needed and in case improve validate function
      (
        validateRange(train.distanceTravelled, 0.0, 0.0, Errors.TrainAlreadyMoved).toValidatedNec,
        Track.createCheckedTrack(container :+ train*).toValidated
      ).mapN((_, t) => t).toEither

    override def updateTrain(train: TrainAgent): Option[Track] = // updateWhen(train.matchId)(_ => train).toOption
      if container.exists(train.matchId) then
        Track.createCheckedTrack(container.updateWhen(train.matchId)(_ => train)*).toOption
      else None

    override def removeTrain(train: TrainAgent): Option[Track] =
      container.find(train.matchId).map(_ => copy(container = container.filterNot(train.matchId)))

  enum Errors extends BaseError:
    case DuplicateTrains, TrainAlreadyMoved

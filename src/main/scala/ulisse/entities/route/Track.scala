package ulisse.entities.route

import cats.data.{NonEmptyChain, ValidatedNec}
import cats.syntax.all.*
import ulisse.entities.simulation.Environments.TrainAgentsContainer
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateRange, validateUniqueItems}

import scala.annotation.targetName
trait Track extends TrainAgentsContainer[Track]:
  // TODO evaluate if is needed a numberid
  val trains: Seq[TrainAgent]
  def minPermittedDistanceBetweenTrains: Double
  override def isAvailable: Boolean =
    trains.forall(t => t.distanceTravelled - t.length >= minPermittedDistanceBetweenTrains)
  override def putTrain(train: TrainAgent): Option[Track]    = :+(train).toOption
  override def updateTrain(train: TrainAgent): Option[Track] = updateWhen(_.name == train.name)(_ => train).toOption
  @targetName("appendedTrain")
  def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  export trains.{contains, isEmpty}
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

  extension (train: TrainAgent)
    def existInTrack(track: Track): Boolean = track.trains.exists(train.matchId)

  private final case class TrackImpl(trains: Seq[TrainAgent])(using val minPermittedDistanceBetweenTrains: Double)
      extends Track:
    @targetName("appendedTrain")
    override def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track] =
      // TODO evaluate if is needed
      val moved =
        if train.distanceTravelled > 0 then
          Left(Errors.TrainAlreadyMoved)
        else
          Right(())

      (
        moved.toValidatedNec,
        Track.createCheckedTrack(trains :+ train*).toValidated
      ).mapN((_, t) => t).toEither

    override def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent)
        : Either[NonEmptyChain[Track.Errors], Track] =
      Track.createCheckedTrack(trains.updateWhen(p)(f)*)

    override def removeTrain(train: TrainAgent): Option[Track] =
      trains.find(train.matchId).map(_ => copy(trains = trains.filterNot(train.matchId)))

  enum Errors extends BaseError:
    case DuplicateTrains, TrainAlreadyMoved

package ulisse.entities.route

import cats.data.{NonEmptyChain, ValidatedNec}
import cats.syntax.all.*
import ulisse.entities.simulation.Environments.EnvironmentElementContainer
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.train.TrainAgent
import ulisse.utils.CollectionUtils.updateWhen
import ulisse.utils.Errors.BaseError
import ulisse.utils.ValidationUtils.{validateRange, validateUniqueItems}

import scala.annotation.targetName
trait Track extends EnvironmentElementContainer[Track]:
  val trains: Seq[TrainAgent]
  def minPermittedDistanceBetweenTrains: Double
  def isAvailable: Boolean = trains.forall(t => t.distanceTravelled - t.length >= minPermittedDistanceBetweenTrains)
  @targetName("appendedTrain")
  def :+(train: TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def updateWhen(p: TrainAgent => Boolean)(f: TrainAgent => TrainAgent): Either[NonEmptyChain[Track.Errors], Track]
  def filterNot(p: TrainAgent => Boolean): Track
  export trains.{contains, exists, isEmpty}

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
    override def filterNot(p: TrainAgent => Boolean): Track = copy(trains = trains.filterNot(p))

    override def putAgent(agent: SimulationAgent): Option[Track] = agent match
      case train: TrainAgent => :+(train).toOption
      case _                 => None

    override def updateAgent(agent: SimulationAgent): Option[Track] = agent match
      case train: TrainAgent =>
        updateWhen(_.name == train.name)(_ => train).toOption
      case _ => None

    override def removeAgent(agent: SimulationAgent): Option[Track] = agent match
      case train: TrainAgent =>
        filterNot(_.name == train.name).some
      case _ => None

  enum Errors extends BaseError:
    case DuplicateTrains, TrainAlreadyMoved

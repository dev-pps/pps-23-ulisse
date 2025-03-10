package ulisse.applications.useCases

import ulisse.applications.events.TrainEventQueue
import ulisse.applications.managers.TechnologyManagers.TechErrors.TechnologyNotExists
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TrainPorts.Input
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Errors
import ulisse.utils.Errors.BaseError

import scala.concurrent.{Future, Promise}

/** Implements of trait [[Input]].
  *
  * @see For more details [[Input]]
  */
final case class TrainService(eventQueue: TrainEventQueue) extends Input:

  override def removeTrain(name: String): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]

    eventQueue.addDeleteTrainEvent((trainManager, timetableManager) =>
      val train = trainManager.findTrain(name)
      trainManager.removeTrain(name) match
        case Left(err) =>
          promise.success(Left(err))
          (trainManager, timetableManager)
        case Right(updatedTrainManager) =>
          promise.success(Right(updatedTrainManager.trains))
          val updatedTimetableManager = train.flatMap(timetableManager.trainDeleted).getOrElse(timetableManager)
          (updatedTrainManager, updatedTimetableManager)
    )
    promise.future

  override def createTrain(
      name: String,
      technologyName: String,
      wagonUseTypeName: String,
      wagonCapacity: Int,
      wagonCount: Int
  ): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    eventQueue.addCreateTrainEvent((trainManager, technologyManager) =>
      val tech = technologyManager.technologiesList.find(_.name.contentEquals(technologyName))
      val managerResult = tech match
        case Some(tk) => trainManager.createTrain(name, tk, wagonUseTypeName, wagonCapacity, wagonCount)
        case None     => Left(TechnologyNotExists(technologyName))
      unpackResult(managerResult)(promise, trainManager)
    )
    promise.future

  override def updateTrain(name: String)(
      technologyName: String,
      wagonUseTypeName: String,
      wagonCapacity: Int,
      wagonCount: Int
  ): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    eventQueue.addUpdateTrainEvent: (trainManager, technologyManager, timetableManager) =>
      val train = trainManager.findTrain(name)
      val trainManagerRes =
        for
          technology     <- technologyManager.getBy(technologyName)
          updatedManager <- trainManager.updateTrain(name)(technology, wagonUseTypeName, wagonCapacity, wagonCount)
        yield updatedManager

      trainManagerRes match
        case Left(error) =>
          promise.success(Left(error))
          (trainManager, timetableManager)
        case Right(updatedTrainManager) =>
          promise.success(Right(updatedTrainManager.trains))
          val updatedTimetableManager = train.flatMap(timetableManager.trainUpdated).getOrElse(timetableManager)
          (updatedTrainManager, updatedTimetableManager)

    promise.future

  override def trains: Future[List[Train]] =
    val promise = Promise[List[Train]]
    eventQueue.addReadTrainEvent((traintManager, technologyManager) => promise.success(traintManager.trains))
    promise.future

  override def technologies: Future[List[TrainTechnology]] =
    itemsRequest[List[TrainTechnology]]((trainManager, technologyManager) => technologyManager.technologiesList)(
      eventQueue
    )

  override def wagonTypes: Future[List[UseType]] =
    itemsRequest[List[UseType]]((trainManager, technologyManager) => trainManager.wagonTypes)(eventQueue)

  private def itemsRequest[T](items: (TrainManager, TechnologyManager[TrainTechnology]) => T)(queue: TrainEventQueue) =
    val promise = Promise[T]
    queue.addReadTrainEvent((trainManager, technologyManager) =>
      promise.success(items(trainManager, technologyManager))
    )
    promise.future

  private def unpackResult(managerResult: Either[BaseError, TrainManager])(
      promise: Promise[Either[BaseError, List[Train]]],
      currentManager: TrainManager
  ) =
    managerResult match
      case Left(error) => promise.success(Left(error)); currentManager
      case Right(newManager) =>
        promise.success(Right(newManager.trains))
        newManager

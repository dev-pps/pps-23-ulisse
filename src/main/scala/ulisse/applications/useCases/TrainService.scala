package ulisse.applications.useCases

import ulisse.applications.event.TrainEventQueue
import ulisse.applications.managers.TechnologyManagers.TechErrors.TechnologyNotExists
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TrainPorts.Input
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Errors
import ulisse.utils.Errors.BaseError

import scala.concurrent.{Future, Promise}

final case class TrainService(eventQueue: TrainEventQueue) extends Input:

  def removeTrain(name: String): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]

    eventQueue.addDeleteTrainEvent((trainManager, timetableManager) =>
      val managerResult = trainManager.removeTrain(name)
      (unpackResult(managerResult)(promise, trainManager), timetableManager)
    )
    promise.future

  def addTrain(
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

  def updateTrain(name: String)(
      technologyName: String,
      wagonUseTypeName: String,
      wagonCapacity: Int,
      wagonCount: Int
  ): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]

    eventQueue.addUpdateTrainEvent((trainManager, technologyManager) =>
      val res =
        for
          t <-
            technologyManager.technologiesList.find(_.name.contentEquals(technologyName)).toRight(TechnologyNotExists(
              technologyName
            ))
          r <- trainManager.updateTrain(name)(t, wagonUseTypeName, wagonCapacity, wagonCount)
        yield r
      unpackResult(res)(promise, trainManager)
    )
    promise.future

  def trains: Future[List[Train]] =
    val promise = Promise[List[Train]]
    eventQueue.addReadTrainEvent((traintManager, technologyManager) => promise.success(traintManager.trains))
    promise.future

  def technologies: Future[List[TrainTechnology]] =
    itemsRequest[List[TrainTechnology]]((trainManager, technologyManager) => technologyManager.technologiesList)(
      eventQueue
    )

  def wagonTypes: Future[List[UseType]] =
    itemsRequest[List[UseType]]((trainManager, technologyManager) => trainManager.wagonTypes)(eventQueue)

  private def itemsRequest[T](items: (TrainManager, TechnologyManager[TrainTechnology]) => T)(queue: TrainEventQueue) =
    val promise = Promise[T]
    queue.addReadTrainEvent((trainManager, technologyManager) =>
      promise.success(items(trainManager, technologyManager))
    )
    promise.future

  private def unpackResult(managerResult: Either[BaseError, TrainManager])(
      promise: Promise[Either[BaseError, List[Train]]],
      state: TrainManager
  ) =
    managerResult match
      case Left(error) => promise.success(Left(error)); state
      case Right(newManager) =>
        promise.success(Right(newManager.trains))
        newManager

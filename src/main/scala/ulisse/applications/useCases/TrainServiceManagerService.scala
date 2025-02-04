package ulisse.applications.useCases

import ulisse.applications.managers.TechnologyManagers.TechErrors.TechnologyNotExists
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.ports.TrainPorts.Input
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Errors
import ulisse.utils.Errors.{BaseError, ErrorNotExist}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

type AppState = (TrainManager, TechnologyManager[TrainTechnology])

final case class TrainServiceManagerService(stateEventQueue: LinkedBlockingQueue[AppState => AppState])
    extends Input:

  def removeTrain(name: String): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    stateEventQueue.offer((state: AppState) => {
      val managerResult = state._1.removeTrain(name)
      unpackResult(managerResult)(promise, state)
    })
    promise.future

  def addTrain(
      name: String,
      technologyName: String,
      wagonUseTypeName: String,
      wagonCapacity: Int,
      wagonCount: Int
  ): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    stateEventQueue.offer((state: AppState) => {
      val (trainManager, techManager) = state
      val tech                        = techManager.technologiesList.find(_.name.contentEquals(technologyName))
      val managerResult = tech match
        case Some(tk) => trainManager.createTrain(name, tk, wagonUseTypeName, wagonCapacity, wagonCount)
        case None     => Left(TechnologyNotExists(technologyName))
      unpackResult(managerResult)(promise, state)
    })
    promise.future

  def updateTrain(name: String)(
      technologyName: String,
      wagonUseTypeName: String,
      wagonCapacity: Int,
      wagonCount: Int
  ): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    stateEventQueue.offer((state: AppState) => {
      val (trainManager, techManager) = state
      val res =
        for
          t <- techManager.technologiesList.find(_.name.contentEquals(technologyName)).toRight(TechnologyNotExists(
            technologyName
          ))
          r <- trainManager.updateTrain(name)(t, wagonUseTypeName, wagonCapacity, wagonCount)
        yield r
      unpackResult(res)(promise, state)
    })
    promise.future

  def trains: Future[List[Train]] =
    val promise = Promise[List[Train]]
    stateEventQueue.offer((state: AppState) => {
      promise.success(state._1.trains)
      state
    })
    promise.future

  def technologies: Future[List[TrainTechnology]] =
    itemsRequest[List[TrainTechnology]](state => state._2.technologiesList)(stateEventQueue)

  def wagonTypes: Future[List[UseType]] =
    itemsRequest[List[UseType]](state => state._1.wagonTypes)(stateEventQueue)

  private def itemsRequest[T](items: AppState => T)(queue: LinkedBlockingQueue[AppState => AppState]) =
    val promise = Promise[T]
    queue.offer((state: AppState) => {
      promise.success(items(state))
      state
    })
    promise.future

  private def unpackResult(managerResult: Either[BaseError, TrainManager])(
      promise: Promise[Either[BaseError, List[Train]]],
      state: AppState
  ) = {
    managerResult match
      case Left(e) =>
        promise.success(Left(e))
        state
      case Right(newManager) =>
        promise.success(Right(newManager.trains))
        state.copy(_1 = newManager)
  }

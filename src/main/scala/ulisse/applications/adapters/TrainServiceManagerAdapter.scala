package ulisse.applications.adapters

import ulisse.applications.ports.TrainPorts.Input
import ulisse.applications.useCases.train.TechnologyManagers.TechErrors.TechnologyNotExists
import ulisse.applications.useCases.train.TechnologyManagers.TechnologyManager
import ulisse.applications.useCases.train.TrainManagers.TrainManager
import ulisse.entities.train.Technology
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

type AppState = (TrainManager, TechnologyManager)

final case class TrainServiceManagerAdapter(stateEventQueue: LinkedBlockingQueue[AppState => AppState])
    extends Input:

  def removeTrain(name: String): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    stateEventQueue.offer((state: AppState) => {
      println("remove train")
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
      println("add train")
      val (trainManager, techManager) = state
      val tech                        = techManager.technologiesList.find(_.name.contentEquals(technologyName))
      val managerResult = tech match
        case Some(tk) => trainManager.createTrain(name, tk, wagonUseTypeName, wagonCapacity, wagonCount)
        case None     => Left(TechnologyNotExists(technologyName))
      unpackResult(managerResult)(promise, state)
    })
    promise.future

  def updateTrain(name: String)(
      technology: Technology,
      wagon: Wagon,
      wagonCount: Int
  ): Future[Either[BaseError, List[Train]]] =
    val promise = Promise[Either[BaseError, List[Train]]]
    stateEventQueue.offer((state: AppState) => {
      println("update train")
      val managerResult = state._1.updateTrain(name)(technology, wagon, wagonCount)
      unpackResult(managerResult)(promise, state)
    })
    promise.future

  def trains: Future[List[Train]] =
    val promise = Promise[List[Train]]
    stateEventQueue.offer((state: AppState) => {
      println("get trains")
      promise.success(state._1.trains)
      state
    })
    promise.future

  def technologies: Future[List[Technology]] =
    itemsRequest[List[Technology]](state => state._2.technologiesList)(stateEventQueue)

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

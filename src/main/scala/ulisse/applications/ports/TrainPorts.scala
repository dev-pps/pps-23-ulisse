package ulisse.applications.ports

import cats.data.State
import ulisse.applications.useCases.train.TechnologyManagers.TechErrors.TechnologyNotExists
import ulisse.applications.useCases.train.TechnologyManagers.TechnologyManager
import ulisse.applications.useCases.train.TrainManagers.TrainManager
import ulisse.entities.train.Technology
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Wagons.{UseType, Wagon}
import ulisse.utils.Errors.BaseError

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object TrainPorts:

  /** It represents all possible requests that can be done from external components to application for what concern
    * about `Trains`, trains `Technology` and wagon `UseType`.
    */
  trait Input:
    /** Returns the list of `Train`
      * @return
      *   List of `Train`
      */
    def trains: Future[List[Train]]

    /** @return
      *   List of saved train `Technology`.
      */
    def technologies: Future[List[Technology]]

    /** @return
      *   List of `Wagons.UseType`
      */
    def wagonTypes: Future[List[UseType]]

    /** Add/save new train. If train does not exist it is saved as new one otherwise is returned a `Left` of
      * [[BaseError]]
      *
      * @param name
      *   Train name
      * @param technologyName
      *   Train technology name
      * @param wagonUseTypeName
      *   Wagon type of use name
      * @param wagonCapacity
      *   Single wagon transport capacity
      * @param wagonCount
      *   Amount of wagons that compose train
      * @return
      *   `Left` type of [[BaseError]] in case of some errors, `Right(List[Train])` if edits are saved.
      */
    def addTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

    /** Deletes train with a given name if it exists
      *
      * @param trainName
      *   Train name to delete
      * @return
      *   `Some` type of [[BaseError.DeleteTrainError]] in case of some deletion errors.
      */
    def removeTrain(trainName: String): Future[Either[BaseError, List[Train]]]

    /** Updates train with new information.
      *
      * @param name
      *   train name to be updated
      * @param technology
      *   updated technology
      * @param wagon
      *   updated wagon
      * @param wagonCount
      *   updated wagon count
      * @return
      *   Returns [[Right]] of Train if train is updated else [[Left]] of [[BaseError]]
      */
    def updateTrain(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

  object Input:
    type AppState = (TrainManager, TechnologyManager)
    def apply(stateEventQueue: LinkedBlockingQueue[AppState => AppState]): Input = BaseInBoundPort(stateEventQueue)

    private case class BaseInBoundPort(stateEventQueue: LinkedBlockingQueue[AppState => AppState]) extends Input:

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

      private def unpackResult(
          managerResult: Either[BaseError, TrainManager]
      )(
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

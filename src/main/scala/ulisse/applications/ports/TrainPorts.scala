package ulisse.applications.ports

import ulisse.applications.EventQueues.TrainEventQueue
import ulisse.applications.useCases.TrainService
import ulisse.entities.train.Trains.{Train, TrainTechnology}
import ulisse.entities.train.Wagons.UseType
import ulisse.utils.Errors.{BaseError, ErrorMessage}

import scala.concurrent.Future

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
    def technologies: Future[List[TrainTechnology]]

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
      *   `Some` type of [[DeleteTrainError]] in case of some deletion errors.
      */
    def removeTrain(trainName: String): Future[Either[BaseError, List[Train]]]

    /** Updates train with new information except name.
      *
      * @param name
      *   train name to be updated
      * @param technologyName
      *   Train technology name
      * @param wagonUseTypeName
      *   Wagon type of use name
      * @param wagonCapacity
      *   Single wagon transport capacity
      * @param wagonCount
      *   Amount of wagons that compose train
      * @return
      *   Returns [[Right]] of List[Train] if train is updated else [[Left]] of [[ErrorMessage]]
      */
    def updateTrain(name: String)(
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

  object Input:
    def apply(eventQueue: TrainEventQueue): Input = TrainService(eventQueue)

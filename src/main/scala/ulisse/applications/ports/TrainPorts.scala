package ulisse.applications.ports

import ulisse.applications.event.TrainEventQueue
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
    /** Returns list of `Train` */
    def trains: Future[List[Train]]

    /** Returns list of saved train `Technology`. */
    def technologies: Future[List[TrainTechnology]]

    /** Returns list of [[UseType]] */
    def wagonTypes: Future[List[UseType]]

    /** Add/save new train given train `name`, existing `technologyName` and `wagonUseTypeName`, valid values of `wagonCapacity` and `wagonCount`
      *
      * Returns `Left` type of [[BaseError]] in case of some errors, `Right(List[Train])` if edits are saved.
      */
    def createTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

    /** Deletes train given its `name` if it exists.
      *
      * Returns [[Right]] of List[Train] updated if train is deleted otherwise [[Left]] of [[DeleteTrainError]]
      */
    def removeTrain(trainName: String): Future[Either[BaseError, List[Train]]]

    /** Updates train with new information given its `name`.
      *
      * Returns [[Right]] of List[Train] if train is updated else [[Left]] of [[ErrorMessage]]
      */
    def updateTrain(name: String)(
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Future[Either[BaseError, List[Train]]]

  object Input:
    def apply(eventQueue: TrainEventQueue): Input = TrainService(eventQueue)

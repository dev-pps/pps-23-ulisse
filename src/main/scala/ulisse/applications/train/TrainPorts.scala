package ulisse.applications.train

import ulisse.applications.train.TrainManagers.{Errors, TrainManager}
import ulisse.entities.train.Technology
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Wagons.{UseType, Wagon}

object TrainPorts:

  /** It represents all possible requests that can be done from external components to application for what concern
    * about `Trains`, trains `Technology` and wagon `UseType`.
    *
    * A default implementation is [[BaseInBoundPort]]
    */
  trait InBound:
    /** Returns the list of `Train`
      * @return
      *   List of `Train`
      */
    def trains: List[Train]

    /** @return
      *   List of saved train `Technology`.
      */
    def technologies: List[Technology]

    /** @return
      *   List of `Wagons.UseType`
      */
    def wagonTypes: List[UseType]

    /** Add/save new train. If train does not exist it is saved as new one otherwise is returned a `Left` of [[Errors]]
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
      *   `Left` type of [[TrainManager.Errors]] in case of some errors, `Right(List[Train])` if edits are saved.
      */
    def addTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Either[Errors, List[Train]]

    /** Deletes train with a given name if it exists
      * @param trainName
      *   Train name to delete
      * @return
      *   `Some` type of [[Errors.DeleteTrainError]] in case of some deletion errors.
      */
    def removeTrain(trainName: String): Either[Errors, List[Train]]

    /** Updates train with new information.
      * @param name
      *   train name to be updated
      * @param technology
      *   updated technology
      * @param wagon
      *   updated wagon
      * @param wagonCount
      *   updated wagon count
      * @return
      *   Returns [[Right]] of Train if train is updated else [[Left]] of [[Errors]]
      */
    def updateTrain(name: String)(
        technology: Technology,
        wagon: Wagon,
        wagonCount: Int
    ): Either[Errors, List[Train]]

  /** @param manager
    *   TrainManager that handle input port requests
    */
  case class BaseInBoundPort(manager: TrainManager) extends InBound:
    export manager.{addTrain => _, *}

    override def addTrain(
        name: String,
        technologyName: String,
        wagonUseTypeName: String,
        wagonCapacity: Int,
        wagonCount: Int
    ): Either[Errors, List[Train]] =
      manager.createTrain(
        name,
        technologyName,
        wagonUseTypeName,
        wagonCapacity,
        wagonCount
      )

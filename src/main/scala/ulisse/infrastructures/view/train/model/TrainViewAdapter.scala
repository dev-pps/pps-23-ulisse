package ulisse.infrastructures.view.train.model

import ulisse.entities.train.Trains.Train
import TrainViewModel.*
import ulisse.applications.ports.TrainPorts
import ulisse.infrastructures.view.train.TrainEditorView
import ulisse.utils.Errors.BaseError
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait TrainViewAdapter:
  def requestTrains(): Unit
  def requestTechnologies(): Unit
  def requestWagonTypes(): Unit
  def addTrain(trainData: TrainData): Unit
  def deleteTrain(name: String): Unit
  def updateTrain(trainData: TrainData): Unit

object TrainViewAdapter:
  def apply(trainService: TrainPorts.Input, view: TrainEditorView): TrainViewAdapter =
    BaseAdapter(trainService, view)

  private final case class BaseAdapter(trainService: TrainPorts.Input, view: TrainEditorView)
      extends TrainViewAdapter:
    given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(1)
    )

    override def requestTrains(): Unit =
      trainService.trains.handleOnComplete(l =>
        view.updateTrainList(l.toTrainDatas)
      )

    override def addTrain(trainData: TrainData): Unit =
      trainData.extractThenPerform: (name, tkName, wType, wCap, tLen) =>
        trainService.addTrain(name, tkName, wType, wCap, tLen)

    override def deleteTrain(name: String): Unit =
      trainService.removeTrain(name).handleOnComplete(t => showNewTrainList(t))

    override def updateTrain(trainData: TrainData): Unit =
      trainData.extractThenPerform { (name, tkName, wType, wCap, tLen) =>
        trainService.updateTrain(name)(tkName, wType, wCap, tLen)
      }

    override def requestWagonTypes(): Unit =
      trainService.wagonTypes.handleOnComplete(w => view.updateWagons(w.toWagonNames))

    override def requestTechnologies(): Unit =
      trainService.technologies.handleOnComplete(t => view.updateTechnology(t.toTechType))

    extension (trainData: TrainData)
      private def extractThenPerform(action: (
          String,
          String,
          String,
          Int,
          Int
      ) => Future[Either[BaseError, List[Train]]]): Unit =
        val data =
          for
            n  <- trainData.name
            tn <- trainData.technologyName
            ts <- trainData.technologyMaxSpeed
            wq <- trainData.wagonCount
            wn <- trainData.wagonNameType
            wc <- trainData.wagonCapacity
          yield (n, tn, wn, wc, wq)
        data match
          case Some(d) => action(d._1, d._2, d._3, d._4, d._5).handleOnComplete(t => showNewTrainList(t))
          case None    => view.showError("Some field are empty!")

    extension [T](toComplete: Future[T])
      private def handleOnComplete(onSuccess: T => Unit): Unit =
        toComplete.onComplete {
          case Failure(e) => view.showError(e.getMessage)
          case Success(r) => onSuccess(r)
        }

    private def showNewTrainList(r: Either[BaseError, List[Train]]): Unit =
      r match
        case Left(err) => view.showError(s"Error: $err")
        case Right(t)  => view.updateTrainList(t.toTrainDatas)

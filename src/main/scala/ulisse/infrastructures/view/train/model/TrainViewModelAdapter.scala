package ulisse.infrastructures.view.train.model

import ulisse.entities.train.Wagons.UseType
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.{Technology, Wagons}
import TrainViewModel.*
import ulisse.applications.ports.TrainPorts
import ulisse.infrastructures.view.train.TrainEditorView
import ulisse.utils.Errors.BaseError

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait TrainViewModelAdapter:
  def requestTrains(): Unit
  def requestTechnologies(): Unit
  def requestWagonTypes(): Unit
  def addTrain(trainData: TrainData): Unit
  def deleteTrain(name: String): Unit
  def updateTrain(trainData: TrainData): Unit

object TrainViewModelAdapter:
  def apply(trainService: TrainPorts.Input, view: TrainEditorView): TrainViewModelAdapter =
    BaseAdapter(trainService, view)

  private final case class BaseAdapter(trainService: TrainPorts.Input, view: TrainEditorView)
      extends TrainViewModelAdapter:
    implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(
      Executors.newFixedThreadPool(1)
    )

    extension (l: List[Train])
      private def toTrainDatas: List[TrainData] =
        l.map(t =>
          TrainData(
            name = Some(t.name),
            technologyName = Some(t.techType.name),
            technologyMaxSpeed = Some(t.techType.maxSpeed),
            technologyAcc = Some(t.techType.acceleration),
            technologyDec = Some(t.techType.deceleration),
            wagonNameType = Some(t.wagon.use.name),
            wagonCapacity = Some(t.wagon.capacity),
            wagonCount = Some(t.length)
          )
        )

    extension (t: List[Technology])
      private def toTechType: List[TechType] =
        t.map(tk => TechType(tk.name, tk.maxSpeed, tk.acceleration, tk.deceleration))

    extension (w: List[UseType])
      private def toWagonNames: List[WagonName] = w.map(w => WagonName(w.name))

    override def requestTrains(): Unit =
      trainService.trains.onComplete {
        case Failure(e) => view.showError(e.getMessage)
        case Success(l) => view.updateTrainList(l.toTrainDatas)
      }

    override def addTrain(trainData: TrainData): Unit =
      for
        n  <- trainData.name
        tk <- trainData.technologyName
        wc <- trainData.wagonCount
        wt <- trainData.wagonNameType
        wa <- trainData.wagonCapacity
      yield trainService.addTrain(n, tk, wt, wa, wc).onComplete {
        case Failure(e) => view.showError(e.getMessage)
        case Success(r) => showNewTrainList(r)
      }

    override def deleteTrain(name: String): Unit = trainService.removeTrain(name).onComplete {
      case Failure(e) => view.showError(e.getMessage)
      case Success(r) => showNewTrainList(r)
    }

    private def showNewTrainList(r: Either[BaseError, List[Train]]): Unit =
      r match
        case Left(err) => view.showError("Errore")
        case Right(t)  => view.updateTrainList(t.toTrainDatas)

    override def updateTrain(trainData: TrainData): Unit =
      for
        n  <- trainData.name
        tk <- trainData.technologyName
        ts <- trainData.technologyMaxSpeed
        ac <- trainData.technologyAcc
        de <- trainData.technologyDec
        wc <- trainData.wagonCount
        wt <- trainData.wagonNameType
        wa <- trainData.wagonCapacity
      yield trainService.updateTrain(n)(Technology(tk, ts, ac, de), Wagons.Wagon(UseType.valueOf(wt), wa), wc)

    override def requestWagonTypes(): Unit =
      trainService.wagonTypes.onComplete {
        case Failure(e) => view.showError(e.getMessage)
        case Success(w) => view.updateWagons(w.toWagonNames)
      }

    override def requestTechnologies(): Unit =
      trainService.technologies.onComplete {
        case Failure(e) => view.showError(e.getMessage)
        case Success(t) => view.updateTechnology(t.toTechType)
      }

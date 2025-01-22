package immutable.state.demo.asyncAPI.completeFlowDemo

import immutable.state.demo.asyncAPI.completeFlowDemo.Application.Adapters.StationInputAdapter
import immutable.state.demo.asyncAPI.completeFlowDemo.Application.{AppState, StationManager}
import immutable.state.demo.asyncAPI.completeFlowDemo.UI.{AppFrame, TestUI}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object Application:

  object Ports:
    object StationPorts:
      trait Input:
        def stationPortMethodAdd(arg: String): Future[Either[String, String]]
        def stationPortMethodGet(arg: String): Future[Option[String]]
        def stationPortMethodGetAll(): Future[List[String]]

  object Adapters:
    case class StationInputAdapter(stateEventQueue: LinkedBlockingQueue[AppState => AppState])
        extends Ports.StationPorts.Input:
      override def stationPortMethodAdd(arg: String): Future[Either[String, String]] =
        val promise = Promise[Either[String, String]]()
        stateEventQueue.offer((state: AppState) => {
          println("Adding station")
          val newStationManager = state.stationManager.addStation(arg)
          val newState          = state.copy(stationManager = newStationManager._1)
          promise.success(newStationManager._2)
          newState
        })
        promise.future
      override def stationPortMethodGet(arg: String): Future[Option[String]] =
        val promise = Promise[Option[String]]()
        stateEventQueue.offer((state: AppState) => {
          println("Getting station")
          val station = state.stationManager.getStation(arg)
          promise.success(station)
          state
        })
        promise.future
      override def stationPortMethodGetAll(): Future[List[String]] =
        val promise = Promise[List[String]]()
        stateEventQueue.offer((state: AppState) => {
          println("Getting all stations")
          val stations = state.stationManager.getAllStations
          promise.success(stations)
          state
        })
        promise.future

  case class StationManager(stations: List[String]):
    def addStation(station: String): (StationManager, Either[String, String]) =
      (StationManager(station :: stations), Right(s"station $station"))
    def getStation(station: String): Option[String] = stations.find(_ == station)
    def getAllStations: List[String]                = stations

  case class AppState(stationManager: StationManager)

@main def main(): Unit =
  val stateEventQueue     = LinkedBlockingQueue[AppState => AppState]
  val stationInputAdapter = StationInputAdapter(stateEventQueue)

  val testUI = TestUI(stationInputAdapter)
  val app    = AppFrame()
  app.contents = testUI
  app.open()

  val initialState = AppState(StationManager(List.empty))
  LazyList.continually(stateEventQueue.take()).scanLeft(initialState)((state, event) =>
    event(state)
  ).foreach((appState: AppState) =>
    println(s"Stations: ${appState.stationManager.stations.length}")
  )

package ulisse.applications

import ulisse.applications.useCases.{RouteService, SimulationService, StationService, TimetableService, TrainService}

trait ServicesManager

object ServicesManager:

  def apply(eventQueue: EventQueue): ServicesManager = ServicesManagerImpl(eventQueue)

  private case class ServicesManagerImpl(eventQueue: EventQueue) extends ServicesManager:
    private val stationService   = StationService(eventQueue)
    private val routeService     = RouteService(eventQueue)
    private val trainService     = TrainService(eventQueue)
    private val timetableService = TimetableService(eventQueue)

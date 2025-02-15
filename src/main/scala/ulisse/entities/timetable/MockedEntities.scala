package ulisse.entities.timetable

import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.entities.Routes.{IdRoute, TypeRoute}
import ulisse.entities.Technology
import ulisse.entities.station.Station

object MockedEntities:

  type StationT = Station[_]

  trait StationManager:
    def stations: List[StationT]

  trait Route:
    val id: IdRoute
    val departure: StationT
    val arrival: StationT
    val typology: TypeRoute
    val technology: Technology
    val railsCount: Int
    val length: Double

  trait RouteManager:
    def routes: List[Route]

  trait AppStateTimetable:
    def trainManager: TrainManager
    def timetableManager: TimetableManager
    def timetableManagerUpdate(timetableManager: TimetableManager): AppStateTimetable
    def stationManager: StationManager
    def routeManager: RouteManager

  object MockRoutesService: // after remove that
    case class Route(startStationName: String, endStationName: String, technology: Technology)

    def routes: List[Route] = List(
      Route("A", "B", Technology("AV", maxSpeed = 300)),
      Route("A", "B1", Technology("AV", maxSpeed = 300)),
      Route("A", "B2", Technology("NORMAL", maxSpeed = 300)),
      Route("B", "C", Technology("AV", maxSpeed = 130)),
      Route("C", "D", Technology("NORMAL", maxSpeed = 130)),
      Route("C", "D1", Technology("magnetic", maxSpeed = 500))
    )

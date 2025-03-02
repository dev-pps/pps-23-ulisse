package ulisse.entities.timetable

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager

object MockedEntities

//  trait AppStateTimetable:
//    def trainManager: TrainManager
//    def timetableManager: TimetableManager
//    def timetableManagerUpdate(timetableManager: TimetableManager): AppStateTimetable
//    def routeManager: RouteManager
//
//  case class AppStateMocked(
//      trainManager: TrainManager,
//      timetableManager: TimetableManager,
//      routeManager: RouteManager
//  ) extends AppStateTimetable:
//    override def timetableManagerUpdate(timetableManager: TimetableManager): AppStateTimetable =
//      this.copy(timetableManager = timetableManager)

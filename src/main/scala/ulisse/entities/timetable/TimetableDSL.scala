package ulisse.entities.timetable

import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.ClockTime

object TimetableDSL:

  /** train Rv2378 at h(9).m(34) travelsOn route ROUTEXX then tranvelsOn route ROUTEYY waiting [1-9] minutes then travelsOn route ROUTEZZZ asFinalRoute */

  type RouteT = String

  trait TrainInit:
    def train(train: Train): DepartTime

  trait DepartTime:
    def at(time: ClockTime): PartialTimetable

  trait PartialTimetable:
    def travelsOn(route: RouteT): PartialTimetable

  case class DepartTimeImpl(train: Train)
